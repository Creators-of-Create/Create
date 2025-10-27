package com.simibubi.create.api.schematic.nbt;

import com.simibubi.create.api.registry.SimpleRegistry;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Registry for safe NBT writers, used for filtering unsafe BlockEntity data out of schematics.
 * <p>
 * This is used to exclude specific tags that would result in exploits, ex. signs that execute commands when clicked.
 * <p>
 * This is provided as an alternative to {@link PartialSafeNBT}.
 */
public class SafeNbtWriterRegistry {
	public static final SimpleRegistry<BlockEntityType<?>, SafeNbtWriter> REGISTRY = SimpleRegistry.create();

	@FunctionalInterface
	public interface SafeNbtWriter {
		/**
		 * Write filtered, safe NBT to the given tag. This is always called on the logical server.
		 * @param tag the NBT tag to write to
		 */
		void writeSafe(BlockEntity be, CompoundTag tag, HolderLookup.Provider registries);
	}

	private SafeNbtWriterRegistry() {
		throw new AssertionError("This class should not be instantiated");
	}
}
