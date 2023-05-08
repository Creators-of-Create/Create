package com.simibubi.create.content.logistics.trains.track;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;

public class PlaceExtendedCurvePacket extends SimplePacketBase {

	boolean mainHand;
	boolean ctrlDown;
	
	public PlaceExtendedCurvePacket(boolean mainHand, boolean ctrlDown) {
		this.mainHand = mainHand;
		this.ctrlDown = ctrlDown;
	}

	public PlaceExtendedCurvePacket(FriendlyByteBuf buffer) {
		mainHand = buffer.readBoolean();
		ctrlDown = buffer.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBoolean(mainHand);
		buffer.writeBoolean(ctrlDown);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ServerPlayer sender = context.getSender();
			ItemStack stack = sender.getItemInHand(mainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
			if (!AllBlocks.TRACK.isIn(stack) || !stack.hasTag())
				return;
			CompoundTag tag = stack.getTag();
			tag.putBoolean("ExtendCurve", true);
			stack.setTag(tag);
		});
		return true;
	}

}
