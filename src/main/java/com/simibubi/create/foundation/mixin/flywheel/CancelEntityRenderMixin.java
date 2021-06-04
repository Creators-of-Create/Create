package com.simibubi.create.foundation.mixin.flywheel;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.Lists;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.IInstanceRendered;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;

@Mixin(ClientWorld.class)
public class CancelEntityRenderMixin {

	@Inject(at = @At("RETURN"), method = "getAllEntities", cancellable = true)
	private void filterEntities(CallbackInfoReturnable<Iterable<Entity>> cir) {
		if (Backend.canUseInstancing()) {
			Iterable<Entity> entities = cir.getReturnValue();

			ArrayList<Entity> list = Lists.newArrayList(entities);
			list.removeIf(tile -> tile instanceof IInstanceRendered && !((IInstanceRendered) tile).shouldRenderNormally());

			cir.setReturnValue(list);
		}
	}
}
