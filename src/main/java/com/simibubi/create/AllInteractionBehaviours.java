package com.simibubi.create;

import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.api.registry.SimpleRegistry;
import com.simibubi.create.content.contraptions.behaviour.DoorMovingInteraction;
import com.simibubi.create.content.contraptions.behaviour.FenceGateMovingInteraction;
import com.simibubi.create.content.contraptions.behaviour.LeverMovingInteraction;
import com.simibubi.create.content.contraptions.behaviour.TrapdoorMovingInteraction;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;

public class AllInteractionBehaviours {
	static void registerDefaults() {
		MovingInteractionBehaviour.REGISTRY.register(Blocks.LEVER, new LeverMovingInteraction());

		MovingInteractionBehaviour.REGISTRY.registerProvider(SimpleRegistry.Provider.forBlockTag(BlockTags.MOB_INTERACTABLE_DOORS, new DoorMovingInteraction()));
		MovingInteractionBehaviour.REGISTRY.registerProvider(SimpleRegistry.Provider.forBlockTag(BlockTags.WOODEN_TRAPDOORS, new TrapdoorMovingInteraction()));
		MovingInteractionBehaviour.REGISTRY.registerProvider(SimpleRegistry.Provider.forBlockTag(BlockTags.FENCE_GATES, new FenceGateMovingInteraction()));

		TrapdoorMovingInteraction copperTrapdoorInteraction =
			new TrapdoorMovingInteraction(SoundEvents.COPPER_TRAPDOOR_OPEN, SoundEvents.COPPER_TRAPDOOR_CLOSE);
		MovingInteractionBehaviour.REGISTRY.register(Blocks.COPPER_TRAPDOOR, copperTrapdoorInteraction);
		MovingInteractionBehaviour.REGISTRY.register(Blocks.EXPOSED_COPPER_TRAPDOOR, copperTrapdoorInteraction);
		MovingInteractionBehaviour.REGISTRY.register(Blocks.WEATHERED_COPPER_TRAPDOOR, copperTrapdoorInteraction);
		MovingInteractionBehaviour.REGISTRY.register(Blocks.OXIDIZED_COPPER_TRAPDOOR, copperTrapdoorInteraction);
		MovingInteractionBehaviour.REGISTRY.register(Blocks.WAXED_COPPER_TRAPDOOR, copperTrapdoorInteraction);
		MovingInteractionBehaviour.REGISTRY.register(Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR, copperTrapdoorInteraction);
		MovingInteractionBehaviour.REGISTRY.register(Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR, copperTrapdoorInteraction);
		MovingInteractionBehaviour.REGISTRY.register(Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR, copperTrapdoorInteraction);
	}
}
