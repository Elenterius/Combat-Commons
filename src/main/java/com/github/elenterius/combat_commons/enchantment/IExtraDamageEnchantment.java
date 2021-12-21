package com.github.elenterius.combat_commons.enchantment;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public interface IExtraDamageEnchantment {

	/**
	 * Extra damage bonus to apply on top of the damage to be dealt to the victim of the attack.<br>
	 * This is independent of {@link Enchantment#getDamageBonus(int, MobType)}
	 *
	 * @param stack    the item that holds this enchantment
	 * @param level    the level of the enchantment
	 * @param attacker Entity that attacked the victim
	 * @param victim   Entity that was attacked
	 * @return extra damage bonus against the given entity
	 */
	float getExtraDamage(ItemStack stack, int level, Entity attacker, Entity victim);

}
