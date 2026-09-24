package com.simibubi.create.content.kinetics.belt;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.SpriteShiftEntry;

import org.jetbrains.annotations.Nullable;

/**
 * Represents the client rendering information for belt casings, including cover models and texture shifts. (null sprite shift results in brass textures)
 */
public record BeltCasingRenderInfo(
	PartialModel coverModelX,
	PartialModel coverModelZ,
	@Nullable SpriteShiftEntry spriteShift
) {
}
