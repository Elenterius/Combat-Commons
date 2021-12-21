package com.github.elenterius.combat_commons.enchantment;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;


public final class EnchantmentUtil {

	private EnchantmentUtil() {}

	/**
	 * Get the total damage bonus from enchantments.<br>
	 * Used by several Mixins to redirect the
	 * {@link EnchantmentHelper#getDamageBonus(ItemStack, MobType)}
	 * calls from Players/Mobs to this one.
	 */
	public static float getDamageBonus(ItemStack stack, Entity attacker, Entity victim, MobType victimMobType) {
		return EnchantmentHelper.getDamageBonus(stack, victimMobType) + EnchantmentUtil.getExtraDamageBonus(stack, attacker, victim);
	}

	/**
	 * Get extra damage from enchantments that implement the {@link IExtraDamageEnchantment} interface.<br>
	 * Extra damge bonus is independent of {@link EnchantmentHelper#getDamageBonus(ItemStack, MobType)}
	 */
	public static float getExtraDamageBonus(ItemStack stack, Entity attacker, Entity victim) {
		float damageBonus = 0;
		if (!stack.isEmpty()) {
			ListTag list = stack.getEnchantmentTags();
			for (int i = 0; i < list.size(); i++) {
				CompoundTag tag = list.getCompound(i);
				ResourceLocation id = EnchantmentHelper.getEnchantmentId(tag);
				if (id != null) {
					Enchantment value = ForgeRegistries.ENCHANTMENTS.getValue(id);
					if (value instanceof IExtraDamageEnchantment enchantment) {
						damageBonus += enchantment.getExtraDamage(stack, EnchantmentHelper.getEnchantmentLevel(tag), attacker, victim);
					}
				}
			}
		}
		return damageBonus;
	}

}
