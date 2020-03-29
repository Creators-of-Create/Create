package com.simibubi.create.modules.curiosities.zapper.blockzapper;

import static com.simibubi.create.modules.curiosities.zapper.blockzapper.BlockzapperItem.Components.Accelerator;
import static com.simibubi.create.modules.curiosities.zapper.blockzapper.BlockzapperItem.Components.Amplifier;
import static com.simibubi.create.modules.curiosities.zapper.blockzapper.BlockzapperItem.Components.Body;
import static com.simibubi.create.modules.curiosities.zapper.blockzapper.BlockzapperItem.Components.Retriever;
import static com.simibubi.create.modules.curiosities.zapper.blockzapper.BlockzapperItem.Components.Scope;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.modules.curiosities.zapper.ZapperItemRenderer;
import com.simibubi.create.modules.curiosities.zapper.ZapperRenderHandler;
import com.simibubi.create.modules.curiosities.zapper.blockzapper.BlockzapperItem.ComponentTier;
import com.simibubi.create.modules.curiosities.zapper.blockzapper.BlockzapperItem.Components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;

@SuppressWarnings("deprecation")
public class BlockzapperItemRenderer extends ZapperItemRenderer {
	
	@Override
	public void render(ItemStack stack, MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay) {
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		BlockzapperModel mainModel = (BlockzapperModel) itemRenderer.getItemModelWithOverrides(stack, Minecraft.getInstance().world, null);
		float pt = Minecraft.getInstance().getRenderPartialTicks();
		float worldTime = AnimationTickHolder.getRenderTick() / 20;

		ms.push();
		ms.translate(0.5F, 0.5F, 0.5F);

		itemRenderer.renderItem(stack, TransformType.NONE, false, ms, buffer, light, overlay, mainModel);
		renderComponent(stack, mainModel, Body, itemRenderer, ms, buffer, light, overlay);
		renderComponent(stack, mainModel, Amplifier, itemRenderer, ms, buffer, light, overlay);
		renderComponent(stack, mainModel, Retriever, itemRenderer, ms, buffer, light, overlay);
		renderComponent(stack, mainModel, Scope, itemRenderer, ms, buffer, light, overlay);

		// Block indicator
		if (mainModel.getCurrentPerspective() == TransformType.GUI && stack.hasTag()
				&& stack.getTag().contains("BlockUsed"))
			renderBlockUsed(stack, itemRenderer, ms, buffer, light, overlay);

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
		float multiplier = MathHelper.sin(worldTime * 5);
		if (mainHand || offHand) {
			multiplier = animation;
		}
		int glowLight = LightTexture.pack((int) (15 * multiplier), 15);
		itemRenderer.renderItem(stack, TransformType.NONE, false, ms, buffer, glowLight, overlay, mainModel.getPartial("core"));
		if (BlockzapperItem.getTier(Amplifier, stack) != ComponentTier.None)
			itemRenderer.renderItem(stack, TransformType.NONE, false, ms, buffer, glowLight, overlay, mainModel.getPartial("amplifier_core"));

		// Accelerator spins
		float angle = worldTime * -25;
		if (mainHand || offHand)
			angle += 360 * animation;

		angle %= 360;
		float offset = -.155f;
		ms.translate(0, offset, 0);
		ms.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(angle));
		ms.translate(0, -offset, 0);
		renderComponent(stack, mainModel, Accelerator, itemRenderer, ms, buffer, light, overlay);

		ms.pop();
	}

	public void renderComponent(ItemStack stack, BlockzapperModel model, Components component,
			ItemRenderer itemRenderer, MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay) {
		ComponentTier tier = BlockzapperItem.getTier(component, stack);
		IBakedModel partial = model.getComponentPartial(tier, component);
		if (partial != null)
			itemRenderer.renderItem(stack, TransformType.NONE, false, ms, buffer, light, overlay, partial);
	}

}
