package com.github.elenterius.combat_commons.item;

import com.github.elenterius.combat_commons.utils.ClipShape;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;

public interface IClipShapeProvider {

	default ClipShape getRayTraceModeForPickEntity(ItemStack stack, Player player) {
		return ClipShape.OUTLINE;
	}

	/**
	 * determines which fluid is hittable by the attack ray trace
	 */
	default ClipContext.Fluid getRayTraceFluidModeForPickEntity(ItemStack stack, Player player) {
		return ClipContext.Fluid.NONE;
	}

	/**
	 * determines which fluid is hittable by the pick block ray trace
	 */
	default ClipContext.Fluid getRayTraceFluidModeForPickBlock(ItemStack stack, Player player) {
		return ClipContext.Fluid.NONE;
	}

}
