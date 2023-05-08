package com.simibubi.create.content.contraptions.components.structureMovement.piston;

import static com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.isExtensionPole;
import static com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.isPiston;
import static com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.isPistonHead;

import java.util.function.Predicate;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.PistonState;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;
import com.simibubi.create.foundation.utility.placement.IPlacementHelper;
import com.simibubi.create.foundation.utility.placement.PlacementHelpers;
import com.simibubi.create.foundation.utility.placement.util.PoleHelper;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PistonExtensionPoleBlock extends WrenchableDirectionalBlock implements IWrenchable, SimpleWaterloggedBlock {

    private static final int placementHelperId = PlacementHelpers.register(PlacementHelper.get());

    public PistonExtensionPoleBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP).setValue(BlockStateProperties.WATERLOGGED, false));
    }

	@Override
	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.NORMAL;
	}

	@Override
	public void playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player) {
		Axis axis = state.getValue(FACING)
			.getAxis();
		Direction direction = Direction.get(AxisDirection.POSITIVE, axis);
		BlockPos pistonHead = null;
		BlockPos pistonBase = null;

		for (int modifier : new int[] { 1, -1 }) {
			for (int offset = modifier; modifier * offset < MechanicalPistonBlock.maxAllowedPistonPoles(); offset +=
				modifier) {
				BlockPos currentPos = pos.relative(direction, offset);
				BlockState block = worldIn.getBlockState(currentPos);

				if (isExtensionPole(block) && axis == block.getValue(FACING)
					.getAxis())
					continue;

				if (isPiston(block) && block.getValue(BlockStateProperties.FACING)
					.getAxis() == axis)
					pistonBase = currentPos;

				if (isPistonHead(block) && block.getValue(BlockStateProperties.FACING)
					.getAxis() == axis)
					pistonHead = currentPos;

				break;
			}
		}

		if (pistonHead != null && pistonBase != null && worldIn.getBlockState(pistonHead)
			.getValue(BlockStateProperties.FACING) == worldIn.getBlockState(pistonBase)
				.getValue(BlockStateProperties.FACING)) {

			final BlockPos basePos = pistonBase;
			BlockPos.betweenClosedStream(pistonBase, pistonHead)
					.filter(p -> !p.equals(pos) && !p.equals(basePos))
					.forEach(p -> worldIn.destroyBlock(p, !player.isCreative()));
			worldIn.setBlockAndUpdate(basePos, worldIn.getBlockState(basePos)
					.setValue(MechanicalPistonBlock.STATE, PistonState.RETRACTED));

			BlockEntity be = worldIn.getBlockEntity(basePos);
			if (be instanceof MechanicalPistonBlockEntity) {
				MechanicalPistonBlockEntity baseBE = (MechanicalPistonBlockEntity) be;
				baseBE.offset = 0;
				baseBE.onLengthBroken();
			}
		}

		super.playerWillDestroy(worldIn, pos, state, player);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AllShapes.FOUR_VOXEL_POLE.get(state.getValue(FACING)
			.getAxis());
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		FluidState FluidState = context.getLevel()
			.getFluidState(context.getClickedPos());
		return defaultBlockState().setValue(FACING, context.getClickedFace()
			.getOpposite())
			.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(FluidState.getType() == Fluids.WATER));
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult ray) {
		ItemStack heldItem = player.getItemInHand(hand);

        IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
        if (placementHelper.matchesItem(heldItem) && !player.isShiftKeyDown())
            return placementHelper.getOffset(player, world, state, pos, ray).placeInWorld(world, (BlockItem) heldItem.getItem(), player, hand, ray);

		return InteractionResult.PASS;
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false)
			: Fluids.EMPTY.defaultFluidState();
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(BlockStateProperties.WATERLOGGED);
		super.createBlockStateDefinition(builder);
	}

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor world, BlockPos pos, BlockPos neighbourPos) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) 
            world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        return state;
    }

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
		return false;
	}

    @MethodsReturnNonnullByDefault
    public static class PlacementHelper extends PoleHelper<Direction> {

        private static final PlacementHelper instance = new PlacementHelper();

        public static PlacementHelper get() {
            return instance;
        }

        private PlacementHelper(){
            super(
                    AllBlocks.PISTON_EXTENSION_POLE::has,
                    state -> state.getValue(FACING).getAxis(),
                    FACING
            );
        }

        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return AllBlocks.PISTON_EXTENSION_POLE::isIn;
        }
    }
}
