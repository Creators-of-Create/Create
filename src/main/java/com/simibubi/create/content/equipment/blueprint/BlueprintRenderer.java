package com.simibubi.create.content.equipment.blueprint;

import org.joml.Matrix3f;

import com.jozufozu.flywheel.lib.model.baked.PartialModel;
import com.jozufozu.flywheel.lib.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.equipment.blueprint.BlueprintEntity.BlueprintSection;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.Couple;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class BlueprintRenderer extends EntityRenderer<BlueprintEntity> {

	public BlueprintRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(BlueprintEntity entity, float yaw, float pt, PoseStack ms, MultiBufferSource buffer,
		int light) {
		PartialModel partialModel = entity.size == 3 ? AllPartialModels.CRAFTING_BLUEPRINT_3x3
			: entity.size == 2 ? AllPartialModels.CRAFTING_BLUEPRINT_2x2 : AllPartialModels.CRAFTING_BLUEPRINT_1x1;
		SuperByteBuffer sbb = CachedBufferer.partial(partialModel, Blocks.AIR.defaultBlockState());
		sbb.rotateYDegrees(-yaw)
			.rotateXDegrees(90.0F + entity.getXRot())
			.translate(-.5, -1 / 32f, -.5);
		if (entity.size == 2)
			sbb.translate(.5, 0, -.5);

		sbb.disableDiffuse()
			.light(light)
			.renderInto(ms, buffer.getBuffer(Sheets.solidBlockSheet()));
		super.render(entity, yaw, pt, ms, buffer, light);

		ms.pushPose();

		float fakeNormalXRotation = -15;
		int bl = light >> 4 & 0xf;
		int sl = light >> 20 & 0xf;
		boolean vertical = entity.getXRot() != 0;
		if (entity.getXRot() == -90)
			fakeNormalXRotation = -45;
		else if (entity.getXRot() == 90 || yaw % 180 != 0) {
			bl /= 1.35;
			sl /= 1.35;
		}
		int itemLight = Mth.floor(sl + .5) << 20 | (Mth.floor(bl + .5) & 0xf) << 4;

		TransformStack.of(ms)
			.rotateYDegrees(vertical ? 0 : -yaw)
			.rotateXDegrees(fakeNormalXRotation);
		Matrix3f copy = new Matrix3f(ms.last()
			.normal());

		ms.popPose();
		ms.pushPose();

		TransformStack.of(ms)
			.rotateYDegrees(-yaw)
			.rotateXDegrees(entity.getXRot())
			.translate(0, 0, 1 / 32f + .001);

		if (entity.size == 3)
			ms.translate(-1, -1, 0);

		PoseStack squashedMS = new PoseStack();
		squashedMS.last()
			.pose()
			.mul(ms.last()
				.pose());

		for (int x = 0; x < entity.size; x++) {
			squashedMS.pushPose();
			for (int y = 0; y < entity.size; y++) {
				BlueprintSection section = entity.getSection(x * entity.size + y);
				Couple<ItemStack> displayItems = section.getDisplayItems();
				squashedMS.pushPose();
				squashedMS.scale(.5f, .5f, 1 / 1024f);
				displayItems.forEachWithContext((stack, primary) -> {
					if (stack.isEmpty())
						return;

					squashedMS.pushPose();
					if (!primary) {
						squashedMS.translate(0.325f, -0.325f, 1);
						squashedMS.scale(.625f, .625f, 1);
					}

					squashedMS.last()
						.normal()
						.set(copy);

					Minecraft.getInstance()
						.getItemRenderer()
						.renderStatic(stack, ItemDisplayContext.GUI, itemLight, OverlayTexture.NO_OVERLAY, squashedMS,
							buffer, entity.level(), 0);
					squashedMS.popPose();
				});
				squashedMS.popPose();
				squashedMS.translate(1, 0, 0);
			}
			squashedMS.popPose();
			squashedMS.translate(0, 1, 0);
		}

		ms.popPose();
	}

	@Override
	public ResourceLocation getTextureLocation(BlueprintEntity entity) {
		return null;
	}

}
