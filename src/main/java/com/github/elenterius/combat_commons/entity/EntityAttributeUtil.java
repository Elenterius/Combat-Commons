package com.github.elenterius.combat_commons.entity;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;

import javax.annotation.Nullable;

public final class EntityAttributeUtil {

	private EntityAttributeUtil() {}

	public static Attribute getAttackReach() {
		return EntityAttributes.ATTACK_REACH.get();
	}

	public static Attribute getBlockReach() {
		return ForgeMod.REACH_DISTANCE.get();
	}

	public static double getValueOrDefault(@Nullable LivingEntity entity, Attribute attribute) {
		if (entity == null) return attribute.getDefaultValue();

		AttributeInstance instance = entity.getAttribute(attribute);
		return instance != null ? instance.getValue() : attribute.getDefaultValue();
	}

	public static double getValueOrElse(@Nullable LivingEntity entity, Attribute attribute, double other) {
		if (entity == null) return other;

		AttributeInstance instance = entity.getAttribute(attribute);
		return instance != null ? instance.getValue() : other;
	}

	public static double getAttackReachDist(@Nullable LivingEntity entity) {
		return getValueOrDefault(entity, getAttackReach());
	}

	/**
	 * @param isCreative on client side this should be PlayerController#hasFarPickRange()
	 */
	public static double getAttackReachDist(@Nullable LivingEntity player, boolean isCreative) {
		return getValueOrDefault(player, getAttackReach()) + (isCreative ? 3 : 0);
	}

	public static double getBlockReachDist(@Nullable LivingEntity entity) {
		return getValueOrDefault(entity, getBlockReach());
	}

	@OnlyIn(Dist.CLIENT)
	public static float getBlockReachDist(MultiPlayerGameMode gameMode) {
		return gameMode.getPickRange(); //pehkui applies a mixin to PlayerController#getPickRange()
	}

	public static double getBlockReachDist(@Nullable LivingEntity player, boolean isCreative) {
		return getValueOrDefault(player, getBlockReach()) - (isCreative ? 0 : 0.5); //player has a default block reach of 4.5 in survival and 5 in creative
	}

}
