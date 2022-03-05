package com.simibubi.create.content.contraptions.components.structureMovement.sync;

import java.util.function.Supplier;

import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidStack;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

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
		containedFluid = FluidStack.fromBuffer(buffer);
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(entityId);
		buffer.writeBlockPos(localPos);
		containedFluid.toBuffer(buffer);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> {
				Entity entityByID = Minecraft.getInstance().level.getEntity(entityId);
				if (!(entityByID instanceof AbstractContraptionEntity))
					return;
				AbstractContraptionEntity contraptionEntity = (AbstractContraptionEntity) entityByID;
				contraptionEntity.getContraption().updateContainedFluid(localPos, containedFluid);
			});
		context.get()
			.setPacketHandled(true);
	}
}
