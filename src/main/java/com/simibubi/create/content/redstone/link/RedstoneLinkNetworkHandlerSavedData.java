package com.simibubi.create.content.redstone.link;

import java.util.HashMap;
import java.util.Map;

import net.createmod.catnip.data.Couple;
import net.createmod.catnip.nbt.NBTHelper;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

public class RedstoneLinkNetworkHandlerSavedData extends SavedData {

	private final Map<Couple<RedstoneLinkNetworkHandler.Frequency>, RedstoneLinkNetwork> connections = new HashMap<>();

	public static SavedData.Factory<RedstoneLinkNetworkHandlerSavedData> factory() {
		return new SavedData.Factory<>(RedstoneLinkNetworkHandlerSavedData::new, RedstoneLinkNetworkHandlerSavedData::load);
	}

	@Override
	public CompoundTag save(final CompoundTag compoundTag, final HolderLookup.Provider registries) {
		ListTag tag = NBTHelper.writeCompoundList(connections.entrySet(), entry -> {
			CompoundTag innerTag = new CompoundTag(3);
			innerTag.put("FrequencyFirst", entry.getKey().getFirst().getStack().saveOptional(registries));
			innerTag.put("FrequencyLast", entry.getKey().getSecond().getStack().saveOptional(registries));
			innerTag.put("Network", entry.getValue().save());
			return innerTag;
		});

		compoundTag.put("connections", tag);
		return compoundTag;
	}

	private static RedstoneLinkNetworkHandlerSavedData load(CompoundTag nbt, HolderLookup.Provider registries) {
		return new RedstoneLinkNetworkHandlerSavedData(nbt, registries);
	}

	private RedstoneLinkNetworkHandlerSavedData() {

	}

	private RedstoneLinkNetworkHandlerSavedData(CompoundTag nbt, HolderLookup.Provider registries) {
		NBTHelper.iterateCompoundList(nbt.getList("connections", Tag.TAG_COMPOUND), compoundTag -> {
			RedstoneLinkNetworkHandler.Frequency frequencyFirst = RedstoneLinkNetworkHandler.Frequency.of(ItemStack.parseOptional(registries, nbt.getCompound("FrequencyFirst")));
			RedstoneLinkNetworkHandler.Frequency frequencyLast = RedstoneLinkNetworkHandler.Frequency.of(ItemStack.parseOptional(registries, nbt.getCompound("FrequencyLast")));

			connections.put(Couple.create(frequencyFirst, frequencyLast), new RedstoneLinkNetwork(compoundTag.getCompound("Network")));
		});
	}

	public RedstoneLinkNetwork getNetwork(final Couple<RedstoneLinkNetworkHandler.Frequency> networkKey) {
		return connections.computeIfAbsent(networkKey, frequencies -> new RedstoneLinkNetwork());
	}
}
