package com.simibubi.create.content.logistics.trains.management.edgePoint.station;

import java.util.Objects;
import java.util.Optional;

import com.simibubi.create.AllTileEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.saveddata.maps.MapDecoration;

public class StationMarker {

	private final BlockPos pos;
	private final Component name;

	public StationMarker(BlockPos pos, Component name) {
		this.pos = pos;
		this.name = name;
	}

	public static StationMarker load(CompoundTag tag) {
		BlockPos pos = NbtUtils.readBlockPos(tag.getCompound("pos"));
		Component name = Component.Serializer.fromJson(tag.getString("name"));
		if (name == null) name = TextComponent.EMPTY;

		return new StationMarker(pos, name);
	}

	public static StationMarker fromWorld(BlockGetter level, BlockPos pos) {
		Optional<StationTileEntity> stationOption = AllTileEntities.TRACK_STATION.get(level, pos);

		if (stationOption.isEmpty() || stationOption.get().getStation() == null)
			return null;

		String name = stationOption.get().getStation().name;
		return new StationMarker(pos, new TextComponent(name));
	}

	public CompoundTag save() {
		CompoundTag tag = new CompoundTag();
		tag.put("pos", NbtUtils.writeBlockPos(this.pos));
		tag.putString("name", Component.Serializer.toJson(this.name));

		return tag;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		StationMarker that = (StationMarker) o;

		if (!pos.equals(that.pos)) return false;
		return name.equals(that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.pos, this.name);
	}

	public BlockPos getPos() {
		return this.pos;
	}

	public Component getName() {
		return name;
	}

	public MapDecoration.Type getType() {
		//todo replace with own type
		return MapDecoration.Type.MANSION;
	}

	public String getId() {
		return "create:station-" + this.pos.getX() + "," + this.pos.getY() + "," + this.pos.getZ();
	}
}
