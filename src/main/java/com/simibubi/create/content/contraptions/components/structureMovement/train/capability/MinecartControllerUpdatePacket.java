package com.simibubi.create.content.contraptions.components.structureMovement.train.capability;

import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MinecartControllerUpdatePacket extends SimplePacketBase {

	int entityID;
	CompoundNBT nbt;

	public MinecartControllerUpdatePacket(MinecartController controller) {
		entityID = controller.cart()
			.getEntityId();
		nbt = controller.serializeNBT();
	}

	public MinecartControllerUpdatePacket(PacketBuffer buffer) {
		entityID = buffer.readInt();
		nbt = buffer.readCompoundTag();
	}

	@Override
	public void write(PacketBuffer buffer) {
 		buffer.writeInt(entityID);
		buffer.writeCompoundTag(nbt);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> DistExecutor.runWhenOn(Dist.CLIENT, () -> this::handleCL));
		context.get()
			.setPacketHandled(true);
	}

	@OnlyIn(Dist.CLIENT)
	private void handleCL() {
		ClientWorld world = Minecraft.getInstance().world;
		if (world == null)
			return;
		Entity entityByID = world.getEntityByID(entityID);
		if (entityByID == null)
			return;
		entityByID.getCapability(CapabilityMinecartController.MINECART_CONTROLLER_CAPABILITY)
			.ifPresent(mc -> mc.deserializeNBT(nbt));
	}

}
