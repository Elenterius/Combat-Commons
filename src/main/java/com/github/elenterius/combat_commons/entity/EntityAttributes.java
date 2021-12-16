package com.github.elenterius.combat_commons.entity;


import com.github.elenterius.combat_commons.CombatCommonsMod;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class EntityAttributes {

	public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Attribute.class, CombatCommonsMod.MOD_ID);
	public static final RegistryObject<Attribute> ATTACK_REACH = ATTRIBUTES.register("attack_reach", () -> new RangedAttribute("attribute.generic.attack_reach", 3, 0, 1024).setSyncable(true));

	private EntityAttributes() {}

}
