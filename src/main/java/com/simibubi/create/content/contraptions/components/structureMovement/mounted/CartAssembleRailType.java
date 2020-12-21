package com.simibubi.create.content.contraptions.components.structureMovement.mounted;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.tracks.ControllerRailBlock;
import com.simibubi.create.foundation.utility.Lang;

import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.IStringSerializable;

import java.util.function.Predicate;
import java.util.function.Supplier;

public enum CartAssembleRailType implements IStringSerializable {
	
	REGULAR(Blocks.RAIL),
	POWERED_RAIL(Blocks.POWERED_RAIL),
	DETECTOR_RAIL(Blocks.DETECTOR_RAIL),
	ACTIVATOR_RAIL(Blocks.ACTIVATOR_RAIL),
	CONTROLLER_RAIL(AllBlocks.CONTROLLER_RAIL, blockState -> AllBlocks.CONTROLLER_RAIL.has(blockState)
		&& blockState.has(ControllerRailBlock.BACKWARDS) && !blockState.get(ControllerRailBlock.BACKWARDS)),
	CONTROLLER_RAIL_BACKWARDS(AllBlocks.CONTROLLER_RAIL, blockState -> AllBlocks.CONTROLLER_RAIL.has(blockState)
		&& blockState.has(ControllerRailBlock.BACKWARDS) && blockState.get(ControllerRailBlock.BACKWARDS))
	
	;

	private final Supplier<Block> railBlockSupplier;
	private final Supplier<Item> railItemSupplier;
	public final Predicate<BlockState> matches;

	CartAssembleRailType(Block block) {
		this.railBlockSupplier = () -> block;
		this.railItemSupplier = block::asItem;
		this.matches = blockState -> blockState.getBlock() == getBlock();
	}

	CartAssembleRailType(BlockEntry<?> block, Predicate<BlockState> matches) {
		this.railBlockSupplier = block::get;
		this.railItemSupplier = () -> block.get().asItem();
		this.matches = matches;
	}

	public Block getBlock() {
		return railBlockSupplier.get();
	}

	public Item getItem() {
		return railItemSupplier.get();
	}
	
	@Override
	public String getName() {
		return Lang.asId(name());
	}

}
