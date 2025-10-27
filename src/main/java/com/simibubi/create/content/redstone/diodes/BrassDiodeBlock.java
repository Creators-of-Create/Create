package com.simibubi.create.content.redstone.diodes;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class BrassDiodeBlock extends AbstractDiodeBlock implements IBE<BrassDiodeBlockEntity> {

	public static final BooleanProperty POWERING = BooleanProperty.create("powering");
	public static final BooleanProperty INVERTED = BooleanProperty.create("inverted");

	public static final MapCodec<BrassDiodeBlock> CODEC = simpleCodec(BrassDiodeBlock::new);

	public BrassDiodeBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(POWERED, false)
			.setValue(POWERING, false)
			.setValue(INVERTED, false));
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		return toggle(level, pos, state, player, hand);
	}

	public ItemInteractionResult toggle(Level pLevel, BlockPos pPos, BlockState pState, Player player,
									InteractionHand pHand) {
		if (!player.mayBuild())
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if (player.isShiftKeyDown())
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if (AllItems.WRENCH.isIn(player.getItemInHand(pHand)))
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if (pLevel.isClientSide)
			return ItemInteractionResult.SUCCESS;
		pLevel.setBlock(pPos, pState.cycle(INVERTED), Block.UPDATE_ALL);
		float f = !pState.getValue(INVERTED) ? 0.6F : 0.5F;
		pLevel.playSound(null, pPos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, f);
		return ItemInteractionResult.SUCCESS;
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
	protected int getDelay(BlockState state) {
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
	public Class<BrassDiodeBlockEntity> getBlockEntityClass() {
		return BrassDiodeBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends BrassDiodeBlockEntity> getBlockEntityType() {
		return AllBlocks.PULSE_TIMER.is(this) ? AllBlockEntityTypes.PULSE_TIMER.get()
			: AllBlocks.PULSE_EXTENDER.is(this) ? AllBlockEntityTypes.PULSE_EXTENDER.get()
				: AllBlockEntityTypes.PULSE_REPEATER.get();
	}

	@Override
	protected @NotNull MapCodec<? extends DiodeBlock> codec() {
		return CODEC;
	}
}
