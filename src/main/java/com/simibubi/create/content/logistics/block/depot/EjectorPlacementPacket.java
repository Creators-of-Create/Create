package com.simibubi.create.content.logistics.block.depot;

import java.util.function.Supplier;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;

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

	public EjectorPlacementPacket(PacketBuffer buffer) {
		h = buffer.readInt();
		v = buffer.readInt();
		pos = buffer.readBlockPos();
		facing = Direction.from3DDataValue(buffer.readVarInt());
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeInt(h);
		buffer.writeInt(v);
		buffer.writeBlockPos(pos);
		buffer.writeVarInt(facing.get3DDataValue());
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> {
				ServerPlayerEntity player = context.get()
					.getSender();
				if (player == null)
					return;
				World world = player.level;
				if (world == null || !world.isLoaded(pos))
					return;
				TileEntity tileEntity = world.getBlockEntity(pos);
				BlockState state = world.getBlockState(pos);
				if (tileEntity instanceof EjectorTileEntity)
					((EjectorTileEntity) tileEntity).setTarget(h, v);
				if (AllBlocks.WEIGHTED_EJECTOR.has(state))
					world.setBlockAndUpdate(pos, state.setValue(EjectorBlock.HORIZONTAL_FACING, facing));
			});
		context.get()
			.setPacketHandled(true);

	}

}
