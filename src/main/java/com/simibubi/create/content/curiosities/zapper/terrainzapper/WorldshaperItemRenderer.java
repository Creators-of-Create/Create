package com.simibubi.create.content.curiosities.zapper.terrainzapper;

import static java.lang.Math.max;
import static net.minecraft.util.math.MathHelper.clamp;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.simibubi.create.content.curiosities.zapper.ZapperItemRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

public class WorldshaperItemRenderer extends ZapperItemRenderer<WorldshaperModel> {

	@Override
	protected void render(ItemStack stack, WorldshaperModel model, PartialItemModelRenderer renderer, ItemTransforms.TransformType transformType,
		PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		super.render(stack, model, renderer, transformType, ms, buffer, light, overlay);

		float pt = AnimationTickHolder.getPartialTicks();
		float worldTime = AnimationTickHolder.getRenderTime() / 20;

		renderer.renderSolid(model.getOriginalModel(), light);

		LocalPlayer player = Minecraft.getInstance().player;
		boolean leftHanded = player.getMainArm() == HumanoidArm.LEFT;
		boolean mainHand = player.getMainHandItem() == stack;
		boolean offHand = player.getOffhandItem() == stack;
		float animation = getAnimationProgress(pt, leftHanded, mainHand);

		// Core glows
		float multiplier = Mth.sin(worldTime * 5);
		if (mainHand || offHand) 
			multiplier = animation;

		int lightItensity = (int) (15 * clamp(multiplier, 0, 1));
		int glowLight = LightTexture.pack(lightItensity, max(lightItensity, 4));
		renderer.renderSolidGlowing(model.getPartial("core"), glowLight);
		renderer.renderGlowing(model.getPartial("core_glow"), glowLight);

		// Accelerator spins
		float angle = worldTime * -25;
		if (mainHand || offHand)
			angle += 360 * animation;

		angle %= 360;
		float offset = -.155f;
		ms.translate(0, offset, 0);
		ms.mulPose(Vector3f.ZP.rotationDegrees(angle));
		ms.translate(0, -offset, 0);
		renderer.render(model.getPartial("accelerator"), light);
	}

	@Override
	public WorldshaperModel createModel(BakedModel originalModel) {
		return new WorldshaperModel(originalModel);
	}

}
