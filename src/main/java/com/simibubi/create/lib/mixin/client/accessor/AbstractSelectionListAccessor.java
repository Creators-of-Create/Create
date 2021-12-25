package com.simibubi.create.lib.mixin.client.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractSelectionList;

@Environment(EnvType.CLIENT)
@Mixin(AbstractSelectionList.class)
public interface AbstractSelectionListAccessor {
	@Accessor("width")
	int create$getWidth();
}
