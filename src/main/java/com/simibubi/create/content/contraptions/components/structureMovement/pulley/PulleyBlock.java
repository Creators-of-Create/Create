package com.simibubi.create.content.contraptions.components.structureMovement.pulley;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.HorizontalAxisKineticBlock;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.BlockHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
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

public class PulleyBlock extends HorizontalAxisKineticBlock implements ITE<PulleyTileEntity> {

    public static EnumProperty<Axis> HORIZONTAL_AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    public PulleyBlock(Properties properties) {
        super(properties);
    }

    private static void onRopeBroken(World world, BlockPos pulleyPos) {
        TileEntity te = world.getTileEntity(pulleyPos);
        if (!(te instanceof PulleyTileEntity))
            return;
        PulleyTileEntity pulley = (PulleyTileEntity) te;
        pulley.offset = 0;
        pulley.sendData();
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return AllTileEntities.ROPE_PULLEY.create();
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (!worldIn.isRemote) {
                BlockState below = worldIn.getBlockState(pos.down());
                if (below.getBlock() instanceof RopeBlockBase)
                    worldIn.destroyBlock(pos.down(), true);
            }
            if (state.hasTileEntity())
                worldIn.removeTileEntity(pos);
        }
    }

    public ActionResultType onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                  BlockRayTraceResult hit) {
        if (!player.isAllowEdit())
            return ActionResultType.PASS;
        if (player.isSneaking())
            return ActionResultType.PASS;
        if (player.getHeldItem(handIn)
                .isEmpty()) {
            withTileEntityDo(worldIn, pos, te -> te.assembleNextTick = true);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return AllShapes.PULLEY.get(state.get(HORIZONTAL_AXIS));
    }

    @Override
    public Class<PulleyTileEntity> getTileEntityClass() {
        return PulleyTileEntity.class;
    }

    private static class RopeBlockBase extends Block implements IWaterLoggable {

        public RopeBlockBase(Properties properties) {
            super(properties);
            setDefaultState(super.getDefaultState().with(BlockStateProperties.WATERLOGGED, false));
        }

        @Override
        public PushReaction getPushReaction(BlockState state) {
            return PushReaction.BLOCK;
        }

        @Override
        public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos,
                                      PlayerEntity player) {
            return AllBlocks.ROPE_PULLEY.asStack();
        }

        @Override
        public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
            if (!isMoving && (!state.contains(BlockStateProperties.WATERLOGGED) || !newState.contains(BlockStateProperties.WATERLOGGED) || state.get(BlockStateProperties.WATERLOGGED) == newState.get(BlockStateProperties.WATERLOGGED))) {
                onRopeBroken(worldIn, pos.up());
                if (!worldIn.isRemote) {
                    BlockState above = worldIn.getBlockState(pos.up());
                    BlockState below = worldIn.getBlockState(pos.down());
                    if (above.getBlock() instanceof RopeBlockBase)
                        worldIn.destroyBlock(pos.up(), true);
                    if (below.getBlock() instanceof RopeBlockBase)
                        worldIn.destroyBlock(pos.down(), true);
                }
            }
            if (state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
                worldIn.removeTileEntity(pos);
            }
        }


        @Override
        public FluidState getFluidState(BlockState state) {
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

        @Override
        public BlockState getStateForPlacement(BlockItemUseContext context) {
            FluidState FluidState = context.getWorld().getFluidState(context.getPos());
            return super.getStateForPlacement(context).with(BlockStateProperties.WATERLOGGED, Boolean.valueOf(FluidState.getFluid() == Fluids.WATER));
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
