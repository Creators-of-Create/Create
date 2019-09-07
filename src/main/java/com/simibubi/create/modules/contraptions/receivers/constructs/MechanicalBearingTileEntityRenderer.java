package com.simibubi.create.modules.contraptions.receivers.constructs;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.animation.Animation;

public class MechanicalBearingTileEntityRenderer extends KineticTileEntityRenderer {

	@Override
	public void renderTileEntityFast(KineticTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		final Direction facing = te.getBlockState().get(BlockStateProperties.FACING);
		final BlockPos pos = te.getPos();
		float time = Animation.getWorldTime(Minecraft.getInstance().world, partialTicks);
		BlockState shaftState = AllBlocks.SHAFT_HALF.get().getDefaultState().with(BlockStateProperties.FACING,
				facing.getOpposite());
		BlockState capState = AllBlocks.MECHANICAL_BEARING_TOP.get().getDefaultState().with(BlockStateProperties.FACING,
				facing);

		cacheIfMissing(shaftState, BlockModelSpinner::new);
		cacheIfMissing(capState, BlockModelSpinner::new);

		float offset = getRotationOffsetForPosition(te, pos, facing.getAxis());
		float angle = (time * te.getSpeed()) % 360;

		angle += offset;
		angle = angle / 180f * (float) Math.PI;

		renderFromCache(buffer, shaftState, (float) x, (float) y, (float) z, pos, facing.getAxis(), angle);
		renderFromCache(buffer, capState, (float) x, (float) y, (float) z, pos, facing.getAxis(), angle);
	}

}
