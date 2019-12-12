package com.simibubi.create.modules.contraptions.relays.encased;

import com.simibubi.create.modules.contraptions.RotationPropagator;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.relays.gearbox.GearshiftTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class GearshiftBlock extends EncasedShaftBlock {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public GearshiftBlock() {
		super();
		setDefaultState(getDefaultState().with(POWERED, false));
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new GearshiftTileEntity();
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(POWERED);
		super.fillStateContainer(builder);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return super.getStateForPlacement(context).with(POWERED,
				Boolean.valueOf(context.getWorld().isBlockPowered(context.getPos())));
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (worldIn.isRemote)
			return;

		boolean previouslyPowered = state.get(POWERED);
		if (previouslyPowered != worldIn.isBlockPowered(pos)) {
			worldIn.setBlockState(pos, state.cycle(POWERED), 2 | 16);
			RotationPropagator.handleRemoved(worldIn, pos, (KineticTileEntity) worldIn.getTileEntity(pos));
		}
	}

	public boolean hasShaftTowards(World world, BlockPos pos, BlockState state, Direction face) {
		return super.hasShaftTowards(world, pos, state, face);
	}

}
