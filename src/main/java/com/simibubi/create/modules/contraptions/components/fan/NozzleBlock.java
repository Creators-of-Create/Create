package com.simibubi.create.modules.contraptions.components.fan;

import com.simibubi.create.AllBlocksNew;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;
import com.simibubi.create.foundation.utility.AllShapes;
import com.simibubi.create.modules.contraptions.IWrenchable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class NozzleBlock extends ProperDirectionalBlock implements IWrenchable {

	public NozzleBlock() {
		super(Properties.from(AllBlocksNew.ENCASED_FAN.get()));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		return ActionResultType.FAIL;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new NozzleTileEntity();
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return getDefaultState().with(FACING, context.getFace());
	}
	
//	@Override // TODO 1.15 register layer
//	public BlockRenderLayer getRenderLayer() {
//		return BlockRenderLayer.CUTOUT_MIPPED;
//	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.NOZZLE.get(state.get(FACING));
	}
	
	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (worldIn.isRemote)
			return;

		if (fromPos.equals(pos.offset(state.get(FACING).getOpposite())))
			if (!isValidPosition(state, worldIn, pos)) {
				worldIn.destroyBlock(pos, true);
				return;
			}
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		Direction towardsFan = state.get(FACING).getOpposite();
		BlockState fanState = worldIn.getBlockState(pos.offset(towardsFan));
		return AllBlocksNew.ENCASED_FAN.has(fanState)
				&& fanState.get(EncasedFanBlock.FACING) == towardsFan.getOpposite();
	}

}
