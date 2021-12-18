package com.github.elenterius.combat_commons.mixin.attack_reach;

import com.github.elenterius.combat_commons.compat.pehkui.PehkuiCompat;
import com.github.elenterius.combat_commons.entity.EntityAttributeUtil;
import com.github.elenterius.combat_commons.item.IClipShapeProvider;
import com.github.elenterius.combat_commons.utils.ClipShape;
import com.github.elenterius.combat_commons.utils.RayTraceUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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

	@Inject(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void combatCommons_CaptureCameraEntity(float partialTicks, CallbackInfo ci, Entity entity) {
		camera = entity; //not null at this point

		final float scale = PehkuiCompat.getPlayerReachScale(camera, partialTicks);
		maxAttackReachDist = scale * EntityAttributeUtil.getAttackReachDist(minecraft.player, Objects.requireNonNull(minecraft.gameMode).hasFarPickRange());
	}

	@Redirect(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;pick(DFZ)Lnet/minecraft/world/phys/HitResult;"))
	private HitResult combatCommons_PickBlockProxy(Entity instance, double rayTraceDistance, float partialTicks, boolean rayTraceFluids) {
		//block reach value is already changed (pehkui applies a mixin to PlayerController#getPickRange() when not in creative mode)
		// 0.9 * block_reach vs block_reach - 0.5
		final float blockReachDist = Objects.requireNonNull(minecraft.gameMode).getPickRange();

		if (blockReachDist > 0) {

			ItemStack heldStack = Objects.requireNonNull(minecraft.player).getMainHandItem();
			if (heldStack.getItem() instanceof IClipShapeProvider provider) {
				ClipContext.Fluid fluid = provider.getRayTraceFluidModeForPickBlock(heldStack, minecraft.player);
				return RayTraceUtil.pickBlock(camera, partialTicks, blockReachDist, ClipShape.OUTLINE, fluid);
			}

			return RayTraceUtil.pickBlock(camera, partialTicks, blockReachDist, ClipShape.OUTLINE, rayTraceFluids);
		}
		else {
			//prevent the hitting of any block
			//fixes forge bug where block reach <= 0 returns a hit and not a miss
			Vec3 eyePosition = camera.getEyePosition(partialTicks);
			Vec3 lookVec = camera.getViewVector(partialTicks);
			return BlockHitResult.miss(eyePosition, Direction.getNearest(lookVec.x, lookVec.y, lookVec.z), new BlockPos(eyePosition));
		}
	}

	/**
	 * if we hit a block reduce the max ray distance of the EntityRayTrace --> prevents attacks through walls on the client side
	 *
	 * @param rayStartPos vector3d = entity.getEyePosition(pPartialTicks)
	 * @return maxAttackReachDistanceSquare
	 */
	@Redirect(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;distanceToSqr(Lnet/minecraft/world/phys/Vec3;)D", ordinal = 0))
	private double combatCommons_GetModifiedAttackReachProxy(Vec3 hitPosition, Vec3 rayStartPos, float partialTicks) {
		// We ignore the previous block raytrace hit result and perform a second raytrace (independent of block reach)
		// using the attack reach distance to determine if the targeted entity is not obstructed by blocks (prevent attacks through walls)

		HitResult hitResult;
		ItemStack heldStack = Objects.requireNonNull(minecraft.player).getMainHandItem();
		if (heldStack.getItem() instanceof IClipShapeProvider provider) {
			ClipShape clipShape = provider.getRayTraceModeForPickEntity(heldStack, minecraft.player);
			ClipContext.Fluid fluid = provider.getRayTraceFluidModeForPickEntity(heldStack, minecraft.player);

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

		if (hitResult.getType() != HitResult.Type.MISS) {
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
	@Redirect(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"))
	private Vec3 combatCommons_VectorAddProxy(Vec3 instance, double pX, double pY, double pZ) {
		return instance.add(camera.getViewVector(1f).scale(maxAttackReachDist));
	}

	@ModifyArg(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;scale(D)Lnet/minecraft/world/phys/Vec3;"), index = 0)
	private double combatCommons_AdjustVectorScale(double d0) {
		return maxAttackReachDist;
	}

	@ModifyArg(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;"), index = 5)
	private double combatCommons_AdjustMaxDistance(double d0) {
		return maxAttackReachDistSq; //fixes unstable variable >> sometimes the value isn't successfully changed by the previous combatCommons_GetModifiedAttackReachProxy mixin, LOL??
	}

	@Redirect(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;distanceToSqr(Lnet/minecraft/world/phys/Vec3;)D", ordinal = 1))
	private double combatCommons_DistanceToSqrProxy(Vec3 instance, Vec3 hitVec) {
		double distSq = instance.distanceToSqr(hitVec);
		double maxReachDistSq = maxAttackReachDist * maxAttackReachDist;
		return distSq > maxReachDistSq ? Double.MAX_VALUE : Double.MIN_VALUE;
	}

}
