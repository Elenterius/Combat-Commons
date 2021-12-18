package com.github.elenterius.combat_commons.mixin.attack_reach;

import com.github.elenterius.combat_commons.compat.pehkui.PehkuiCompat;
import com.github.elenterius.combat_commons.entity.EntityAttributeUtil;
import com.github.elenterius.combat_commons.item.IClipShapeProvider;
import com.github.elenterius.combat_commons.utils.ClipShape;
import com.github.elenterius.combat_commons.utils.RayTraceUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Objects;

/**
 * Enables increased and decreased (attack) reach distance
 *
 * @author Elenterius
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

	@Shadow
	@Final
	private Minecraft minecraft;

	@Unique
	private Entity camera;

	@Unique
	private double maxAttackReachDist;

	@Unique
	private double maxAttackReachDistSq;

	@Inject(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/IProfiler;push(Ljava/lang/String;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void combatCommons_CaptureCameraEntity(float partialTicks, CallbackInfo ci, Entity entity) {
		camera = entity; //not null at this point

		final float scale = PehkuiCompat.getPlayerReachScale(camera, partialTicks);
		maxAttackReachDist = scale * EntityAttributeUtil.getAttackReachDist(minecraft.player, Objects.requireNonNull(minecraft.gameMode).hasFarPickRange());
	}

	@Redirect(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;pick(DFZ)Lnet/minecraft/util/math/RayTraceResult;"))
	private RayTraceResult combatCommons_PickBlockProxy(Entity instance, double rayTraceDistance, float partialTicks, boolean rayTraceFluids) {
		//block reach value is already changed (pehkui applies a mixin to PlayerController#getPickRange() when not in creative mode)
		// 0.9 * block_reach vs block_reach - 0.5
		final float blockReachDist = Objects.requireNonNull(minecraft.gameMode).getPickRange();

		if (blockReachDist > 0) {

			ItemStack heldStack = Objects.requireNonNull(minecraft.player).getMainHandItem();
			if (heldStack.getItem() instanceof IClipShapeProvider) {
				IClipShapeProvider provider = (IClipShapeProvider) heldStack.getItem();
				RayTraceContext.FluidMode fluid = provider.getClipFluidModeForPickBlock(heldStack, minecraft.player);
				return RayTraceUtil.pickBlock(camera, partialTicks, blockReachDist, ClipShape.OUTLINE, fluid);
			}

			return RayTraceUtil.pickBlock(camera, partialTicks, blockReachDist, ClipShape.OUTLINE, rayTraceFluids);
		}
		else {
			//prevent the hitting of any block
			//fixes forge bug where block reach <= 0 returns a hit and not a miss
			Vector3d eyePosition = camera.getEyePosition(partialTicks);
			Vector3d lookVec = camera.getViewVector(partialTicks);
			return BlockRayTraceResult.miss(eyePosition, Direction.getNearest(lookVec.x, lookVec.y, lookVec.z), new BlockPos(eyePosition));
		}
	}

	/**
	 * if we hit a block reduce the max ray distance of the EntityRayTrace --> prevents attacks through walls on the client side
	 *
	 * @param rayStartPos vector3d = entity.getEyePosition(pPartialTicks)
	 * @return maxAttackReachDistanceSquare
	 */
	@Redirect(method = "pick", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/util/math/vector/Vector3d;distanceToSqr(Lnet/minecraft/util/math/vector/Vector3d;)D"))
	private double combatCommons_GetModifiedAttackReachProxy(Vector3d hitPosition, Vector3d rayStartPos, float partialTicks) {
		// We ignore the previous block raytrace hit result and perform a second raytrace (independent of block reach)
		// using the attack reach distance to determine if the targeted entity is not obstructed by blocks (prevent attacks through walls)

		RayTraceResult hitResult;
		ItemStack heldStack = Objects.requireNonNull(minecraft.player).getMainHandItem();
		if (heldStack.getItem() instanceof IClipShapeProvider) {
			IClipShapeProvider provider = (IClipShapeProvider) heldStack.getItem();
			ClipShape clipShape = provider.getClipShapeForPickEntity(heldStack, minecraft.player);
			RayTraceContext.FluidMode fluid = provider.getClipFluidForPickEntity(heldStack, minecraft.player);

			/*
			 * In COLLIDER BlockMode:
			 * decreasing block reach allows attacks to pass through grass/plant blocks
			 * >>> This is proper behavior because block reach determines what blocks can be interacted with,
			 * >>> so any blocks outside the block reach shouldn't be able to be targeted & interacted with when the attack ray trace is done
			 * */
			hitResult = RayTraceUtil.pickBlock(camera, partialTicks, maxAttackReachDist, clipShape, fluid);
		}
		else {
			hitResult = RayTraceUtil.pickBlock(camera, partialTicks, maxAttackReachDist, ClipShape.OUTLINE, false);
		}

		if (hitResult.getType() != RayTraceResult.Type.MISS) {
			//we hit a wall, reduce the ray distance for the EntityRayTrace --> blocks attacks through walls (block)
			double v = hitResult.getLocation().distanceToSqr(rayStartPos);
			maxAttackReachDistSq = v;
			return v;
		}

		double v = maxAttackReachDist * maxAttackReachDist;
		maxAttackReachDistSq = v;
		return v;
	}

	/**
	 * @return start position of the ray
	 */
	@Redirect(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/vector/Vector3d;add(DDD)Lnet/minecraft/util/math/vector/Vector3d;"))
	private Vector3d combatCommons_VectorAddProxy(Vector3d instance, double pX, double pY, double pZ) {
		return instance.add(camera.getViewVector(1f).scale(maxAttackReachDist));
	}

	@ModifyArg(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/vector/Vector3d;scale(D)Lnet/minecraft/util/math/vector/Vector3d;"), index = 0)
	private double combatCommons_AdjustVectorScale(double d0) {
		return maxAttackReachDist;
	}

	@ModifyArg(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileHelper;getEntityHitResult(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/vector/Vector3d;Lnet/minecraft/util/math/vector/Vector3d;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/function/Predicate;D)Lnet/minecraft/util/math/EntityRayTraceResult;"), index = 5)
	private double combatCommons_AdjustMaxDistance(double d0) {
		return maxAttackReachDistSq; //fixes unstable variable >> sometimes the value isn't successfully changed by the previous combatCommons_GetModifiedAttackReachProxy mixin, LOL??
	}

	@Redirect(method = "pick", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/util/math/vector/Vector3d;distanceToSqr(Lnet/minecraft/util/math/vector/Vector3d;)D"))
	private double combatCommons_DistanceToSqrProxy(Vector3d instance, Vector3d hitVec) {
		double distSq = instance.distanceToSqr(hitVec);
		double maxReachDistSq = maxAttackReachDist * maxAttackReachDist;
		return distSq > maxReachDistSq ? Double.MAX_VALUE : Double.MIN_VALUE;
	}

}
