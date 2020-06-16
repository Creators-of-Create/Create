package com.simibubi.create.content.logistics.block.mechanicalArm;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.SuperByteBuffer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.MathHelper;

public class ArmRenderer extends KineticTileEntityRenderer {

	public ArmRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);
		IVertexBuilder builder = buffer.getBuffer(RenderType.getSolid());
		BlockState blockState = te.getBlockState();
		MatrixStacker msr = MatrixStacker.of(ms);
		
		float angle = 0;
		float clawAngle = -25;
		float otherAngle = 0;
		int color = 0xFFFFFF;
		
		boolean rave = te instanceof ArmTileEntity && ((ArmTileEntity) te).debugRave;
		if (rave) {
			clawAngle = angle = MathHelper.lerp((MathHelper.sin(AnimationTickHolder.getRenderTick() / 2) + 1) / 2, -45, 15);
			otherAngle = MathHelper.lerp((MathHelper.sin(AnimationTickHolder.getRenderTick() / 4) + 1) / 2, -95, 95);
			color = ColorHelper.rainbowColor(AnimationTickHolder.ticks * 100);
		}
		
		ms.push();

		SuperByteBuffer base = AllBlockPartials.ARM_BASE.renderOn(blockState);
		SuperByteBuffer lowerBody = AllBlockPartials.ARM_LOWER_BODY.renderOn(blockState);
		SuperByteBuffer upperBody = AllBlockPartials.ARM_UPPER_BODY.renderOn(blockState);
		SuperByteBuffer head = AllBlockPartials.ARM_HEAD.renderOn(blockState);
		SuperByteBuffer claw = AllBlockPartials.ARM_CLAW_BASE.renderOn(blockState);
		SuperByteBuffer clawGrip = AllBlockPartials.ARM_CLAW_GRIP.renderOn(blockState);

		msr.centre();

		ms.translate(0, 4 / 16d, 0);
		msr.rotateY(rave ? AnimationTickHolder.getRenderTick() * 10 : 0);
		base.renderInto(ms, builder);

		ms.translate(0, 1 / 16d, -2 / 16d);
		msr.rotateX(angle);
		ms.translate(0, -1 / 16d, 0);
		lowerBody.color(color).renderInto(ms, builder);
		
		ms.translate(0, 12 / 16d, 12 / 16d);
		msr.rotateX(-otherAngle / 2f);
		upperBody.color(color).renderInto(ms, builder);
		
		ms.translate(0, 11 / 16d, -11 / 16d);
		msr.rotateX(-angle);
		head.renderInto(ms, builder);
		
		ms.translate(0, 0, -4 / 16d);
		claw.renderInto(ms, builder);
		
		for (int flip : Iterate.positiveAndNegative) {
			ms.push();
			ms.translate(0, flip * 3 / 16d, -1 / 16d);
			msr.rotateX(flip * clawAngle);
			clawGrip.renderInto(ms, builder);
			ms.pop();
		}

		ms.pop();
	}

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		return AllBlockPartials.ARM_COG.renderOn(te.getBlockState());
	}

}
