package com.github.elenterius.combat_commons.test;

import com.github.elenterius.combat_commons.enchantment.AttributeModifierEnchantments;
import com.github.elenterius.combat_commons.enchantment.IExtraDamageEnchantment;
import com.github.elenterius.combat_commons.entity.EntityAttributeUtil;
import com.google.common.collect.ImmutableMultimap;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = TestMod.MOD_ID) //forces loading of fields
public final class EnchantmentStuff {

	private EnchantmentStuff() {}

	public static final DeferredRegister<Enchantment> ENCHANTMENTS = TestMod.ENCHANTMENTS;

	public static final Marker DAMAGE_BONUS_MARKER = MarkerManager.getMarker("DamageBonus");

	public static RegistryObject<Enchantment> CORRUPTED_ATTACK_REACH_ENCHANT = ENCHANTMENTS.register("corrupted_attack_reach", () -> new CorruptedAttackReachEnchantment(EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND));
	public static RegistryObject<Enchantment> CORRUPTED_ARMOR_ENCHANT = ENCHANTMENTS.register("corrupted_armor", CorruptedArmorEnchantment::new);
	public static RegistryObject<DamageBonusEnchantment> RAVAGER_BANE_ENCHANTMENT = ENCHANTMENTS.register("ravager_bane", () -> new DamageBonusEnchantment(Enchantment.Rarity.RARE, EntityType.RAVAGER, EquipmentSlot.MAINHAND));
	public static RegistryObject<DamageBonusEnchantment> PIG_BANE_ENCHANTMENT = ENCHANTMENTS.register("pig_bane", () -> new DamageBonusEnchantment(Enchantment.Rarity.RARE, EntityType.PIG, EquipmentSlot.MAINHAND));
	public static RegistryObject<BerserkerEnchantment> BERSERKER_ENCHANTMENT = ENCHANTMENTS.register("berserker", () -> new BerserkerEnchantment(Enchantment.Rarity.RARE, EquipmentSlot.MAINHAND));

	static List<ItemStack> getItemGroupItems() {
		List<ItemStack> items = new ArrayList<>();
		for (RegistryObject<Enchantment> entry : ENCHANTMENTS.getEntries()) {
			Enchantment enchantment = entry.get();

			ItemStack stack = new ItemStack(Items.DIAMOND_SWORD);
			stack.setHoverName(new TextComponent("[TEST] DAMAGE BONUS"));
			stack.enchant(enchantment, enchantment.getMaxLevel());
			items.add(stack);

			items.add(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, enchantment.getMaxLevel())));
		}
		return items;
	}

	private static class DamageBonusEnchantment extends Enchantment implements IExtraDamageEnchantment {

		private final EntityType<?> entityType;

		public DamageBonusEnchantment(Rarity rarityIn, EntityType<?> entityType, EquipmentSlot... slots) {
			super(rarityIn, EnchantmentCategory.WEAPON, slots);
			this.entityType = entityType;
		}

		@Override
		public float getDamageBonus(int level, MobType mobType) {
			return 1f; //initialDamageBonus
		}

		@Override
		public float getExtraDamage(ItemStack stack, int level, Entity attacker, Entity victim) {
			boolean isValidVictim = victim.getType() == entityType;
			if (isValidVictim) {
				TestMod.LOGGER.debug(DAMAGE_BONUS_MARKER, "found valid damage bonus target");
			}
			return isValidVictim ? level : 0f;
		}

		@Override
		public int getMaxLevel() {
			return 5;
		}

		@Override
		public boolean canEnchant(ItemStack stack) {
			Item item = stack.getItem();
			return item instanceof AxeItem || item instanceof TridentItem || super.canEnchant(stack);
		}

	}

	private static class BerserkerEnchantment extends Enchantment implements IExtraDamageEnchantment {

		public BerserkerEnchantment(Rarity rarityIn, EquipmentSlot... slots) {
			super(rarityIn, EnchantmentCategory.WEAPON, slots);
		}

		@Override
		public float getDamageBonus(int level, MobType mobType) {
			return 0f; //initialDamageBonus
		}

		@Override
		public float getExtraDamage(ItemStack stack, int level, Entity attacker, Entity victim) {
			if (attacker instanceof LivingEntity living) {
				float damageBonus = level * 5f * (1f - living.getHealth() / living.getMaxHealth());
				TestMod.LOGGER.debug(DAMAGE_BONUS_MARKER, String.format("BerserkerEnchantment added damage bonus of %.2f", damageBonus));
				return damageBonus;
			}
			return 0f;
		}

		@Override
		public int getMaxLevel() {
			return 5;
		}

	}

	static class CorruptedAttackReachEnchantment extends AttributeModifierEnchantments.MultiEquipment {

		protected CorruptedAttackReachEnchantment(EquipmentSlot... applicableSlots) {
			super(Enchantment.Rarity.COMMON, EnchantmentCategory.WEAPON, applicableSlots);
		}

		@Override
		protected void registerAttributeModifiers(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder, EquipmentSlot slot) {
			builder.put(EntityAttributeUtil.getAttackReach(), new AttributeModifier("Weapon modifier", -1f, AttributeModifier.Operation.MULTIPLY_TOTAL));
			builder.put(EntityAttributeUtil.getBlockReach(), new AttributeModifier("Tool modifier", 2, AttributeModifier.Operation.ADDITION));
		}

	}

	static class CorruptedArmorEnchantment extends AttributeModifierEnchantments.MultiEquipmentWithLevel {

		protected CorruptedArmorEnchantment(EquipmentSlot... applicableSlots) {
			super(Enchantment.Rarity.COMMON, EnchantmentCategory.ARMOR, 5, applicableSlots);
		}

		protected CorruptedArmorEnchantment() {
			this(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
		}

		@Override
		protected void registerAttributeModifiers(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder, int level, EquipmentSlot slot) {
			builder.put(Attributes.MOVEMENT_SPEED, new AttributeModifier("Armor modifier", -0.025f * level, AttributeModifier.Operation.MULTIPLY_TOTAL));
			builder.put(Attributes.ATTACK_SPEED, new AttributeModifier("Armor modifier", -0.02f * level, AttributeModifier.Operation.MULTIPLY_TOTAL));
			builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier("Armor modifier", 0.5 * level, AttributeModifier.Operation.ADDITION));
			builder.put(Attributes.MAX_HEALTH, new AttributeModifier("Armor modifier", level + 1, AttributeModifier.Operation.ADDITION));
		}
	}

}
