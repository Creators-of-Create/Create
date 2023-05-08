package com.simibubi.create.content.contraptions.components.structureMovement.sync;

import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent.Context;

public class ContraptionFluidPacket extends SimplePacketBase {

	private int entityId;
	private BlockPos localPos;
	private FluidStack containedFluid;

	public ContraptionFluidPacket(int entityId, BlockPos localPos, FluidStack containedFluid) {
		this.entityId = entityId;
		this.localPos = localPos;
		this.containedFluid = containedFluid;
	}

	public ContraptionFluidPacket(FriendlyByteBuf buffer) {
		entityId = buffer.readInt();
		localPos = buffer.readBlockPos();
		containedFluid = FluidStack.readFromPacket(buffer);
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(entityId);
		buffer.writeBlockPos(localPos);
		containedFluid.writeToPacket(buffer);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			Entity entityByID = Minecraft.getInstance().level.getEntity(entityId);
			if (!(entityByID instanceof AbstractContraptionEntity))
				return;
			AbstractContraptionEntity contraptionEntity = (AbstractContraptionEntity) entityByID;
			contraptionEntity.getContraption().handleContraptionFluidPacket(localPos, containedFluid);
		});
		return true;
	}
}
