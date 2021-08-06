package com.simibubi.create.content.logistics.item;

import java.util.Vector;

import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.item.LinkedControllerClientHandler.Mode;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;

public class LinkedControllerItemRenderer extends CustomRenderedItemModelRenderer<LinkedControllerModel> {

	static LerpedFloat equipProgress;
	static Vector<LerpedFloat> buttons;

	static {
		equipProgress = LerpedFloat.linear()
			.startWithValue(0);
		buttons = new Vector<>(6);
		for (int i = 0; i < 6; i++)
			buttons.add(LerpedFloat.linear()
				.startWithValue(0));
	}

	static void tick() {
		boolean active = LinkedControllerClientHandler.MODE != Mode.IDLE;
		equipProgress.chase(active ? 1 : 0, .2f, Chaser.EXP);
		equipProgress.tickChaser();

		if (!active)
			return;

		for (int i = 0; i < buttons.size(); i++) {
			LerpedFloat lerpedFloat = buttons.get(i);
			lerpedFloat.chase(LinkedControllerClientHandler.currentlyPressed.contains(i) ? 1 : 0, .4f, Chaser.EXP);
			lerpedFloat.tickChaser();
		}
	}

	static void resetButtons() {
		for (int i = 0; i < buttons.size(); i++) {
			buttons.get(i).startWithValue(0);
		}
	}

	@Override
	protected void render(ItemStack stack, LinkedControllerModel model, PartialItemModelRenderer renderer,
		ItemCameraTransforms.TransformType transformType, MatrixStack ms, IRenderTypeBuffer buffer, int light,
		int overlay) {
		renderNormal(stack, model, renderer, transformType, ms, light);
	}

	protected static void renderNormal(ItemStack stack, LinkedControllerModel model,
	  	PartialItemModelRenderer renderer, ItemCameraTransforms.TransformType transformType, MatrixStack ms,
  		int light) {
		render(stack, model, renderer, transformType, ms, light, RenderType.NORMAL, false, false);
	}

	public static void renderInLectern(ItemStack stack, LinkedControllerModel model,
	  	PartialItemModelRenderer renderer, ItemCameraTransforms.TransformType transformType, MatrixStack ms,
  		int light, boolean active, boolean renderDepression) {
		render(stack, model, renderer, transformType, ms, light, RenderType.LECTERN, active, renderDepression);
	}

	protected static void render(ItemStack stack, LinkedControllerModel model,
	  	PartialItemModelRenderer renderer, ItemCameraTransforms.TransformType transformType, MatrixStack ms,
  		int light, RenderType renderType, boolean active, boolean renderDepression) {
		float pt = AnimationTickHolder.getPartialTicks();
		MatrixTransformStack msr = MatrixTransformStack.of(ms);

		ms.pushPose();

		if (renderType == RenderType.NORMAL) {
			Minecraft mc = Minecraft.getInstance();
			boolean rightHanded = mc.options.mainHand == HandSide.RIGHT;
			TransformType mainHand =
					rightHanded ? TransformType.FIRST_PERSON_RIGHT_HAND : TransformType.FIRST_PERSON_LEFT_HAND;
			TransformType offHand =
					rightHanded ? TransformType.FIRST_PERSON_LEFT_HAND : TransformType.FIRST_PERSON_RIGHT_HAND;

			active = false;
			boolean noControllerInMain = !AllItems.LINKED_CONTROLLER.isIn(mc.player.getMainHandItem());

			if (transformType == mainHand || (transformType == offHand && noControllerInMain)) {
				float equip = equipProgress.getValue(pt);
				int handModifier = transformType == TransformType.FIRST_PERSON_LEFT_HAND ? -1 : 1;
				msr.translate(0, equip / 4, equip / 4 * handModifier);
				msr.rotateY(equip * -30 * handModifier);
				msr.rotateZ(equip * -30);
				active = true;
			}

			if (transformType == TransformType.GUI) {
				if (stack == mc.player.getMainHandItem())
					active = true;
				if (stack == mc.player.getOffhandItem() && noControllerInMain)
					active = true;
			}

			active &= LinkedControllerClientHandler.MODE != Mode.IDLE;

			renderDepression = true;
		}

		renderer.render(active ? model.getPartial("powered") : model.getOriginalModel(), light);

		if (!active) {
			ms.popPose();
			return;
		}

		IBakedModel button = model.getPartial("button");
		float s = 1 / 16f;
		float b = s * -.75f;
		int index = 0;

		if (renderType == RenderType.NORMAL) {
			if (LinkedControllerClientHandler.MODE == Mode.BIND) {
				int i = (int) MathHelper.lerp((MathHelper.sin(AnimationTickHolder.getRenderTime() / 4f) + 1) / 2, 5, 15);
				light = i << 20;
			}
		}

		ms.pushPose();
		msr.translate(2 * s, 0, 8 * s);
		renderButton(renderer, ms, light, pt, button, b, index++, renderDepression);
		msr.translate(4 * s, 0, 0);
		renderButton(renderer, ms, light, pt, button, b, index++, renderDepression);
		msr.translate(-2 * s, 0, 2 * s);
		renderButton(renderer, ms, light, pt, button, b, index++, renderDepression);
		msr.translate(0, 0, -4 * s);
		renderButton(renderer, ms, light, pt, button, b, index++, renderDepression);
		ms.popPose();

		msr.translate(3 * s, 0, 3 * s);
		renderButton(renderer, ms, light, pt, button, b, index++, renderDepression);
		msr.translate(2 * s, 0, 0);
		renderButton(renderer, ms, light, pt, button, b, index++, renderDepression);

		ms.popPose();
	}

	protected static void renderButton(PartialItemModelRenderer renderer, MatrixStack ms, int light, float pt, IBakedModel button,
		float b, int index, boolean renderDepression) {
		ms.pushPose();
		if (renderDepression) {
			float depression = b * buttons.get(index).getValue(pt);
			ms.translate(0, depression, 0);
		}
		renderer.renderSolid(button, light);
		ms.popPose();
	}

	protected enum RenderType {
		NORMAL, LECTERN;
	}

}
