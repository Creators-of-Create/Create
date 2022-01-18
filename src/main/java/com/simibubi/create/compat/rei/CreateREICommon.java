package com.simibubi.create.compat.rei;

import com.simibubi.create.content.curiosities.tools.BlueprintContainer;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoRegistry;

// this class name is hideous
public class CreateREICommon implements REIServerPlugin {

	@Override
	public void registerMenuInfo(MenuInfoRegistry registry) {
		registry.register(CategoryIdentifier.of("minecraft", "plugins/crafting"), BlueprintContainer.class, new BlueprintTransferHandler());
	}
}
