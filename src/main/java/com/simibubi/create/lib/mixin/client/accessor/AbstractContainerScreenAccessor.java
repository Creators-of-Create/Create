package com.simibubi.create.lib.mixin.client.accessor;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
	@Accessor("leftPos")
	int create$getGuiLeft();

	@Accessor("topPos")
	int create$getGuiTop();
}
