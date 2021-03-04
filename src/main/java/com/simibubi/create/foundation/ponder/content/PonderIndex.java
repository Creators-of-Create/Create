package com.simibubi.create.foundation.ponder.content;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.ponder.PonderRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;

public class PonderIndex {

	public static final boolean EDITOR_MODE = true;

	public static void register() {
		// Register storyboards here
		// (!) Added entries require re-launch
		// (!) Modifications inside storyboard methods only require re-opening the ui

		PonderRegistry.forComponents(AllBlocks.SHAFT)
			.addStoryBoard("shaft/relay", KineticsScenes::shaftAsRelay, b -> b.highlightAllTags().chapter(PonderChapter.of("basic_kinetics")))
			.addStoryBoard("shaft/encasing", KineticsScenes::shaftsCanBeEncased, b -> b.chapter(PonderChapter.of("encasing")));

		// Funnels
		PonderRegistry.addStoryBoard(AllBlocks.BRASS_FUNNEL, "funnels/brass", FunnelScenes::brass);
		PonderRegistry.forComponents(AllBlocks.ANDESITE_FUNNEL, AllBlocks.BRASS_FUNNEL)
			.addStoryBoard("funnels/intro", FunnelScenes::intro)
			.addStoryBoard("funnels/direction", FunnelScenes::directionality)
			.addStoryBoard("funnels/compat", FunnelScenes::compat)
			.addStoryBoard("funnels/redstone", FunnelScenes::redstone)
			.addStoryBoard("funnels/transposer", FunnelScenes::transposer);
		PonderRegistry.addStoryBoard(AllBlocks.ANDESITE_FUNNEL, "funnels/brass", FunnelScenes::brass);
		
		// Gantries
		PonderRegistry.addStoryBoard(AllBlocks.GANTRY_SHAFT, "gantry/intro", GantryScenes::introForShaft);
		PonderRegistry.addStoryBoard(AllBlocks.GANTRY_CARRIAGE, "gantry/intro", GantryScenes::introForPinion);
		PonderRegistry.forComponents(AllBlocks.GANTRY_SHAFT, AllBlocks.GANTRY_CARRIAGE)
			.addStoryBoard("gantry/redstone", GantryScenes::redstone)
			.addStoryBoard("gantry/direction", GantryScenes::direction)
			.addStoryBoard("gantry/subgantry", GantryScenes::subgantry);

		// Debug scenes, can be found in game via the Brass Hand
		if (EDITOR_MODE)
			DebugScenes.registerAll();
	}

	public static void registerTags() {

		PonderRegistry.tags.forItems(AllBlocks.SHAFT.getId())
				.add(PonderTag.Create.KINETICS);

		PonderRegistry.tags.forItems(AllBlocks.ANDESITE_FUNNEL.getId(), AllBlocks.BRASS_FUNNEL.getId())
				.add(PonderTag.Create.ARM_ACCESS)
				.add(PonderTag.Vanilla.ITEM_TRANSFER)
				.add(PonderTag.Vanilla.REDSTONE_CONTROL);

		PonderRegistry.tags.forTag(PonderTag.Vanilla.REDSTONE_CONTROL)
				.add(Items.REDSTONE.getRegistryName())
				.add(Blocks.LEVER.getRegistryName());

		PonderRegistry.tags.forTag(PonderTag.Create.KINETICS)
				.add(AllBlocks.COGWHEEL.getId())
				.add(AllBlocks.LARGE_COGWHEEL.getId())
				.add(AllItems.BELT_CONNECTOR.getId())
				.add(AllBlocks.ENCASED_CHAIN_DRIVE.getId());

		PonderChapter.of("basic_kinetics").addTagsToChapter(
				PonderTag.Create.KINETICS
		);

	}

}
