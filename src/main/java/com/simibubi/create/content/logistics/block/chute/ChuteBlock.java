package com.simibubi.create.content.logistics.block.chute;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class ChuteBlock extends Block implements IWrenchable {

	public static final BooleanProperty WINDOW = BooleanProperty.create("window");
	
	public ChuteBlock(Properties p_i48440_1_) {
		super(p_i48440_1_);
		setDefaultState(getDefaultState().with(WINDOW, false));
	}
	
	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		if (!context.getWorld().isRemote)
			context.getWorld().setBlockState(context.getPos(), state.cycle(WINDOW));
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public VoxelShape getShape(BlockState p_220053_1_, IBlockReader p_220053_2_, BlockPos p_220053_3_,
		ISelectionContext p_220053_4_) {
		return AllShapes.CHUTE;
	}
	
	@Override
	protected void fillStateContainer(Builder<Block, BlockState> p_206840_1_) {
		super.fillStateContainer(p_206840_1_.add(WINDOW));
	}
	
	

}
