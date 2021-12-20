package com.github.elenterius.combat_commons.enchantment;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

public interface IAttributeModifierEnchantment {

	ImmutableMultimap<Attribute, AttributeModifier> EMPTY = ImmutableMultimap.of();

	/**
	 * <b>Attribute modifiers require unique UUIDS for each equipment slot</b><br>
	 * If you don't do that the modifiers can't stack (e.g. Armor) and will get unwillingly removed when the equipment changes.<br><br>
	 * <p>
	 * You should always return the same modifiers for the same parameters, else the proper removal might fail.<br>see {@link LivingEntity#collectEquipmentChanges()}
	 *
	 * @param stack enchanted item
	 * @param level enchantment level
	 * @param slot  current equipment slot
	 * @return modifiers which are used to <b>remove</b> and <b>add</b> modifiers to the player,<br>see {@link LivingEntity#collectEquipmentChanges()}
	 */
	Multimap<Attribute, AttributeModifier> getAttributeModifiers(final ItemStack stack, final int level, final EquipmentSlot slot);

}
