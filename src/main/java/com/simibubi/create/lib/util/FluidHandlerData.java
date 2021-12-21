package com.simibubi.create.lib.util;

import com.simibubi.create.Create;
import com.simibubi.create.lib.transfer.fluid.FluidStack;
import com.simibubi.create.lib.transfer.fluid.IFluidHandler;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class FluidHandlerData {
	public static final ResourceLocation PACKET_ID = Create.asResource("fluid_handler_data");
	public static FluidHandlerData CURRENT = null;

	public FluidTankData[] data;

	public FluidHandlerData(FluidTankData[] data) {
		this.data = data;
	}

	public int getTanks() {
		return data.length;
	}

	public static void sendToClient(ServerPlayer player, IFluidHandler handler) {
		FluidTankData[] tankData = new FluidTankData[handler.getTanks()];
		for (int i = 0; i < tankData.length; i++) {
			FluidStack stack = handler.getFluidInTank(i);
			String translationKey = stack.getTranslationKey();
			long amount = stack.getAmount();
			long capacity = handler.getTankCapacity(i);
			tankData[i] = new FluidTankData(translationKey, amount, capacity);
		}
		ServerPlayNetworking.send(player, PACKET_ID, createPacket(tankData));
	}

	public static FriendlyByteBuf createPacket(FluidTankData[] data) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeInt(data.length);
		for (FluidTankData tank : data) {
			buf.writeUtf(tank.translationKey());
			buf.writeLong(tank.amount());
			buf.writeLong(tank.capacity());
		}
		return buf;
	}

	public static FluidHandlerData readPacket(FriendlyByteBuf buf) {
		FluidTankData[] data = new FluidTankData[buf.readInt()];
		for (int i = 0; i < data.length; i++) {
			String translationKey = buf.readUtf();
			long amount = buf.readLong();
			long capacity = buf.readLong();
			data[i] = new FluidTankData(translationKey, amount, capacity);
		}
		return new FluidHandlerData(data);
	}

	@Environment(EnvType.CLIENT)
	public static void initClient() {
		ClientPlayNetworking.registerGlobalReceiver(PACKET_ID, (client, handler, buf, sender) -> {
			FluidHandlerData data = readPacket(buf);
			client.execute(() -> CURRENT = data);
		});
	}

	public static record FluidTankData(String translationKey, long amount, long capacity) {
	}
}
