package com.simibubi.create.networking;

import java.util.function.Supplier;

import com.simibubi.create.block.SchematicannonTileEntity;
import com.simibubi.create.block.SchematicannonTileEntity.State;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketConfigureSchematicannon {

	public static enum Option {
		DONT_REPLACE, REPLACE_SOLID, REPLACE_ANY, REPLACE_EMPTY, SKIP_MISSING, PLAY, PAUSE, STOP;
	}

	private Option option;
	private boolean set;
	private BlockPos pos;

	public static PacketConfigureSchematicannon setOption(BlockPos pos, Option option, boolean set) {
		PacketConfigureSchematicannon packet = new PacketConfigureSchematicannon(pos);
		packet.option = option;
		packet.set = set;
		return packet;
	}

	public PacketConfigureSchematicannon(BlockPos pos) {
		this.pos = pos;
	}

	public PacketConfigureSchematicannon(PacketBuffer buffer) {
		pos = buffer.readBlockPos();
		option = Option.values()[buffer.readInt()];
		set = buffer.readBoolean();
	}

	public void toBytes(PacketBuffer buffer) {
		buffer.writeBlockPos(pos);
		buffer.writeInt(option.ordinal());
		buffer.writeBoolean(set);
	}

	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(() -> {
			ServerPlayerEntity player = context.get().getSender();
			World world = player.world;

			if (world == null || world.getTileEntity(pos) == null)
				return;
			TileEntity tileEntity = world.getTileEntity(pos);
			if (tileEntity instanceof SchematicannonTileEntity) {

				SchematicannonTileEntity te = (SchematicannonTileEntity) tileEntity;
				switch (option) {
				case DONT_REPLACE:
				case REPLACE_ANY:
				case REPLACE_EMPTY:
				case REPLACE_SOLID:
					te.replaceMode = option.ordinal();
					break;
				case SKIP_MISSING:
					te.skipMissing = set;
					break;
					
				case PLAY:
					te.state = State.RUNNING;
					te.statusMsg = "Running";
					break;
				case PAUSE:
					te.state = State.PAUSED;
					te.statusMsg = "Paused";
					break;
				case STOP:
					te.state = State.STOPPED;
					te.statusMsg = "Stopped";
					break;
				default:
					break;
				}
				
				te.sendUpdate = true;

			}

			return;
		});
	}

}
