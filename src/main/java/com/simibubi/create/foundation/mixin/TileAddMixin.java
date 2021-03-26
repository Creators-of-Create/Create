package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.CreateClient;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(World.class)
public class TileAddMixin {

	@Shadow @Final public boolean isRemote;

	@Inject(at = @At("TAIL"), method = "addTileEntity")
	private void onAddTile(TileEntity te, CallbackInfoReturnable<Boolean> cir) {
		if (isRemote) {
			CreateClient.kineticRenderer.get((World)(Object) this).queueAdd(te);
		}
	}
}
