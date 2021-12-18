package com.github.elenterius.combat_commons.utils;

import com.github.elenterius.combat_commons.mixin.ClipContextAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Optional;

public final class RayTraceUtil {

	private RayTraceUtil() {}

	public static HitResult pickBlock(Entity entity, float partialTicks, double rayDist, ClipContext.Block blockMode, boolean hitFluid) {
		return pickBlock(entity, partialTicks, rayDist, blockMode, hitFluid ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE);
	}

	public static HitResult pickBlock(Entity entity, float partialTicks, double rayDist, ClipContext.Block blockShape, ClipContext.Fluid fluid) {
		Vec3 startPos = entity.getEyePosition(partialTicks);
		Vec3 lookVec = entity.getViewVector(partialTicks);
		Vec3 endPos = startPos.add(lookVec.x * rayDist, lookVec.y * rayDist, lookVec.z * rayDist);
		return entity.level.clip(new ClipContext(startPos, endPos, blockShape, fluid, entity));
	}

	public static HitResult pickBlock(Entity entity, float partialTicks, double rayDist, ClipShape clipShape, boolean hitFluid) {
		return pickBlock(entity, partialTicks, rayDist, clipShape, hitFluid ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE);
	}

	public static HitResult pickBlock(Entity entity, float partialTicks, double rayDist, ClipShape clipShape, ClipContext.Fluid fluid) {
		Vec3 startPos = entity.getEyePosition(partialTicks);
		Vec3 lookVec = entity.getViewVector(partialTicks);
		Vec3 endPos = startPos.add(lookVec.x * rayDist, lookVec.y * rayDist, lookVec.z * rayDist);
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
		Vec3 startPos = source.getEyePosition(1f);
		Vec3 direction = target.position().subtract(startPos).normalize();
		Vec3 endPos = startPos.add(direction.scale(maxDist));

		AABB aabb = inflate != 0f ? target.getBoundingBox().inflate(inflate) : target.getBoundingBox();

		//tries to get the "intersection point" of the aabb with the ray
		Optional<Vec3> optional = aabb.clip(startPos, endPos);
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
	static class EmptyColliderOrOutlineClipContext extends ClipContext {

		protected final CollisionContext collisionContext;

		public EmptyColliderOrOutlineClipContext(Vec3 from, Vec3 to, Fluid fluidMode, @Nullable Entity entity) {
			super(from, to, Block.OUTLINE, fluidMode, entity);
			collisionContext = ((ClipContextAccessor) this).getCollisionContext();
		}

		@Override
		public VoxelShape getBlockShape(BlockState blockState, BlockGetter level, BlockPos pos) {
			VoxelShape collisionShape = blockState.getCollisionShape(level, pos, collisionContext);
			return collisionShape.isEmpty() ? collisionShape : super.getBlockShape(blockState, level, pos);
		}
	}

	/**
	 * RayTraceContext that will return empty shapes when the visual shape is empty, else the outline shape is returned.
	 * This allows us to go through glass while avoiding the 1.5 block tall colliders of fences and walls
	 */
	static class EmptyVisualOrOutlineClipContext extends ClipContext {

		protected final CollisionContext collisionContext;

		public EmptyVisualOrOutlineClipContext(Vec3 from, Vec3 to, Fluid fluidMode, @Nullable Entity entity) {
			super(from, to, Block.OUTLINE, fluidMode, entity);
			collisionContext = ((ClipContextAccessor) this).getCollisionContext();
		}

		@Override
		public VoxelShape getBlockShape(BlockState blockState, BlockGetter level, BlockPos pos) {
			VoxelShape visualShape = blockState.getVisualShape(level, pos, collisionContext);
			return visualShape.isEmpty() ? visualShape : super.getBlockShape(blockState, level, pos);
		}
	}

}
