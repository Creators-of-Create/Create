package com.simibubi.create.content.contraptions.relays.encased;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.instancing.InstanceBuffer;
import com.simibubi.create.foundation.render.instancing.InstanceContext;
import com.simibubi.create.foundation.render.instancing.RotatingData;
import com.simibubi.create.foundation.utility.Iterate;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

public class SplitShaftRenderer extends KineticTileEntityRenderer {

	public SplitShaftRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
			int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);
	}

	@Override
	public void addInstanceData(InstanceContext<KineticTileEntity> ctx) {
		KineticTileEntity te = ctx.te;
		Block block = te.getBlockState().getBlock();
		final Axis boxAxis = ((IRotate) block).getRotationAxis(te.getBlockState());
		final BlockPos pos = te.getPos();

		int blockLight;
		int skyLight;

		if (ctx.checkWorldLight()) {
			blockLight = te.getWorld().getLightLevel(LightType.BLOCK, te.getPos());
			skyLight = te.getWorld().getLightLevel(LightType.SKY, te.getPos());
		} else {
			blockLight = 0;
			skyLight = 0;
		}

		for (Direction direction : Iterate.directions) {
			Axis axis = direction.getAxis();
			if (boxAxis != axis)
				continue;

			InstanceBuffer<RotatingData> shaft = AllBlockPartials.SHAFT_HALF.renderOnDirectionalSouthRotating(ctx, te.getBlockState(), direction);

			shaft.setupInstance(data -> {
				float speed = te.getSpeed();

				float modifier = 1;

				if (te instanceof SplitShaftTileEntity)
					modifier = ((SplitShaftTileEntity) te).getRotationSpeedModifier(direction);

				speed *= modifier;

				data.setBlockLight(blockLight)
					.setSkyLight(skyLight)
					.setRotationalSpeed(speed)
					.setRotationOffset(getRotationOffsetForPosition(te, pos, axis))
					.setRotationAxis(Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, axis).getUnitVector())
					.setTileEntity(te);
			});
		}
	}

	@Override
	public void markForRebuild(InstanceContext<KineticTileEntity> ctx) {
		KineticTileEntity te = ctx.te;
		Block block = te.getBlockState().getBlock();
		final Axis boxAxis = ((IRotate) block).getRotationAxis(te.getBlockState());

		for (Direction direction : Iterate.directions) {
			Axis axis = direction.getAxis();
			if (boxAxis != axis) continue;

			InstanceBuffer<RotatingData> shaft = AllBlockPartials.SHAFT_HALF.renderOnDirectionalSouthRotating(ctx, te.getBlockState(), direction);
			shaft.clearInstanceData();
		}
	}
}
