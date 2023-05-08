package com.simibubi.create.content.logistics.block.depot;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public class EjectorPlacementPacket extends SimplePacketBase {

	private int h, v;
	private BlockPos pos;
	private Direction facing;

	public EjectorPlacementPacket(int h, int v, BlockPos pos, Direction facing) {
		this.h = h;
		this.v = v;
		this.pos = pos;
		this.facing = facing;
	}

	public EjectorPlacementPacket(FriendlyByteBuf buffer) {
		h = buffer.readInt();
		v = buffer.readInt();
		pos = buffer.readBlockPos();
		facing = Direction.from3DDataValue(buffer.readVarInt());
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(h);
		buffer.writeInt(v);
		buffer.writeBlockPos(pos);
		buffer.writeVarInt(facing.get3DDataValue());
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player == null)
				return;
			Level world = player.level;
			if (world == null || !world.isLoaded(pos))
				return;
			BlockEntity blockEntity = world.getBlockEntity(pos);
			BlockState state = world.getBlockState(pos);
			if (blockEntity instanceof EjectorBlockEntity)
				((EjectorBlockEntity) blockEntity).setTarget(h, v);
			if (AllBlocks.WEIGHTED_EJECTOR.has(state))
				world.setBlockAndUpdate(pos, state.setValue(EjectorBlock.HORIZONTAL_FACING, facing));
		});
		return true;
	}

	public static class ClientBoundRequest extends SimplePacketBase {

		BlockPos pos;

		public ClientBoundRequest(BlockPos pos) {
			this.pos = pos;
		}

		public ClientBoundRequest(FriendlyByteBuf buffer) {
			this.pos = buffer.readBlockPos();
		}

		@Override
		public void write(FriendlyByteBuf buffer) {
			buffer.writeBlockPos(pos);
		}

		@Override
		public boolean handle(Context context) {
			context.enqueueWork(
				() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> EjectorTargetHandler.flushSettings(pos)));
			return true;
		}

	}
	
}
