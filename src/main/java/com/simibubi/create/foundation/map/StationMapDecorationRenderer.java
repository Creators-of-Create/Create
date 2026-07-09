package com.simibubi.create.foundation.map;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.neoforged.neoforge.client.gui.map.IMapDecorationRenderer;

public class StationMapDecorationRenderer implements IMapDecorationRenderer {
	@Override
	public boolean render(MapRenderState.MapDecorationRenderState decorationRenderState, PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector, MapRenderState mapRenderState, TextureAtlas decorationSprites,
		boolean inItemFrame, int packedLight, int index) {
		// TODO 26.2: port station map labels to the MapRenderState submit pipeline.
		return false;
	}
}
