package com.simibubi.create.content.logistics.trains.management.edgePoint;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.track.TrackTileEntity;
import com.simibubi.create.foundation.networking.TileEntityConfigurationPacket;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class CurvedTrackSelectionPacket extends TileEntityConfigurationPacket<TrackTileEntity> {

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
	protected void applySettings(ServerPlayer player, TrackTileEntity te) {
		if (!te.getBlockPos()
			.closerThan(player.blockPosition(), 48)) {
			Create.LOGGER.warn(player.getScoreboardName() + " too far away from targeted track");
			return;
		}

		if (player.getInventory().selected != slot)
			return;
		ItemStack stack = player.getInventory()
			.getItem(slot);
		if (!(stack.getItem() instanceof TrackTargetingBlockItem))
			return;

		if (player.isSteppingCarefully() && stack.hasTag()) {
			player.displayClientMessage(Lang.translate("track_target.clear"), true);
			stack.setTag(null);
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

		player.displayClientMessage(Lang.translate("track_target.set"), true);
		stack.setTag(stackTag);
	}

	@Override
	protected void applySettings(TrackTileEntity te) {}

}
