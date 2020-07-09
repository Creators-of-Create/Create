package com.simibubi.create.content.logistics.block.mechanicalArm;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.SuperByteBuffer;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

public class ArmRenderer extends KineticTileEntityRenderer {

	public ArmRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float pt, MatrixStack ms, IRenderTypeBuffer buffer, int light,
		int overlay) {
		super.renderSafe(te, pt, ms, buffer, light, overlay);
		ArmTileEntity arm = (ArmTileEntity) te;
		IVertexBuilder builder = buffer.getBuffer(RenderType.getSolid());
		BlockState blockState = te.getBlockState();
		MatrixStacker msr = MatrixStacker.of(ms);
		int color = 0xFFFFFF;

		ms.push();

		SuperByteBuffer base = AllBlockPartials.ARM_BASE.renderOn(blockState).light(light);
		SuperByteBuffer lowerBody = AllBlockPartials.ARM_LOWER_BODY.renderOn(blockState).light(light);
		SuperByteBuffer upperBody = AllBlockPartials.ARM_UPPER_BODY.renderOn(blockState).light(light);
		SuperByteBuffer head = AllBlockPartials.ARM_HEAD.renderOn(blockState).light(light);
		SuperByteBuffer claw = AllBlockPartials.ARM_CLAW_BASE.renderOn(blockState).light(light);
		SuperByteBuffer clawGrip = AllBlockPartials.ARM_CLAW_GRIP.renderOn(blockState).light(light);

		msr.centre();

		ms.translate(0, 4 / 16d, 0);
		msr.rotateY(arm.baseAngle.get(pt));
		base.renderInto(ms, builder);

		ms.translate(0, 1 / 16d, -2 / 16d);
		msr.rotateX(arm.lowerArmAngle.get(pt) - 135);
		ms.translate(0, -1 / 16d, 0);
		lowerBody.color(color)
			.renderInto(ms, builder);

		ms.translate(0, 12 / 16d, 12 / 16d);
		msr.rotateX(arm.upperArmAngle.get(pt) - 90);
		upperBody.color(color)
			.renderInto(ms, builder);

		ms.translate(0, 11 / 16d, -11 / 16d);
		msr.rotateX(arm.headAngle.get(pt));
		head.renderInto(ms, builder);

		ms.translate(0, 0, -4 / 16d);
		claw.renderInto(ms, builder);
		ItemStack item = arm.heldItem;
		ItemRenderer itemRenderer = Minecraft.getInstance()
			.getItemRenderer();
		boolean hasItem = !item.isEmpty();
		boolean isBlockItem = hasItem && (item.getItem() instanceof BlockItem)
			&& itemRenderer.getItemModelWithOverrides(item, Minecraft.getInstance().world, null)
				.isGui3d();
		
		for (int flip : Iterate.positiveAndNegative) {
			ms.push();
			ms.translate(0, flip * 3 / 16d, -1 / 16d);
			msr.rotateX(flip * (hasItem ? isBlockItem ? 0 : -35 : 0));
			clawGrip.renderInto(ms, builder);
			ms.pop();
		}

		if (hasItem) {
			float itemScale = isBlockItem ? .5f : .625f;
			msr.rotateX(90);
			ms.translate(0, -4 / 16f, 0);
			ms.scale(itemScale, itemScale, itemScale);
			itemRenderer
				.renderItem(item, TransformType.FIXED, light, overlay, ms, buffer);
		}

		ms.pop();
	}

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		return AllBlockPartials.ARM_COG.renderOn(te.getBlockState());
	}

}
