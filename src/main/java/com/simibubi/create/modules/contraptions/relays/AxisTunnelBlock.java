package com.simibubi.create.modules.contraptions.relays;

import com.simibubi.create.modules.contraptions.base.RotatedPillarKineticBlock;

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

public class AxisTunnelBlock extends RotatedPillarKineticBlock {

	public AxisTunnelBlock() {
		super(Properties.from(Blocks.ANDESITE));
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new AxisTunnelTileEntity();
	}

	@Override
	protected boolean hasStaticPart() {
		return true;
	}
	
	@Override
	public PushReaction getPushReaction(BlockState state) {
		return PushReaction.PUSH_ONLY;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		if (context.isPlacerSneaking())
			return super.getStateForPlacement(context);
		return this.getDefaultState().with(AXIS, context.getNearestLookingDirection().getAxis());
	}

	@Override
	public boolean isAxisTowards(World world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == state.get(AXIS);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(AXIS);
	}

}
