package com.simibubi.create.content.schematics.packet;

import java.util.function.Supplier;

import com.simibubi.create.Create;
import com.simibubi.create.content.schematics.SchematicPrinter;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.simibubi.create.foundation.utility.BlockHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent.Context;

public class SchematicPlacePacket extends SimplePacketBase {

	public ItemStack stack;

	public SchematicPlacePacket(ItemStack stack) {
		this.stack = stack;
	}

	public SchematicPlacePacket(FriendlyByteBuf buffer) {
		stack = buffer.readItem();
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeItem(stack);
	}

	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(() -> {
			ServerPlayer player = context.get().getSender();
			if (player == null)
				return;
			if (!player.isCreative())
			{
				Create.LOGGER.warn(player.getName().getString() + " attempted to instant place while not in creative");
				return;
			}

			Level world = player.getLevel();
			SchematicPrinter printer = new SchematicPrinter();
			printer.loadSchematic(stack, world, !player.canUseGameMasterBlocks());
			if (!printer.isLoaded() || printer.isErrored())
				return;
			
			boolean includeAir = AllConfigs.SERVER.schematics.creativePrintIncludesAir.get();

			while (printer.advanceCurrentPos()) {
				if (!printer.shouldPlaceCurrent(world))
					continue;

				printer.handleCurrentTarget((pos, state, tile) -> {
					boolean placingAir = state.isAir();
					if (placingAir && !includeAir)
						return;
					
					CompoundTag tileData = tile != null ? tile.saveWithFullMetadata() : null;
					BlockHelper.placeSchematicBlock(world, state, pos, null, tileData);
				}, (pos, entity) -> {
					world.addFreshEntity(entity);
				});
			}
		});
		context.get().setPacketHandled(true);
	}

}
