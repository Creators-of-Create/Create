package com.simibubi.create.content.contraptions.wrench;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.block.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.PartialItemModelRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueHandler;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;

public class WrenchItemRenderer extends CustomRenderedItemModelRenderer<WrenchModel> {

	@Override
	protected void render(ItemStack stack, WrenchModel model, PartialItemModelRenderer renderer, MatrixStack ms,
		IRenderTypeBuffer buffer, int light, int overlay) {
		renderer.render(model.getBakedModel(), light);

		float xOffset = -1/16f;
		ms.translate(-xOffset, 0, 0);
		ms.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(ScrollValueHandler.getScroll(AnimationTickHolder.getPartialTicks())));
		ms.translate(xOffset, 0, 0);
		
		renderer.render(model.getPartial("gear"), light);
	}
	
}
