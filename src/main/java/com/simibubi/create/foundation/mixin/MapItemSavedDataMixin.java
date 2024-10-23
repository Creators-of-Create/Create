package com.simibubi.create.foundation.mixin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.Maps;
import com.simibubi.create.content.trains.station.StationBlockEntity;
import com.simibubi.create.content.trains.station.StationMapData;
import com.simibubi.create.content.trains.station.StationMarker;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

@Mixin(MapItemSavedData.class)
public abstract class MapItemSavedDataMixin implements StationMapData {
	@Unique
	private static final String STATION_MARKERS_KEY = "create:stations";

	@Shadow
	@Final
	public int centerX;

	@Shadow
	@Final
	public int centerZ;

	@Shadow
	@Final
	public byte scale;

	@Shadow
	@Final
	Map<String, MapDecoration> decorations;

	@Shadow
	private int trackedDecorationCount;

	@Unique
	private final Map<String, StationMarker> create$stationMarkers = Maps.newHashMap();

	@Inject(
			method = "load(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;",
			at = @At("TAIL")
	)
	private static void create$onLoad(CompoundTag compound, CallbackInfoReturnable<MapItemSavedData> cir) {
		MapItemSavedData mapData = cir.getReturnValue();
		StationMapData stationMapData = (StationMapData) mapData;

		ListTag listTag = compound.getList(STATION_MARKERS_KEY, Tag.TAG_COMPOUND);
		for (int i = 0; i < listTag.size(); ++i) {
			StationMarker stationMarker = StationMarker.load(listTag.getCompound(i));
			stationMapData.create$addStationMarker(stationMarker);
		}
	}

	@Inject(method = "save(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;", at = @At("TAIL"))
	private void create$onSave(CompoundTag compound, CallbackInfoReturnable<CompoundTag> cir) {
		ListTag listTag = new ListTag();
		for (StationMarker stationMarker : create$stationMarkers.values()) {
			listTag.add(stationMarker.save());
		}
		compound.put(STATION_MARKERS_KEY, listTag);
	}

	@Override
	public void create$addStationMarker(StationMarker marker) {
		create$stationMarkers.put(marker.getId(), marker);

		int scaleMultiplier = 1 << scale;
		float localX = (marker.getTarget().getX() - centerX) / (float) scaleMultiplier;
		float localZ = (marker.getTarget().getZ() - centerZ) / (float) scaleMultiplier;

		if (localX < -63.0F || localX > 63.0F || localZ < -63.0F || localZ > 63.0F) {
			removeDecoration(marker.getId());
			return;
		}

		byte localXByte = (byte) (int) (localX * 2.0F + 0.5F);
		byte localZByte = (byte) (int) (localZ * 2.0F + 0.5F);

		MapDecoration decoration = new StationMarker.Decoration(localXByte, localZByte, marker.getName());
		MapDecoration oldDecoration = decorations.put(marker.getId(), decoration);
		if (!decoration.equals(oldDecoration)) {
			if (oldDecoration != null && oldDecoration.getType().shouldTrackCount()) {
				--trackedDecorationCount;
			}

			if (decoration.getType().shouldTrackCount()) {
				++trackedDecorationCount;
			}

			setDecorationsDirty();
		}
	}

	@Shadow
	protected abstract void removeDecoration(String pIdentifier);

	@Shadow
	protected abstract void setDecorationsDirty();

	@Shadow
	public abstract boolean isTrackedCountOverLimit(int pTrackedCount);

	@Override
	public boolean create$toggleStation(LevelAccessor level, BlockPos pos, StationBlockEntity stationBlockEntity) {
		double xCenter = pos.getX() + 0.5D;
		double zCenter = pos.getZ() + 0.5D;
		int scaleMultiplier = 1 << scale;

		double localX = (xCenter - (double) centerX) / (double) scaleMultiplier;
		double localZ = (zCenter - (double) centerZ) / (double) scaleMultiplier;

		if (localX < -63.0D || localX > 63.0D || localZ < -63.0D || localZ > 63.0D)
			return false;

		StationMarker marker = StationMarker.fromWorld(level, pos);
		if (marker == null)
			return false;

		if (create$stationMarkers.remove(marker.getId(), marker)) {
			removeDecoration(marker.getId());
			return true;
		}

		if (!isTrackedCountOverLimit(256)) {
			create$addStationMarker(marker);
			return true;
		}

		return false;
	}

	@Inject(method = "checkBanners(Lnet/minecraft/world/level/BlockGetter;II)V", at = @At("TAIL"))
	private void create$onCheckBanners(BlockGetter blockGetter, int x, int z, CallbackInfo ci) {
		create$checkStations(blockGetter, x, z);
	}

	@Unique
	private void create$checkStations(BlockGetter blockGetter, int x, int z) {
		Iterator<StationMarker> iterator = create$stationMarkers.values().iterator();
		List<StationMarker> newMarkers = new ArrayList<>();

		while (iterator.hasNext()) {
			StationMarker marker = iterator.next();
			if (marker.getTarget().getX() == x && marker.getTarget().getZ() == z) {
				StationMarker other = StationMarker.fromWorld(blockGetter, marker.getSource());
				if (!marker.equals(other)) {
					iterator.remove();
					removeDecoration(marker.getId());

					if (other != null && marker.getTarget().equals(other.getTarget())) {
						newMarkers.add(other);
					}
				}
			}
		}

		for (StationMarker marker : newMarkers) {
			create$addStationMarker(marker);
		}
	}
}
