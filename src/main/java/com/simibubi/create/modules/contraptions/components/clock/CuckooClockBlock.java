package com.simibubi.create.modules.contraptions.components.clock;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.modules.contraptions.base.HorizontalKineticBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;

public class CuckooClockBlock extends HorizontalKineticBlock {

	private boolean mysterious;

	public CuckooClockBlock(boolean mysterious) {
		super(Properties.from(Blocks.SPRUCE_LOG));
		this.mysterious = mysterious;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new CuckooClockTileEntity();
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		if (!mysterious)
			super.fillItemGroup(group, items);
	}
	
	@Override
	public String getTranslationKey() {
		if (this == AllBlocks.MYSTERIOUS_CUCKOO_CLOCK.get())
			return AllBlocks.CUCKOO_CLOCK.get().getTranslationKey();
		return super.getTranslationKey();
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction preferred = getPreferredHorizontalFacing(context);
		if (preferred != null)
			return getDefaultState().with(HORIZONTAL_FACING, preferred.getOpposite());
		return this.getDefaultState().with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
	}

	@Override
	protected boolean hasStaticPart() {
		return true;
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
