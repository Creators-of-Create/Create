package com.simibubi.create.foundation.ponder;

import java.util.function.Predicate;
import java.util.stream.Stream;

import com.simibubi.create.Create;
import com.simibubi.create.infrastructure.ponder.AllPonderTags;
import com.simibubi.create.infrastructure.ponder.CreatePonderIndex;

import net.createmod.ponder.foundation.PonderLevel;
import net.createmod.ponder.foundation.api.registration.PonderPlugin;
import net.createmod.ponder.foundation.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.foundation.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.foundation.api.registration.SharedTextRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

public class CreatePonderPlugin implements PonderPlugin {

	@Override
	public String getModId() {
		return Create.ID;
	}

	@Override
	public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
		CreatePonderIndex.register(helper);
	}

	@Override
	public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
		AllPonderTags.register(helper);
	}

	@Override
	public void registerSharedText(SharedTextRegistrationHelper helper) {
		helper.registerSharedText("rpm8", "8 RPM");
		helper.registerSharedText("rpm16", "16 RPM");
		helper.registerSharedText("rpm16_source", "Source: 16 RPM");
		helper.registerSharedText("rpm32", "32 RPM");

		helper.registerSharedText("movement_anchors", "With the help of Super Glue, larger structures can be moved.");
		helper.registerSharedText("behaviour_modify_wrench", "This behaviour can be modified using a Wrench");
		helper.registerSharedText("storage_on_contraption", "Inventories attached to the Contraption will pick up their drops automatically");
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
