package com.simibubi.create.content.schematics.packet;

import java.util.function.Supplier;

import com.simibubi.create.content.schematics.SchematicProcessor;
import com.simibubi.create.content.schematics.item.SchematicItem;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
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
			Template t = SchematicItem.loadSchematic(stack);
			PlacementSettings settings = SchematicItem.getSettings(stack);
			if (player.canUseCommandBlock())
				settings.func_215220_b(SchematicProcessor.INSTANCE); // remove processor
			settings.setIgnoreEntities(false);
			t.addBlocksToWorld(player.getServerWorld(), NBTUtil.readBlockPos(stack.getTag().getCompound("Anchor")),
					settings);
		});
		context.get().setPacketHandled(true);
	}

}
