package com.simibubi.create.lib.mixin.client.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;

@Environment(EnvType.CLIENT)
@Mixin(BlockRenderDispatcher.class)
public interface BlockRenderDispatcherAccessor {
	@Accessor("blockEntityRenderer")
	BlockEntityWithoutLevelRenderer getBlockEntityRenderer();
}
