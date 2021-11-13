package com.simibubi.create.lib.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
@Mixin(ItemInHandRenderer.class)
public interface ItemInHandRendererAccessor {
	@Accessor("mainHandItem")
	ItemStack getItemStackMainHand();

	@Accessor("offHandItem")
	ItemStack getItemStackOffHand();
}
