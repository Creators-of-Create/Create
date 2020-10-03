package com.simibubi.create.content.curiosities.zapper.blockzapper;

import static com.simibubi.create.content.curiosities.zapper.blockzapper.BlockzapperItem.Components.Accelerator;
import static com.simibubi.create.content.curiosities.zapper.blockzapper.BlockzapperItem.Components.Amplifier;
import static com.simibubi.create.content.curiosities.zapper.blockzapper.BlockzapperItem.Components.Body;
import static com.simibubi.create.content.curiosities.zapper.blockzapper.BlockzapperItem.Components.Retriever;
import static com.simibubi.create.content.curiosities.zapper.blockzapper.BlockzapperItem.Components.Scope;
import static java.lang.Math.max;
import static net.minecraft.util.math.MathHelper.clamp;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.curiosities.zapper.ZapperItemRenderer;
import com.simibubi.create.content.curiosities.zapper.blockzapper.BlockzapperItem.ComponentTier;
import com.simibubi.create.content.curiosities.zapper.blockzapper.BlockzapperItem.Components;
import com.simibubi.create.foundation.item.PartialItemModelRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class BlockzapperItemRenderer extends ZapperItemRenderer<BlockzapperModel> {

	@Override
	protected void render(ItemStack stack, BlockzapperModel model, PartialItemModelRenderer renderer, MatrixStack ms,
		IRenderTypeBuffer buffer, int light, int overlay) {
		super.render(stack, model, renderer, ms, buffer, light, overlay);

		float pt = Minecraft.getInstance()
			.getRenderPartialTicks();
		float worldTime = AnimationTickHolder.getRenderTick() / 20;

		renderer.render(model.getBakedModel(), light);
		renderComponent(stack, model, Body, renderer, light);
		renderComponent(stack, model, Amplifier, renderer, light);
		renderComponent(stack, model, Retriever, renderer, light);
		renderComponent(stack, model, Scope, renderer, light);

		ClientPlayerEntity player = Minecraft.getInstance().player;
		boolean leftHanded = player.getPrimaryHand() == HandSide.LEFT;
		boolean mainHand = player.getHeldItemMainhand() == stack;
		boolean offHand = player.getHeldItemOffhand() == stack;
		float animation = getAnimationProgress(pt, leftHanded, mainHand);

		// Core glows
		float multiplier = MathHelper.sin(worldTime * 5);
		if (mainHand || offHand)
			multiplier = animation;

		int lightItensity = (int) (15 * clamp(multiplier, 0, 1));
		int glowLight = LightTexture.pack(lightItensity, max(lightItensity, 4));
		renderer.renderSolidGlowing(model.getPartial("core"), glowLight);
		renderer.renderGlowing(model.getPartial("core_glow"), glowLight);

		if (BlockzapperItem.getTier(Amplifier, stack) != ComponentTier.None) {
			renderer.renderSolidGlowing(model.getPartial("amplifier_core"), glowLight);
			renderer.renderGlowing(model.getPartial("amplifier_core_glow"), glowLight);
		}

		// Accelerator spins
		float angle = worldTime * -25;
		if (mainHand || offHand)
			angle += 360 * animation;

		angle %= 360;
		float offset = -.155f;
		ms.translate(0, offset, 0);
		ms.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(angle));
		ms.translate(0, -offset, 0);
		renderComponent(stack, model, Accelerator, renderer, light);
	}

	public void renderComponent(ItemStack stack, BlockzapperModel model, Components component,
		PartialItemModelRenderer renderer, int light) {
		ComponentTier tier = BlockzapperItem.getTier(component, stack);
		IBakedModel partial = model.getComponentPartial(tier, component);
		if (partial != null)
			renderer.render(partial, light);
	}

}
