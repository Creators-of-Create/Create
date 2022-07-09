package com.simibubi.create.content.logistics.block.diodes;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class BrassDiodeBlock extends AbstractDiodeBlock implements ITE<BrassDiodeTileEntity> {

	public static final BooleanProperty POWERING = BooleanProperty.create("powering");
	public static final BooleanProperty INVERTED = BooleanProperty.create("inverted");

	public BrassDiodeBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(POWERED, false)
			.setValue(POWERING, false)
			.setValue(INVERTED, false));
	}

	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player player, InteractionHand pHand,
		BlockHitResult pHit) {
		if (!player.mayBuild())
			return InteractionResult.PASS;
		if (player.isShiftKeyDown())
			return InteractionResult.PASS;
		if (AllItems.WRENCH.isIn(player.getItemInHand(pHand)))
			return InteractionResult.PASS;
		if (pLevel.isClientSide)
			return InteractionResult.SUCCESS;
		pLevel.setBlock(pPos, pState.cycle(INVERTED), 3);
		float f = !pState.getValue(INVERTED) ? 0.6F : 0.5F;
		pLevel.playSound(null, pPos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, f);
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(POWERED, POWERING, FACING, INVERTED);
		super.createBlockStateDefinition(builder);
	}

	@Override
	protected int getOutputSignal(BlockGetter worldIn, BlockPos pos, BlockState state) {
		return state.getValue(POWERING) ^ state.getValue(INVERTED) ? 15 : 0;
	}

	@Override
	public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
		return blockState.getValue(FACING) == side ? this.getOutputSignal(blockAccess, pos, blockState) : 0;
	}

	@Override
	protected int getDelay(BlockState p_196346_1_) {
		return 2;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
		if (side == null)
			return false;
		return side.getAxis() == state.getValue(FACING)
			.getAxis();
	}

	@Override
	public Class<BrassDiodeTileEntity> getTileEntityClass() {
		return BrassDiodeTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends BrassDiodeTileEntity> getTileEntityType() {
		return AllBlocks.PULSE_EXTENDER.is(this) ? AllTileEntities.PULSE_EXTENDER.get()
			: AllTileEntities.PULSE_REPEATER.get();
	}

}
