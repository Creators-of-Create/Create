package com.simibubi.create.content.contraptions.relays.encased;

import javax.annotation.Nullable;

import com.simibubi.create.content.contraptions.base.RotatedPillarKineticBlock;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

import net.minecraft.block.AbstractBlock.Properties;

@MethodsReturnNonnullByDefault
public abstract class AbstractEncasedShaftBlock extends RotatedPillarKineticBlock {
    public AbstractEncasedShaftBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState state, IWorldReader world, BlockPos pos, Direction side) {
        return false;
    }

    @Override
    public PushReaction getPistonPushReaction(@Nullable BlockState state) {
        return PushReaction.NORMAL;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        if (context.getPlayer() != null && context.getPlayer()
                .isShiftKeyDown())
            return super.getStateForPlacement(context);
        Direction.Axis preferredAxis = getPreferredAxis(context);
        return this.defaultBlockState()
                .setValue(AXIS, preferredAxis == null ? context.getNearestLookingDirection()
                        .getAxis() : preferredAxis);
    }

    @Override
    public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(AXIS);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }
}
