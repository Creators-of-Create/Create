package com.simibubi.create.foundation.utility;

import java.util.function.Consumer;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class LegacyBlockEntityTagBridge {
	private static final MapCodec<CompoundTag> ROOT_COMPOUND = MapCodec.assumeMapUnsafe(CompoundTag.CODEC);

	private LegacyBlockEntityTagBridge() {}

	public static CompoundTag read(ValueInput input) {
		return input.read(ROOT_COMPOUND)
			.map(CompoundTag::copy)
			.orElseGet(CompoundTag::new);
	}

	public static ValueInput input(CompoundTag tag, HolderLookup.Provider registries) {
		return TagValueInput.create(ProblemReporter.DISCARDING, registries, tag);
	}

	public static CompoundTag output(HolderLookup.Provider registries, Consumer<ValueOutput> writer) {
		TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
		writer.accept(output);
		return output.buildResult();
	}

	public static CompoundTag saveAsPassenger(Entity entity, HolderLookup.Provider registries) {
		TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
		return entity.saveAsPassenger(output) ? output.buildResult() : new CompoundTag();
	}

	public static Entity loadEntityRecursive(CompoundTag tag, Level level) {
		return EntityType.loadEntityRecursive(input(tag, registries(level)), level, EntitySpawnReason.LOAD, entity -> entity);
	}

	public static void store(ValueOutput output, CompoundTag tag) {
		output.store(tag);
	}

	public static HolderLookup.Provider registries(Level level) {
		return level != null ? level.registryAccess() : RegistryAccess.EMPTY;
	}
}
