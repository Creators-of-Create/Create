package com.simibubi.create;

import com.simibubi.create.block.IJustForRendering;
import com.simibubi.create.block.SchematicTableBlock;
import com.simibubi.create.block.SchematicannonBlock;
import com.simibubi.create.block.symmetry.BlockSymmetryCrossPlane;
import com.simibubi.create.block.symmetry.BlockSymmetryPlane;
import com.simibubi.create.block.symmetry.BlockSymmetryTriplePlane;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

public enum AllBlocks {

	SCHEMATICANNON(new SchematicannonBlock()),
	SCHEMATIC_TABLE(new SchematicTableBlock()),
	
	SYMMETRY_PLANE(new BlockSymmetryPlane()),
	SYMMETRY_CROSSPLANE(new BlockSymmetryCrossPlane()),
	SYMMETRY_TRIPLEPLANE(new BlockSymmetryTriplePlane());

	public Block block;

	private AllBlocks(Block block) {
		this.block = block;
		this.block.setRegistryName(Create.ID, this.name().toLowerCase());
	}

	public static void registerBlocks(IForgeRegistry<Block> registry) {
		for (AllBlocks block : values()) {
			registry.register(block.block);
		}
	}

	public static void registerItemBlocks(IForgeRegistry<Item> registry) {
		for (AllBlocks block : values()) {
			if (block.get() instanceof IJustForRendering)
				continue;

			registry.register(new BlockItem(block.get(), AllItems.standardProperties())
					.setRegistryName(block.get().getRegistryName()));
		}
	}

	public Block get() {
		return block;
	}

	public boolean typeOf(BlockState state) {
		return state.getBlock() == block;
	}

}
