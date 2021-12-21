package com.github.elenterius.combat_commons.mixin.damage_bonus;

import com.github.elenterius.combat_commons.CombatCommonsMod;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.Stream;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {

	@Inject(method = "getDamageBonus", at = @At("HEAD"))
	private static void onGetDamageBonus(ItemStack stack, MobType mobType, CallbackInfoReturnable<Float> cir) {
		String caller = getCaller();
		if (!caller.contains("ItemStack#getTooltipLines")) {
			CombatCommonsMod.LOGGER.warn("EnchantmentHelper#getDamageBonus was called by {}", caller);
		}
	}

	@Unique
	private static String getCaller() {
		StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
		return walker.walk(EnchantmentHelperMixin::walk);
	}

	@Unique
	private static String walk(Stream<StackWalker.StackFrame> stackFrameStream) {
		return stackFrameStream
				.filter(frame -> !(frame.getClassName().contains("EnchantmentHelper")))
				.findFirst()
				.map(f -> f.getClassName() + "#" + f.getMethodName() + ":<<Line " + f.getLineNumber() + ">>")
				.orElse("Unknown caller");
	}

}
