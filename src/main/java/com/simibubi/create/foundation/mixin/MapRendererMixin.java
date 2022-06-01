package com.simibubi.create.foundation.mixin;

import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.logistics.trains.management.edgePoint.station.StationMarker;

import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

@Mixin(MapRenderer.class)
public class MapRendererMixin {

	@Inject(
			method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/level/saveddata/maps/MapItemSavedData;ZI)V",
			at = @At("TAIL")
	)
	public void render(PoseStack ms, MultiBufferSource buffer, int mapId, MapItemSavedData mapData, boolean active, int packedLight, CallbackInfo ci) {
		Iterable<MapDecoration> decorations = mapData.getDecorations();
		int index = 32;
		if (decorations instanceof Collection) {
			index = ((Collection<?>) decorations).size();
		}

		for (MapDecoration deco : decorations) {
			if (!(deco instanceof StationMarker.Decoration stationDeco))
				continue;

			stationDeco.render(ms, buffer, mapId, mapData, active, packedLight, index++);
		}
	}

}
