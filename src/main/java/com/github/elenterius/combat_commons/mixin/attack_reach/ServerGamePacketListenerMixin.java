package com.github.elenterius.combat_commons.mixin.attack_reach;

import com.github.elenterius.combat_commons.CombatCommonsMod;
import com.github.elenterius.combat_commons.compat.pehkui.PehkuiCompat;
import com.github.elenterius.combat_commons.entity.EntityAttributeUtil;
import com.github.elenterius.combat_commons.utils.RayTraceUtil;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerMixin {

	/**
	 * @author Elenterius
	 */
	@Unique
	private double getMaxReachDist(ServerPlayer player, ServerboundInteractPacket packet) {
		float scale = Math.max(PehkuiCompat.getPlayerReachScale(player), 1f);
		if (packet.action == ServerboundInteractPacket.ATTACK_ACTION) {
			return scale * EntityAttributeUtil.getAttackReachDist(player, player.isCreative());
		}
		else {
			return scale * (EntityAttributeUtil.getBlockReachDist(player, true) + 1d);
		}
	}

	/**
	 * Fixes the issue where for players with a low maxReachDist the target's position can be outside the maxReachDist while the bounding box of the target is still within range.
	 *
	 * @author Elenterius
	 */
	@Redirect(method = "handleInteract", require = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;distanceToSqr(Lnet/minecraft/world/entity/Entity;)D"))
	private double combatCommons_distanceToSqProxy(ServerPlayer player, Entity targetOfClient, ServerboundInteractPacket packet) {

		final double maxReachDist = getMaxReachDist(player, packet);
		final double distSqToBBox = RayTraceUtil.distanceSqToInflatedBoundingBox(player, targetOfClient, maxReachDist);
		final double maxReachDistSq = maxReachDist * maxReachDist;

		//TODO: don't trust the client? And perform a block raytrace to prevent attacks through walls?

		//For testing spawn a large slime and attack in survival mode or with reduced attack reach (e.g. /summon minecraft:slime ~ ~ ~ {Size: 10})
		CombatCommonsMod.LOGGER.debug(() -> {
			double distSqToPosition = player.distanceToSqr(targetOfClient);
			return String.format("""
							maxReach[dist: %f, distSq: %f]
							|-> old >> distSqToPosition[%f] is within maxReachDistSq: %s
							|-> new >>     distSqToBBox[%f] is within maxReachDistSq: %s""",
					maxReachDist, maxReachDistSq, distSqToPosition, distSqToPosition < maxReachDistSq, distSqToBBox, distSqToBBox < maxReachDistSq
			);
		});

		return distSqToBBox < maxReachDistSq ? Double.MIN_VALUE : Double.MAX_VALUE;
	}

}
