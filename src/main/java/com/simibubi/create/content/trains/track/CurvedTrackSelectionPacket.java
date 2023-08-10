package com.simibubi.create.content.trains.track;

import org.apache.commons.lang3.mutable.MutableObject;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.track.TrackTargetingBlockItem.OverlapResult;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class CurvedTrackSelectionPacket extends BlockEntityConfigurationPacket<TrackBlockEntity> {

	private BlockPos targetPos;
	private boolean front;
	private int segment;
	private int slot;

	public CurvedTrackSelectionPacket(BlockPos pos, BlockPos targetPos, int segment, boolean front, int slot) {
		super(pos);
		this.targetPos = targetPos;
		this.segment = segment;
		this.front = front;
		this.slot = slot;
	}

	public CurvedTrackSelectionPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(targetPos);
		buffer.writeVarInt(segment);
		buffer.writeBoolean(front);
		buffer.writeVarInt(slot);
	}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		targetPos = buffer.readBlockPos();
		segment = buffer.readVarInt();
		front = buffer.readBoolean();
		slot = buffer.readVarInt();
	}

	@Override
	protected void applySettings(ServerPlayer player, TrackBlockEntity be) {
		if (player.getInventory().selected != slot)
			return;
		ItemStack stack = player.getInventory()
			.getItem(slot);
		if (!(stack.getItem() instanceof TrackTargetingBlockItem))
			return;
		if (player.isSteppingCarefully() && stack.hasTag()) {
			player.displayClientMessage(CreateLang.translateDirect("track_target.clear"), true);
			stack.setTag(null);
			AllSoundEvents.CONTROLLER_CLICK.play(player.level, null, pos, 1, .5f);
			return;
		}

		EdgePointType<?> type = AllBlocks.TRACK_SIGNAL.isIn(stack) ? EdgePointType.SIGNAL : EdgePointType.STATION;
		MutableObject<OverlapResult> result = new MutableObject<>(null);
		TrackTargetingBlockItem.withGraphLocation(player.level, pos, front,
			new BezierTrackPointLocation(targetPos, segment), type, (overlap, location) -> result.setValue(overlap));

		if (result.getValue().feedback != null) {
			player.displayClientMessage(CreateLang.translateDirect(result.getValue().feedback)
				.withStyle(ChatFormatting.RED), true);
			AllSoundEvents.DENY.play(player.level, null, pos, .5f, 1);
			return;
		}

		CompoundTag stackTag = stack.getOrCreateTag();
		stackTag.put("SelectedPos", NbtUtils.writeBlockPos(pos));
		stackTag.putBoolean("SelectedDirection", front);

		CompoundTag bezierNbt = new CompoundTag();
		bezierNbt.putInt("Segment", segment);
		bezierNbt.put("Key", NbtUtils.writeBlockPos(targetPos));
		bezierNbt.putBoolean("FromStack", true);
		stackTag.put("Bezier", bezierNbt);

		player.displayClientMessage(CreateLang.translateDirect("track_target.set"), true);
		stack.setTag(stackTag);
		AllSoundEvents.CONTROLLER_CLICK.play(player.level, null, pos, 1, 1);
	}

	@Override
	protected int maxRange() {
		return 64;
	}

	@Override
	protected void applySettings(TrackBlockEntity be) {}

}
