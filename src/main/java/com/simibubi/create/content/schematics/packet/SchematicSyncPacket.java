package com.simibubi.create.content.schematics.packet;

import java.util.function.Supplier;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.schematics.filtering.SchematicInstances;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class SchematicSyncPacket extends SimplePacketBase {

	public int slot;
	public boolean deployed;
	public BlockPos anchor;
	public Rotation rotation;
	public Mirror mirror;

	public SchematicSyncPacket(int slot, PlacementSettings settings,
			BlockPos anchor, boolean deployed) {
		this.slot = slot;
		this.deployed = deployed;
		this.anchor = anchor;
		this.rotation = settings.getRotation();
		this.mirror = settings.getMirror();
	}

	public SchematicSyncPacket(PacketBuffer buffer) {
		slot = buffer.readVarInt();
		deployed = buffer.readBoolean();
		anchor = buffer.readBlockPos();
		rotation = buffer.readEnum(Rotation.class);
		mirror = buffer.readEnum(Mirror.class);
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeVarInt(slot);
		buffer.writeBoolean(deployed);
		buffer.writeBlockPos(anchor);
		buffer.writeEnum(rotation);
		buffer.writeEnum(mirror);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(() -> {
			ServerPlayerEntity player = context.get().getSender();
			if (player == null)
				return;
			ItemStack stack = ItemStack.EMPTY;
			if (slot == -1) {
				stack = player.getMainHandItem();
			} else {
				stack = player.inventory.getItem(slot);
			}
			if (!AllItems.SCHEMATIC.isIn(stack)) {
				return;
			}
			CompoundNBT tag = stack.getOrCreateTag();
			tag.putBoolean("Deployed", deployed);
			tag.put("Anchor", NBTUtil.writeBlockPos(anchor));
			tag.putString("Rotation", rotation.name());
			tag.putString("Mirror", mirror.name());
			SchematicInstances.clearHash(stack);
		});
		context.get().setPacketHandled(true);
	}

}
