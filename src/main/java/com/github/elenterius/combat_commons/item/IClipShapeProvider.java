package com.github.elenterius.combat_commons.item;

import com.github.elenterius.combat_commons.utils.ClipShape;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceContext;

public interface IClipShapeProvider {

	default ClipShape getClipShapeForPickEntity(ItemStack stack, PlayerEntity player) {
		return ClipShape.OUTLINE;
	}

	/**
	 * determines which fluid is hittable by the attack ray trace
	 */
	default RayTraceContext.FluidMode getClipFluidForPickEntity(ItemStack stack, PlayerEntity player) {
		return RayTraceContext.FluidMode.NONE;
	}

	/**
	 * determines which fluid is hittable by the pick block ray trace
	 */
	default RayTraceContext.FluidMode getClipFluidModeForPickBlock(ItemStack stack, PlayerEntity player) {
		return RayTraceContext.FluidMode.NONE;
	}

}
