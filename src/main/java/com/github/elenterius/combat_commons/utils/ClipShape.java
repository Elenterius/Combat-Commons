package com.github.elenterius.combat_commons.utils;

import com.github.elenterius.combat_commons.utils.RayTraceUtil.EmptyColliderOrOutlineClipContext;
import com.github.elenterius.combat_commons.utils.RayTraceUtil.EmptyVisualOrOutlineClipContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;

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
	OUTLINE((startPos, endPos, fluidMode, entity) -> new RayTraceContext(startPos, endPos, RayTraceContext.BlockMode.OUTLINE, fluidMode, entity)),
	EMPTY_COLLIDER_OR_OUTLINE(EmptyColliderOrOutlineClipContext::new),
	EMPTY_VISUAL_OR_OUTLINE(EmptyVisualOrOutlineClipContext::new),
	COLLIDER((startPos, endPos, fluidMode, entity) -> new RayTraceContext(startPos, endPos, RayTraceContext.BlockMode.COLLIDER, fluidMode, entity)),
	VISUAL((startPos, endPos, fluidMode, entity) -> new RayTraceContext(startPos, endPos, RayTraceContext.BlockMode.VISUAL, fluidMode, entity));

	private final IClipContextFactory factory;

	ClipShape(IClipContextFactory factory) {
		this.factory = factory;
	}

	public RayTraceContext createContext(Vector3d startPos, Vector3d endPos, RayTraceContext.FluidMode fluidMode, Entity entity) {
		return factory.create(startPos, endPos, fluidMode, entity);
	}

}

interface IClipContextFactory {
	RayTraceContext create(Vector3d startPos, Vector3d endPos, RayTraceContext.FluidMode fluidMode, Entity entity);
}
