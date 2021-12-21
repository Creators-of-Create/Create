package com.simibubi.create.content.contraptions.components.structureMovement.train.capability;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.simibubi.create.lib.util.LazyOptional;
import com.simibubi.create.lib.util.MinecartAndRailUtil;
import com.tterrag.registrate.fabric.EnvExecutor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;

import java.util.function.Supplier;

public class MinecartControllerUpdatePacket extends SimplePacketBase {

	int entityID;
	CompoundTag nbt;

	public MinecartControllerUpdatePacket(MinecartController controller) {
		entityID = controller.cart()
			.getId();
		nbt = controller.create$serializeNBT();
	}

	public MinecartControllerUpdatePacket(FriendlyByteBuf buffer) {
		entityID = buffer.readInt();
		nbt = buffer.readNbt();
	}

	@Override
	public void encode(FriendlyByteBuf buffer) {
 		buffer.writeInt(entityID);
		buffer.writeNbt(nbt);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
				.enqueueWork(() -> EnvExecutor.runWhenOn(EnvType.CLIENT, () -> this::handleCL));
		context.get()
				.setPacketHandled(true);
	}

	@Environment(EnvType.CLIENT)
	private void handleCL() {
		ClientLevel world = Minecraft.getInstance().level;
		if (world == null)
			return;
		Entity entityByID = world.getEntity(entityID);
		if (entityByID == null)
			return;
		LazyOptional.ofObject(MinecartAndRailUtil.getController((AbstractMinecart) entityByID))
				.ifPresent(mc -> mc.create$deserializeNBT(nbt));
	}

}
