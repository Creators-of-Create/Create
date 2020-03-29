package com.simibubi.create.modules.curiosities.zapper.blockzapper;

import static com.simibubi.create.modules.curiosities.zapper.blockzapper.BlockzapperItem.Components.Accelerator;
import static com.simibubi.create.modules.curiosities.zapper.blockzapper.BlockzapperItem.Components.Amplifier;
import static com.simibubi.create.modules.curiosities.zapper.blockzapper.BlockzapperItem.Components.Body;
import static com.simibubi.create.modules.curiosities.zapper.blockzapper.BlockzapperItem.Components.Retriever;
import static com.simibubi.create.modules.curiosities.zapper.blockzapper.BlockzapperItem.Components.Scope;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.modules.curiosities.zapper.ZapperItemRenderer;
import com.simibubi.create.modules.curiosities.zapper.ZapperRenderHandler;
import com.simibubi.create.modules.curiosities.zapper.blockzapper.BlockzapperItem.ComponentTier;
import com.simibubi.create.modules.curiosities.zapper.blockzapper.BlockzapperItem.Components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;

@SuppressWarnings("deprecation")
public class BlockzapperItemRenderer extends ZapperItemRenderer {

	@Override
	public void renderByItem(ItemStack stack) {
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		BlockzapperModel mainModel = (BlockzapperModel) itemRenderer.getModelWithOverrides(stack);
		float pt = Minecraft.getInstance().getRenderPartialTicks();
		float worldTime = AnimationTickHolder.getRenderTick() / 20;

		RenderSystem.pushMatrix();
		RenderSystem.translatef(0.5F, 0.5F, 0.5F);
		float lastCoordx = GLX.lastBrightnessX;
		float lastCoordy = GLX.lastBrightnessY;
		GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, Math.min(lastCoordx + 60, 240), Math.min(lastCoordy + 120, 240));

		itemRenderer.renderItem(stack, mainModel.getBakedModel());
		renderComponent(stack, mainModel, Body, itemRenderer);
		renderComponent(stack, mainModel, Amplifier, itemRenderer);
		renderComponent(stack, mainModel, Retriever, itemRenderer);
		renderComponent(stack, mainModel, Scope, itemRenderer);

		// Block indicator
		if (mainModel.getCurrentPerspective() == TransformType.GUI && stack.hasTag()
				&& stack.getTag().contains("BlockUsed"))
			renderBlockUsed(stack, itemRenderer);

		ClientPlayerEntity player = Minecraft.getInstance().player;
		boolean leftHanded = player.getPrimaryHand() == HandSide.LEFT;
		boolean mainHand = player.getHeldItemMainhand() == stack;
		boolean offHand = player.getHeldItemOffhand() == stack;
		float last = mainHand ^ leftHanded ? ZapperRenderHandler.lastRightHandAnimation
				: ZapperRenderHandler.lastLeftHandAnimation;
		float current = mainHand ^ leftHanded ? ZapperRenderHandler.rightHandAnimation
				: ZapperRenderHandler.leftHandAnimation;
		float animation = MathHelper.clamp(MathHelper.lerp(pt, last, current) * 5, 0, 1);

		// Core glows
		RenderSystem.disableLighting();
		float multiplier = MathHelper.sin(worldTime * 5);
		if (mainHand || offHand) {
			multiplier = animation;
		}
		GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, multiplier * 240, 120);
		itemRenderer.renderItem(stack, mainModel.getPartial("core"));
		if (BlockzapperItem.getTier(Amplifier, stack) != ComponentTier.None)
			itemRenderer.renderItem(stack, mainModel.getPartial("amplifier_core"));
		GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, lastCoordx, lastCoordy);
		RenderSystem.enableLighting();

		// Accelerator spins
		float angle = worldTime * -25;
		if (mainHand || offHand)
			angle += 360 * animation;

		angle %= 360;
		float offset = -.155f;
		RenderSystem.translatef(0, offset, 0);
		RenderSystem.rotatef(angle, 0, 0, 1);
		RenderSystem.translatef(0, -offset, 0);
		renderComponent(stack, mainModel, Accelerator, itemRenderer);

		RenderSystem.popMatrix();
	}

	public void renderComponent(ItemStack stack, BlockzapperModel model, Components component,
			ItemRenderer itemRenderer) {
		ComponentTier tier = BlockzapperItem.getTier(component, stack);
		IBakedModel partial = model.getComponentPartial(tier, component);
		if (partial != null)
			itemRenderer.renderItem(stack, partial);
	}

}
