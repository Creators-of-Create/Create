package com.simibubi.create.compat.jei.category.animations;

import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;

public class AnimatedBlazeBurner extends AnimatedKinetics {

	private HeatLevel heatLevel;

	public AnimatedBlazeBurner withHeat(HeatLevel heatLevel) {
		this.heatLevel = heatLevel;
		return this;
	}

	public void draw(PoseStack matrixStack, int xOffset, int yOffset) {
		matrixStack.pushPose();
		matrixStack.translate(xOffset, yOffset, 200);
		matrixStack.mulPose(Vector3f.XP.rotationDegrees(-15.5f));
		matrixStack.mulPose(Vector3f.YP.rotationDegrees(22.5f));
		int scale = 23;

		float offset = (Mth.sin(AnimationTickHolder.getRenderTime() / 16f) + 0.5f) / 16f;

		blockElement(AllBlocks.BLAZE_BURNER.getDefaultState()).atLocal(0, 1.65, 0)
			.scale(scale)
			.render(matrixStack);

		PartialModel blaze =
			heatLevel == HeatLevel.SEETHING ? AllBlockPartials.BLAZE_SUPER : AllBlockPartials.BLAZE_ACTIVE;
		PartialModel rods2 = heatLevel == HeatLevel.SEETHING ? AllBlockPartials.BLAZE_BURNER_SUPER_RODS_2
			: AllBlockPartials.BLAZE_BURNER_RODS_2;

		blockElement(blaze).atLocal(1, 1.8, 1)
			.rotate(0, 180, 0)
			.scale(scale)
			.render(matrixStack);
		blockElement(rods2).atLocal(1, 1.7 + offset, 1)
			.rotate(0, 180, 0)
			.scale(scale)
			.render(matrixStack);

		matrixStack.scale(scale, -scale, scale);
		matrixStack.translate(0, -1.8, 0);

		SpriteShiftEntry spriteShift =
			heatLevel == HeatLevel.SEETHING ? AllSpriteShifts.SUPER_BURNER_FLAME : AllSpriteShifts.BURNER_FLAME;

		float spriteWidth = spriteShift.getTarget()
			.getU1()
			- spriteShift.getTarget()
				.getU0();

		float spriteHeight = spriteShift.getTarget()
			.getV1()
			- spriteShift.getTarget()
				.getV0();

		float time = AnimationTickHolder.getRenderTime(Minecraft.getInstance().level);
		float speed = 1 / 32f + 1 / 64f * heatLevel.ordinal();

		double vScroll = speed * time;
		vScroll = vScroll - Math.floor(vScroll);
		vScroll = vScroll * spriteHeight / 2;

		double uScroll = speed * time / 2;
		uScroll = uScroll - Math.floor(uScroll);
		uScroll = uScroll * spriteWidth / 2;

		Minecraft mc = Minecraft.getInstance();
		MultiBufferSource.BufferSource buffer = mc.renderBuffers()
			.bufferSource();
		VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());
		CachedBufferer.partial(AllBlockPartials.BLAZE_BURNER_FLAME, Blocks.AIR.defaultBlockState())
			.shiftUVScrolling(spriteShift, (float) uScroll, (float) vScroll)
			.light(LightTexture.FULL_BRIGHT)
			.renderInto(matrixStack, vb);
		matrixStack.popPose();
	}

}
