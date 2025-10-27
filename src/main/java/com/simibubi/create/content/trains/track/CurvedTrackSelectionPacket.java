package com.simibubi.create.content.trains.track;

import org.apache.commons.lang3.mutable.MutableObject;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllPackets;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.track.TrackTargetingBlockItem.OverlapResult;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;

import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class CurvedTrackSelectionPacket extends BlockEntityConfigurationPacket<TrackBlockEntity> {
	public static final StreamCodec<ByteBuf, CurvedTrackSelectionPacket> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, packet -> packet.pos,
			BlockPos.STREAM_CODEC, packet -> packet.targetPos,
			ByteBufCodecs.BOOL, packet -> packet.front,
			ByteBufCodecs.VAR_INT, packet -> packet.segment,
			ByteBufCodecs.VAR_INT, packet -> packet.slot,
			CurvedTrackSelectionPacket::new
	);

	private final BlockPos targetPos;
	private final boolean front;
	private final int segment;
	private final int slot;

	public CurvedTrackSelectionPacket(BlockPos pos, BlockPos targetPos, boolean front, int segment, int slot) {
		super(pos);
		this.targetPos = targetPos;
		this.front = front;
		this.segment = segment;
		this.slot = slot;
	}

	@Override
	protected void applySettings(ServerPlayer player, TrackBlockEntity be) {
		if (player.getInventory().selected != slot)
			return;
		ItemStack stack = player.getInventory()
			.getItem(slot);
		if (!(stack.getItem() instanceof TrackTargetingBlockItem))
			return;
		if (player.isShiftKeyDown() && stack.has(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS)) {
			player.displayClientMessage(CreateLang.translateDirect("track_target.clear"), true);
			stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS);
			stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_DIRECTION);
			stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_BEZIER);
			AllSoundEvents.CONTROLLER_CLICK.play(player.level(), null, pos, 1, .5f);
			return;
		}

		EdgePointType<?> type = AllBlocks.TRACK_SIGNAL.isIn(stack) ? EdgePointType.SIGNAL : EdgePointType.STATION;
		MutableObject<OverlapResult> result = new MutableObject<>(null);
		BezierTrackPointLocation bezierTrackPointLocation = new BezierTrackPointLocation(targetPos, segment);
		TrackTargetingBlockItem.withGraphLocation(player.level(), pos, front,
				bezierTrackPointLocation, type, (overlap, location) -> result.setValue(overlap));

		if (result.getValue().feedback != null) {
			player.displayClientMessage(CreateLang.translateDirect(result.getValue().feedback)
				.withStyle(ChatFormatting.RED), true);
			AllSoundEvents.DENY.play(player.level(), null, pos, .5f, 1);
			return;
		}

		stack.set(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS, pos);
		stack.set(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_DIRECTION, front);
		stack.set(AllDataComponents.TRACK_TARGETING_ITEM_BEZIER, bezierTrackPointLocation);

		player.displayClientMessage(CreateLang.translateDirect("track_target.set"), true);
		AllSoundEvents.CONTROLLER_CLICK.play(player.level(), null, pos, 1, 1);
	}

	@Override
	protected int maxRange() {
		return AllConfigs.server().trains.maxTrackPlacementLength.get() + 16;
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.SELECT_CURVED_TRACK;
	}
}
