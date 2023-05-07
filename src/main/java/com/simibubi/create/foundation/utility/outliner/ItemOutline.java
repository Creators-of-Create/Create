package com.simibubi.create.foundation.utility.outliner;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.foundation.render.RenderTypes;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class ItemOutline extends Outline {

	protected Vec3 pos;
	protected ItemStack stack;

	public ItemOutline(Vec3 pos, ItemStack stack) {
		this.pos = pos;
		this.stack = stack;
	}

	@Override
	public void render(PoseStack ms, SuperRenderTypeBuffer buffer, float pt) {
		Minecraft mc = Minecraft.getInstance();
		ms.pushPose();

		TransformStack.cast(ms)
			.translate(pos)
			.scale(params.alpha);

		mc.getItemRenderer().render(stack, ItemTransforms.TransformType.FIXED, false, ms,
				buffer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
				mc.getItemRenderer().getModel(stack, null, null, 0));

		ms.popPose();
	}
}
