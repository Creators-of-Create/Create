package com.simibubi.create.lib.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.simibubi.create.lib.item.CustomGuiOverlayItem;
import com.simibubi.create.lib.utility.DurabilityBarUtil;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
	// TODO: improve this to not use a redirect
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isBarVisible()Z"), method = "renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V")
	public boolean create$checkRenderDurabilityBar(ItemStack stack, Font textRenderer, ItemStack stack1, int x, int y) {
		Item item = stack.getItem();
		if (item instanceof CustomGuiOverlayItem && ((CustomGuiOverlayItem) item).renderOverlay(stack, x, y, textRenderer, (ItemRenderer) (Object) this)) {
			return false;
		}
		return DurabilityBarUtil.showDurabilityBarDefault(stack);
	}
}
