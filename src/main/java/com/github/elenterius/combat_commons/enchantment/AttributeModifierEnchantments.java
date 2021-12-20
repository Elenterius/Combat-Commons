package com.github.elenterius.combat_commons.enchantment;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.common.util.Lazy;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class AttributeModifierEnchantments {

	private AttributeModifierEnchantments() {}

	/**
	 * simple implementation that applies modifiers for a single slot type
	 */
	public abstract static class SingleEquipment extends Enchantment implements IAttributeModifierEnchantment {

		protected final ImmutableMultimap<Attribute, AttributeModifier> attributeModifiers;

		protected SingleEquipment(Rarity rarity, EnchantmentCategory category, EquipmentSlot applicableSlot) {
			super(rarity, category, new EquipmentSlot[]{applicableSlot});
			ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
			registerAttributeModifiers(builder);
			attributeModifiers = builder.build();
		}

		/**
		 * <b>The attribute modifiers require unique UUIDS!</b><br>
		 * If you don't do that the modifiers can't stack and might get unwillingly removed when the equipment changes.
		 */
		protected abstract void registerAttributeModifiers(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder);

		@Override
		public Multimap<Attribute, AttributeModifier> getAttributeModifiers(final ItemStack stack, int level, final EquipmentSlot slot) {
			return slot == slots[0] ? attributeModifiers : EMPTY;
		}
	}

	/**
	 * provides different attribute modifiers for each enchantment level
	 */
	public abstract static class SingleEquipmentWithLevel extends Enchantment implements IAttributeModifierEnchantment {

		protected final List<ImmutableMultimap<Attribute, AttributeModifier>> attributeModifiers;
		protected final int maxLevel;

		protected SingleEquipmentWithLevel(Rarity rarity, EnchantmentCategory category, int maxLevel, EquipmentSlot applicableSlot) {
			super(rarity, category, new EquipmentSlot[]{applicableSlot});
			assert maxLevel > 0;
			this.maxLevel = maxLevel;

			ImmutableList.Builder<ImmutableMultimap<Attribute, AttributeModifier>> listBuilder = ImmutableList.builderWithExpectedSize(maxLevel);
			for (int i = 0; i < maxLevel; i++) {
				ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
				registerAttributeModifiers(builder, i + 1);
				listBuilder.add(builder.build());
			}
			attributeModifiers = listBuilder.build();
		}

		@Override
		public int getMaxLevel() {
			return maxLevel;
		}

		/**
		 * <b>The attribute modifiers require unique UUIDS!</b><br>
		 * If you don't do that the modifiers can't stack and might get unwillingly removed when the equipment changes.
		 */
		protected abstract void registerAttributeModifiers(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder, int level);

		@Override
		public Multimap<Attribute, AttributeModifier> getAttributeModifiers(final ItemStack stack, int level, final EquipmentSlot slot) {
			if (level > 0 && slot == slots[0]) {
				int index = Math.min(level - 1, attributeModifiers.size() - 1);
				return attributeModifiers.get(index);
			}
			return EMPTY;
		}
	}

	/**
	 * If during mod loading some Attributes are not yet available, you can use this version
	 */
	public abstract static class SingleEquipmentLazy extends Enchantment implements IAttributeModifierEnchantment {

		protected final Lazy<ImmutableMultimap<Attribute, AttributeModifier>> attributeModifiers;

		protected SingleEquipmentLazy(Rarity rarity, EnchantmentCategory category, EquipmentSlot applicableSlot) {
			super(rarity, category, new EquipmentSlot[]{applicableSlot});
			attributeModifiers = Lazy.of(() -> {
				ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
				registerAttributeModifiers(builder);
				return builder.build();
			});
		}

		/**
		 * <b>The attribute modifiers require unique UUIDS!</b><br>
		 * If you don't do that the modifiers can't stack and might get unwillingly removed when the equipment changes.
		 */
		protected abstract void registerAttributeModifiers(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder);

		@Override
		public Multimap<Attribute, AttributeModifier> getAttributeModifiers(final ItemStack stack, int level, final EquipmentSlot slot) {
			return slot == slots[0] ? attributeModifiers.get() : EMPTY;
		}
	}

	/**
	 * handles modifiers for several equipment slots (e.g. Armor)
	 */
	public abstract static class MultiEquipment extends Enchantment implements IAttributeModifierEnchantment {

		protected final Map<EquipmentSlot, ImmutableMultimap<Attribute, AttributeModifier>> attributeModifiers = new EnumMap<>(EquipmentSlot.class);

		protected MultiEquipment(Rarity rarity, EnchantmentCategory category, EquipmentSlot... applicableSlots) {
			super(rarity, category, applicableSlots);
			for (EquipmentSlot slot : slots) {
				ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
				registerAttributeModifiers(builder, slot);
				attributeModifiers.put(slot, builder.build());
			}
		}

		/**
		 * <b>The attribute modifiers require unique UUIDS for each equipment slot!</b><br>
		 * If you don't do that the modifiers can't stack and will get unwillingly removed when the equipment changes.
		 *
		 * @param slot for which slot to register the modifiers
		 */
		protected abstract void registerAttributeModifiers(final ImmutableMultimap.Builder<Attribute, AttributeModifier> builder, final EquipmentSlot slot);

		@Override
		public Multimap<Attribute, AttributeModifier> getAttributeModifiers(final ItemStack stack, int level, final EquipmentSlot slot) {
			return attributeModifiers.getOrDefault(slot, EMPTY);
		}
	}

	public abstract static class MultiEquipmentWithLevel extends Enchantment implements IAttributeModifierEnchantment {

		protected final Map<EquipmentSlot, List<ImmutableMultimap<Attribute, AttributeModifier>>> attributeModifiers = new EnumMap<>(EquipmentSlot.class);
		protected final int maxLevel;

		protected MultiEquipmentWithLevel(Rarity rarity, EnchantmentCategory category, int maxLevel, EquipmentSlot... applicableSlots) {
			super(rarity, category, applicableSlots);
			assert maxLevel > 0;
			this.maxLevel = maxLevel;

			for (EquipmentSlot slot : slots) {
				ImmutableList.Builder<ImmutableMultimap<Attribute, AttributeModifier>> listBuilder = ImmutableList.builderWithExpectedSize(maxLevel);
				for (int i = 0; i < maxLevel; i++) {
					ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
					registerAttributeModifiers(builder, i + 1, slot);
					listBuilder.add(builder.build());
				}
				attributeModifiers.put(slot, listBuilder.build());
			}
		}

		@Override
		public int getMaxLevel() {
			return maxLevel;
		}

		/**
		 * <b>The attribute modifiers require unique UUIDS for each equipment slot!</b><br>
		 * If you don't do that the modifiers can't stack and will get unwillingly removed when the equipment changes.
		 *
		 * @param slot for which slot to register the modifiers
		 */
		protected abstract void registerAttributeModifiers(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder, int level, final EquipmentSlot slot);

		@Override
		public Multimap<Attribute, AttributeModifier> getAttributeModifiers(final ItemStack stack, int level, final EquipmentSlot slot) {
			if (level > 0) {
				List<ImmutableMultimap<Attribute, AttributeModifier>> list = attributeModifiers.get(slot);
				if (list != null) {
					int index = Math.min(level - 1, attributeModifiers.size() - 1);
					return list.get(index);
				}
			}
			return EMPTY;
		}
	}

	/**
	 * Handles modifiers for several equipment slots (e.g. Armor)<br>
	 * If during mod loading some Attributes are not yet available, you can use this version.
	 */
	public abstract static class MultiEquipmentLazy extends Enchantment implements IAttributeModifierEnchantment {

		protected final Map<EquipmentSlot, Lazy<ImmutableMultimap<Attribute, AttributeModifier>>> attributeModifiers = new EnumMap<>(EquipmentSlot.class);

		protected MultiEquipmentLazy(Rarity rarity, EnchantmentCategory category, EquipmentSlot... applicableSlots) {
			super(rarity, category, applicableSlots);
			for (EquipmentSlot slot : slots) {
				attributeModifiers.put(slot, Lazy.of(() -> {
					ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
					registerAttributeModifiers(builder, slot);
					return builder.build();
				}));
			}
		}

		/**
		 * <b>The attribute modifiers require unique UUIDS for each equipment slot!</b><br>
		 * If you don't do that the modifiers can't stack and will get unwillingly removed when the equipment changes.
		 *
		 * @param slot for which slot to register the modifiers
		 */
		protected abstract void registerAttributeModifiers(final ImmutableMultimap.Builder<Attribute, AttributeModifier> builder, final EquipmentSlot slot);

		@Override
		public Multimap<Attribute, AttributeModifier> getAttributeModifiers(final ItemStack stack, int level, final EquipmentSlot slot) {
			Lazy<ImmutableMultimap<Attribute, AttributeModifier>> lazySupplier = attributeModifiers.get(slot);
			return lazySupplier != null ? lazySupplier.get() : EMPTY;
		}
	}

}
