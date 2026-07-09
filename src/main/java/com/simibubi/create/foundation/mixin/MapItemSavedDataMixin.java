package com.simibubi.create.foundation.mixin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.trains.station.StationBlockEntity;
import com.simibubi.create.content.trains.station.StationMapData;
import com.simibubi.create.content.trains.station.StationMarker;

import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.saveddata.maps.MapBanner;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapFrame;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.nio.ByteBuffer;

@Mixin(MapItemSavedData.class)
public class MapItemSavedDataMixin implements StationMapData {
	@Unique
	private static final String STATION_MARKERS_KEY = "create:stations";

	@Unique
	private static final Codec<StationMarker> CREATE_STATION_MARKER_CODEC = CompoundTag.CODEC.xmap(
		tag -> StationMarker.load(tag, null),
		marker -> marker.save(null)
	);

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
	public ResourceKey<Level> dimension;

	@Shadow
	@Final
	private boolean trackingPosition;

	@Shadow
	@Final
	private boolean unlimitedTracking;

	@Shadow
	@Final
	public boolean locked;

	@Shadow
	public byte[] colors;

	@Shadow
	@Final
	Map<String, MapDecoration> decorations;

	@Shadow
	@Final
	private Map<String, MapBanner> bannerMarkers;

	@Shadow
	@Final
	private Map<String, MapFrame> frameMarkers;

	@Shadow
	private int trackedDecorationCount;

	@Unique
	private final Map<String, StationMarker> create$stationMarkers = Maps.newHashMap();

	@Inject(
			method = "type",
			at = @At("RETURN"),
			cancellable = true
	)
	private static void create$useStationMarkerCodec(MapId id, CallbackInfoReturnable<SavedDataType<MapItemSavedData>> cir) {
		SavedDataType<MapItemSavedData> original = cir.getReturnValue();
		cir.setReturnValue(new SavedDataType<>(original.id(), original.factory(), level -> create$codec(), original.dataFixType()));
	}

	@Unique
	private static Codec<MapItemSavedData> create$codec() {
		return RecordCodecBuilder.create(instance -> instance.group(
				Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(mapData -> mapData.dimension),
				Codec.INT.fieldOf("xCenter").forGetter(mapData -> mapData.centerX),
				Codec.INT.fieldOf("zCenter").forGetter(mapData -> mapData.centerZ),
				Codec.BYTE.optionalFieldOf("scale", (byte) 0).forGetter(mapData -> mapData.scale),
				Codec.BYTE_BUFFER.fieldOf("colors").forGetter(mapData -> ByteBuffer.wrap(mapData.colors)),
				Codec.BOOL.optionalFieldOf("trackingPosition", true).forGetter(mapData -> ((MapItemSavedDataMixin) (Object) mapData).trackingPosition),
				Codec.BOOL.optionalFieldOf("unlimitedTracking", false).forGetter(mapData -> ((MapItemSavedDataMixin) (Object) mapData).unlimitedTracking),
				Codec.BOOL.optionalFieldOf("locked", false).forGetter(mapData -> mapData.locked),
				MapBanner.CODEC.listOf().optionalFieldOf("banners", List.of()).forGetter(mapData -> List.copyOf(((MapItemSavedDataMixin) (Object) mapData).bannerMarkers.values())),
				MapFrame.CODEC.listOf().optionalFieldOf("frames", List.of()).forGetter(mapData -> List.copyOf(((MapItemSavedDataMixin) (Object) mapData).frameMarkers.values())),
				CREATE_STATION_MARKER_CODEC.listOf().optionalFieldOf(STATION_MARKERS_KEY, List.of()).forGetter(mapData -> List.copyOf(((MapItemSavedDataMixin) (Object) mapData).create$stationMarkers.values()))
			)
			.apply(instance, MapItemSavedDataMixin::create$load));
	}

	@Unique
	private static MapItemSavedData create$load(ResourceKey<Level> dimension, int centerX, int centerZ, byte scale,
												ByteBuffer colors, boolean trackingPosition, boolean unlimitedTracking,
												boolean locked, List<MapBanner> banners, List<MapFrame> frames,
												List<StationMarker> stationMarkers) {
		MapItemSavedData mapData = create$createMapItemSavedData(dimension, centerX, centerZ, scale, colors,
			trackingPosition, unlimitedTracking, locked, banners, frames);
		StationMapData stationMapData = (StationMapData) mapData;
		stationMarkers.forEach(stationMapData::addStationMarker);
		return mapData;
	}

	@Invoker("<init>")
	private static MapItemSavedData create$createMapItemSavedData(ResourceKey<Level> dimension, int centerX, int centerZ,
																  byte scale, ByteBuffer colors, boolean trackingPosition,
																  boolean unlimitedTracking, boolean locked,
																  List<MapBanner> banners, List<MapFrame> frames) {
		throw new AssertionError();
	}

	@Override
	public void addStationMarker(StationMarker marker) {
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

		MapDecoration decoration = StationMarker.createStationDecoration(localXByte, localZByte, Optional.of(marker.getName()));
		MapDecoration oldDecoration = decorations.put(marker.getId(), decoration);
		if (!decoration.equals(oldDecoration)) {
			if (oldDecoration != null && oldDecoration.type().value().trackCount()) {
				--trackedDecorationCount;
			}

			if (decoration.type().value().trackCount()) {
				++trackedDecorationCount;
			}

			setDecorationsDirty();
		}
	}

	@Shadow
	public void removeDecoration(String identifier) {
		throw new AssertionError();
	}

	@Shadow
	private void setDecorationsDirty() {
		throw new AssertionError();
	}

	@Shadow
	public boolean isTrackedCountOverLimit(int trackedCount) {
		throw new AssertionError();
	}

	@Override
	public boolean toggleStation(LevelAccessor level, BlockPos pos, StationBlockEntity stationBlockEntity) {
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
			addStationMarker(marker);
			return true;
		}

		return false;
	}

	@Inject(
			method = "checkBanners(Lnet/minecraft/world/level/BlockGetter;II)V",
			at = @At("RETURN")
	)
	public void create$onCheckBanners(BlockGetter blockGetter, int x, int z, CallbackInfo ci) {
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
			addStationMarker(marker);
		}
	}
}
