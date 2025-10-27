package com.simibubi.create.content.logistics.factoryBoard;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;

public record FactoryPanelPosition(BlockPos pos, PanelSlot slot) {
	public static final Codec<FactoryPanelPosition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		BlockPos.CODEC.fieldOf("pos").forGetter(FactoryPanelPosition::pos),
		PanelSlot.CODEC.fieldOf("slot").forGetter(FactoryPanelPosition::slot)
	).apply(instance, FactoryPanelPosition::new));

	public static final StreamCodec<ByteBuf, FactoryPanelPosition> STREAM_CODEC = StreamCodec.composite(
	    BlockPos.STREAM_CODEC, FactoryPanelPosition::pos,
		PanelSlot.STREAM_CODEC, FactoryPanelPosition::slot,
	    FactoryPanelPosition::new
	);
}
