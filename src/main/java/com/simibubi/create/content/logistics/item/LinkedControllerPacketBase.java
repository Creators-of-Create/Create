package com.simibubi.create.content.logistics.item;

import java.util.function.Supplier;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public abstract class LinkedControllerPacketBase extends SimplePacketBase {

	private BlockPos lecternPos;

	public LinkedControllerPacketBase(BlockPos lecternPos) {
		this.lecternPos = lecternPos;
	}

	public LinkedControllerPacketBase(PacketBuffer buffer) {
		if (buffer.readBoolean()) {
			lecternPos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
		}
	}

	protected boolean inLectern() {
		return lecternPos != null;
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeBoolean(inLectern());
		if (inLectern()) {
			buffer.writeInt(lecternPos.getX());
			buffer.writeInt(lecternPos.getY());
			buffer.writeInt(lecternPos.getZ());
		}
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(() -> {
			ServerPlayerEntity player = context.get().getSender();
			if (player == null)
				return;

			if (inLectern()) {
				TileEntity te = player.world.getTileEntity(lecternPos);
				if (!(te instanceof LecternControllerTileEntity))
					return;
				handleLectern(player, (LecternControllerTileEntity) te);
			} else {
				ItemStack controller = player.getHeldItemMainhand();
				if (!AllItems.LINKED_CONTROLLER.isIn(controller)) {
					controller = player.getHeldItemOffhand();
					if (!AllItems.LINKED_CONTROLLER.isIn(controller))
						return;
				}
				handleItem(player, controller);
			}
		});

		context.get().setPacketHandled(true);
	}

	protected abstract void handleItem(ServerPlayerEntity player, ItemStack heldItem);
	protected abstract void handleLectern(ServerPlayerEntity player, LecternControllerTileEntity lectern);

}
