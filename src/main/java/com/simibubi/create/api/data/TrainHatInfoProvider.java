package com.simibubi.create.api.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.mojang.serialization.JsonOps;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.schedule.hat.TrainHatInfo;
import com.simibubi.create.content.trains.schedule.hat.TrainHatInfoReloadListener;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

public abstract class TrainHatInfoProvider implements DataProvider {
	protected final Map<ResourceLocation, TrainHatInfo> trainHatOffsets = new HashMap<>();
	private final PackOutput.PathProvider path;

	public TrainHatInfoProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		this.path = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, TrainHatInfoReloadListener.HAT_INFO_DIRECTORY);
	}

	protected abstract void createOffsets();

	protected void makeInfoFor(EntityType<?> type, Vec3 offset) {
		this.makeInfoFor(type, offset, "", 0, 1.0F);
	}

	protected void makeInfoFor(EntityType<?> type, Vec3 offset, String part) {
		this.makeInfoFor(type, offset, part, 0, 1.0F);
	}

	protected void makeInfoFor(EntityType<?> type, Vec3 offset, float scale) {
		this.makeInfoFor(type, offset, "", 0, scale);
	}

	protected void makeInfoFor(EntityType<?> type, Vec3 offset, String part, float scale) {
		this.makeInfoFor(type, offset, part, 0, scale);
	}

	protected void makeInfoFor(EntityType<?> type, Vec3 offset, String part, int cubeIndex, float scale) {
		this.trainHatOffsets.put(BuiltInRegistries.ENTITY_TYPE.getKey(type), new TrainHatInfo(part, cubeIndex, offset, scale));
	}

	@Override
	public CompletableFuture<?> run(CachedOutput output) {
		this.trainHatOffsets.clear();
		this.createOffsets();
		return CompletableFuture.allOf(
			this.trainHatOffsets.entrySet().stream().map(entry ->
				DataProvider.saveStable(output,
					TrainHatInfo.CODEC.encodeStart(JsonOps.INSTANCE, entry.getValue()).resultOrPartial(Create.LOGGER::error).orElseThrow(),
					this.path.json(entry.getKey()))
			).toArray(CompletableFuture[]::new));
	}

	@Override
	public String getName() {
		return "Create Train Hat Information";
	}
}
