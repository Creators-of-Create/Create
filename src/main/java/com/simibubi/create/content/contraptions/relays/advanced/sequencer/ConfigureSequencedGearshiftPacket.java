package com.simibubi.create.content.contraptions.relays.advanced.sequencer;

import com.simibubi.create.foundation.networking.TileEntityConfigurationPacket;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;

public class ConfigureSequencedGearshiftPacket extends TileEntityConfigurationPacket<SequencedGearshiftTileEntity> {

	private ListNBT instructions;

	public ConfigureSequencedGearshiftPacket(BlockPos pos, ListNBT instructions) {
		super(pos);
		this.instructions = instructions;
	}

	public ConfigureSequencedGearshiftPacket(PacketBuffer buffer) {
		super(buffer);
	}

	@Override
	protected void readSettings(PacketBuffer buffer) {
		instructions = buffer.readNbt().getList("data", NBT.TAG_COMPOUND);
	}

	@Override
	protected void writeSettings(PacketBuffer buffer) {
		CompoundNBT tag = new CompoundNBT();
		tag.put("data", instructions);
		buffer.writeNbt(tag);
	}

	@Override
	protected void applySettings(SequencedGearshiftTileEntity te) {
		te.run(-1);
		te.instructions = Instruction.deserializeAll(instructions);
		te.sendData();
	}

}
