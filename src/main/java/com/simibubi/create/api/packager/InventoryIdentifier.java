package com.simibubi.create.api.packager;

import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.registry.SimpleRegistry;
import com.simibubi.create.content.logistics.packager.AllInventoryIdentifiers;

import net.createmod.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Interface identifying an inventory spanning multiple block faces.
 * This is used to avoid multiple packagers on the same inventory requesting items from each other.
 */
@FunctionalInterface
public interface InventoryIdentifier {
	SimpleRegistry<Block, Finder> REGISTRY = SimpleRegistry.create();

	/**
	 * @return true if the given face is part of the inventory this identifier represents
	 */
	boolean contains(BlockFace face);

	/**
	 * Get the InventoryIdentifier for the given BlockFace, if present.
	 */
	@Nullable
	static InventoryIdentifier get(Level level, BlockFace face) {
		BlockState state = level.getBlockState(face.getPos());
		Finder finder = REGISTRY.get(state);
		Finder toQuery = finder != null ? finder : AllInventoryIdentifiers::fallback;
		return toQuery.find(level, state, face);
	}

	/**
	 * Interface for finding an InventoryIdentifier for a block.
	 */
	@FunctionalInterface
	interface Finder {
		/**
		 * Attempt to find the InventoryIdentifier that contains the given BlockFace.
		 * @return the found identifier, or null if one isn't present
		 */
		@Nullable
		InventoryIdentifier find(Level level, BlockState state, BlockFace face);
	}

	// common identifier implementations.

	record Single(BlockPos pos) implements InventoryIdentifier {
		@Override
		public boolean contains(BlockFace face) {
			return this.pos.equals(face.getPos());
		}
	}

	record Pair(BlockPos first, BlockPos second) implements InventoryIdentifier {
		@Override
		public boolean contains(BlockFace face) {
			BlockPos pos = face.getPos();
			return this.first.equals(pos) || this.second.equals(pos);
		}
	}

	record Bounds(BoundingBox bounds) implements InventoryIdentifier {
		@Override
		public boolean contains(BlockFace face) {
			return this.bounds.isInside(face.getPos());
		}
	}

	record MultiFace(BlockPos pos, Set<Direction> sides) implements InventoryIdentifier {
		@Override
		public boolean contains(BlockFace face) {
			return this.pos.equals(face.getPos()) && this.sides.contains(face.getFace());
		}
	}
}
