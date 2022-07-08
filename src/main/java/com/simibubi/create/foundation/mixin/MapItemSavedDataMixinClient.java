package com.simibubi.create.foundation.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.content.logistics.trains.management.edgePoint.station.StationMarker;

import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapDecoration.Type;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

@Mixin(MapItemSavedData.class)
public class MapItemSavedDataMixinClient {

	@Inject(method = "addClientSideDecorations(Ljava/util/List;)V", at = @At("HEAD"))
	private void addClientSideDecorations(List<MapDecoration> pDecorations, CallbackInfo ci) {
		for (int i = 0; i < pDecorations.size(); i++) {
			MapDecoration deco = pDecorations.get(i);
			if (deco.getType() != Type.MANSION)
				continue;
			if (deco.getName() == null)
				continue;
			pDecorations.set(i, new StationMarker.Decoration(deco.getX(), deco.getY(), deco.getName()));
		}
	}
	
}
