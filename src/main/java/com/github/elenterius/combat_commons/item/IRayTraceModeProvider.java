package com.github.elenterius.combat_commons.item;

import com.github.elenterius.combat_commons.utils.RayTraceMode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceContext;

public interface IRayTraceModeProvider {

	default RayTraceMode getRayTraceModeForPickEntity(ItemStack stack, PlayerEntity player) {
		return RayTraceMode.OUTLINE;
	}

	/**
	 * determines which fluid is hittable by the attack ray trace
	 */
	default RayTraceContext.FluidMode getRayTraceFluidModeForPickEntity(ItemStack stack, PlayerEntity player) {
		return RayTraceContext.FluidMode.NONE;
	}

	/**
	 * determines which fluid is hittable by the pick block ray trace
	 */
	default RayTraceContext.FluidMode getRayTraceFluidModeForPickBlock(ItemStack stack, PlayerEntity player) {
		return RayTraceContext.FluidMode.NONE;
	}

}
