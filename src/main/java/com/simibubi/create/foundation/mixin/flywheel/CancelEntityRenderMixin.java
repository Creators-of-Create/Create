package com.simibubi.create.foundation.mixin.flywheel;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.google.common.collect.Lists;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.IInstanceRendered;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;

@Mixin(WorldRenderer.class)
public class CancelEntityRenderMixin {

//	@Inject(at = @At("HEAD"), method = "shouldRender", cancellable = true)
//	private <E extends Entity> void dontRenderFlywheelEntities(E entity, ClippingHelper p_229086_2_, double p_229086_3_, double p_229086_5_, double p_229086_7_, CallbackInfoReturnable<Boolean> cir) {
//		if (Backend.getInstance().canUseInstancing()) {
//			if (entity instanceof IInstanceRendered && !((IInstanceRendered) entity).shouldRenderNormally())
//				cir.setReturnValue(false);
//		}
//	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getAllEntities()Ljava/lang/Iterable;"))
	private Iterable<Entity> filterEntities(ClientWorld world) {
		Iterable<Entity> entities = world.getAllEntities();
		if (Backend.getInstance().canUseInstancing()) {

			ArrayList<Entity> filtered = Lists.newArrayList(entities);
			filtered.removeIf(tile -> tile instanceof IInstanceRendered && !((IInstanceRendered) tile).shouldRenderNormally());

			return filtered;
		}
		return entities;
	}
}
