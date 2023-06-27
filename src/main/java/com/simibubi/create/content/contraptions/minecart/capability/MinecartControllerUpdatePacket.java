package com.simibubi.create.content.contraptions.minecart.capability;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public class MinecartControllerUpdatePacket extends SimplePacketBase {

	int entityID;
	CompoundTag nbt;

	public MinecartControllerUpdatePacket(MinecartController controller) {
		entityID = controller.cart()
			.getId();
		nbt = controller.serializeNBT();
	}

	public MinecartControllerUpdatePacket(FriendlyByteBuf buffer) {
		entityID = buffer.readInt();
		nbt = buffer.readNbt();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
 		buffer.writeInt(entityID);
		buffer.writeNbt(nbt);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::handleCL));
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	private void handleCL() {
		ClientLevel world = Minecraft.getInstance().level;
		if (world == null)
			return;
		Entity entityByID = world.getEntity(entityID);
		if (entityByID == null)
			return;
		entityByID.getCapability(CapabilityMinecartController.MINECART_CONTROLLER_CAPABILITY)
			.ifPresent(mc -> mc.deserializeNBT(nbt));
	}

}
