package com.simibubi.create.content.logistics.block.chute;

import com.simibubi.create.AllBlockEntityTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class SmartChuteBlock extends AbstractChuteBlock {

	public SmartChuteBlock(Properties p_i48440_1_) {
		super(p_i48440_1_);
		registerDefaultState(defaultBlockState().setValue(POWERED, true));
	}

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
		boolean isMoving) {
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
		if (worldIn.isClientSide)
			return;
		if (!worldIn.getBlockTicks()
			.willTickThisTick(pos, this))
			worldIn.scheduleTick(pos, this, 0);
	}

	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource r) {
		boolean previouslyPowered = state.getValue(POWERED);
		if (previouslyPowered != worldIn.hasNeighborSignal(pos))
			worldIn.setBlock(pos, state.cycle(POWERED), 2);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext p_196258_1_) {
		return super.getStateForPlacement(p_196258_1_).setValue(POWERED, p_196258_1_.getLevel()
			.hasNeighborSignal(p_196258_1_.getClickedPos()));
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		return true;
	}
	
	@Override
	public BlockEntityType<? extends ChuteBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.SMART_CHUTE.get();
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> p_206840_1_) {
		super.createBlockStateDefinition(p_206840_1_.add(POWERED));
	}

	@Override
	public BlockState updateChuteState(BlockState state, BlockState above, BlockGetter world, BlockPos pos) {
		return state;
	}

}
