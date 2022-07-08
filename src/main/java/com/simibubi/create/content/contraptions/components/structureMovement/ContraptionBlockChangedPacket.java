package com.simibubi.create.content.contraptions.components.structureMovement;

import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public class ContraptionBlockChangedPacket extends SimplePacketBase {

	int entityID;
	BlockPos localPos;
	BlockState newState;

	public ContraptionBlockChangedPacket(int id, BlockPos pos, BlockState state) {
		entityID = id;
		localPos = pos;
		newState = state;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(entityID);
		buffer.writeBlockPos(localPos);
		buffer.writeNbt(NbtUtils.writeBlockState(newState));
	}

	public ContraptionBlockChangedPacket(FriendlyByteBuf buffer) {
		entityID = buffer.readInt();
		localPos = buffer.readBlockPos();
		newState = NbtUtils.readBlockState(buffer.readNbt());
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
				() -> () -> AbstractContraptionEntity.handleBlockChangedPacket(this)));
		context.get()
			.setPacketHandled(true);
	}

}
