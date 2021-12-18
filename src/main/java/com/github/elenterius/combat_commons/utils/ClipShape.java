package com.github.elenterius.combat_commons.utils;

import com.github.elenterius.combat_commons.utils.RayTraceUtil.EmptyColliderOrOutlineClipContext;
import com.github.elenterius.combat_commons.utils.RayTraceUtil.EmptyVisualOrOutlineClipContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;

/**
 * determines which block shape is hittable by the ray trace
 * <br><br>
 * OUTLINE:  <pre>	use outline voxel shape of block (default minecraft behavior)</pre>
 * EMPTY_COLLIDER_OR_OUTLINE:  <pre>	when the block has not an empty collider shape use the blocks outline instead</pre>
 * EMPTY_VISUAL_OR_OUTLINE:  <pre>	when the block has not an empty visual shape use the blocks outline instead
 * 	(this allows the ray to go through glass/plants but still interact normally with other blocks) </pre>
 * VISUAL:   <pre>	use the visual block shape
 * 	(some blocks are visually empty (Glass) or bigger (SoulSand) --> the ray will go through Glass)</pre>
 * COLLIDER: <pre>	use collision voxel shape of block (plants have an empty collider, fences/walls have a 1.5 blocks tall one)</pre>
 */
public enum ClipShape {
	OUTLINE((startPos, endPos, fluidMode, entity) -> new ClipContext(startPos, endPos, ClipContext.Block.OUTLINE, fluidMode, entity)),
	EMPTY_COLLIDER_OR_OUTLINE(EmptyColliderOrOutlineClipContext::new),
	EMPTY_VISUAL_OR_OUTLINE(EmptyVisualOrOutlineClipContext::new),
	COLLIDER((startPos, endPos, fluidMode, entity) -> new ClipContext(startPos, endPos, ClipContext.Block.COLLIDER, fluidMode, entity)),
	VISUAL((startPos, endPos, fluidMode, entity) -> new ClipContext(startPos, endPos, ClipContext.Block.VISUAL, fluidMode, entity));

	private final IClipContextFactory factory;

	ClipShape(IClipContextFactory factory) {
		this.factory = factory;
	}

	public ClipContext createContext(Vec3 startPos, Vec3 endPos, ClipContext.Fluid fluidMode, Entity entity) {
		return factory.create(startPos, endPos, fluidMode, entity);
	}

}

interface IClipContextFactory {
	ClipContext create(Vec3 startPos, Vec3 endPos, ClipContext.Fluid fluidMode, Entity entity);
}
