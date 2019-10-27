package com.simibubi.create.modules.contraptions.receivers;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

import java.nio.ByteBuffer;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.modules.contraptions.base.IRotate;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.modules.contraptions.receivers.constructs.IHaveMovementBehavior.MovementContext;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.animation.Animation;

public class DrillTileEntityRenderer extends KineticTileEntityRenderer {

	@Override
	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return getRenderedBlockState(te.getBlockState());
	}

	private static BlockState getRenderedBlockState(BlockState state) {
		return AllBlocks.DRILL_HEAD.get().getDefaultState().with(FACING, state.get(FACING));
	}

	public static ByteBuffer renderInConstruct(MovementContext context) {
		World world = context.world;
		BlockState state = context.state;
		BlockPos pos = context.currentGridPos;

		final BlockState renderedState = getRenderedBlockState(state);
		cacheIfMissing(renderedState, world, BlockModelSpinner::new);

		float speed = (float) (context.getMovementDirection() == state.get(FACING) ? context.getAnimationSpeed() : 0);
		Axis axis = ((IRotate) state.getBlock()).getRotationAxis(state);
		float time = Animation.getWorldTime(Minecraft.getInstance().world,
				Minecraft.getInstance().getRenderPartialTicks());
		float angle = (float) (((time * speed) % 360) / 180 * (float) Math.PI);
		return ((BlockModelSpinner) getBuffer(renderedState)).getTransformed(0, 0, 0, angle, axis,
				world.getCombinedLight(pos, 0));
	}

}