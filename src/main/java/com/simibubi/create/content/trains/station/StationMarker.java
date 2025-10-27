package com.simibubi.create.content.trains.station;

import java.util.Objects;
import java.util.Optional;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllMapDecorationTypes;
import com.simibubi.create.content.trains.track.TrackTargetingBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.saveddata.maps.MapDecoration;

public class StationMarker {
	private final BlockPos source;
	private final BlockPos target;
	private final Component name;
	private final String id;

	public StationMarker(BlockPos source, BlockPos target, Component name) {
		this.source = source;
		this.target = target;
		this.name = name;
		id = "create:station-" + target.getX() + "," + target.getY() + "," + target.getZ();
	}

	public static StationMarker load(CompoundTag tag, HolderLookup.Provider registries) {
		BlockPos source = NBTHelper.readBlockPos(tag, "source");
		BlockPos target = NBTHelper.readBlockPos(tag, "target");
		Component name = Component.Serializer.fromJson(tag.getString("name"), registries);
		if (name == null) name = CommonComponents.EMPTY;

		return new StationMarker(source, target, name);
	}

	public static StationMarker fromWorld(BlockGetter level, BlockPos pos) {
		Optional<StationBlockEntity> stationOption = AllBlockEntityTypes.TRACK_STATION.get(level, pos);

		if (stationOption.isEmpty() || stationOption.get().getStation() == null)
			return null;

		String name = stationOption.get()
			.getStation().name;
		return new StationMarker(pos, BlockEntityBehaviour.get(stationOption.get(), TrackTargetingBehaviour.TYPE)
			.getPositionForMapMarker(), Component.literal(name));
	}

	public CompoundTag save(HolderLookup.Provider registries) {
		CompoundTag tag = new CompoundTag();
		tag.put("source", NbtUtils.writeBlockPos(source));
		tag.put("target", NbtUtils.writeBlockPos(target));
		tag.putString("name", Component.Serializer.toJson(name, registries));

		return tag;
	}

	public BlockPos getSource() {
		return source;
	}

	public BlockPos getTarget() {
		return target;
	}

	public Component getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		StationMarker that = (StationMarker) o;

		if (!target.equals(that.target)) return false;
		return name.equals(that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(target, name);
	}

	public static MapDecoration createStationDecoration(byte x, byte y, Optional<Component> name) {
		return new MapDecoration(AllMapDecorationTypes.STATION_MAP_DECORATION, x, y, (byte) 0, name);
	}
}
