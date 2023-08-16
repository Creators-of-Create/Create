package com.simibubi.create.foundation.ponder;

import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.simibubi.create.Create;
import com.simibubi.create.infrastructure.ponder.AllPonderTags;
import com.simibubi.create.infrastructure.ponder.CreatePonderIndex;

import net.createmod.ponder.foundation.PonderLevel;
import net.createmod.ponder.foundation.PonderPlugin;
import net.minecraft.world.level.ItemLike;

public class CreatePonderPlugin implements PonderPlugin {

	@Override
	public String getModID() {
		return Create.ID;
	}

	@Override
	public void registerScenes() {
		CreatePonderIndex.register();
	}

	@Override
	public void registerTags() {
		AllPonderTags.register();
	}

	@Override
	public void registerSharedText(BiConsumer<String, String> adder) {
		adder.accept("rpm8", "8 RPM");
		adder.accept("rpm16", "16 RPM");
		adder.accept("rpm16_source", "Source: 16 RPM");
		adder.accept("rpm32", "32 RPM");

		adder.accept("movement_anchors", "With the help of Super Glue, larger structures can be moved.");
		adder.accept("behaviour_modify_wrench", "This behaviour can be modified using a Wrench");
		adder.accept("storage_on_contraption", "Inventories attached to the Contraption will pick up their drops automatically");
	}

	@Override
	public void onPonderWorldRestore(PonderLevel world) {
		PonderWorldBlockEntityFix.fixControllerBlockEntities(world);
	}

	@Override
	public Stream<Predicate<ItemLike>> indexExclusions() {
		return CreatePonderIndex.INDEX_SCREEN_EXCLUSIONS.stream();
	}
}
