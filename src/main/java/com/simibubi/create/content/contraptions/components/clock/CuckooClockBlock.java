package com.simibubi.create.content.contraptions.components.clock;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.HorizontalKineticBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;

public class CuckooClockBlock extends HorizontalKineticBlock {

	private boolean mysterious;

	public static CuckooClockBlock regular(Properties properties) {
		return new CuckooClockBlock(false, properties);
	}
	
	public static CuckooClockBlock mysterious(Properties properties) {
		return new CuckooClockBlock(true, properties);
	}
	
	protected CuckooClockBlock(boolean mysterious, Properties properties) {
		super(properties);
		this.mysterious = mysterious;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.CUCKOO_CLOCK.create();
	}
	
	@Override
	public VoxelShape getShape(BlockState p_220053_1_, IBlockReader p_220053_2_, BlockPos p_220053_3_,
		ISelectionContext p_220053_4_) {
		return AllShapes.CUCKOO_CLOCK;
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		if (!mysterious)
			super.fillItemGroup(group, items);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction preferred = getPreferredHorizontalFacing(context);
		if (preferred != null)
			return getDefaultState().with(HORIZONTAL_FACING, preferred.getOpposite());
		return this.getDefaultState().with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return face == state.get(HORIZONTAL_FACING).getOpposite();
	}

	public static boolean containsSurprise(BlockState state) {
		Block block = state.getBlock();
		return block instanceof CuckooClockBlock && ((CuckooClockBlock) block).mysterious;
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(HORIZONTAL_FACING).getAxis();
	}

}
