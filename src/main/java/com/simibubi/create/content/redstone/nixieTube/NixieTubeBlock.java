package com.simibubi.create.content.redstone.nixieTube;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

import java.util.List;
import java.util.function.BiConsumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement.ItemUseType;
import com.simibubi.create.foundation.block.IBE;

import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class NixieTubeBlock extends DoubleFaceAttachedBlock
	implements IBE<NixieTubeBlockEntity>, IWrenchable, SimpleWaterloggedBlock, SpecialBlockItemRequirement {

	protected final DyeColor color;

	public NixieTubeBlock(Properties properties, DyeColor color) {
		super(properties);
		this.color = color;
		registerDefaultState(defaultBlockState().setValue(FACE, DoubleAttachFace.FLOOR)
			.setValue(WATERLOGGED, false));
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (player.isShiftKeyDown())
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		NixieTubeBlockEntity nixie = getBlockEntity(level, pos);

		if (nixie == null)
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		// Refuse interaction if nixie tube is in a computer-controlled row
		if (isInComputerControlledRow(level, pos))
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		if (stack.isEmpty()) {
			if (nixie.reactsToRedstone())
				return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
			nixie.clearCustomText();
			updateDisplayedRedstoneValue(state, level, pos);
			return ItemInteractionResult.SUCCESS;
		}

		boolean display =
			stack.getItem() == Items.NAME_TAG && stack.has(DataComponents.CUSTOM_NAME) || AllBlocks.CLIPBOARD.isIn(stack);
		DyeColor dye = DyeColor.getColor(stack);

		if (!display && dye == null)
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		Component component = stack.getOrDefault(DataComponents.CUSTOM_NAME, Component.empty());

		if (AllBlocks.CLIPBOARD.isIn(stack)) {
			List<ClipboardEntry> entries = ClipboardEntry.getLastViewedEntries(stack);
			if (!entries.isEmpty())
				component = entries.getFirst().text;
		}

		if (level.isClientSide)
			return ItemInteractionResult.SUCCESS;

		String tagUsed = Component.Serializer.toJson(component, level.registryAccess());
		// Skip computer check in this walk since it was already performed at the start.
		walkNixies(level, pos, true, (currentPos, rowPosition) -> {
			if (display)
				withBlockEntityDo(level, currentPos, be -> be.displayCustomText(tagUsed, rowPosition));
			if (dye != null)
				level.setBlockAndUpdate(currentPos, withColor(state, dye));
		});

		return ItemInteractionResult.SUCCESS;
	}

	public static Direction getLeftNixieDirection(@NotNull BlockState state) {
		Direction left = state.getValue(FACING).getOpposite();
		if (state.getValue(FACE) == DoubleAttachFace.WALL)
			left = Direction.UP;
		if (state.getValue(FACE) == DoubleAttachFace.WALL_REVERSED)
			left = Direction.DOWN;
		return left;
	}

	public static Direction getRightNixieDirection(@NotNull BlockState state) {
		return getLeftNixieDirection(state).getOpposite();
	}

	public static boolean isInComputerControlledRow(@NotNull LevelAccessor world, @NotNull BlockPos pos) {
		return Mods.COMPUTERCRAFT.isLoaded() && !walkNixies(world, pos, false, null);
	}

	/**
	 * Walk down a nixie tube row and execute a callback on each tube in said row.
	 * @param world The world the tubes are in.
	 * @param start Start position for the walk.
	 * @param allowComputerControlled Allow or disallow running callbacks if the row is computer-controlled.
	 * @param callback Callback to run for each tube.
	 * @return True if the row was walked, false if the walk was aborted because it is computer-controlled.
	 */
	public static boolean walkNixies(@NotNull LevelAccessor world, @NotNull BlockPos start,
									 boolean allowComputerControlled,
									 @Nullable BiConsumer<BlockPos, Integer> callback) {
		BlockState state = world.getBlockState(start);
		if (!(state.getBlock() instanceof NixieTubeBlock))
			return false;

		// If ComputerCraft is not installed, ignore allowComputerControlled since
		// nixies can't be computer-controlled
		if (!Mods.COMPUTERCRAFT.isLoaded())
			allowComputerControlled = true;

		BlockPos currentPos = start;
		Direction left = getLeftNixieDirection(state);
		Direction right = left.getOpposite();

		while (true) {
			BlockPos nextPos = currentPos.relative(left);
			if (!areNixieBlocksEqual(world.getBlockState(nextPos), state))
				break;
			// If computer-controlled nixie walking is disallowed, presence of any (same-color)
			// controlled nixies aborts the entire nixie walk.
			if (!allowComputerControlled && world.getBlockEntity(nextPos) instanceof NixieTubeBlockEntity ntbe &&
					ntbe.computerBehaviour.hasAttachedComputer()) {
				return false;
			}
			currentPos = nextPos;
		}

		// As explained above, a controlled nixie in the row aborts the walk if they are disallowed,
		// and that includes those down the chain too.
		if (!allowComputerControlled) {
			// Check the start block itself
			if (world.getBlockEntity(start) instanceof NixieTubeBlockEntity ntbe &&
					ntbe.computerBehaviour.hasAttachedComputer()) {
				return false;
			}
			BlockPos leftmostPos = currentPos;
			// No need to iterate over the nixies to the left again
			currentPos = start;
			while (true) {
				BlockPos nextPos = currentPos.relative(right);
				if (!areNixieBlocksEqual(world.getBlockState(nextPos), state))
					break;
				if (world.getBlockEntity(nextPos) instanceof NixieTubeBlockEntity ntbe &&
						ntbe.computerBehaviour.hasAttachedComputer()) {
					return false;
				}
				currentPos = nextPos;
			}
			currentPos = leftmostPos;
		}

		int index = 0;

		while (true) {
			final int rowPosition = index;
			if (callback != null)
				callback.accept(currentPos, rowPosition);
			BlockPos nextPos = currentPos.relative(right);
			if (!areNixieBlocksEqual(world.getBlockState(nextPos), state))
				break;
			currentPos = nextPos;
			index++;
		}

		return true;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACE, FACING, WATERLOGGED));
	}

	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (newState.getBlock() instanceof NixieTubeBlock)
			return;
		world.removeBlockEntity(pos);
		if (Mods.COMPUTERCRAFT.isLoaded()) {
			// A computer-controlled nixie tube row may have been broken in the middle.
			Direction left = getLeftNixieDirection(state);
			BlockPos leftPos = pos.relative(left);
			if (areNixieBlocksEqual(world.getBlockState(leftPos), state)) {
				boolean leftRowComputerControlled = isInComputerControlledRow(world, leftPos);
				walkNixies(world, leftPos, true, leftRowComputerControlled ?
						(currentPos, rowPosition) -> {
							if (world.getBlockEntity(currentPos) instanceof NixieTubeBlockEntity ntbe)
								ntbe.displayEmptyText(rowPosition);
						} :
						(currentPos, rowPosition) -> {
							if (world.getBlockEntity(currentPos) instanceof NixieTubeBlockEntity ntbe)
								NixieTubeBlock.updateDisplayedRedstoneValue(ntbe, state, true);
						});
			}
			Direction right = left.getOpposite();
			BlockPos rightPos = pos.relative(right);
			if (areNixieBlocksEqual(world.getBlockState(rightPos), state)) {
				boolean rightRowComputerControlled = isInComputerControlledRow(world, rightPos);
				walkNixies(world, rightPos, true, rightRowComputerControlled ?
						(currentPos, rowPosition) -> {
							if (world.getBlockEntity(currentPos) instanceof NixieTubeBlockEntity ntbe)
								ntbe.displayEmptyText(rowPosition);
						} :
						(currentPos, rowPosition) -> {
							if (world.getBlockEntity(currentPos) instanceof NixieTubeBlockEntity ntbe)
								NixieTubeBlock.updateDisplayedRedstoneValue(ntbe, state, true);
						});
			}
		}
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader pLevel, BlockPos pPos, BlockState pState) {
		return AllBlocks.ORANGE_NIXIE_TUBE.asStack();
	}

	@Override
	public ItemRequirement getRequiredItems(BlockState state, BlockEntity be) {
		return new ItemRequirement(ItemUseType.CONSUME, AllBlocks.ORANGE_NIXIE_TUBE.get()
			.asItem());
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		Direction facing = pState.getValue(FACING);
		return switch (pState.getValue(FACE)) {
			case CEILING -> AllShapes.NIXIE_TUBE_CEILING.get(facing.getClockWise().getAxis());
			case FLOOR -> AllShapes.NIXIE_TUBE.get(facing.getClockWise().getAxis());
			default -> AllShapes.NIXIE_TUBE_WALL.get(facing);
		};
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos,
									   Player player) {
		if (color != DyeColor.ORANGE)
			return AllBlocks.ORANGE_NIXIE_TUBE.get()
				.getCloneItemStack(state, target, level, pos, player);
		return super.getCloneItemStack(state, target, level, pos, player);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor world,
		BlockPos pos, BlockPos neighbourPos) {
		if (state.getValue(WATERLOGGED))
			world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		return state;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = super.getStateForPlacement(context);
		if (state == null)
			return null;
		if (state.getValue(FACE) != DoubleAttachFace.WALL && state.getValue(FACE) != DoubleAttachFace.WALL_REVERSED)
			state = state.setValue(FACING, state.getValue(FACING)
				.getClockWise());
		return state.setValue(WATERLOGGED, Boolean.valueOf(context.getLevel()
			.getFluidState(context.getClickedPos())
			.getType() == Fluids.WATER));
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
		boolean isMoving) {
		if (level.isClientSide)
			return;
		if (!level.getBlockTicks()
			.willTickThisTick(pos, this))
			level.scheduleTick(pos, this, 1);
	}

	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource r) {
		updateDisplayedRedstoneValue(state, worldIn, pos);
	}

	@Override
	public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (state.getBlock() == oldState.getBlock() || isMoving || oldState.getBlock() instanceof NixieTubeBlock)
			return;
		if (Mods.COMPUTERCRAFT.isLoaded() && isInComputerControlledRow(worldIn, pos)) {
			// The nixie tube has been placed in a computer-controlled row.
			walkNixies(worldIn, pos, true, (currentPos, rowPosition) -> {
				if (worldIn.getBlockEntity(currentPos) instanceof NixieTubeBlockEntity ntbe)
					ntbe.displayEmptyText(rowPosition);
			});
			return;
		}
		updateDisplayedRedstoneValue(state, worldIn, pos);
	}

	public static void updateDisplayedRedstoneValue(NixieTubeBlockEntity be, BlockState state, boolean force) {
		if (be.getLevel() == null || be.getLevel().isClientSide)
			return;
		if (be.reactsToRedstone() || force)
			be.updateRedstoneStrength(getPower(be.getLevel(), state, be.getBlockPos()));
	}

	private void updateDisplayedRedstoneValue(BlockState state, Level level, BlockPos pos) {
		if (level.isClientSide)
			return;
		withBlockEntityDo(level, pos, be -> NixieTubeBlock.updateDisplayedRedstoneValue(be, state, false));
	}

	static boolean isValidBlock(BlockGetter world, BlockPos pos, boolean above) {
		BlockState state = world.getBlockState(pos.above(above ? 1 : -1));
		return !state.getShape(world, pos)
			.isEmpty();
	}

	private static int getPower(Level level, BlockState state, BlockPos pos) {
		int power = 0;
		for (Direction direction : Iterate.directions)
			power = Math.max(level.getSignal(pos.relative(direction), direction), power);
		for (Direction direction : Iterate.directions) {
			if (state.getValue(FACING).getOpposite() != direction)
				power = Math.max(level.getSignal(pos.relative(direction), Direction.UP), power);
		}
		return power;
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
		return false;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
		return side != null;
	}

	@Override
	public Class<NixieTubeBlockEntity> getBlockEntityClass() {
		return NixieTubeBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends NixieTubeBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.NIXIE_TUBE.get();
	}

	public DyeColor getColor() {
		return color;
	}

	public static boolean areNixieBlocksEqual(BlockState blockState, BlockState otherState) {
		if (!(blockState.getBlock() instanceof NixieTubeBlock))
			return false;
		if (!(otherState.getBlock() instanceof NixieTubeBlock))
			return false;
		return withColor(blockState, DyeColor.WHITE) == withColor(otherState, DyeColor.WHITE);
	}

	public static BlockState withColor(BlockState state, DyeColor color) {
		return (color == DyeColor.ORANGE ? AllBlocks.ORANGE_NIXIE_TUBE : AllBlocks.NIXIE_TUBES.get(color))
			.getDefaultState()
			.setValue(FACING, state.getValue(FACING))
			.setValue(WATERLOGGED, state.getValue(WATERLOGGED))
			.setValue(FACE, state.getValue(FACE));
	}

	public static DyeColor colorOf(BlockState blockState) {
		return blockState.getBlock() instanceof NixieTubeBlock ? ((NixieTubeBlock) blockState.getBlock()).color
			: DyeColor.ORANGE;
	}

	public static Direction getFacing(BlockState sideState) {
		return getConnectedDirection(sideState);
	}

}
