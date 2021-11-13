package com.simibubi.create.lib.condition;

import java.util.function.Predicate;

import net.minecraft.client.renderer.block.model.multipart.Condition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class NotCondition implements Condition
{
	private static final ResourceLocation NAME = new ResourceLocation("forge", "not");
	private final Condition child;

	public NotCondition(Condition child) {
		this.child = child;
	}

	@Override
	public String toString() {
		return "!" + child;
	}

	@Override
	public Predicate<BlockState> getPredicate(StateDefinition<Block, BlockState> stateContainer) {
		return child.getPredicate(stateContainer).negate();
	}
}
