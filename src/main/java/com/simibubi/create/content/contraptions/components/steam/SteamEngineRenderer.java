package com.simibubi.create.content.contraptions.components.steam;

import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class SteamEngineRenderer extends SafeTileEntityRenderer<SteamEngineTileEntity> {

	public SteamEngineRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(SteamEngineTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		VertexConsumer vb = buffer.getBuffer(RenderType.solid());

		BlockState blockState = te.getBlockState();
		Direction facing = SteamEngineBlock.getFacing(blockState);
		Axis facingAxis = facing.getAxis();
		Axis axis = Axis.Y;

		PoweredShaftTileEntity shaft = te.getShaft();
		if (shaft != null)
			axis = KineticTileEntityRenderer.getRotationAxisOf(shaft);

		Float angle = te.getTargetAngle();
		if (angle == null)
			return;

		float sine = Mth.sin(angle);
		float sine2 = Mth.sin(angle - Mth.HALF_PI);
		float piston = ((1 - sine) / 4) * 24 / 16f;
		boolean roll90 = facingAxis.isHorizontal() && axis == Axis.Y || facingAxis.isVertical() && axis == Axis.Z;

		transformed(AllBlockPartials.ENGINE_PISTON, blockState, facing).rotateY(roll90 ? -90 : 0)
			.unCentre()
			.light(light)
			.translate(0, piston, 0)
			.renderInto(ms, vb);

		transformed(AllBlockPartials.ENGINE_LINKAGE, blockState, facing).rotateY(roll90 ? -90 : 0)
			.translate(0, 1, 0)
			.unCentre()
			.translate(0, piston, 0)
			.translate(0, 4 / 16f, 8 / 16f)
			.rotateX(sine2 * 23f)
			.translate(0, -4 / 16f, -8 / 16f)
			.light(light)
			.renderInto(ms, vb);

		transformed(AllBlockPartials.ENGINE_CONNECTOR, blockState, facing).rotateY(roll90 ? -90 : 0)
			.unCentre()
			.light(light)
			.translate(0, 2, 0)
			.centre()
			.rotateXRadians(-angle + Mth.HALF_PI)
			.unCentre()
			.renderInto(ms, vb);
	}

	private SuperByteBuffer transformed(PartialModel model, BlockState blockState, Direction facing) {
		return CachedBufferer.partial(model, blockState)
			.centre()
			.rotateY(AngleHelper.horizontalAngle(facing))
			.rotateX(AngleHelper.verticalAngle(facing) + 90);
	}
	
	@Override
	public int getViewDistance() {
		return 128;
	}

}
