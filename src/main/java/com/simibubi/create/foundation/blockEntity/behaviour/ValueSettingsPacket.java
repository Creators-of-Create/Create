package com.simibubi.create.foundation.blockEntity.behaviour;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour.ValueSettings;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.createmod.catnip.codecs.stream.CatnipLargerStreamCodecs;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;

public class ValueSettingsPacket extends BlockEntityConfigurationPacket<SmartBlockEntity> {
	public static final StreamCodec<ByteBuf, ValueSettingsPacket> STREAM_CODEC = CatnipLargerStreamCodecs.composite(
			BlockPos.STREAM_CODEC, p -> p.pos,
			ByteBufCodecs.VAR_INT, p -> p.row,
			ByteBufCodecs.VAR_INT, p -> p.value,
			CatnipStreamCodecBuilders.nullable(CatnipStreamCodecs.HAND), p -> p.interactHand,
			CatnipStreamCodecBuilders.nullable(CatnipStreamCodecs.BLOCK_HIT_RESULT), p -> p.hitResult,
			Direction.STREAM_CODEC, p -> p.side,
			ByteBufCodecs.BOOL, p -> p.ctrlDown,
			ByteBufCodecs.VAR_INT, p -> p.behaviourIndex,
			ValueSettingsPacket::new
	);

	private final int row;
	private final int value;
	private final InteractionHand interactHand;
	private final Direction side;
	private final boolean ctrlDown;
	private final int behaviourIndex;
	private final BlockHitResult hitResult;

	public ValueSettingsPacket(BlockPos pos, int row, int value, @Nullable InteractionHand interactHand,
		@Nullable BlockHitResult hitResult, Direction side, boolean ctrlDown, int behaviourIndex) {
		super(pos);
		this.row = row;
		this.value = value;
		this.interactHand = interactHand;
		this.hitResult = hitResult;
		this.side = side;
		this.ctrlDown = ctrlDown;
		this.behaviourIndex = behaviourIndex;
	}

	@Override
	protected void applySettings(ServerPlayer player, SmartBlockEntity be) {
		for (BlockEntityBehaviour behaviour : be.getAllBehaviours()) {
			if (!(behaviour instanceof ValueSettingsBehaviour valueSettingsBehaviour))
				continue;
			if (!valueSettingsBehaviour.acceptsValueSettings())
				continue;
			if (behaviourIndex != valueSettingsBehaviour.netId())
				continue;
			if (interactHand != null) {
				valueSettingsBehaviour.onShortInteract(player, interactHand, side, hitResult);
				return;
			}
			valueSettingsBehaviour.setValueSettings(player, new ValueSettings(row, value), ctrlDown);
			return;
		}
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.VALUE_SETTINGS;
	}
}
