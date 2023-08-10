package com.simibubi.create.content.redstone.nixieTube;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.schematics.requirement.ISpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement.ItemUseType;
import com.simibubi.create.foundation.block.IBE;

import net.createmod.catnip.utility.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
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
	implements IBE<NixieTubeBlockEntity>, IWrenchable, SimpleWaterloggedBlock, ISpecialBlockItemRequirement {

	protected final DyeColor color;

	public NixieTubeBlock(Properties properties, DyeColor color) {
		super(properties);
		this.color = color;
		registerDefaultState(defaultBlockState().setValue(FACE, DoubleAttachFace.FLOOR)
			.setValue(WATERLOGGED, false));
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult ray) {

		if (player.isShiftKeyDown())
			return InteractionResult.PASS;

		ItemStack heldItem = player.getItemInHand(hand);
		NixieTubeBlockEntity nixie = getBlockEntity(world, pos);

		if (nixie == null)
			return InteractionResult.PASS;
		if (heldItem.isEmpty()) {
			if (nixie.reactsToRedstone())
				return InteractionResult.PASS;
			nixie.clearCustomText();
			updateDisplayedRedstoneValue(state, world, pos);
			return InteractionResult.SUCCESS;
		}

		boolean display =
			heldItem.getItem() == Items.NAME_TAG && heldItem.hasCustomHoverName() || AllBlocks.CLIPBOARD.isIn(heldItem);
		DyeColor dye = DyeColor.getColor(heldItem);

		if (!display && dye == null)
			return InteractionResult.PASS;

		CompoundTag tag = heldItem.getTagElement("display");
		String tagElement = tag != null && tag.contains("Name", Tag.TAG_STRING) ? tag.getString("Name") : null;

		if (AllBlocks.CLIPBOARD.isIn(heldItem)) {
			List<ClipboardEntry> entries = ClipboardEntry.getLastViewedEntries(heldItem);
			for (int i = 0; i < entries.size();) {
				tagElement = Component.Serializer.toJson(entries.get(i).text);
				break;
			}
		}

		if (world.isClientSide)
			return InteractionResult.SUCCESS;

		String tagUsed = tagElement;
		walkNixies(world, pos, (currentPos, rowPosition) -> {
			if (display)
				withBlockEntityDo(world, currentPos, be -> be.displayCustomText(tagUsed, rowPosition));
			if (dye != null)
				world.setBlockAndUpdate(currentPos, withColor(state, dye));
		});

		return InteractionResult.SUCCESS;
	}

	public static void walkNixies(LevelAccessor world, BlockPos start, BiConsumer<BlockPos, Integer> callback) {
		BlockState state = world.getBlockState(start);
		if (!(state.getBlock() instanceof NixieTubeBlock))
			return;

		BlockPos currentPos = start;
		Direction left = state.getValue(FACING)
			.getOpposite();

		if (state.getValue(FACE) == DoubleAttachFace.WALL)
			left = Direction.UP;
		if (state.getValue(FACE) == DoubleAttachFace.WALL_REVERSED)
			left = Direction.DOWN;

		Direction right = left.getOpposite();

		while (true) {
			BlockPos nextPos = currentPos.relative(left);
			if (!areNixieBlocksEqual(world.getBlockState(nextPos), state))
				break;
			currentPos = nextPos;
		}

		int index = 0;

		while (true) {
			final int rowPosition = index;
			callback.accept(currentPos, rowPosition);
			BlockPos nextPos = currentPos.relative(right);
			if (!areNixieBlocksEqual(world.getBlockState(nextPos), state))
				break;
			currentPos = nextPos;
			index++;
		}
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACE, FACING, WATERLOGGED));
	}

	@Override
	public void onRemove(BlockState p_196243_1_, Level p_196243_2_, BlockPos p_196243_3_, BlockState p_196243_4_,
		boolean p_196243_5_) {
		if (!(p_196243_4_.getBlock() instanceof NixieTubeBlock))
			p_196243_2_.removeBlockEntity(p_196243_3_);
	}

	@Override
	public ItemStack getCloneItemStack(BlockGetter p_185473_1_, BlockPos p_185473_2_, BlockState p_185473_3_) {
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
		switch (pState.getValue(FACE)) {
		case CEILING:
			return AllShapes.NIXIE_TUBE_CEILING.get(facing.getClockWise()
				.getAxis());
		case FLOOR:
			return AllShapes.NIXIE_TUBE.get(facing.getClockWise()
				.getAxis());
		default:
			return AllShapes.NIXIE_TUBE_WALL.get(facing);
		}
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos,
		Player player) {
		if (color != DyeColor.ORANGE)
			return AllBlocks.ORANGE_NIXIE_TUBE.get()
				.getCloneItemStack(state, target, world, pos, player);
		return super.getCloneItemStack(state, target, world, pos, player);
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
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block p_220069_4_, BlockPos p_220069_5_,
		boolean p_220069_6_) {
		if (worldIn.isClientSide)
			return;
		if (!worldIn.getBlockTicks()
			.willTickThisTick(pos, this))
			worldIn.scheduleTick(pos, this, 0);
	}

	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random r) {
		updateDisplayedRedstoneValue(state, worldIn, pos);
	}

	@Override
	public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (state.getBlock() == oldState.getBlock() || isMoving)
			return;
		updateDisplayedRedstoneValue(state, worldIn, pos);
	}

	private void updateDisplayedRedstoneValue(BlockState state, Level worldIn, BlockPos pos) {
		if (worldIn.isClientSide)
			return;
		withBlockEntityDo(worldIn, pos, be -> {
			if (be.reactsToRedstone())
				be.updateRedstoneStrength(getPower(worldIn, pos));
		});
	}

	static boolean isValidBlock(BlockGetter world, BlockPos pos, boolean above) {
		BlockState state = world.getBlockState(pos.above(above ? 1 : -1));
		return !state.getShape(world, pos)
			.isEmpty();
	}

	private int getPower(Level worldIn, BlockPos pos) {
		int power = 0;
		for (Direction direction : Iterate.directions)
			power = Math.max(worldIn.getSignal(pos.relative(direction), direction), power);
		for (Direction direction : Iterate.directions)
			power = Math.max(worldIn.getSignal(pos.relative(direction), Direction.UP), power);
		return power;
	}

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
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
