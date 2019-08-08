package com.simibubi.create;

import com.simibubi.create.foundation.block.IRenderUtilityBlock;
import com.simibubi.create.foundation.block.RenderUtilityBlock;
import com.simibubi.create.modules.schematics.block.CreativeCrateBlock;
import com.simibubi.create.modules.schematics.block.SchematicTableBlock;
import com.simibubi.create.modules.schematics.block.SchematicannonBlock;
import com.simibubi.create.modules.shopping.ShopShelfBlock;
import com.simibubi.create.modules.symmetry.block.CrossPlaneSymmetryBlock;
import com.simibubi.create.modules.symmetry.block.PlaneSymmetryBlock;
import com.simibubi.create.modules.symmetry.block.TriplePlaneSymmetryBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

public enum AllBlocks {

	SCHEMATICANNON(new SchematicannonBlock()),
	SCHEMATICANNON_CONNECTOR(new RenderUtilityBlock()),
	SCHEMATICANNON_PIPE(new RenderUtilityBlock()),
	CREATIVE_CRATE(new CreativeCrateBlock()),
	
	SCHEMATIC_TABLE(new SchematicTableBlock()),
	
	SYMMETRY_PLANE(new PlaneSymmetryBlock()),
	SYMMETRY_CROSSPLANE(new CrossPlaneSymmetryBlock()),
	SYMMETRY_TRIPLEPLANE(new TriplePlaneSymmetryBlock()),
	
	SHOP_SHELF(new ShopShelfBlock()),
	
	;

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
			if (block.get() instanceof IRenderUtilityBlock)
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
