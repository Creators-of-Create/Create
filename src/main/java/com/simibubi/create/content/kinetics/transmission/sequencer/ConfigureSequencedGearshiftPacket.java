package com.simibubi.create.content.kinetics.transmission.sequencer;

import java.util.Vector;

import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.codec.CreateStreamCodecs;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public class ConfigureSequencedGearshiftPacket extends BlockEntityConfigurationPacket<SequencedGearshiftBlockEntity> {
	public static final StreamCodec<ByteBuf, ConfigureSequencedGearshiftPacket> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, packet -> packet.pos,
			Instruction.STREAM_CODEC.apply(CreateStreamCodecs.vector()), packet -> packet.instructions,
			ConfigureSequencedGearshiftPacket::new
	);

	private final Vector<Instruction> instructions;

	public ConfigureSequencedGearshiftPacket(BlockPos pos, Vector<Instruction> instructions) {
		super(pos);
		this.instructions = instructions;
	}

	@Override
	protected void applySettings(ServerPlayer player, SequencedGearshiftBlockEntity be) {
		if (be.computerBehaviour.hasAttachedComputer())
			return;

		be.run(-1);
		be.instructions = this.instructions;
		be.sendData();
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CONFIGURE_SEQUENCER;
	}
}
