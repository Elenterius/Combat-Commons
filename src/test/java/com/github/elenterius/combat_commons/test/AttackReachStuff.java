package com.github.elenterius.combat_commons.test;

import com.github.elenterius.combat_commons.enchantment.AttributeModifierEnchantments;
import com.github.elenterius.combat_commons.entity.EntityAttributeUtil;
import com.github.elenterius.combat_commons.item.IClipShapeProvider;
import com.github.elenterius.combat_commons.utils.ClipShape;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = TestMod.MOD_ID) //forces loading of class
public final class AttackReachStuff {

	public static final DeferredRegister<Item> ITEMS = TestMod.ITEMS;
	public static final DeferredRegister<Enchantment> ENCHANTMENTS = TestMod.ENCHANTMENTS;

	public static final UUID ATTACK_REACH_UUID = UUID.fromString("9bc1ee60-2d06-40d2-aeb5-1292cc416f72");

	public static RegistryObject<Enchantment> CORRUPTED_ATTACK_REACH_ENCHANT = ENCHANTMENTS.register("corrupted_attack_reach", () -> new CorruptedAttackReachEnchantment(EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND));
	public static RegistryObject<Enchantment> CORRUPTED_ARMOR_ENCHANT = ENCHANTMENTS.register("corrupted_armor", CorruptedArmorEnchantment::new);

	public static RegistryObject<Item> VERY_SHORT_SWORD = ITEMS.register("very_short_sword", () -> new TestSwordItem(Tiers.DIAMOND, -1.5f, 3, -1.5f, getDefaultProperties()));
	public static RegistryObject<Item> SHORT_SWORD = ITEMS.register("short_sword", () -> new TestSwordItem(Tiers.DIAMOND, -1f, 3, -2f, getDefaultProperties()) {
		@Override
		protected void addAdditionalAttributeModifiers(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder) {
			//test item with increased block reach and reduced attack reach
			builder.put(EntityAttributeUtil.getBlockReach(), new AttributeModifier(UUID.fromString("d77d85ea-0112-4aad-9c18-1bc2a0b70f9e"), "Weapon modifier", 3, AttributeModifier.Operation.ADDITION));
		}
	});
	public static RegistryObject<Item> LONG_SWORD = ITEMS.register("long_sword", () -> new TestSwordItem(Tiers.DIAMOND, 2f, 3, -2.8f, getDefaultProperties()));
	public static RegistryObject<Item> VERY_LONG_SWORD = ITEMS.register("very_long_sword", () -> new TestSwordItem(Tiers.DIAMOND, 6f, 3, -3f, getDefaultProperties()) {
		@Override
		public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
			return !pState.getCollisionShape(pLevel, pPos).isEmpty();
		}

		@Override
		public ClipShape getRayTraceModeForPickEntity(ItemStack stack, Player player) {
			return ClipShape.EMPTY_COLLIDER_OR_OUTLINE;
		}
	});
	//sword that sets block reach to zero (vanilla attack reach)
	public static RegistryObject<Item> ETHEREAL_SWORD = ITEMS.register("ethereal_sword", () -> new TestSwordItem(Tiers.DIAMOND, 0f, 3, -2.4f, getDefaultProperties()) {
		@Override
		protected void addAdditionalAttributeModifiers(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder) {
			builder.put(EntityAttributeUtil.getBlockReach(), new AttributeModifier(UUID.fromString("43483e9b-a2f2-4673-bb6b-1157653584f9"), "Weapon modifier", -1f, AttributeModifier.Operation.MULTIPLY_TOTAL));
		}

		@Override
		public ClipShape getRayTraceModeForPickEntity(ItemStack stack, Player player) {
			return ClipShape.EMPTY_VISUAL_OR_OUTLINE;
		}
	});
	//pickaxe that sets attack reach to zero
	public static RegistryObject<Item> ETHEREAL_PICKAXE = ITEMS.register("ethereal_pickaxe", () -> new EtherealPickaxeItem(Tiers.DIAMOND, 1, -2.8f, getDefaultProperties()));

	private AttackReachStuff() {}

	private static Item.Properties getDefaultProperties() {
		return new Item.Properties().tab(TestMod.ITEM_GROUP);
	}

	static class TestSwordItem extends SwordItem implements IClipShapeProvider {

		final Lazy<Multimap<Attribute, AttributeModifier>> lazyAttributeModifiers;

		public TestSwordItem(Tier tier, float attackReachModifier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
			super(tier, attackDamageModifier, attackSpeedModifier, properties);
			lazyAttributeModifiers = Lazy.of(() -> {
				ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
				builder.putAll(super.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND));
				builder.put(EntityAttributeUtil.getAttackReach(), new AttributeModifier(ATTACK_REACH_UUID, "Weapon modifier", attackReachModifier, AttributeModifier.Operation.ADDITION));
				addAdditionalAttributeModifiers(builder);
				return builder.build();
			});
		}

		protected void addAdditionalAttributeModifiers(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder) {}

		@Override
		public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlotType) {
			return equipmentSlotType == EquipmentSlot.MAINHAND ? lazyAttributeModifiers.get() : super.getDefaultAttributeModifiers(equipmentSlotType);
		}

	}

	//pickaxe that can't attack entities
	static class EtherealPickaxeItem extends PickaxeItem {

		final Lazy<Multimap<Attribute, AttributeModifier>> lazyAttributeModifiers;

		public EtherealPickaxeItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
			super(tier, attackDamageModifier, attackSpeedModifier, properties);
			lazyAttributeModifiers = Lazy.of(() -> {
				ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
				builder.putAll(super.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND));
				builder.put(EntityAttributeUtil.getAttackReach(), new AttributeModifier(ATTACK_REACH_UUID, "Tool modifier", -1f, AttributeModifier.Operation.MULTIPLY_TOTAL)); // sets attack reach to zero
				builder.put(EntityAttributeUtil.getBlockReach(), new AttributeModifier(UUID.fromString("7d67c367-3a17-461a-b362-e7b75b2709fb"), "Tool modifier", 2, AttributeModifier.Operation.ADDITION));
				return builder.build();
			});
		}

		@Override
		public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlotType) {
			return equipmentSlotType == EquipmentSlot.MAINHAND ? lazyAttributeModifiers.get() : super.getDefaultAttributeModifiers(equipmentSlotType);
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
