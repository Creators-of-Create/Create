package com.simibubi.create.content.logistics.chute;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.funnel.FunnelBlock;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;

import net.createmod.catnip.utility.Iterate;
import net.createmod.catnip.utility.lang.Lang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;

public class ChuteBlock extends AbstractChuteBlock implements ProperWaterloggedBlock {

	public static final Property<Shape> SHAPE = EnumProperty.create("shape", Shape.class);
	public static final DirectionProperty FACING = BlockStateProperties.FACING_HOPPER;

	public ChuteBlock(Properties p_i48440_1_) {
		super(p_i48440_1_);
		registerDefaultState(defaultBlockState().setValue(SHAPE, Shape.NORMAL)
			.setValue(FACING, Direction.DOWN)
			.setValue(WATERLOGGED, false));
	}

	public enum Shape implements StringRepresentable {
		INTERSECTION, WINDOW, NORMAL, ENCASED;

		@Override
		public String getSerializedName() {
			return Lang.asId(name());
		}
	}

	@Override
	public Direction getFacing(BlockState state) {
		return state.getValue(FACING);
	}

	@Override
	public boolean isOpen(BlockState state) {
		return state.getValue(FACING) == Direction.DOWN || state.getValue(SHAPE) == Shape.INTERSECTION;
	}

	@Override
	public boolean isTransparent(BlockState state) {
		return state.getValue(SHAPE) == Shape.WINDOW;
	}

	@Override
	public FluidState getFluidState(BlockState pState) {
		return fluidState(pState);
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		Shape shape = state.getValue(SHAPE);
		boolean down = state.getValue(FACING) == Direction.DOWN;
		if (shape == Shape.INTERSECTION)
			return InteractionResult.PASS;
		Level level = context.getLevel();
		if (level.isClientSide)
			return InteractionResult.SUCCESS;
		if (shape == Shape.ENCASED) {
			level.setBlockAndUpdate(context.getClickedPos(), state.setValue(SHAPE, Shape.NORMAL));
			level.levelEvent(2001, context.getClickedPos(),
				Block.getId(AllBlocks.INDUSTRIAL_IRON_BLOCK.getDefaultState()));
			return InteractionResult.SUCCESS;
		}
		if (down)
			level.setBlockAndUpdate(context.getClickedPos(),
				state.setValue(SHAPE, shape != Shape.NORMAL ? Shape.NORMAL : Shape.WINDOW));
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult hitResult) {
		Shape shape = state.getValue(SHAPE);
		if (!AllBlocks.INDUSTRIAL_IRON_BLOCK.isIn(player.getItemInHand(hand)))
			return super.use(state, level, pos, player, hand, hitResult);
		if (shape == Shape.INTERSECTION || shape == Shape.ENCASED)
			return super.use(state, level, pos, player, hand, hitResult);
		if (player == null || level.isClientSide)
			return InteractionResult.SUCCESS;

		level.setBlockAndUpdate(pos, state.setValue(SHAPE, Shape.ENCASED));
		level.playSound(null, pos, SoundEvents.NETHERITE_BLOCK_HIT, SoundSource.BLOCKS, 0.5f, 1.05f);
		return InteractionResult.SUCCESS;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockState state = withWater(super.getStateForPlacement(ctx), ctx);
		Direction face = ctx.getClickedFace();
		if (face.getAxis()
			.isHorizontal() && !ctx.isSecondaryUseActive()) {
			Level world = ctx.getLevel();
			BlockPos pos = ctx.getClickedPos();
			return updateChuteState(state.setValue(FACING, face), world.getBlockState(pos.above()), world, pos);
		}
		return state;
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState above, LevelAccessor world,
		BlockPos pos, BlockPos p_196271_6_) {
		updateWater(world, state, pos);
		return super.updateShape(state, direction, above, world, pos, p_196271_6_);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> p_206840_1_) {
		super.createBlockStateDefinition(p_206840_1_.add(SHAPE, FACING, WATERLOGGED));
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		BlockState above = world.getBlockState(pos.above());
		return !isChute(above) || getChuteFacing(above) == Direction.DOWN;
	}

	@Override
	public BlockState updateChuteState(BlockState state, BlockState above, BlockGetter world, BlockPos pos) {
		if (!(state.getBlock() instanceof ChuteBlock))
			return state;

		Map<Direction, Boolean> connections = new HashMap<>();
		int amtConnections = 0;
		Direction facing = state.getValue(FACING);
		boolean vertical = facing == Direction.DOWN;

		if (!vertical) {
			BlockState target = world.getBlockState(pos.below()
				.relative(facing.getOpposite()));
			if (!isChute(target))
				return state.setValue(FACING, Direction.DOWN)
					.setValue(SHAPE, Shape.NORMAL);
		}

		for (Direction direction : Iterate.horizontalDirections) {
			BlockState diagonalInputChute = world.getBlockState(pos.above()
				.relative(direction));
			boolean value =
				diagonalInputChute.getBlock() instanceof ChuteBlock && diagonalInputChute.getValue(FACING) == direction;
			connections.put(direction, value);
			if (value)
				amtConnections++;
		}

		boolean noConnections = amtConnections == 0;
		if (vertical)
			return state.setValue(SHAPE,
				noConnections ? state.getValue(SHAPE) == Shape.INTERSECTION ? Shape.NORMAL : state.getValue(SHAPE)
					: Shape.INTERSECTION);
		if (noConnections)
			return state.setValue(SHAPE, Shape.INTERSECTION);
		if (connections.get(Direction.NORTH) && connections.get(Direction.SOUTH))
			return state.setValue(SHAPE, Shape.INTERSECTION);
		if (connections.get(Direction.EAST) && connections.get(Direction.WEST))
			return state.setValue(SHAPE, Shape.INTERSECTION);
		if (amtConnections == 1 && connections.get(facing) && !(getChuteFacing(above) == Direction.DOWN)
			&& !(above.getBlock() instanceof FunnelBlock && FunnelBlock.getFunnelFacing(above) == Direction.DOWN))
			return state.setValue(SHAPE, state.getValue(SHAPE) == Shape.ENCASED ? Shape.ENCASED : Shape.NORMAL);
		return state.setValue(SHAPE, Shape.INTERSECTION);
	}

	@Override
	public BlockState rotate(BlockState pState, Rotation pRot) {
		return pState.setValue(FACING, pRot.rotate(pState.getValue(FACING)));
	}

	@Override
	@SuppressWarnings("deprecation")
	public BlockState mirror(BlockState pState, Mirror pMirror) {
		return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
	}

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
		return false;
	}

	@Override
	public BlockEntityType<? extends ChuteBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.CHUTE.get();
	}

}
