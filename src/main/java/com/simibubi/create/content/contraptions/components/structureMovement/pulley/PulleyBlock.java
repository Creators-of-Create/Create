package com.simibubi.create.content.contraptions.components.structureMovement.pulley;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.HorizontalAxisKineticBlock;
import com.simibubi.create.foundation.block.ITE;

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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import net.minecraft.block.AbstractBlock.Properties;

public class PulleyBlock extends HorizontalAxisKineticBlock implements ITE<PulleyTileEntity> {

    public static EnumProperty<Axis> HORIZONTAL_AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    public PulleyBlock(Properties properties) {
        super(properties);
    }

    private static void onRopeBroken(World world, BlockPos pulleyPos) {
		TileEntity te = world.getBlockEntity(pulleyPos);
		if (te instanceof PulleyTileEntity) {
			PulleyTileEntity pulley = (PulleyTileEntity) te;
			pulley.initialOffset = 0;
			pulley.onLengthBroken();
		}
	}

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return AllTileEntities.ROPE_PULLEY.create();
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (!worldIn.isClientSide) {
                BlockState below = worldIn.getBlockState(pos.below());
                if (below.getBlock() instanceof RopeBlockBase)
                    worldIn.destroyBlock(pos.below(), true);
            }
            if (state.hasTileEntity())
                worldIn.removeBlockEntity(pos);
        }
    }

    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                  BlockRayTraceResult hit) {
        if (!player.mayBuild())
            return ActionResultType.PASS;
        if (player.isShiftKeyDown())
            return ActionResultType.PASS;
        if (player.getItemInHand(handIn)
                .isEmpty()) {
            withTileEntityDo(worldIn, pos, te -> te.assembleNextTick = true);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return AllShapes.PULLEY.get(state.getValue(HORIZONTAL_AXIS));
    }

    @Override
    public Class<PulleyTileEntity> getTileEntityClass() {
        return PulleyTileEntity.class;
    }

    private static class RopeBlockBase extends Block implements IWaterLoggable {

        public RopeBlockBase(Properties properties) {
            super(properties);
            registerDefaultState(super.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, false));
        }

		@Override
    	public boolean isPathfindable(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
    		return false;
    	}

        @Override
        public PushReaction getPistonPushReaction(BlockState state) {
            return PushReaction.BLOCK;
        }

        @Override
        public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos,
                                      PlayerEntity player) {
            return AllBlocks.ROPE_PULLEY.asStack();
        }

        @Override
        public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
            if (!isMoving && (!state.hasProperty(BlockStateProperties.WATERLOGGED) || !newState.hasProperty(BlockStateProperties.WATERLOGGED) || state.getValue(BlockStateProperties.WATERLOGGED) == newState.getValue(BlockStateProperties.WATERLOGGED))) {
                onRopeBroken(worldIn, pos.above());
                if (!worldIn.isClientSide) {
                    BlockState above = worldIn.getBlockState(pos.above());
                    BlockState below = worldIn.getBlockState(pos.below());
                    if (above.getBlock() instanceof RopeBlockBase)
                        worldIn.destroyBlock(pos.above(), true);
                    if (below.getBlock() instanceof RopeBlockBase)
                        worldIn.destroyBlock(pos.below(), true);
                }
            }
            if (state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
                worldIn.removeBlockEntity(pos);
            }
        }


        @Override
        public FluidState getFluidState(BlockState state) {
            return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
        }

        @Override
        protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
            builder.add(BlockStateProperties.WATERLOGGED);
            super.createBlockStateDefinition(builder);
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

    }

    public static class MagnetBlock extends RopeBlockBase {

        public MagnetBlock(Properties properties) {
            super(properties);
        }

        @Override
        public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
            return AllShapes.PULLEY_MAGNET;
        }

    }

    public static class RopeBlock extends RopeBlockBase {

        public RopeBlock(Properties properties) {
            super(properties);
        }

        @Override
        public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
            return AllShapes.FOUR_VOXEL_POLE.get(Direction.UP);
        }
    }

}
