package com.simibubi.create.content.redstone.diodes;

import com.simibubi.create.AllItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class ToggleLatchBlock extends AbstractDiodeBlock {

	public static BooleanProperty POWERING = BooleanProperty.create("powering");

	public ToggleLatchBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(POWERING, false)
			.setValue(POWERED, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(POWERED, POWERING, FACING);
	}

	@Override
	public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
		return blockState.getValue(FACING) == side ? this.getOutputSignal(blockAccess, pos, blockState) : 0;
	}

	@Override
	protected int getDelay(BlockState state) {
		return 1;
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
		BlockHitResult hit) {
		if (!player.mayBuild())
			return InteractionResult.PASS;
		if (player.isShiftKeyDown())
			return InteractionResult.PASS;
		if (AllItems.WRENCH.isIn(player.getItemInHand(handIn)))
			return InteractionResult.PASS;
		return activated(worldIn, pos, state);
	}

	@Override
	protected int getOutputSignal(BlockGetter worldIn, BlockPos pos, BlockState state) {
		return state.getValue(POWERING) ? 15 : 0;
	}

	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
		boolean poweredPreviously = state.getValue(POWERED);
		super.tick(state, worldIn, pos, random);
		BlockState newState = worldIn.getBlockState(pos);
		if (newState.getValue(POWERED) && !poweredPreviously)
			worldIn.setBlock(pos, newState.cycle(POWERING), 2);
	}

	protected InteractionResult activated(Level worldIn, BlockPos pos, BlockState state) {
		if (!worldIn.isClientSide) {
			float f = !state.getValue(POWERING) ? 0.6F : 0.5F;
			worldIn.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, f);
			worldIn.setBlock(pos, state.cycle(POWERING), 2);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
		if (side == null)
			return false;
		return side.getAxis() == state.getValue(FACING)
			.getAxis();
	}

}
