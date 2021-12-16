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

	public static RayTraceResult pickBlock(Entity entity, float partialTicks, double rayDist, RayTraceContext.BlockMode blockMode, boolean traceFluids) {
		return pickBlock(entity, partialTicks, rayDist, blockMode, traceFluids ? RayTraceContext.FluidMode.ANY : RayTraceContext.FluidMode.NONE);
	}

	public static RayTraceResult pickBlock(Entity entity, float partialTicks, double rayDist, RayTraceContext.BlockMode blockMode, RayTraceContext.FluidMode fluidMode) {
		Vector3d startPos = entity.getEyePosition(partialTicks);
		Vector3d lookVec = entity.getViewVector(partialTicks);
		Vector3d endPos = startPos.add(lookVec.x * rayDist, lookVec.y * rayDist, lookVec.z * rayDist);
		return entity.level.clip(new RayTraceContext(startPos, endPos, blockMode, fluidMode, entity));
	}

	public static RayTraceResult pickBlock(Entity entity, float partialTicks, double rayDist, RayTraceMode rayTraceMode, boolean traceFluids) {
		return pickBlock(entity, partialTicks, rayDist, rayTraceMode, traceFluids ? RayTraceContext.FluidMode.ANY : RayTraceContext.FluidMode.NONE);
	}

	public static RayTraceResult pickBlock(Entity entity, float partialTicks, double rayDist, RayTraceMode rayTraceMode, RayTraceContext.FluidMode fluidMode) {
		Vector3d startPos = entity.getEyePosition(partialTicks);
		Vector3d lookVec = entity.getViewVector(partialTicks);
		Vector3d endPos = startPos.add(lookVec.x * rayDist, lookVec.y * rayDist, lookVec.z * rayDist);
		return entity.level.clip(rayTraceMode.createContext(startPos, endPos, fluidMode, entity));
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
	static class EmptyColliderOrOutlineRayTraceContext extends RayTraceContext {

		protected final ISelectionContext collisionContext;

		public EmptyColliderOrOutlineRayTraceContext(Vector3d from, Vector3d to, FluidMode fluidMode, @Nullable Entity entity) {
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
	static class EmptyVisualOrOutlineRayTraceContext extends RayTraceContext {

		protected final ISelectionContext collisionContext;

		public EmptyVisualOrOutlineRayTraceContext(Vector3d from, Vector3d to, FluidMode fluidMode, @Nullable Entity entity) {
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
