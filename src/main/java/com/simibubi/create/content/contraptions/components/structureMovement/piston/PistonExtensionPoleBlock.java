package com.simibubi.create.content.contraptions.components.structureMovement.piston;

import static com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.isExtensionPole;
import static com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.isPiston;
import static com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.isPistonHead;

import java.util.function.Predicate;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.PistonState;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;
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

public class PistonExtensionPoleBlock extends ProperDirectionalBlock implements IWrenchable, IWaterLoggable {

    private static final int placementHelperId = PlacementHelpers.register(PlacementHelper.get());

    public PistonExtensionPoleBlock(Properties properties) {
        super(properties);
        setDefaultState(getDefaultState().with(FACING, Direction.UP).with(BlockStateProperties.WATERLOGGED, false));
    }

    @Override
	public ToolType getHarvestTool(BlockState state) {
		return null;
	}

	@Override
	public boolean canHarvestBlock(BlockState state, IBlockReader world, BlockPos pos, PlayerEntity player) {
		for (ToolType toolType : player.getHeldItemMainhand()
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
	public PushReaction getPushReaction(BlockState state) {
		return PushReaction.NORMAL;
	}

	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
		Axis axis = state.get(FACING)
			.getAxis();
		Direction direction = Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis);
		BlockPos pistonHead = null;
		BlockPos pistonBase = null;

		for (int modifier : new int[] { 1, -1 }) {
			for (int offset = modifier; modifier * offset < MechanicalPistonBlock.maxAllowedPistonPoles(); offset +=
				modifier) {
				BlockPos currentPos = pos.offset(direction, offset);
				BlockState block = worldIn.getBlockState(currentPos);

				if (isExtensionPole(block) && axis == block.get(FACING)
					.getAxis())
					continue;

				if (isPiston(block) && block.get(BlockStateProperties.FACING)
					.getAxis() == axis)
					pistonBase = currentPos;

				if (isPistonHead(block) && block.get(BlockStateProperties.FACING)
					.getAxis() == axis)
					pistonHead = currentPos;

				break;
			}
		}

		if (pistonHead != null && pistonBase != null && worldIn.getBlockState(pistonHead)
			.get(BlockStateProperties.FACING) == worldIn.getBlockState(pistonBase)
				.get(BlockStateProperties.FACING)) {

			final BlockPos basePos = pistonBase;
			BlockPos.getAllInBox(pistonBase, pistonHead)
					.filter(p -> !p.equals(pos) && !p.equals(basePos))
					.forEach(p -> worldIn.destroyBlock(p, !player.isCreative()));
			worldIn.setBlockState(basePos, worldIn.getBlockState(basePos)
					.with(MechanicalPistonBlock.STATE, PistonState.RETRACTED));

			TileEntity te = worldIn.getTileEntity(basePos);
			if (te instanceof MechanicalPistonTileEntity) {
				MechanicalPistonTileEntity baseTE = (MechanicalPistonTileEntity) te;
				baseTE.offset = 0;
				baseTE.onLengthBroken();
			}
		}

		super.onBlockHarvested(worldIn, pos, state, player);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.FOUR_VOXEL_POLE.get(state.get(FACING)
			.getAxis());
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		FluidState FluidState = context.getWorld()
			.getFluidState(context.getPos());
		return getDefaultState().with(FACING, context.getFace()
			.getOpposite())
			.with(BlockStateProperties.WATERLOGGED, Boolean.valueOf(FluidState.getFluid() == Fluids.WATER));
	}

	@Override
	public ActionResultType onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
		BlockRayTraceResult ray) {
		ItemStack heldItem = player.getHeldItem(hand);

        IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
        if (placementHelper.matchesItem(heldItem) && !player.isSneaking())
            return placementHelper.getOffset(player, world, state, pos, ray).placeInWorld(world, (BlockItem) heldItem.getItem(), player, hand, ray);

		return ActionResultType.PASS;
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.get(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getStillFluidState(false)
			: Fluids.EMPTY.getDefaultState();
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(BlockStateProperties.WATERLOGGED);
		super.fillStateContainer(builder);
	}

    @Override
    public BlockState updatePostPlacement(BlockState state, Direction direction, BlockState neighbourState, IWorld world, BlockPos pos, BlockPos neighbourPos) {
        if (state.get(BlockStateProperties.WATERLOGGED)) {
            world.getPendingFluidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return state;
    }

	@Override
	public boolean allowsMovement(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
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
                    state -> state.get(FACING).getAxis(),
                    FACING
            );
        }

        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return AllBlocks.PISTON_EXTENSION_POLE::isIn;
        }
    }
}
