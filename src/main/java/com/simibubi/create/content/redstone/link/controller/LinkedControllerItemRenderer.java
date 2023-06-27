package com.simibubi.create.content.redstone.link.controller;

import java.util.Vector;

import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerClientHandler.Mode;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class LinkedControllerItemRenderer extends CustomRenderedItemModelRenderer {

	protected static final PartialModel POWERED = new PartialModel(Create.asResource("item/linked_controller/powered"));
	protected static final PartialModel BUTTON = new PartialModel(Create.asResource("item/linked_controller/button"));

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
		if (Minecraft.getInstance()
			.isPaused())
			return;

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
	protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer,
		ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light,
		int overlay) {
		renderNormal(stack, model, renderer, transformType, ms, light);
	}

	protected static void renderNormal(ItemStack stack, CustomRenderedItemModel model,
	  	PartialItemModelRenderer renderer, ItemDisplayContext transformType, PoseStack ms,
  		int light) {
		render(stack, model, renderer, transformType, ms, light, RenderType.NORMAL, false, false);
	}

	public static void renderInLectern(ItemStack stack, CustomRenderedItemModel model,
	  	PartialItemModelRenderer renderer, ItemDisplayContext transformType, PoseStack ms,
  		int light, boolean active, boolean renderDepression) {
		render(stack, model, renderer, transformType, ms, light, RenderType.LECTERN, active, renderDepression);
	}

	protected static void render(ItemStack stack, CustomRenderedItemModel model,
	  	PartialItemModelRenderer renderer, ItemDisplayContext transformType, PoseStack ms,
  		int light, RenderType renderType, boolean active, boolean renderDepression) {
		float pt = AnimationTickHolder.getPartialTicks();
		TransformStack msr = TransformStack.cast(ms);

		ms.pushPose();

		if (renderType == RenderType.NORMAL) {
			Minecraft mc = Minecraft.getInstance();
			boolean rightHanded = mc.options.mainHand().get() == HumanoidArm.RIGHT;
			ItemDisplayContext mainHand =
					rightHanded ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
			ItemDisplayContext offHand =
					rightHanded ? ItemDisplayContext.FIRST_PERSON_LEFT_HAND : ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;

			active = false;
			boolean noControllerInMain = !AllItems.LINKED_CONTROLLER.isIn(mc.player.getMainHandItem());

			if (transformType == mainHand || (transformType == offHand && noControllerInMain)) {
				float equip = equipProgress.getValue(pt);
				int handModifier = transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? -1 : 1;
				msr.translate(0, equip / 4, equip / 4 * handModifier);
				msr.rotateY(equip * -30 * handModifier);
				msr.rotateZ(equip * -30);
				active = true;
			}

			if (transformType == ItemDisplayContext.GUI) {
				if (stack == mc.player.getMainHandItem())
					active = true;
				if (stack == mc.player.getOffhandItem() && noControllerInMain)
					active = true;
			}

			active &= LinkedControllerClientHandler.MODE != Mode.IDLE;

			renderDepression = true;
		}

		renderer.render(active ? POWERED.get() : model.getOriginalModel(), light);

		if (!active) {
			ms.popPose();
			return;
		}

		BakedModel button = BUTTON.get();
		float s = 1 / 16f;
		float b = s * -.75f;
		int index = 0;

		if (renderType == RenderType.NORMAL) {
			if (LinkedControllerClientHandler.MODE == Mode.BIND) {
				int i = (int) Mth.lerp((Mth.sin(AnimationTickHolder.getRenderTime() / 4f) + 1) / 2, 5, 15);
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

	protected static void renderButton(PartialItemModelRenderer renderer, PoseStack ms, int light, float pt, BakedModel button,
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
