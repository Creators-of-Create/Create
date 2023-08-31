package com.simibubi.create.content.equipment.zapper.terrainzapper;

import static java.lang.Math.max;

import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.Create;
import com.simibubi.create.content.equipment.zapper.ZapperItemRenderer;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;

import net.createmod.catnip.utility.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class WorldshaperItemRenderer extends ZapperItemRenderer {

	protected static final PartialModel CORE = new PartialModel(Create.asResource("item/handheld_worldshaper/core"));
	protected static final PartialModel CORE_GLOW = new PartialModel(Create.asResource("item/handheld_worldshaper/core_glow"));
	protected static final PartialModel ACCELERATOR = new PartialModel(Create.asResource("item/handheld_worldshaper/accelerator"));

	@Override
	protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType,
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
		float multiplier;
		if (mainHand || offHand)
			multiplier = animation;
		else
			multiplier = Mth.sin(worldTime * 5);

		int lightItensity = (int) (15 * Mth.clamp(multiplier, 0, 1));
		int glowLight = LightTexture.pack(lightItensity, max(lightItensity, 4));
		renderer.renderSolidGlowing(CORE.get(), glowLight);
		renderer.renderGlowing(CORE_GLOW.get(), glowLight);

		// Accelerator spins
		float angle = worldTime * -25;
		if (mainHand || offHand)
			angle += 360 * animation;

		angle %= 360;
		float offset = -.155f;
		ms.translate(0, offset, 0);
		ms.mulPose(Axis.ZP.rotationDegrees(angle));
		ms.translate(0, -offset, 0);
		renderer.render(ACCELERATOR.get(), light);
	}

}
