package com.simibubi.create.content.contraptions.wrench;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.block.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.PartialItemModelRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.item.ItemStack;

public class WrenchItemRenderer extends CustomRenderedItemModelRenderer<WrenchModel> {

	@Override
	protected void render(ItemStack stack, WrenchModel model, PartialItemModelRenderer renderer, MatrixStack ms,
		IRenderTypeBuffer buffer, int light, int overlay) {
		renderer.render(model.getBakedModel(), light);

		float worldTime = AnimationTickHolder.getRenderTick();
		float angle = worldTime * -.5f % 360;
		float xOffset = -1/32f;
		ms.translate(-xOffset, 0, 0);
		ms.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(angle));
		ms.translate(xOffset, 0, 0);
		
		renderer.render(model.getPartial("gear"), light);
	}
	
}
