package com.simibubi.create.content.contraptions.relays.advanced.sequencer;

import com.simibubi.create.foundation.networking.TileEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

public class ConfigureSequencedGearshiftPacket extends TileEntityConfigurationPacket<SequencedGearshiftTileEntity> {

	private ListTag instructions;

	public ConfigureSequencedGearshiftPacket(BlockPos pos, ListTag instructions) {
		super(pos);
		this.instructions = instructions;
	}

	public ConfigureSequencedGearshiftPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		instructions = buffer.readNbt().getList("data", Tag.TAG_COMPOUND);
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		CompoundTag tag = new CompoundTag();
		tag.put("data", instructions);
		buffer.writeNbt(tag);
	}

	@Override
	protected void applySettings(SequencedGearshiftTileEntity te) {
		if (te.computerBehaviour.hasAttachedComputer())
			return;

		te.run(-1);
		te.instructions = Instruction.deserializeAll(instructions);
		te.sendData();
	}

}
