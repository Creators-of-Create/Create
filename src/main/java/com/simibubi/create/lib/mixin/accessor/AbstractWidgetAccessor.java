package com.simibubi.create.lib.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;

@Environment(EnvType.CLIENT)
@Mixin(AbstractWidget.class)
public interface AbstractWidgetAccessor {
	@Accessor("height")
	int create$getHeight();

	@Accessor("height")
	void create$setHeight(int height);
}
