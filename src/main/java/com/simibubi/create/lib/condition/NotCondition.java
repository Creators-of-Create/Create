package com.simibubi.create.lib.condition;

import com.simibubi.create.lib.data.ICondition;

import net.minecraft.resources.ResourceLocation;

public class NotCondition implements ICondition
{
	private static final ResourceLocation NAME = new ResourceLocation("forge", "not");
	private final ICondition child;

	public NotCondition(ICondition child) {
		this.child = child;
	}

	@Override
	public String toString() {
		return "!" + child;
	}

	@Override
	public ResourceLocation getID() {
		return NAME;
	}

	@Override
	public boolean test() {
		return !child.test();
	}
}
