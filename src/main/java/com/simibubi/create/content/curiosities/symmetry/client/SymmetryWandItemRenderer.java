package com.simibubi.create.content.curiosities.symmetry.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.block.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.PartialItemModelRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class SymmetryWandItemRenderer extends CustomRenderedItemModelRenderer<SymmetryWandModel> {

	@Override
	protected void render(ItemStack stack, SymmetryWandModel model, PartialItemModelRenderer renderer, MatrixStack ms,
		IRenderTypeBuffer buffer, int light, int overlay) {
		float worldTime = AnimationTickHolder.getRenderTime() / 20;
		int maxLight = 0xF000F0;

		renderer.render(model.getBakedModel(), light);
		renderer.renderSolidGlowing(model.getPartial("core"), maxLight);
		renderer.renderGlowing(model.getPartial("core_glow"), maxLight);

		float floating = MathHelper.sin(worldTime) * .05f;
		float angle = worldTime * -10 % 360;
		
		ms.translate(0, floating, 0);
		ms.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(angle));
		
		renderer.renderGlowing(model.getPartial("bits"), maxLight);
	}

}
