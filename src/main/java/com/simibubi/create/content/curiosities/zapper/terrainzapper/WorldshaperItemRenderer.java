package com.simibubi.create.content.curiosities.zapper.terrainzapper;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.curiosities.zapper.ZapperItemRenderer;
import com.simibubi.create.foundation.item.PartialItemModelRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

import static java.lang.Math.max;
import static net.minecraft.util.math.MathHelper.clamp;

public class WorldshaperItemRenderer extends ZapperItemRenderer<WorldshaperModel> {

	@Override
	protected void render(ItemStack stack, WorldshaperModel model, PartialItemModelRenderer renderer, MatrixStack ms,
		IRenderTypeBuffer buffer, int light, int overlay) {
		super.render(stack, model, renderer, ms, buffer, light, overlay);

		float pt = AnimationTickHolder.getPartialTicks();
		float worldTime = AnimationTickHolder.getRenderTime() / 20;

		renderer.renderSolid(model.getBakedModel(), light);

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

		// Accelerator spins
		float angle = worldTime * -25;
		if (mainHand || offHand)
			angle += 360 * animation;

		angle %= 360;
		float offset = -.155f;
		ms.translate(0, offset, 0);
		ms.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(angle));
		ms.translate(0, -offset, 0);
		renderer.render(model.getPartial("accelerator"), light);
	}

}
