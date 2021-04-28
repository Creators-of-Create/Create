package com.simibubi.create.content.curiosities.projector;

import java.util.Vector;
import java.util.stream.Collectors;

import com.simibubi.create.foundation.networking.TileEntityConfigurationPacket;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class ConfigureProjectorPacket extends TileEntityConfigurationPacket<ChromaticProjectorTileEntity> {

	Vector<CompoundNBT> stages;
	float radius;

	float feather;
	float density;
	float fade;
	boolean blend;

	public boolean surface;
	public boolean field;
	public float strength;

	public boolean rMask;
	public boolean gMask;
	public boolean bMask;

	public ConfigureProjectorPacket(PacketBuffer buffer) {
		super(buffer);
	}

	public ConfigureProjectorPacket(ChromaticProjectorTileEntity tile) {
		super(tile.getPos());

		stages = tile.stages.stream()
				.map(FilterStep::write)
				.collect(Collectors.toCollection(Vector::new));
		radius = tile.radius;

		feather = tile.feather;
		density = tile.density;
		fade = tile.fade;
		blend = tile.blend;

		surface = tile.surface;
		field = tile.field;
		strength = tile.strength;

		rMask = tile.rMask;
		gMask = tile.gMask;
		bMask = tile.bMask;
	}

	@Override
	protected void writeSettings(PacketBuffer buffer) {
		buffer.writeFloat(radius);

		buffer.writeFloat(feather);
		buffer.writeFloat(density);
		buffer.writeFloat(fade);
		buffer.writeBoolean(blend);

		buffer.writeBoolean(surface);
		buffer.writeBoolean(field);
		buffer.writeFloat(strength);

		buffer.writeBoolean(rMask);
		buffer.writeBoolean(gMask);
		buffer.writeBoolean(bMask);

		buffer.writeInt(stages.size());
		for (CompoundNBT stage : stages) {
			buffer.writeCompoundTag(stage);
		}
	}

	@Override
	protected void readSettings(PacketBuffer buffer) {
		radius = buffer.readFloat();

		feather = buffer.readFloat();
		density = buffer.readFloat();
		fade = buffer.readFloat();
		blend = buffer.readBoolean();

		surface = buffer.readBoolean();
		field = buffer.readBoolean();
		strength = buffer.readFloat();

		rMask = buffer.readBoolean();
		gMask = buffer.readBoolean();
		bMask = buffer.readBoolean();

		int count = buffer.readInt();
		stages = new Vector<>(FilterStep.MAX_STEPS);

		for (int i = 0; i < count; i++) {
			stages.add(buffer.readCompoundTag());
		}
	}

	@Override
	protected void applySettings(ChromaticProjectorTileEntity tile) {
		tile.stages = stages.stream()
				.map(FilterStep::new)
				.collect(Collectors.toCollection(Vector::new));

		tile.radius = radius;

		tile.feather = feather;
		tile.density = density;
		tile.fade = fade;
		tile.blend = blend;

		tile.surface = surface;
		tile.field = field;
		tile.strength = strength;

		tile.rMask = rMask;
		tile.gMask = gMask;
		tile.bMask = bMask;

		tile.sendData();
	}
}
