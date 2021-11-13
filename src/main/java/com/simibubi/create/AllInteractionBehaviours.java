package com.simibubi.create;

import java.util.HashMap;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.content.contraptions.components.deployer.DeployerMovingInteraction;
import com.simibubi.create.content.contraptions.components.structureMovement.MovingInteractionBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.interaction.DoorMovingInteraction;
import com.simibubi.create.content.contraptions.components.structureMovement.interaction.LeverMovingInteraction;
import com.simibubi.create.content.contraptions.components.structureMovement.interaction.TrapdoorMovingInteraction;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class AllInteractionBehaviours {
	private static final HashMap<ResourceLocation, Supplier<MovingInteractionBehaviour>> INTERACT_BEHAVIOURS =
		new HashMap<>();

	public static void addInteractionBehaviour(ResourceLocation loc, Supplier<MovingInteractionBehaviour> behaviour) {
		if (INTERACT_BEHAVIOURS.containsKey(loc))
			Create.LOGGER.warn("Interaction behaviour for " + loc.toString() + " was overridden");
		INTERACT_BEHAVIOURS.put(loc, behaviour);
	}

	public static void addInteractionBehaviour(Block block, Supplier<MovingInteractionBehaviour> behaviour) {
		addInteractionBehaviour(Registry.BLOCK.getKey(block), behaviour);
	}

	@Nullable
	public static MovingInteractionBehaviour of(ResourceLocation loc) {
		return (INTERACT_BEHAVIOURS.get(loc) == null) ? null
			: INTERACT_BEHAVIOURS.get(loc)
				.get();
	}

	@Nullable
	public static MovingInteractionBehaviour of(Block block) {
		return of(Registry.BLOCK.getKey(block));
	}

	public static boolean contains(Block block) {
		return INTERACT_BEHAVIOURS.containsKey(Registry.BLOCK.getKey(block));
	}

	static void register() {
		addInteractionBehaviour(Blocks.LEVER.getRegistryName(), LeverMovingInteraction::new);
		addInteractionBehaviour(AllBlocks.DEPLOYER.getId(), DeployerMovingInteraction::new);

		// TODO: Scan registry for instanceof (-> modded door support)

		for (Block trapdoor : ImmutableList.of(Blocks.ACACIA_TRAPDOOR, Blocks.OAK_TRAPDOOR, Blocks.DARK_OAK_TRAPDOOR,
			Blocks.SPRUCE_TRAPDOOR, Blocks.JUNGLE_TRAPDOOR, Blocks.BIRCH_TRAPDOOR, Blocks.WARPED_TRAPDOOR,
			Blocks.CRIMSON_TRAPDOOR)) {
			addInteractionBehaviour(trapdoor.getRegistryName(), TrapdoorMovingInteraction::new);
		}

		for (Block door : ImmutableList.of(Blocks.ACACIA_DOOR, Blocks.OAK_DOOR, Blocks.DARK_OAK_DOOR,
			Blocks.SPRUCE_DOOR, Blocks.JUNGLE_DOOR, Blocks.BIRCH_DOOR, Blocks.WARPED_DOOR, Blocks.CRIMSON_DOOR)) {
			addInteractionBehaviour(door.getRegistryName(), DoorMovingInteraction::new);
		}
	}
}
