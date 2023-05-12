package com.simibubi.create.content.contraptions.relays.advanced.sequencer;

import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

public class ConfigureSequencedGearshiftPacket extends BlockEntityConfigurationPacket<SequencedGearshiftBlockEntity> {

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
	protected void applySettings(SequencedGearshiftBlockEntity be) {
		if (be.computerBehaviour.hasAttachedComputer())
			return;

		be.run(-1);
		be.instructions = Instruction.deserializeAll(instructions);
		be.sendData();
	}

}
