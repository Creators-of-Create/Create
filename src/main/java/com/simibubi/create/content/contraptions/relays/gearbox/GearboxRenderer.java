package com.simibubi.create.content.contraptions.relays.gearbox;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.instancing.InstanceContext;
import com.simibubi.create.foundation.render.instancing.InstancedModel;
import com.simibubi.create.foundation.render.instancing.RotatingData;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Pair;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

import java.util.List;

public class GearboxRenderer extends KineticTileEntityRenderer {

	public GearboxRenderer(TileEntityRendererDispatcher dispatcher) {
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

		for (Pair<Direction, InstancedModel<RotatingData>> shaft : getBuffers(ctx)) {
			shaft.getSecond().setupInstance(data -> {
				float speed = te.getSpeed();
				Direction direction = shaft.getFirst();
				Axis axis = direction.getAxis();

				if (te.getSpeed() != 0 && te.hasSource()) {
					BlockPos source = te.source.subtract(te.getPos());
					Direction sourceFacing = Direction.getFacingFromVector(source.getX(), source.getY(), source.getZ());
					if (sourceFacing.getAxis() == axis)
						speed *= sourceFacing == direction ? 1 : -1;
					else if (sourceFacing.getAxisDirection() == direction.getAxisDirection())
						speed *= -1;
				}

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
		getBuffers(ctx).stream().map(Pair::getSecond).forEach(InstancedModel::clearInstanceData);
	}

	private List<Pair<Direction, InstancedModel<RotatingData>>> getBuffers(InstanceContext<KineticTileEntity> ctx) {
		KineticTileEntity te = ctx.te;
		final Axis boxAxis = te.getBlockState().get(BlockStateProperties.AXIS);

		List<Pair<Direction, InstancedModel<RotatingData>>> buffers = Lists.newArrayListWithCapacity(4);

		for (Direction direction : Iterate.directions) {
			final Axis axis = direction.getAxis();
			if (boxAxis == axis)
				continue;

			InstancedModel<RotatingData> buffer = AllBlockPartials.SHAFT_HALF.renderOnDirectionalSouthRotating(ctx, te.getBlockState(), direction);
			Pair<Direction, InstancedModel<RotatingData>> pair = Pair.of(direction, buffer);

			buffers.add(pair);
		}

		return buffers;
	}
}
