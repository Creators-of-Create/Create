package com.simibubi.create.networking;

import java.util.function.Supplier;

import com.simibubi.create.item.BlueprintItem;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class SchematicPlacePacket {

	public ItemStack stack;

	public SchematicPlacePacket(ItemStack stack) {
		this.stack = stack;
	}

	public SchematicPlacePacket(PacketBuffer buffer) {
		stack = buffer.readItemStack();
	}

	public void toBytes(PacketBuffer buffer) {
		buffer.writeItemStack(stack);
	}

	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(() -> {
			ServerPlayerEntity player = context.get().getSender();
			Template t = BlueprintItem.getSchematic(stack);
			t.addBlocksToWorld(player.getServerWorld(), NBTUtil.readBlockPos(stack.getTag().getCompound("Anchor")),
					BlueprintItem.getSettings(stack));
		});
	}

}
