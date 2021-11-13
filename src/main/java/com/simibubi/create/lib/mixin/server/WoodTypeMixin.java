package com.simibubi.create.lib.mixin.server;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.state.properties.WoodType;

@Environment(EnvType.SERVER)
@Mixin(WoodType.class)
public abstract class WoodTypeMixin {
	@Final
	@Shadow
	private String name;

	public String getName() {
		return name;
	}

	public String method_24028() {
		return getName();
	}
}
