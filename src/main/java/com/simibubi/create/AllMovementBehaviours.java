package com.simibubi.create;

import java.util.HashMap;

import javax.annotation.Nullable;

import com.simibubi.create.content.contraptions.components.actors.BellMovementBehaviour;
import com.simibubi.create.content.contraptions.components.actors.CampfireMovementBehaviour;
import com.simibubi.create.content.contraptions.components.actors.dispenser.DispenserMovementBehaviour;
import com.simibubi.create.content.contraptions.components.actors.dispenser.DropperMovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.tterrag.registrate.util.nullness.NonNullConsumer;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;

public class AllMovementBehaviours {
	private static final HashMap<ResourceLocation, MovementBehaviour> movementBehaviours = new HashMap<>();

	public static void addMovementBehaviour(ResourceLocation resourceLocation, MovementBehaviour movementBehaviour) {
		if (movementBehaviours.containsKey(resourceLocation))
			Create.logger.warn("Movement behaviour for " + resourceLocation.toString() + " was overridden");
		movementBehaviours.put(resourceLocation, movementBehaviour);
	}

	public static void addMovementBehaviour(Block block, MovementBehaviour movementBehaviour) {
		addMovementBehaviour(block.getRegistryName(), movementBehaviour);
	}

	@Nullable
	public static MovementBehaviour getMovementBehaviour(ResourceLocation resourceLocation) {
		return movementBehaviours.getOrDefault(resourceLocation, null);
	}

	@Nullable
	public static MovementBehaviour getMovementBehaviour(Block block) {
		return getMovementBehaviour(block.getRegistryName());
	}

	public static boolean hasMovementBehaviour(Block block) {
		return movementBehaviours.containsKey(block.getRegistryName());
	}

	public static <B extends Block> NonNullConsumer<? super B> addMovementBehaviour(
		MovementBehaviour movementBehaviour) {
		return b -> addMovementBehaviour(b.getRegistryName(), movementBehaviour);
	}

	static void register() {
		addMovementBehaviour(Blocks.BELL, new BellMovementBehaviour());
		addMovementBehaviour(Blocks.CAMPFIRE, new CampfireMovementBehaviour());

		DispenserMovementBehaviour.gatherMovedDispenseItemBehaviours();
		addMovementBehaviour(Blocks.DISPENSER, new DispenserMovementBehaviour());
		addMovementBehaviour(Blocks.DROPPER, new DropperMovementBehaviour());
	}
}
