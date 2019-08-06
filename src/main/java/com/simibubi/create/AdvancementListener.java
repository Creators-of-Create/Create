package com.simibubi.create;

import net.minecraft.advancements.AdvancementManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class AdvancementListener {

	@SubscribeEvent
	public static void onAdvancementGet(AdvancementEvent event) {
		PlayerEntity player = event.getPlayer();
		if (player == null)
			return;
		if (player.getServer() == null)
			return;

		// DEBUG 
//		AdvancementManager advancements = player.getServer().getAdvancementManager();
//		player.sendMessage(new StringTextComponent(event.getAdvancement().getId().toString()));

		unlockWhen("story/smelt_iron", recipeOf(AllItems.ANDESITE_ALLOY_CUBE), event);
		unlockWhen("story/smelt_iron", recipeOf(AllItems.PLACEMENT_HANDGUN), event);
		unlockWhen("nether/obtain_blaze_rod", recipeOf(AllItems.BLAZE_BRASS_CUBE), event);
		unlockWhen("recipes/misc/popped_chorus_fruit", recipeOf(AllItems.CHORUS_CHROME_CUBE), event);
		unlockWhen("recipes/decorations/end_rod", recipeOf(AllItems.SYMMETRY_WAND), event);
		unlockWhen("recipes/misc/bone_meal", recipeOf(AllItems.TREE_FERTILIZER), event);
		
		unlockWhen("recipes/misc/book", recipeOf(AllItems.EMPTY_BLUEPRINT), event);
		unlockWhen("recipes/misc/book", recipeOf(AllItems.BLUEPRINT_AND_QUILL), event);
		unlockWhen("recipes/misc/book", recipeOf(AllBlocks.SCHEMATIC_TABLE), event);
		unlockWhen("recipes/misc/book", recipeOf(AllBlocks.SCHEMATICANNON), event);
	}

	private static void unlockWhen(String advancement, ResourceLocation recipe, AdvancementEvent event) {
		AdvancementManager advancements = event.getPlayer().getServer().getAdvancementManager();
		if (event.getAdvancement() == advancements.getAdvancement(new ResourceLocation(advancement)))
			event.getPlayer().unlockRecipes(new ResourceLocation[] { recipe });
	}
	
	private static ResourceLocation recipeOf(AllItems item) {
		return item.get().getRegistryName();
	}
	
	private static ResourceLocation recipeOf(AllBlocks block) {
		return block.get().getRegistryName();
	}

}
