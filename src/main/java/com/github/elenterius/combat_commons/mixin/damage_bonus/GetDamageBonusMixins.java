package com.github.elenterius.combat_commons.mixin.damage_bonus;

import com.github.elenterius.combat_commons.enchantment.EnchantmentUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Elenterius
 */
public final class GetDamageBonusMixins {

	private GetDamageBonusMixins() {}

	@Mixin(Player.class)
	public abstract static class PlayerEntityMixin {

		@Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getDamageBonus(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/MobType;)F"))
		protected float combatCommons_transformGetDamageBonus(ItemStack stack, MobType mobType, Entity victim) {
			return EnchantmentUtil.getDamageBonus(stack, (Player) (Object) this, victim, mobType);
		}

	}

	@Mixin(Mob.class)
	public abstract static class MobEntityMixin {

		@Redirect(method = "doHurtTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getDamageBonus(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/MobType;)F"))
		protected float combatCommons_transformGetDamageBonus(ItemStack stack, MobType mobType, Entity victim) {
			return EnchantmentUtil.getDamageBonus(stack, (Mob) (Object) this, victim, mobType);
		}

	}

	@Mixin(ThrownTrident.class)
	public abstract static class TridentEntityMixin {

		@Redirect(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getDamageBonus(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/MobType;)F"))
		protected float combatCommons_transformGetDamageBonus(ItemStack stack, MobType mobType, EntityHitResult hitResult) {
			ThrownTrident thrownTrident = (ThrownTrident) (Object) this;
			Entity owner = thrownTrident.getOwner();
			return EnchantmentUtil.getDamageBonus(stack, owner != null ? owner : thrownTrident, hitResult.getEntity(), mobType);
		}

	}

}
