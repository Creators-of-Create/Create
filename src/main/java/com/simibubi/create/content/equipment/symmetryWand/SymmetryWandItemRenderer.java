package com.simibubi.create.content.equipment.symmetryWand;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class SymmetryWandItemRenderer extends CustomRenderedItemModelRenderer {

	protected static final PartialModel BITS = new PartialModel(Create.asResource("item/wand_of_symmetry/bits"));
	protected static final PartialModel CORE = new PartialModel(Create.asResource("item/wand_of_symmetry/core"));
	protected static final PartialModel CORE_GLOW = new PartialModel(Create.asResource("item/wand_of_symmetry/core_glow"));

	@Override
	protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType,
		PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		float worldTime = AnimationTickHolder.getRenderTime() / 20;
		int maxLight = LightTexture.FULL_BRIGHT;

		renderer.render(model.getOriginalModel(), light);
		renderer.renderSolidGlowing(CORE.get(), maxLight);
		renderer.renderGlowing(CORE_GLOW.get(), maxLight);

		float floating = Mth.sin(worldTime) * .05f;
		float angle = worldTime * -10 % 360;

		ms.translate(0, floating, 0);
		ms.mulPose(Axis.YP.rotationDegrees(angle));

		renderer.renderGlowing(BITS.get(), maxLight);
	}

}
