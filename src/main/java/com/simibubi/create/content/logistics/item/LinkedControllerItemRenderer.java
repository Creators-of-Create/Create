package com.simibubi.create.content.logistics.item;

import java.util.Vector;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.item.LinkedControllerClientHandler.Mode;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;
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

	@Override
	protected void render(ItemStack stack, LinkedControllerModel model, PartialItemModelRenderer renderer,
		ItemCameraTransforms.TransformType transformType, MatrixStack ms, IRenderTypeBuffer buffer, int light,
		int overlay) {

		renderLinkedController(stack, model, renderer, transformType, ms, light, null, null);
	}

	public static void renderLinkedController(ItemStack stack, LinkedControllerModel model,
	  	PartialItemModelRenderer renderer, ItemCameraTransforms.TransformType transformType, MatrixStack ms,
  		int light, Boolean active, Boolean usedByMe) {

		float pt = AnimationTickHolder.getPartialTicks();
		MatrixStacker msr = MatrixStacker.of(ms);

		ms.push();

		Minecraft mc = Minecraft.getInstance();
		boolean rightHanded = mc.gameSettings.mainHand == HandSide.RIGHT;
		TransformType mainHand =
				rightHanded ? TransformType.FIRST_PERSON_RIGHT_HAND : TransformType.FIRST_PERSON_LEFT_HAND;
		TransformType offHand =
				rightHanded ? TransformType.FIRST_PERSON_LEFT_HAND : TransformType.FIRST_PERSON_RIGHT_HAND;

		if (active == null) {
			active = false;

			boolean noControllerInMain = !AllItems.LINKED_CONTROLLER.isIn(mc.player.getHeldItemMainhand());
			if (transformType == mainHand || (transformType == offHand && noControllerInMain)) {
				active = true;
			}

			if (transformType == TransformType.GUI) {
				if (stack == mc.player.getHeldItemMainhand())
					active = true;
				if (stack == mc.player.getHeldItemOffhand() && noControllerInMain)
					active = true;
			}

			active &= LinkedControllerClientHandler.MODE != Mode.IDLE && !LinkedControllerClientHandler.inLectern();
			usedByMe = active;

			if (active && (transformType == mainHand || transformType == offHand)) {
				float equip = equipProgress.getValue(pt);
				int handModifier = transformType == TransformType.FIRST_PERSON_LEFT_HAND ? -1 : 1;
				msr.translate(0, equip / 4, equip / 4 * handModifier);
				msr.rotateY(equip * -30 * handModifier);
				msr.rotateZ(equip * -30);
			}
		}

		renderer.render(active ? model.getPartial("powered") : model.getOriginalModel(), light);

		if (!usedByMe) {
			ms.pop();
			return;
		}

		IBakedModel button = model.getPartial("button");
		float s = 1 / 16f;
		float b = s * -.75f;
		int index = 0;

		if (LinkedControllerClientHandler.MODE == Mode.BIND) {
			int i = (int) MathHelper.lerp((MathHelper.sin(AnimationTickHolder.getRenderTime() / 4f) + 1) / 2, 5, 15);
			light = i << 20;
		}

		ms.push();
		msr.translate(2 * s, 0, 8 * s);
		button(renderer, ms, light, pt, button, b, index++);
		msr.translate(4 * s, 0, 0);
		button(renderer, ms, light, pt, button, b, index++);
		msr.translate(-2 * s, 0, 2 * s);
		button(renderer, ms, light, pt, button, b, index++);
		msr.translate(0, 0, -4 * s);
		button(renderer, ms, light, pt, button, b, index++);
		ms.pop();

		msr.translate(3 * s, 0, 3 * s);
		button(renderer, ms, light, pt, button, b, index++);
		msr.translate(2 * s, 0, 0);
		button(renderer, ms, light, pt, button, b, index++);

		ms.pop();
	}

	protected static void button(PartialItemModelRenderer renderer, MatrixStack ms, int light, float pt, IBakedModel button,
		float b, int index) {
		ms.push();
		ms.translate(0, b * buttons.get(index)
			.getValue(pt), 0);
		renderer.renderSolid(button, light);
		ms.pop();
	}

}
