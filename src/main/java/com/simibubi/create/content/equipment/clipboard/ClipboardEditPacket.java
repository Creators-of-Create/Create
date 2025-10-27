package com.simibubi.create.content.equipment.clipboard;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllPackets;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.nbt.NBTProcessors;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record ClipboardEditPacket(int hotbarSlot, @Nullable ClipboardContent clipboardContent,
								  @Nullable BlockPos targetedBlock) implements ServerboundPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClipboardEditPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, ClipboardEditPacket::hotbarSlot,
		CatnipStreamCodecBuilders.nullable(ClipboardContent.STREAM_CODEC), ClipboardEditPacket::clipboardContent,
		CatnipStreamCodecBuilders.nullable(BlockPos.STREAM_CODEC), ClipboardEditPacket::targetedBlock,
		ClipboardEditPacket::new
	);

	@Override
	public void handle(ServerPlayer sender) {
		ClipboardContent processedContent = clipboardProcessor(clipboardContent);

		if (targetedBlock != null) {
			Level world = sender.level();
			if (!world.isLoaded(targetedBlock))
				return;
			if (!targetedBlock.closerThan(sender.blockPosition(), 20))
				return;
			if (world.getBlockEntity(targetedBlock) instanceof ClipboardBlockEntity cbe) {
				PatchedDataComponentMap map = new PatchedDataComponentMap(cbe.components());
				if (processedContent == null) {
					map.remove(AllDataComponents.CLIPBOARD_CONTENT);
				} else {
					map.set(AllDataComponents.CLIPBOARD_CONTENT, processedContent);
				}
				cbe.setComponents(map);
				cbe.onEditedBy(sender);
			}
			return;
		}

		ItemStack itemStack = sender.getInventory()
				.getItem(hotbarSlot);
		if (!AllBlocks.CLIPBOARD.isIn(itemStack))
			return;
		if (processedContent == null) {
			itemStack.remove(AllDataComponents.CLIPBOARD_CONTENT);
		} else {
			itemStack.set(AllDataComponents.CLIPBOARD_CONTENT, processedContent);
		}
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CLIPBOARD_EDIT;
	}

	public static ClipboardContent clipboardProcessor(@Nullable ClipboardContent content) {
		if (content == null)
			return null;

		for (List<ClipboardEntry> page : content.pages()) {
			for (ClipboardEntry entry : page) {
				if (NBTProcessors.textComponentHasClickEvent(entry.text))
					return null;
			}
		}

		return content;
	}
}
