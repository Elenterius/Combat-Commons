package com.github.elenterius.combat_commons.enchantment;

import com.github.elenterius.combat_commons.CombatCommonsMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.BiConsumer;


@Mod.EventBusSubscriber(modid = CombatCommonsMod.MOD_ID)
public final class EnchantmentEventHandler {

	private EnchantmentEventHandler() {}

	@SubscribeEvent
	public static void onGetItemAttributeModifiers(final ItemAttributeModifierEvent event) {
		applySimpleAttributeModifiers(event.getItemStack(), event.getSlotType(), event::addModifier);
	}

	private static void applySimpleAttributeModifiers(ItemStack stack, EquipmentSlot slot, BiConsumer<Attribute, AttributeModifier> consumer) {
		if (!stack.isEmpty() && !(stack.getItem() instanceof EnchantedBookItem) && stack.isEnchanted()) {
			ListTag list = stack.getEnchantmentTags();
			for (int i = 0; i < list.size(); i++) {
				CompoundTag tag = list.getCompound(i);
				ResourceLocation id = EnchantmentHelper.getEnchantmentId(tag);
				if (id != null) {
					Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(id);
					if (enchantment instanceof IAttributeModifierEnchantment provider) {
						provider.getAttributeModifiers(stack, EnchantmentHelper.getEnchantmentLevel(tag), slot).forEach(consumer);
					}
				}
			}
		}
	}

}
