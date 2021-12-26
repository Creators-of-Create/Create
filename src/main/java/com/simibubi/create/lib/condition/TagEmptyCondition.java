package com.simibubi.create.lib.condition;

import com.simibubi.create.lib.data.ICondition;

import net.minecraft.resources.ResourceLocation;

public class TagEmptyCondition implements ICondition {
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
	public ResourceLocation getID() {
		return new ResourceLocation("create", "tag_empty");
	}

	@Override
	public boolean test() {
		return false;
	}
}
