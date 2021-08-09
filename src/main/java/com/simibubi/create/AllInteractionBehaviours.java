package com.simibubi.create;

import com.simibubi.create.content.contraptions.components.structureMovement.LeverMovingInteraction;
import com.simibubi.create.content.contraptions.components.structureMovement.MovingInteractionBehaviour;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

import java.util.HashMap;

public class AllInteractionBehaviours {
	private static final HashMap<ResourceLocation, MovingInteractionBehaviour> INTERACT_BEHAVIOURS = new HashMap<>();

	public static void addInteractionBehaviour (ResourceLocation loc, MovingInteractionBehaviour behaviour) {
		if (INTERACT_BEHAVIOURS.containsKey(loc)) {
			Create.LOGGER.warn("Interaction behaviour for " + loc.toString() + " was overridden");
		}
		INTERACT_BEHAVIOURS.put(loc, behaviour);
	}

	public static void addInteractionBehavioiur (Block block, MovingInteractionBehaviour behaviour) {
		addInteractionBehaviour(block.getRegistryName(), behaviour);
	}

	@Nullable
	public static MovingInteractionBehaviour of (ResourceLocation loc) {
		return INTERACT_BEHAVIOURS.getOrDefault(loc, null);
	}

	@Nullable
	public static MovingInteractionBehaviour of (Block block) {
		return of(block.getRegistryName());
	}

	public static boolean contains (Block block) {
		return INTERACT_BEHAVIOURS.containsKey(block.getRegistryName());
	}

	static void register () {
		addInteractionBehaviour(Blocks.LEVER.getRegistryName(), new LeverMovingInteraction());
	}
}
