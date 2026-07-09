package com.simibubi.create.content.logistics.packagerLink;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.simibubi.create.Create;

import net.createmod.catnip.api.nbt.NBTHelper;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class LogisticsNetwork {

	public UUID id;
	public RequestPromiseQueue panelPromises;

	public Set<GlobalPos> totalLinks;
	public Set<GlobalPos> loadedLinks;

	public UUID owner;
	public boolean locked;

	public LogisticsNetwork(UUID networkId) {
		id = networkId;
		panelPromises = new RequestPromiseQueue(Create.LOGISTICS::markDirty);
		totalLinks = new HashSet<>();
		loadedLinks = new HashSet<>();
		owner = null;
		locked = false;
	}

	public CompoundTag write(HolderLookup.Provider registries) {
		CompoundTag tag = new CompoundTag();
		tag.put("Id", com.simibubi.create.foundation.utility.LegacyNbtUtilsBridge.createUUID(id));
		tag.put("Promises", panelPromises.write(registries));

		tag.put("Links", NBTHelper.writeCompoundList(totalLinks, p -> {
			CompoundTag nbt = new CompoundTag();
			nbt.put("Pos", com.simibubi.create.foundation.utility.LegacyNbtUtilsBridge.writeBlockPos(p.pos()));
			if (p.dimension() != Level.OVERWORLD)
				NBTHelper.writeIdentifier(nbt, "Dim", p.dimension().identifier());
			return nbt;
		}));

		if (owner != null)
			tag.put("Owner", com.simibubi.create.foundation.utility.LegacyNbtUtilsBridge.createUUID(owner));

		tag.putBoolean("Locked", locked);
		return tag;
	}

	public static LogisticsNetwork read(CompoundTag tag, HolderLookup.Provider registries) {
		LogisticsNetwork network = new LogisticsNetwork(com.simibubi.create.foundation.utility.LegacyNbtUtilsBridge.loadUUID(tag.get("Id")));
		network.panelPromises = RequestPromiseQueue.read(tag.getCompoundOrEmpty("Promises"), registries, Create.LOGISTICS::markDirty);

		NBTHelper.iterateCompoundList(tag.getListOrEmpty("Links"), nbt -> {
			network.totalLinks.add(GlobalPos.of(nbt.contains("Dim")
				? ResourceKey.create(Registries.DIMENSION, NBTHelper.readIdentifier(nbt, "Dim"))
				: Level.OVERWORLD, com.simibubi.create.foundation.utility.LegacyNbtUtilsBridge.readBlockPos(nbt, "Pos")));
		});

		network.owner = tag.contains("Owner") ? com.simibubi.create.foundation.utility.LegacyNbtUtilsBridge.loadUUID(tag.get("Owner")) : null;
		network.locked = tag.getBooleanOr("Locked", false);

		return network;
	}

}
