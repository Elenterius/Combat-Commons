package com.github.elenterius.combat_commons.utils;

import com.github.elenterius.combat_commons.mixin.RayTraceContextAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.Optional;

public final class RayTraceUtil {

	private RayTraceUtil() {}

	public static RayTraceResult pickBlock(Entity entity, float partialTicks, double rayDist, RayTraceContext.BlockMode blockMode, boolean hitFluid) {
		return pickBlock(entity, partialTicks, rayDist, blockMode, hitFluid ? RayTraceContext.FluidMode.ANY : RayTraceContext.FluidMode.NONE);
	}

	public static RayTraceResult pickBlock(Entity entity, float partialTicks, double rayDist, RayTraceContext.BlockMode blockMode, RayTraceContext.FluidMode fluid) {
		Vector3d startPos = entity.getEyePosition(partialTicks);
		Vector3d lookVec = entity.getViewVector(partialTicks);
		Vector3d endPos = startPos.add(lookVec.x * rayDist, lookVec.y * rayDist, lookVec.z * rayDist);
		return entity.level.clip(new RayTraceContext(startPos, endPos, blockMode, fluid, entity));
	}

	public static RayTraceResult pickBlock(Entity entity, float partialTicks, double rayDist, ClipShape clipShape, boolean hitFluid) {
		return pickBlock(entity, partialTicks, rayDist, clipShape, hitFluid ? RayTraceContext.FluidMode.ANY : RayTraceContext.FluidMode.NONE);
	}

	public static RayTraceResult pickBlock(Entity entity, float partialTicks, double rayDist, ClipShape clipShape, RayTraceContext.FluidMode fluid) {
		Vector3d startPos = entity.getEyePosition(partialTicks);
		Vector3d lookVec = entity.getViewVector(partialTicks);
		Vector3d endPos = startPos.add(lookVec.x * rayDist, lookVec.y * rayDist, lookVec.z * rayDist);
		return entity.level.clip(clipShape.createContext(startPos, endPos, fluid, entity));
	}

	/**
	 * instead of getting the distance between two positions, tries to get the distance between the position of the source and the inflated bounding box of the target
	 */
	public static double distanceSqToInflatedBoundingBox(Entity source, Entity target, double maxDist) {
		//make it easier to hit small targets
		return distanceSqToBoundingBox(source, target, 0.3f, maxDist);
	}

	/**
	 * instead of getting the distance between two points, tries to get the distance between the position of the source and the bounding box of the target
	 *
	 * @param inflate inflates the target aabb if the value is not zero, a possible input value could be {@link Entity#getPickRadius()}
	 */
	public static double distanceSqToBoundingBox(Entity source, Entity target, float inflate, double maxDist) {
		//create "ray"
		Vector3d startPos = source.getEyePosition(1f);
		Vector3d direction = target.position().subtract(startPos).normalize();
		Vector3d endPos = startPos.add(direction.scale(maxDist));

		AxisAlignedBB aabb = inflate != 0f ? target.getBoundingBox().inflate(inflate) : target.getBoundingBox();

		//tries to get the "intersection point" of the aabb with the ray
		Optional<Vector3d> optional = aabb.clip(startPos, endPos);
		if (aabb.contains(startPos)) {
			return startPos.distanceToSqr(optional.orElse(startPos));
		}
		else if (optional.isPresent()) {
			return startPos.distanceToSqr(optional.get());
		}

		return source.distanceToSqr(target); //fallback
	}

	/**
	 * RayTraceContext that will return empty shapes when the collider shape is empty, else the outline shape is returned.
	 * This allows us to go through tall grass and plants while avoiding the 1.5 block tall colliders of fences and walls
	 */
	static class EmptyColliderOrOutlineClipContext extends RayTraceContext {

		protected final ISelectionContext collisionContext;

		public EmptyColliderOrOutlineClipContext(Vector3d from, Vector3d to, FluidMode fluidMode, @Nullable Entity entity) {
			super(from, to, BlockMode.OUTLINE, fluidMode, entity);
			collisionContext = ((RayTraceContextAccessor) this).getCollisionContext();
		}

		@Override
		public VoxelShape getBlockShape(BlockState blockState, IBlockReader level, BlockPos pos) {
			VoxelShape collisionShape = blockState.getCollisionShape(level, pos, collisionContext);
			return collisionShape.isEmpty() ? collisionShape : super.getBlockShape(blockState, level, pos);
		}
	}

	/**
	 * RayTraceContext that will return empty shapes when the visual shape is empty, else the outline shape is returned.
	 * This allows us to go through glass while avoiding the 1.5 block tall colliders of fences and walls
	 */
	static class EmptyVisualOrOutlineClipContext extends RayTraceContext {

		protected final ISelectionContext collisionContext;

		public EmptyVisualOrOutlineClipContext(Vector3d from, Vector3d to, FluidMode fluidMode, @Nullable Entity entity) {
			super(from, to, BlockMode.OUTLINE, fluidMode, entity);
			collisionContext = ((RayTraceContextAccessor) this).getCollisionContext();
		}

		@Override
		public VoxelShape getBlockShape(BlockState blockState, IBlockReader level, BlockPos pos) {
			VoxelShape visualShape = blockState.getVisualShape(level, pos, collisionContext);
			return visualShape.isEmpty() ? visualShape : super.getBlockShape(blockState, level, pos);
		}
	}

}
