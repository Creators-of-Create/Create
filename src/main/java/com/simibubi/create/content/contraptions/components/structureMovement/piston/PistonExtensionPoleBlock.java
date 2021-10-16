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

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

public class PistonExtensionPoleBlock extends WrenchableDirectionalBlock implements IWrenchable, IWaterLoggable {

    private static final int placementHelperId = PlacementHelpers.register(PlacementHelper.get());

    public PistonExtensionPoleBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP).setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @Override
	public ToolType getHarvestTool(BlockState state) {
		return null;
	}

	@Override
	public boolean canHarvestBlock(BlockState state, IBlockReader world, BlockPos pos, PlayerEntity player) {
		for (ToolType toolType : player.getMainHandItem()
			.getToolTypes()) {
			if (isToolEffective(state, toolType))
				return true;
		}
		return super.canHarvestBlock(state, world, pos, player);
	}

	@Override
	public boolean isToolEffective(BlockState state, ToolType tool) {
		return tool == ToolType.AXE || tool == ToolType.PICKAXE;
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.NORMAL;
	}

	@Override
	public void playerWillDestroy(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
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

			TileEntity te = worldIn.getBlockEntity(basePos);
			if (te instanceof MechanicalPistonTileEntity) {
				MechanicalPistonTileEntity baseTE = (MechanicalPistonTileEntity) te;
				baseTE.offset = 0;
				baseTE.onLengthBroken();
			}
		}

		super.playerWillDestroy(worldIn, pos, state, player);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.FOUR_VOXEL_POLE.get(state.getValue(FACING)
			.getAxis());
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		FluidState FluidState = context.getLevel()
			.getFluidState(context.getClickedPos());
		return defaultBlockState().setValue(FACING, context.getClickedFace()
			.getOpposite())
			.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(FluidState.getType() == Fluids.WATER));
	}

	@Override
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
		BlockRayTraceResult ray) {
		ItemStack heldItem = player.getItemInHand(hand);

        IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
        if (placementHelper.matchesItem(heldItem) && !player.isShiftKeyDown())
            return placementHelper.getOffset(player, world, state, pos, ray).placeInWorld(world, (BlockItem) heldItem.getItem(), player, hand, ray);

		return ActionResultType.PASS;
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
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, IWorld world, BlockPos pos, BlockPos neighbourPos) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            world.getLiquidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }
        return state;
    }

	@Override
	public boolean isPathfindable(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
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
