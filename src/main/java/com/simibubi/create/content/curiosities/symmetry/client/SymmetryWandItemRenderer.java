package com.simibubi.create.content.curiosities.symmetry.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Mth;
import com.mojang.math.Vector3f;

public class SymmetryWandItemRenderer extends CustomRenderedItemModelRenderer<SymmetryWandModel> {

	public SymmetryWandItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet set) {
		super(dispatcher, set);
	}

	@Override
	protected void render(ItemStack stack, SymmetryWandModel model, PartialItemModelRenderer renderer, ItemTransforms.TransformType transformType,
		PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		float worldTime = AnimationTickHolder.getRenderTime() / 20;
		int maxLight = 0xF000F0;

		renderer.render(model.getOriginalModel(), light);
		renderer.renderSolidGlowing(model.getPartial("core"), maxLight);
		renderer.renderGlowing(model.getPartial("core_glow"), maxLight);

		float floating = Mth.sin(worldTime) * .05f;
		float angle = worldTime * -10 % 360;

		ms.translate(0, floating, 0);
		ms.mulPose(Vector3f.YP.rotationDegrees(angle));

		renderer.renderGlowing(model.getPartial("bits"), maxLight);
	}
}
