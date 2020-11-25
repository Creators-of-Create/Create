package com.simibubi.create.content.contraptions.components.structureMovement.sync;

import java.util.function.Supplier;

import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class ContraptionFluidPacket extends SimplePacketBase {

	private int entityId;
	private BlockPos localPos;
	private FluidStack containedFluid;

	public ContraptionFluidPacket(int entityId, BlockPos localPos, FluidStack containedFluid) {
		this.entityId = entityId;
		this.localPos = localPos;
		this.containedFluid = containedFluid;
	}
	
	public ContraptionFluidPacket(PacketBuffer buffer) {
		entityId = buffer.readInt();
		localPos = buffer.readBlockPos();
		containedFluid = FluidStack.readFromPacket(buffer);
	}
	
	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeInt(entityId);
		buffer.writeBlockPos(localPos);
		containedFluid.writeToPacket(buffer);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> {
				Entity entityByID = Minecraft.getInstance().world.getEntityByID(entityId);
				if (!(entityByID instanceof AbstractContraptionEntity))
					return;
				AbstractContraptionEntity contraptionEntity = (AbstractContraptionEntity) entityByID;
				contraptionEntity.getContraption().updateContainedFluid(localPos, containedFluid);
			});
		context.get()
			.setPacketHandled(true);
	}
}
