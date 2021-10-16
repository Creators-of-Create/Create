package com.simibubi.create.content.contraptions.components.structureMovement.piston;

import static com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.isExtensionPole;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.PistonState;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.PistonType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class MechanicalPistonHeadBlock extends WrenchableDirectionalBlock implements IWaterLoggable {

    public static final EnumProperty<PistonType> TYPE = BlockStateProperties.PISTON_TYPE;

    public MechanicalPistonHeadBlock(Properties p_i48415_1_) {
        super(p_i48415_1_);
        registerDefaultState(super.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(TYPE, BlockStateProperties.WATERLOGGED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.NORMAL;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos,
                                  PlayerEntity player) {
        return AllBlocks.PISTON_EXTENSION_POLE.asStack();
    }

    @Override
    public void playerWillDestroy(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        Direction direction = state.getValue(FACING);
        BlockPos pistonHead = pos;
        BlockPos pistonBase = null;

        for (int offset = 1; offset < MechanicalPistonBlock.maxAllowedPistonPoles(); offset++) {
            BlockPos currentPos = pos.relative(direction.getOpposite(), offset);
            BlockState block = worldIn.getBlockState(currentPos);

            if (isExtensionPole(block) && direction.getAxis() == block.getValue(BlockStateProperties.FACING)
                    .getAxis())
                continue;

            if (MechanicalPistonBlock.isPiston(block) && block.getValue(BlockStateProperties.FACING) == direction)
                pistonBase = currentPos;

            break;
        }

        if (pistonHead != null && pistonBase != null) {
            final BlockPos basePos = pistonBase;
            BlockPos.betweenClosedStream(pistonBase, pistonHead)
                    .filter(p -> !p.equals(pos) && !p.equals(basePos))
                    .forEach(p -> worldIn.destroyBlock(p, !player.isCreative()));
            worldIn.setBlockAndUpdate(basePos, worldIn.getBlockState(basePos)
                    .setValue(MechanicalPistonBlock.STATE, PistonState.RETRACTED));
        }

        super.playerWillDestroy(worldIn, pos, state, player);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return AllShapes.MECHANICAL_PISTON_HEAD.get(state.getValue(FACING));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState,
                                          IWorld world, BlockPos pos, BlockPos neighbourPos) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            world.getLiquidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }
        return state;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        FluidState FluidState = context.getLevel().getFluidState(context.getClickedPos());
        return super.getStateForPlacement(context).setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(FluidState.getType() == Fluids.WATER));
    }
    
    @Override
	public boolean isPathfindable(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
		return false;
	}
}
