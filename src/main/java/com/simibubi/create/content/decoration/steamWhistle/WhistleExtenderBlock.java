package com.simibubi.create.content.decoration.steamWhistle;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.decoration.steamWhistle.WhistleBlock.WhistleSize;
import com.simibubi.create.content.equipment.wrench.IWrenchable;

import net.createmod.catnip.utility.lang.Lang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WhistleExtenderBlock extends Block implements IWrenchable {

	public static final EnumProperty<WhistleExtenderShape> SHAPE =
		EnumProperty.create("shape", WhistleExtenderShape.class);
	public static final EnumProperty<WhistleSize> SIZE = WhistleBlock.SIZE;

	public static enum WhistleExtenderShape implements StringRepresentable {
		SINGLE, DOUBLE, DOUBLE_CONNECTED;

		@Override
		public String getSerializedName() {
			return Lang.asId(name());
		}
	}

	public WhistleExtenderBlock(Properties p_49795_) {
		super(p_49795_);
		registerDefaultState(defaultBlockState().setValue(SHAPE, WhistleExtenderShape.SINGLE)
			.setValue(SIZE, WhistleSize.MEDIUM));
	}

	@Override
	public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();

		if (context.getClickLocation().y < context.getClickedPos()
			.getY() + .5f || state.getValue(SHAPE) == WhistleExtenderShape.SINGLE)
			return IWrenchable.super.onSneakWrenched(state, context);
		if (!(world instanceof ServerLevel))
			return InteractionResult.SUCCESS;
		world.setBlock(pos, state.setValue(SHAPE, WhistleExtenderShape.SINGLE), 3);
		playRemoveSound(world, pos);
		return InteractionResult.SUCCESS;
	}

	protected UseOnContext relocateContext(UseOnContext context, BlockPos target) {
		return new UseOnContext(context.getPlayer(), context.getHand(),
			new BlockHitResult(context.getClickLocation(), context.getClickedFace(), target, context.isInside()));
	}

	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand,
		BlockHitResult pHit) {
		if (pPlayer == null || !AllBlocks.STEAM_WHISTLE.isIn(pPlayer.getItemInHand(pHand)))
			return InteractionResult.PASS;
		Level level = pLevel;
		BlockPos findRoot = findRoot(level, pPos);
		BlockState blockState = level.getBlockState(findRoot);
		if (blockState.getBlock()instanceof WhistleBlock whistle)
			return whistle.use(blockState, pLevel, findRoot, pPlayer, pHand,
				new BlockHitResult(pHit.getLocation(), pHit.getDirection(), findRoot, pHit.isInside()));
		return InteractionResult.PASS;
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		Level level = context.getLevel();
		BlockPos findRoot = findRoot(level, context.getClickedPos());
		BlockState blockState = level.getBlockState(findRoot);
		if (blockState.getBlock()instanceof WhistleBlock whistle)
			return whistle.onWrenched(blockState, relocateContext(context, findRoot));
		return IWrenchable.super.onWrenched(state, context);
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos,
		Player player) {
		return AllBlocks.STEAM_WHISTLE.asStack();
	}

	public static BlockPos findRoot(LevelAccessor pLevel, BlockPos pPos) {
		BlockPos currentPos = pPos.below();
		while (true) {
			BlockState blockState = pLevel.getBlockState(currentPos);
			if (AllBlocks.STEAM_WHISTLE_EXTENSION.has(blockState)) {
				currentPos = currentPos.below();
				continue;
			}
			return currentPos;
		}
	}

	@Override
	public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
		BlockState below = pLevel.getBlockState(pPos.below());
		return below.is(this) && below.getValue(SHAPE) != WhistleExtenderShape.SINGLE
			|| AllBlocks.STEAM_WHISTLE.has(below);
	}

	public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel,
		BlockPos pCurrentPos, BlockPos pFacingPos) {
		if (pFacing.getAxis() != Axis.Y)
			return pState;

		if (pFacing == Direction.UP) {
			boolean connected = pState.getValue(SHAPE) == WhistleExtenderShape.DOUBLE_CONNECTED;
			boolean shouldConnect = pLevel.getBlockState(pCurrentPos.above())
				.is(this);
			if (!connected && shouldConnect)
				return pState.setValue(SHAPE, WhistleExtenderShape.DOUBLE_CONNECTED);
			if (connected && !shouldConnect)
				return pState.setValue(SHAPE, WhistleExtenderShape.DOUBLE);
			return pState;
		}

		return !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState()
			: pState.setValue(SIZE, pLevel.getBlockState(pCurrentPos.below())
				.getValue(SIZE));
	}

	@Override
	public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
		if (pOldState.getBlock() != this || pOldState.getValue(SHAPE) != pState.getValue(SHAPE))
			WhistleBlock.queuePitchUpdate(pLevel, findRoot(pLevel, pPos));
	}

	@Override
	public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
		if (pNewState.getBlock() != this)
			WhistleBlock.queuePitchUpdate(pLevel, findRoot(pLevel, pPos));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder.add(SHAPE, SIZE));
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		WhistleSize size = pState.getValue(SIZE);
		switch (pState.getValue(SHAPE)) {
		case DOUBLE:
			return size == WhistleSize.LARGE ? AllShapes.WHISTLE_EXTENDER_LARGE_DOUBLE
				: size == WhistleSize.MEDIUM ? AllShapes.WHISTLE_EXTENDER_MEDIUM_DOUBLE
					: AllShapes.WHISTLE_EXTENDER_SMALL_DOUBLE;
		case DOUBLE_CONNECTED:
			return size == WhistleSize.LARGE ? AllShapes.WHISTLE_EXTENDER_LARGE_DOUBLE_CONNECTED
				: size == WhistleSize.MEDIUM ? AllShapes.WHISTLE_EXTENDER_MEDIUM_DOUBLE_CONNECTED
					: AllShapes.WHISTLE_EXTENDER_SMALL_DOUBLE_CONNECTED;
		case SINGLE:
		default:
			return size == WhistleSize.LARGE ? AllShapes.WHISTLE_EXTENDER_LARGE
				: size == WhistleSize.MEDIUM ? AllShapes.WHISTLE_EXTENDER_MEDIUM : AllShapes.WHISTLE_EXTENDER_SMALL;
		}
	}

	@Override
	public boolean hidesNeighborFace(BlockGetter level, BlockPos pos, BlockState state, BlockState neighborState,
		Direction dir) {
		return AllBlocks.STEAM_WHISTLE.has(neighborState) && dir == Direction.DOWN;
	}

}
