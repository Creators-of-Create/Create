package com.simibubi.create.content.schematics.packet;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllPackets;
import com.simibubi.create.content.schematics.SchematicInstances;
import net.createmod.catnip.net.base.ServerboundPacketPayload;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

public record SchematicSyncPacket(int slot, boolean deployed, BlockPos anchor, Rotation rotation, Mirror mirror) implements ServerboundPacketPayload {
	public static final StreamCodec<ByteBuf, SchematicSyncPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, SchematicSyncPacket::slot,
			ByteBufCodecs.BOOL, SchematicSyncPacket::deployed,
			BlockPos.STREAM_CODEC, SchematicSyncPacket::anchor,
			CatnipStreamCodecs.ROTATION, SchematicSyncPacket::rotation,
			CatnipStreamCodecs.MIRROR, SchematicSyncPacket::mirror,
	        SchematicSyncPacket::new
	);

	public SchematicSyncPacket(int slot, StructurePlaceSettings settings, BlockPos anchor, boolean deployed) {
		this(slot, deployed, anchor, settings.getRotation(), settings.getMirror());
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.SYNC_SCHEMATIC;
	}

	@Override
	public void handle(ServerPlayer player) {
		ItemStack stack;
		if (slot == -1) {
			stack = player.getMainHandItem();
		} else {
			stack = player.getInventory().getItem(slot);
		}
		if (!AllItems.SCHEMATIC.isIn(stack)) {
			return;
		}
		stack.set(AllDataComponents.SCHEMATIC_DEPLOYED, deployed);
		stack.set(AllDataComponents.SCHEMATIC_ANCHOR, anchor);
		stack.set(AllDataComponents.SCHEMATIC_ROTATION, rotation);
		stack.set(AllDataComponents.SCHEMATIC_MIRROR, mirror);
		SchematicInstances.clearHash(stack);
	}
}
