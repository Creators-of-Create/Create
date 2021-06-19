package com.simibubi.create.content.schematics.packet;

import java.util.function.Supplier;

import com.simibubi.create.content.schematics.SchematicPrinter;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.simibubi.create.foundation.utility.BlockHelper;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class SchematicPlacePacket extends SimplePacketBase {

	public ItemStack stack;

	public SchematicPlacePacket(ItemStack stack) {
		this.stack = stack;
	}

	public SchematicPlacePacket(PacketBuffer buffer) {
		stack = buffer.readItemStack();
	}

	public void write(PacketBuffer buffer) {
		buffer.writeItemStack(stack);
	}

	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(() -> {
			ServerPlayerEntity player = context.get().getSender();
			if (player == null)
				return;

			World world = player.getServerWorld();
			SchematicPrinter printer = new SchematicPrinter();
			printer.loadSchematic(stack, world, !player.canUseCommandBlock());

			while (printer.advanceCurrentPos()) {
				if (!printer.shouldPlaceCurrent(world))
					continue;

				printer.handleCurrentTarget((pos, state, tile) -> {
					CompoundNBT tileData = tile != null ? tile.write(new CompoundNBT()) : null;
					BlockHelper.placeSchematicBlock(world, state, pos, null, tileData);
				}, (pos, entity) -> {
					world.addEntity(entity);
				});
			}
		});
		context.get().setPacketHandled(true);
	}

}
