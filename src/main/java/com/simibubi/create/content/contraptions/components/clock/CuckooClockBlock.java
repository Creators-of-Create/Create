package com.simibubi.create.content.contraptions.components.clock;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CuckooClockBlock extends HorizontalKineticBlock implements IBE<CuckooClockBlockEntity> {

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
	public VoxelShape getShape(BlockState p_220053_1_, BlockGetter p_220053_2_, BlockPos p_220053_3_,
		CollisionContext p_220053_4_) {
		return AllShapes.CUCKOO_CLOCK;
	}

	@Override
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
		if (!mysterious)
			super.fillItemCategory(group, items);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction preferred = getPreferredHorizontalFacing(context);
		if (preferred != null)
			return defaultBlockState().setValue(HORIZONTAL_FACING, preferred.getOpposite());
		return this.defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return face == state.getValue(HORIZONTAL_FACING).getOpposite();
	}

	public static boolean containsSurprise(BlockState state) {
		Block block = state.getBlock();
		return block instanceof CuckooClockBlock && ((CuckooClockBlock) block).mysterious;
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(HORIZONTAL_FACING).getAxis();
	}
	
	@Override
	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
		return false;
	}

	@Override
	public Class<CuckooClockBlockEntity> getBlockEntityClass() {
		return CuckooClockBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends CuckooClockBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.CUCKOO_CLOCK.get();
	}

}
