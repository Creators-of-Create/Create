package com.simibubi.create.modules.contraptions.relays;

import com.simibubi.create.modules.contraptions.base.IRotate;
import com.simibubi.create.modules.contraptions.base.RotatedPillarKineticBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.PushReaction;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class GearboxBlock extends RotatedPillarKineticBlock {

	public GearboxBlock() {
		super(Properties.from(Blocks.ANDESITE));
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new GearboxTileEntity();
	}

	@Override
	public PushReaction getPushReaction(BlockState state) {
		return PushReaction.PUSH_ONLY;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState placedOnState = context.getWorld()
				.getBlockState(context.getPos().offset(context.getFace().getOpposite()));
		Block placedOn = placedOnState.getBlock();

		if (!(placedOn instanceof IRotate) || !((IRotate) placedOn).hasShaftTowards(context.getWorld(),
				context.getPos(), placedOnState, context.getFace()))
			return getDefaultState().with(AXIS, context.getFace().getAxis());

		Axis badAxis = context.getFace().getAxis();
		Axis otherBadAxis = null;

		for (Direction side : Direction.values()) {
			if (side.getAxis() == badAxis)
				continue;

			BlockState blockState = context.getWorld().getBlockState(context.getPos().offset(side));
			if (blockState.getBlock() instanceof IRotate) {
				if (((IRotate) blockState.getBlock()).hasShaftTowards(context.getWorld(), context.getPos().offset(side),
						blockState, side.getOpposite()))
					if (otherBadAxis != null && otherBadAxis != side.getAxis()) {
						otherBadAxis = null;
						break;
					} else {
						otherBadAxis = side.getAxis();
					}
			}
		}

		boolean skipped = false;
		for (Axis axis : Axis.values()) {
			if (axis == badAxis)
				continue;
			if (!skipped && context.isPlacerSneaking() && (otherBadAxis == null || badAxis == null)) {
				skipped = true;
				continue;
			}
			if (axis == otherBadAxis)
				continue;
			return getDefaultState().with(AXIS, axis);
		}

		return super.getStateForPlacement(context);
	}

	// IRotate:

	@Override
	public boolean hasShaftTowards(World world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() != state.get(AXIS);
	}

	@Override
	protected boolean hasStaticPart() {
		return true;
	}

}
