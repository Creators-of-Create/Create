package com.simibubi.create.content.contraptions.fluids.pipes;

import com.simibubi.create.AllTileEntities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class GlassFluidPipeBlock extends AxisPipeBlock {

	public static final BooleanProperty ALT = BooleanProperty.create("alt");

	public GlassFluidPipeBlock(Properties p_i48339_1_) {
		super(p_i48339_1_);
		setDefaultState(getDefaultState().with(ALT, false));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> p_206840_1_) {
		super.fillStateContainer(p_206840_1_.add(ALT));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.GLASS_FLUID_PIPE.create();
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		BlockState newState = state;
		World world = context.getWorld();
		BlockPos pos = context.getPos();
//		if (!state.get(ALT))
//			newState = state.with(ALT, true);
//		else
		newState = toRegularPipe(world, pos, state);
		world.setBlockState(pos, newState, 3);
		return ActionResultType.SUCCESS;
	}

}
