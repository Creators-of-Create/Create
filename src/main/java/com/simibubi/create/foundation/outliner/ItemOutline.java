package com.simibubi.create.foundation.outliner;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ItemOutline extends Outline {

	protected Vec3 pos;
	protected ItemStack stack;

	public ItemOutline(Vec3 pos, ItemStack stack) {
		this.pos = pos;
		this.stack = stack;
	}

	@Override
	public void render(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera, float pt) {
		Minecraft mc = Minecraft.getInstance();
		ms.pushPose();

		TransformStack.cast(ms)
			.translate(pos.x - camera.x, pos.y - camera.y, pos.z - camera.z)
			.scale(params.alpha);

		mc.getItemRenderer().render(stack, ItemTransforms.TransformType.FIXED, false, ms,
				buffer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
				mc.getItemRenderer().getModel(stack, null, null, 0));

		ms.popPose();
	}
}
