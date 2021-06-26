package com.simibubi.create;

import com.jozufozu.flywheel.core.AtlasStitcher;
import com.jozufozu.flywheel.core.StitchedSprite;

import net.minecraft.util.ResourceLocation;

public class AllStitchedTextures {

	public static final StitchedSprite SUPER_GLUE = AtlasStitcher.getInstance().get(new ResourceLocation(Create.ID, "entity/super_glue/slime"));

	public static void init() {

	}
}
