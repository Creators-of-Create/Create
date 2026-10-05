package com.simibubi.create.foundation;

import java.util.List;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.equipment.clipboard.ClipboardContent;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;

import net.createmod.catnip.codecs.CatnipCodecUtils;
import net.createmod.catnip.nbt.NBTProcessors;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class CreateNBTProcessors {
	public static void register() {
		NBTProcessors.addProcessor(BlockEntityType.LECTERN, data -> {
			if (!data.contains("Book", Tag.TAG_COMPOUND))
				return data;
			CompoundTag book = data.getCompound("Book");

			// Writable books can't have click events, so they're safe to keep
			ResourceLocation writableBookResource = BuiltInRegistries.ITEM.getKey(Items.WRITABLE_BOOK);
			if (writableBookResource != BuiltInRegistries.ITEM.getDefaultKey() && book.getString("id").equals(writableBookResource.toString()))
				return null;

			WrittenBookContent bookContent = CatnipCodecUtils.decodeOrNull(WrittenBookContent.CODEC, book);
			if (bookContent == null)
				return null;

			for (Filterable<Component> page : bookContent.pages()) {
				if (NBTProcessors.textComponentHasClickEvent(page.get(false)))
					return null;
			}

			return data;
		});

		NBTProcessors.addProcessor(AllBlockEntityTypes.CLIPBOARD.get(), CreateNBTProcessors::clipboardProcessor);

		NBTProcessors.addProcessor(AllBlockEntityTypes.CREATIVE_CRATE.get(), NBTProcessors.itemProcessor("Filter"));
	}

	public static CompoundTag clipboardProcessor(CompoundTag data) {
		DataComponentMap components = CatnipCodecUtils.decodeOrNull(DataComponentMap.CODEC, data.getCompound("components"));
		boolean needsReEncode = false;

		if (components == null) {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			if (server == null)
				return null;

			RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, server.registryAccess());
			components = CatnipCodecUtils.decodeOrNull(DataComponentMap.CODEC, ops, data.getCompound("components"));

			if (components == null)
				return null;

			needsReEncode = true;
		}


		ClipboardContent content = components.get(AllDataComponents.CLIPBOARD_CONTENT);
		if (content == null)
			return null;

		for (List<ClipboardEntry> entries : content.pages()) {
			for (ClipboardEntry entry : entries) {
				if (NBTProcessors.textComponentHasClickEvent(entry.text))
					return null;
			}
		}

		if (needsReEncode) {
			components = DataComponentMap.builder().set(AllDataComponents.CLIPBOARD_CONTENT, content).build();
			var result = DataComponentMap.CODEC.encodeStart(NbtOps.INSTANCE, components);
			result.result().ifPresent(tag -> data.put("components", tag));

			if (result.result().isEmpty())
				return null;

			data.put("components", result.result().get());

		}

		return data;
	}
}
