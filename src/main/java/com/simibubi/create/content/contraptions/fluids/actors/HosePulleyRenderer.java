package com.simibubi.create.content.contraptions.fluids.actors;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.SuperByteBuffer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HosePulleyRenderer extends KineticTileEntityRenderer {

	public HosePulleyRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	public boolean isGlobalRenderer(KineticTileEntity p_188185_1_) {
		return true;
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);
		HosePulleyTileEntity pulley = (HosePulleyTileEntity) te;
		float offset = pulley.getInterpolatedOffset(partialTicks);

		Axis rotationAxis = ((IRotate) te.getBlockState()
			.getBlock()).getRotationAxis(te.getBlockState());
		kineticRotationTransform(getRotatedCoil(te), te, rotationAxis, AngleHelper.rad(offset * 180), light).renderInto(ms,
			buffer.getBuffer(RenderType.getSolid()));

		World world = te.getWorld();
		BlockState blockState = te.getBlockState();
		BlockPos pos = te.getPos();

		SuperByteBuffer halfMagnet = AllBlockPartials.HOSE_HALF_MAGNET.renderOn(blockState);
		SuperByteBuffer halfRope = AllBlockPartials.HOSE_HALF.renderOn(blockState);
		SuperByteBuffer magnet = AllBlockPartials.HOSE_MAGNET.renderOn(blockState);
		SuperByteBuffer rope = AllBlockPartials.HOSE.renderOn(blockState);

		PulleyRenderer.renderPulleyRope(ms, buffer, world, pos, halfMagnet, halfRope, magnet, rope, true, offset);
	}

	@Override
	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return shaft(te.getBlockState()
			.get(HosePulleyBlock.HORIZONTAL_FACING)
			.rotateY()
			.getAxis());
	}

	protected SuperByteBuffer getRotatedCoil(KineticTileEntity te) {
		BlockState blockState = te.getBlockState();
		return AllBlockPartials.HOSE_COIL.renderOnDirectionalSouth(blockState,
			blockState.get(HosePulleyBlock.HORIZONTAL_FACING)
				.rotateY());
	}

}
