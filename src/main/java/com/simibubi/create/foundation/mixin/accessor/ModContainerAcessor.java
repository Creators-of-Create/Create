package com.simibubi.create.foundation.mixin.accessor;

import net.minecraftforge.fml.ModContainer;

import net.minecraftforge.fml.config.ModConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.EnumMap;

@Mixin(ModContainer.class)
public interface ModContainerAcessor {
	@Accessor("configs")
	EnumMap<ModConfig.Type, ModConfig> getConfigs();
}
