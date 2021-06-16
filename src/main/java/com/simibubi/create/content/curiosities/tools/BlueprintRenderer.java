package com.simibubi.create.content.curiosities.tools;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.curiosities.tools.BlueprintEntity.BlueprintSection;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.backend.core.PartialModel;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.MatrixStacker;

import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;

public class BlueprintRenderer extends EntityRenderer<BlueprintEntity> {

	public BlueprintRenderer(EntityRendererManager p_i46179_1_) {
		super(p_i46179_1_);
	}

	@Override
	public void render(BlueprintEntity entity, float yaw, float pt, MatrixStack ms, IRenderTypeBuffer buffer,
		int overlay) {
		PartialModel partialModel = entity.size == 3 ? AllBlockPartials.CRAFTING_BLUEPRINT_3x3
			: entity.size == 2 ? AllBlockPartials.CRAFTING_BLUEPRINT_2x2 : AllBlockPartials.CRAFTING_BLUEPRINT_1x1;
		SuperByteBuffer sbb = PartialBufferer.get(partialModel, Blocks.AIR.getDefaultState());
		int light = WorldRenderer.getLightmapCoordinates(entity.world, entity.getBlockPos());
		sbb.matrixStacker()
			.rotateY(-yaw)
			.rotateX(90.0F + entity.rotationPitch)
			.translate(-.5, -1 / 32f, -.5);
		if (entity.size == 2)
			sbb.translate(.5, 0, -.5);

		RenderType entitySolid = RenderType.getEntitySolid(PlayerContainer.BLOCK_ATLAS_TEXTURE);
		sbb.asEntityModel()
			.light(light)
			.renderInto(ms, buffer.getBuffer(entitySolid));
		super.render(entity, yaw, pt, ms, buffer, light);

		ms.push();

		float fakeNormalXRotation = -15;
		int bl = light >> 4 & 0xf;
		int sl = light >> 20 & 0xf;
		boolean vertical = entity.rotationPitch != 0;
		if (entity.rotationPitch == -90) 
			fakeNormalXRotation = -45;
		else if (entity.rotationPitch == 90 || yaw % 180 != 0) {
			bl /= 1.35;
			sl /= 1.35;
		}
		int itemLight = MathHelper.floor(sl + .5) << 20 | (MathHelper.floor(bl + .5) & 0xf) << 4;
		
		MatrixStacker.of(ms)
			.rotateY(vertical ? 0 : -yaw)
			.rotateX(fakeNormalXRotation);
		Matrix3f copy = ms.peek()
			.getNormal()
			.copy();

		ms.pop();
		ms.push();

		MatrixStacker.of(ms)
			.rotateY(-yaw)
			.rotateX(entity.rotationPitch)
			.translate(0, 0, 1 / 32f + .001);

		if (entity.size == 3)
			ms.translate(-1, -1, 0);

		MatrixStack squashedMS = new MatrixStack();
		squashedMS.peek()
			.getModel()
			.multiply(ms.peek()
				.getModel());

		for (int x = 0; x < entity.size; x++) {
			squashedMS.push();
			for (int y = 0; y < entity.size; y++) {
				BlueprintSection section = entity.getSection(x * entity.size + y);
				Couple<ItemStack> displayItems = section.getDisplayItems();
				squashedMS.push();
				squashedMS.scale(.5f, .5f, 1 / 1024f);
				displayItems.forEachWithContext((stack, primary) -> {
					if (stack.isEmpty())
						return;

					squashedMS.push();
					if (!primary) {
						squashedMS.translate(0.325f, -0.325f, 1);
						squashedMS.scale(.625f, .625f, 1);
					}

					Matrix3f n = squashedMS.peek()
						.getNormal();
					n.a00 = copy.a00;
					n.a01 = copy.a01;
					n.a02 = copy.a02;
					n.a10 = copy.a10;
					n.a11 = copy.a11;
					n.a12 = copy.a12;
					n.a20 = copy.a20;
					n.a21 = copy.a21;
					n.a22 = copy.a22;

					Minecraft.getInstance()
						.getItemRenderer()
						.renderItem(stack, TransformType.GUI, itemLight, OverlayTexture.DEFAULT_UV, squashedMS, buffer);
					squashedMS.pop();
				});
				squashedMS.pop();
				squashedMS.translate(1, 0, 0);
			}
			squashedMS.pop();
			squashedMS.translate(0, 1, 0);
		}

		ms.pop();
	}

	@Override
	public ResourceLocation getEntityTexture(BlueprintEntity p_110775_1_) {
		return null;
	}

}
