package com.github.elenterius.combat_commons.mixin;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

	@Shadow
	public abstract Item getItem();

	/**
	 * only display tooltip info for the slots that the armor item can be equipped in
	 */
	@Redirect(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EquipmentSlot;values()[Lnet/minecraft/world/entity/EquipmentSlot;"))
	private EquipmentSlot[] combatCommons_EquipmentSlotValuesProxy() {
		if (getItem() instanceof ArmorItem armor) {
			return new EquipmentSlot[]{armor.getSlot(), EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND};
		}
		return EquipmentSlot.values();
	}

}
