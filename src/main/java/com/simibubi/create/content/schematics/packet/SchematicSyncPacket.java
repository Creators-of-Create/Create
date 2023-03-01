package com.simibubi.create.content.schematics.packet;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.schematics.filtering.SchematicInstances;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraftforge.network.NetworkEvent.Context;

public class SchematicSyncPacket extends SimplePacketBase {

	public int slot;
	public boolean deployed;
	public BlockPos anchor;
	public Rotation rotation;
	public Mirror mirror;

	public SchematicSyncPacket(int slot, StructurePlaceSettings settings,
			BlockPos anchor, boolean deployed) {
		this.slot = slot;
		this.deployed = deployed;
		this.anchor = anchor;
		this.rotation = settings.getRotation();
		this.mirror = settings.getMirror();
	}

	public SchematicSyncPacket(FriendlyByteBuf buffer) {
		slot = buffer.readVarInt();
		deployed = buffer.readBoolean();
		anchor = buffer.readBlockPos();
		rotation = buffer.readEnum(Rotation.class);
		mirror = buffer.readEnum(Mirror.class);
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeVarInt(slot);
		buffer.writeBoolean(deployed);
		buffer.writeBlockPos(anchor);
		buffer.writeEnum(rotation);
		buffer.writeEnum(mirror);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player == null)
				return;
			ItemStack stack = ItemStack.EMPTY;
			if (slot == -1) {
				stack = player.getMainHandItem();
			} else {
				stack = player.getInventory().getItem(slot);
			}
			if (!AllItems.SCHEMATIC.isIn(stack)) {
				return;
			}
			CompoundTag tag = stack.getOrCreateTag();
			tag.putBoolean("Deployed", deployed);
			tag.put("Anchor", NbtUtils.writeBlockPos(anchor));
			tag.putString("Rotation", rotation.name());
			tag.putString("Mirror", mirror.name());
			SchematicInstances.clearHash(stack);
		});
		return true;
	}

}
