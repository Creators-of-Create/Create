package com.simibubi.create.foundation.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.createmod.catnip.utility.worldWrappers.SchematicWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelDataManager;

@OnlyIn(Dist.CLIENT)
@Mixin(ModelDataManager.class)
public class ModelDataRefreshMixin {
	/**
	 * Normally ModelDataManager will throw an exception if a block entity tries
	 * to refresh its model data from a world the client isn't currently in,
	 * but we need that to not happen for block entities in fake schematic
	 * worlds, so in those cases just do nothing instead.
	 */
	@Inject(method = "requestModelDataRefresh", at = @At("HEAD"), cancellable = true, remap = false)
	private static void create$requestModelDataRefresh(BlockEntity be, CallbackInfo ci) {
		if (be != null) {
			Level world = be.getLevel();
			if (world != Minecraft.getInstance().level && world instanceof SchematicWorld)
				ci.cancel();
		}
	}
}
