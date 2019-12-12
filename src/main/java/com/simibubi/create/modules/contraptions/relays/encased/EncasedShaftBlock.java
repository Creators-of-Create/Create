package com.simibubi.create.modules.contraptions.relays.encased;

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

public class EncasedShaftBlock extends RotatedPillarKineticBlock {

	public EncasedShaftBlock() {
		super(Properties.from(Blocks.ANDESITE));
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new EncasedShaftTileEntity();
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
		Axis preferredAxis = getPreferredAxis(context);
		return this.getDefaultState().with(AXIS,
				preferredAxis == null ? context.getNearestLookingDirection().getAxis() : preferredAxis);
	}

	@Override
	public boolean hasShaftTowards(World world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == state.get(AXIS);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(AXIS);
	}

}
