package com.simibubi.create.foundation.mixin;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.content.trains.station.StationMarker;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

// random priority to prevent networking conflicts
@Mixin(value = ClientboundMapItemDataPacket.class, priority = 826)
public class ClientboundMapItemDataPacketMixin {
	@Shadow
	@Final
	private List<MapDecoration> decorations;

	@Unique
	private int[] create$stationIndices;

	@Inject(method = "<init>(IBZLjava/util/Collection;Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData$MapPatch;)V", at = @At("RETURN"))
	private void create$onInit(int mapId, byte scale, boolean locked, @Nullable Collection<MapDecoration> decorations, @Nullable MapItemSavedData.MapPatch colorPatch, CallbackInfo ci) {
		create$stationIndices = create$getStationIndices(this.decorations);
	}

	@Unique
	private static int[] create$getStationIndices(List<MapDecoration> decorations) {
		if (decorations == null) {
			return new int[0];
		}

		IntList indices = new IntArrayList();
		for (int i = 0; i < decorations.size(); i++) {
			MapDecoration decoration = decorations.get(i);
			if (decoration instanceof StationMarker.Decoration) {
				indices.add(i);
			}
		}
		return indices.toIntArray();
	}

	@Inject(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At("RETURN"))
	private void create$onInit(FriendlyByteBuf buf, CallbackInfo ci) {
		// without this try catch statement, it crashes when loading on a Velocity hub (https://github.com/Fabricators-of-Create/Create/issues/1162)
		// might be a better way to handle this such as checking if on a modded server but this works
		try {
			create$stationIndices = buf.readVarIntArray();

			if (decorations != null) {
				for (int i : create$stationIndices) {
					if (i >= 0 && i < decorations.size()) {
						MapDecoration decoration = decorations.get(i);
						decorations.set(i, StationMarker.Decoration.from(decoration));
					}
				}
			}
		} catch (IndexOutOfBoundsException ignored) {
			// specific exception causing crash
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Inject(method = "write(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At("RETURN"))
	private void create$onWrite(FriendlyByteBuf buf, CallbackInfo ci) {
		buf.writeVarIntArray(create$stationIndices);
	}
}
