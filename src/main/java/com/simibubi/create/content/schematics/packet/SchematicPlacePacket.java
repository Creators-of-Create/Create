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
		stack = buffer.readItem();
	}

	public void write(PacketBuffer buffer) {
		buffer.writeItem(stack);
	}

	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(() -> {
			ServerPlayerEntity player = context.get().getSender();
			if (player == null)
				return;

			World world = player.getLevel();
			SchematicPrinter printer = new SchematicPrinter();
			printer.loadSchematic(stack, world, !player.canUseGameMasterBlocks());

			while (printer.advanceCurrentPos()) {
				if (!printer.shouldPlaceCurrent(world))
					continue;

				printer.handleCurrentTarget((pos, state, tile) -> {
					CompoundNBT tileData = tile != null ? tile.save(new CompoundNBT()) : null;
					BlockHelper.placeSchematicBlock(world, state, pos, null, tileData);
				}, (pos, entity) -> {
					world.addFreshEntity(entity);
				});
			}
		});
		context.get().setPacketHandled(true);
	}

}
