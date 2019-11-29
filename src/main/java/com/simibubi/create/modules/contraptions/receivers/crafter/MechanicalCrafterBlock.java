package com.simibubi.create.modules.contraptions.receivers.crafter;

import com.simibubi.create.foundation.block.IWithTileEntity;
import com.simibubi.create.foundation.block.RenderUtilityBlock;
import com.simibubi.create.modules.contraptions.base.DirectionalKineticBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class MechanicalCrafterBlock extends DirectionalKineticBlock
		implements IWithTileEntity<MechanicalCrafterTileEntity> {

	public static final DirectionProperty OUTPUT = BlockStateProperties.HORIZONTAL_FACING;

	public MechanicalCrafterBlock() {
		super(Properties.from(Blocks.GOLD_BLOCK));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder.add(OUTPUT));
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new MechanicalCrafterTileEntity();
	}

	@Override
	protected boolean hasStaticPart() {
		return true;
	}

	@Override
	public boolean hasCogsTowards(World world, BlockPos pos, BlockState state, Direction face) {
		return state.get(FACING).getAxis() != face.getAxis();
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(FACING).getAxis();
	}

	public static class Lid extends RenderUtilityBlock {
		@Override
		protected void fillStateContainer(Builder<Block, BlockState> builder) {
			super.fillStateContainer(builder.add(BlockStateProperties.FACING));
		}
	}

}
