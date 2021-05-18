package com.simibubi.create.content.contraptions.components.structureMovement.render;

import java.util.ArrayList;

import com.jozufozu.flywheel.backend.light.GridAlignedBB;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.outliner.AABBOutline;

public class LightVolumeDebugger {
	public static void render(MatrixStack ms, SuperRenderTypeBuffer buffer) {
		ContraptionRenderDispatcher.RENDERERS.values()
				.stream()
				.flatMap(r -> {
					GridAlignedBB texture = r.getLighter().lightVolume.getTextureVolume();
					GridAlignedBB sample = r.getLighter().lightVolume.getSampleVolume();

					ArrayList<Pair<GridAlignedBB, Integer>> pairs = new ArrayList<>(2);

					pairs.add(Pair.of(texture, 0xFFFFFF));
					pairs.add(Pair.of(sample, 0xFFFF00));

					return pairs.stream();
				})
				.map(pair -> {
					AABBOutline outline = new AABBOutline(GridAlignedBB.toAABB(pair.getFirst()));

					outline.getParams().colored(pair.getSecond());
					return outline;
				})
				.forEach(outline -> outline.render(ms, buffer, AnimationTickHolder.getPartialTicks()));
	}
}
