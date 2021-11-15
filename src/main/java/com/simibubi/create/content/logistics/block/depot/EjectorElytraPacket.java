package com.simibubi.create.content.logistics.block.depot;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import me.pepperbell.simplenetworking.C2SPacket;
import me.pepperbell.simplenetworking.SimpleChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Supplier;


public class EjectorElytraPacket extends SimplePacketBase {

	private BlockPos pos;

	public EjectorElytraPacket(BlockPos pos) {
		this.pos = pos;
	}

	public EjectorElytraPacket(FriendlyByteBuf buffer) {
		pos = buffer.readBlockPos();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
				.enqueueWork(() -> {
					ServerPlayer player = context.get()
							.getSender();
					if (player == null)
						return;
					Level world = player.level;
					if (world == null || !world.isLoaded(pos))
						return;
					BlockEntity tileEntity = world.getBlockEntity(pos);
					if (tileEntity instanceof EjectorTileEntity)
						((EjectorTileEntity) tileEntity).deployElytra(player);
				});
		context.get()
				.setPacketHandled(true);

	}

}
