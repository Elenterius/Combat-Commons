package com.github.elenterius.combat_commons.mixin;

import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.shapes.ISelectionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RayTraceContext.class)
public interface RayTraceContextAccessor {
	@Accessor
	ISelectionContext getCollisionContext();
}
