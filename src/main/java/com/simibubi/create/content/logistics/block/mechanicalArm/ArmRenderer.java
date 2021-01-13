package com.simibubi.create.content.logistics.block.mechanicalArm;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmTileEntity.Phase;
import com.simibubi.create.foundation.utility.*;

import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.instancing.InstanceBuffer;
import com.simibubi.create.foundation.render.instancing.InstanceContext;
import com.simibubi.create.foundation.render.instancing.RotatingData;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

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

		float baseAngle = arm.baseAngle.get(pt);
		float lowerArmAngle = arm.lowerArmAngle.get(pt) - 135;
		float upperArmAngle = arm.upperArmAngle.get(pt) - 90;
		float headAngle = arm.headAngle.get(pt);
		
		boolean rave = te instanceof ArmTileEntity && ((ArmTileEntity) te).phase == Phase.DANCING;
		float renderTick = AnimationTickHolder.getRenderTick() + (te.hashCode() % 64);
		if (rave) {
			baseAngle = (renderTick * 10) % 360;
			lowerArmAngle = MathHelper.lerp((MathHelper.sin(renderTick / 4) + 1) / 2, -45, 15);
			upperArmAngle = MathHelper.lerp((MathHelper.sin(renderTick / 8) + 1) / 4, -45, 95);
			headAngle = -lowerArmAngle;
			color = ColorHelper.rainbowColor(AnimationTickHolder.ticks * 100);
		}
		
		ms.push();

		SuperByteBuffer base = AllBlockPartials.ARM_BASE.renderOn(blockState).light(light);
		SuperByteBuffer lowerBody = AllBlockPartials.ARM_LOWER_BODY.renderOn(blockState).light(light);
		SuperByteBuffer upperBody = AllBlockPartials.ARM_UPPER_BODY.renderOn(blockState).light(light);
		SuperByteBuffer head = AllBlockPartials.ARM_HEAD.renderOn(blockState).light(light);
		SuperByteBuffer claw = AllBlockPartials.ARM_CLAW_BASE.renderOn(blockState).light(light);
		SuperByteBuffer clawGrip = AllBlockPartials.ARM_CLAW_GRIP.renderOn(blockState);

		msr.centre();
		
		if (blockState.get(ArmBlock.CEILING))
			msr.rotateX(180);

		ms.translate(0, 4 / 16d, 0);
		msr.rotateY(baseAngle);
		base.renderInto(ms, builder);

		ms.translate(0, 1 / 16d, -2 / 16d);
		msr.rotateX(lowerArmAngle);
		ms.translate(0, -1 / 16d, 0);
		lowerBody.color(color)
			.renderInto(ms, builder);

		ms.translate(0, 12 / 16d, 12 / 16d);
		msr.rotateX(upperArmAngle);
		upperBody.color(color)
			.renderInto(ms, builder);

		ms.translate(0, 11 / 16d, -11 / 16d);
		msr.rotateX(headAngle);
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
			clawGrip.light(light).renderInto(ms, builder);
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
	protected InstanceBuffer<RotatingData> getRotatedModel(InstanceContext<? extends KineticTileEntity> ctx) {
		return AllBlockPartials.ARM_COG.renderOnRotating(ctx, ctx.te.getBlockState());
	}

}
