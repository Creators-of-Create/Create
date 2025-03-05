package com.simibubi.create;

import com.simibubi.create.api.schematic.state.SchematicStateFilterRegistry;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class AllSchematicStateFilters {
	public static void registerDefaults() {
		SchematicStateFilterRegistry.REGISTRY.register(Blocks.CHISELED_BOOKSHELF, (blockEntity, state) -> {
			for (BooleanProperty p : ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES)
				state = state.setValue(p, false);

			return state;
		});
	}
}
