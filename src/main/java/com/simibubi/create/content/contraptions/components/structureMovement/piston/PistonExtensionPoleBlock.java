package com.simibubi.create.content.contraptions.components.structureMovement.piston;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.*;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;
import com.simibubi.create.foundation.utility.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
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

import static com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.*;

public class PistonExtensionPoleBlock extends ProperDirectionalBlock implements IWrenchable, IWaterLoggable {

    public PistonExtensionPoleBlock(Properties properties) {
        super(properties);
        setDefaultState(getDefaultState().with(FACING, Direction.UP).with(BlockStateProperties.WATERLOGGED, false));
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

        for (int modifier : new int[]{1, -1}) {
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
        IFluidState ifluidstate = context.getWorld().getFluidState(context.getPos());
        return getDefaultState().with(FACING, context.getFace().getOpposite())
                .with(BlockStateProperties.WATERLOGGED, Boolean.valueOf(ifluidstate.getFluid() == Fluids.WATER));
    }

    @Override
    public ActionResultType onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) {
        ItemStack heldItem = player.getHeldItem(hand);

        if (AllBlocks.PISTON_EXTENSION_POLE.isIn(heldItem) && !player.isSneaking()) {
            Pair<Direction, Integer> offset = PistonPolePlacementHelper.getPlacementOffset(world, state.get(FACING).getAxis(), pos, ray.getHitVec());

            if (offset == null || offset.getSecond() == 0)
                return ActionResultType.PASS;

            BlockPos newPos = pos.offset(offset.getFirst(), offset.getSecond());

            if (!world.getBlockState(newPos).getMaterial().isReplaceable())
                return ActionResultType.PASS;

            if (world.isRemote)
                return ActionResultType.SUCCESS;

            world.setBlockState(newPos, AllBlocks.PISTON_EXTENSION_POLE.getDefaultState().with(FACING, offset.getFirst()));
            if (!player.isCreative())
                heldItem.shrink(1);

            return ActionResultType.SUCCESS;
        }

        return ActionResultType.PASS;
    }

    @Override
    public IFluidState getFluidState(BlockState state) {
        return state.get(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : Fluids.EMPTY.getDefaultState();
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.WATERLOGGED);
        super.fillStateContainer(builder);
    }

    @Override
    public BlockState updatePostPlacement(BlockState state, Direction direction, BlockState neighbourState,
                                          IWorld world, BlockPos pos, BlockPos neighbourPos) {
        if (state.get(BlockStateProperties.WATERLOGGED)) {
            world.getPendingFluidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return state;
    }
}
