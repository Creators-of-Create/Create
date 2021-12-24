package com.simibubi.create.lib.condition;

import java.util.function.Predicate;

import net.minecraft.client.renderer.block.model.multipart.Condition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class TagEmptyCondition implements Condition {
	private final ResourceLocation tag_name;

	public TagEmptyCondition(String location)
	{
		this(new ResourceLocation(location));
	}

	public TagEmptyCondition(String namespace, String path)
	{
		this(new ResourceLocation(namespace, path));
	}

	public TagEmptyCondition(ResourceLocation tag)
	{
		this.tag_name = tag;
	}

	@Override
	public Predicate<BlockState> getPredicate(StateDefinition<Block, BlockState> stateDefinition) {
		return null;
	}
}
