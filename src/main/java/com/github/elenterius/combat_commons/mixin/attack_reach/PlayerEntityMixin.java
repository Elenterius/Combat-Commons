package com.github.elenterius.combat_commons.mixin.attack_reach;

import com.github.elenterius.combat_commons.CombatCommonsMod;
import com.github.elenterius.combat_commons.compat.pehkui.PehkuiCompat;
import com.github.elenterius.combat_commons.entity.EntityAttributeUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.MarkerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerEntityMixin {

	/**
	 * @author Elenterius
	 */
	@Inject(method = "createAttributes", at = @At(value = "RETURN"))
	private static void combatCommons_onCreateAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
		CombatCommonsMod.LOGGER.debug(MarkerManager.getMarker("ATTRIBUTE INJECTION"), "adding attack reach attribute to player");
		cir.getReturnValue().add(EntityAttributeUtil.getAttackReach());
	}

	/**
	 * allow sweep attacks with larger attack reach
	 *
	 * @author Elenterius
	 */
	//TODO: check if this is broken in 1.17/1.18 --> Reason: forge added custom sweep hit boxes in 1.17 (#7981)
	@Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;distanceToSqr(Lnet/minecraft/world/entity/Entity;)D"))
	private double combatCommons_DistanceToSqProxy(Player player, Entity targetEntity) {
		float scale = Math.max(PehkuiCompat.getPlayerReachScale(player), 1f);
		double reachDist = EntityAttributeUtil.getAttackReachDist(player);
		double maxReachDistSq = scale * scale * reachDist * reachDist;

		double distSq = player.distanceToSqr(targetEntity);
		return distSq < maxReachDistSq ? Double.MIN_VALUE : Double.MAX_VALUE;
	}

}
