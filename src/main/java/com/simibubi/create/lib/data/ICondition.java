package com.simibubi.create.lib.data;

import net.minecraft.resources.ResourceLocation;

public interface ICondition {
	ResourceLocation getID();
	boolean test();
}
