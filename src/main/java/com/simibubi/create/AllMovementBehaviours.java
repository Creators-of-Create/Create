package com.simibubi.create;

import com.simibubi.create.content.contraptions.components.actors.BellMovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;

public class AllMovementBehaviours {
	private static final HashMap<ResourceLocation, MovementBehaviour> movementBehaviours = new HashMap<>();

	public static void addMovementBehaviour(ResourceLocation resourceLocation, MovementBehaviour movementBehaviour) {
		movementBehaviours.put(resourceLocation, movementBehaviour);
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
		addMovementBehaviour(Blocks.BELL.getRegistryName(), new BellMovementBehaviour());
	}
}
