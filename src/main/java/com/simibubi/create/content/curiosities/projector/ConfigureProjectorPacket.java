package com.simibubi.create.content.curiosities.projector;

import java.util.Vector;
import java.util.stream.Collectors;

import com.simibubi.create.foundation.networking.TileEntityConfigurationPacket;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class ConfigureProjectorPacket extends TileEntityConfigurationPacket<ChromaticProjectorTileEntity> {

	Vector<CompoundNBT> stages;
	float radius;
	float density;
	float feather;
	float fade;

	public ConfigureProjectorPacket(PacketBuffer buffer) {
		super(buffer);
	}

	public ConfigureProjectorPacket(ChromaticProjectorTileEntity tile) {
		super(tile.getPos());

		stages = tile.stages.stream()
				.map(FilterStep::write)
				.collect(Collectors.toCollection(Vector::new));
		this.radius = tile.radius;
		this.density = tile.density;
		this.feather = tile.feather;
		this.fade = tile.fade;
	}

	@Override
	protected void writeSettings(PacketBuffer buffer) {
		buffer.writeFloat(radius);
		buffer.writeFloat(density);
		buffer.writeFloat(feather);
		buffer.writeFloat(fade);

		buffer.writeInt(stages.size());
		for (CompoundNBT stage : stages) {
			buffer.writeCompoundTag(stage);
		}
	}

	@Override
	protected void readSettings(PacketBuffer buffer) {
		radius = buffer.readFloat();
		density = buffer.readFloat();
		feather = buffer.readFloat();
		fade = buffer.readFloat();

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

		tile.radius = this.radius;
		tile.density = this.density;
		tile.feather = this.feather;
		tile.fade = this.fade;

		tile.sendData();
	}
}
