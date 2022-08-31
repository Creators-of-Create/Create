package com.simibubi.create.foundation.ponder;

import java.util.function.Predicate;
import java.util.stream.Stream;

import net.createmod.ponder.foundation.PonderPlugin;
import net.createmod.ponder.foundation.PonderWorld;
import net.minecraft.world.level.ItemLike;

public class CreatePonderPlugin implements PonderPlugin {

	@Override
	public void registerScenes() {
		CreatePonderIndex.register();
	}

	@Override
	public void registerTags() {
		CreatePonderIndex.registerTags();
	}

	@Override
	public void onPonderWorldRestore(PonderWorld world) {
		PonderWorldTileFix.fixControllerTiles(world);
	}

	@Override
	public Stream<Predicate<ItemLike>> indexExclusions() {
		return CreatePonderIndex.INDEX_SCREEN_EXCLUSIONS.stream();
	}
}
