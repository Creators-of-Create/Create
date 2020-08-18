package com.simibubi.create.content.contraptions.relays.elementary;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.contraptions.relays.encased.EncasedShaftBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class ShaftBlock extends RotatedPillarKineticBlock implements IWaterLoggable {

    public ShaftBlock(Properties properties) {
        super(properties);
        setDefaultState(super.getDefaultState().with(BlockStateProperties.WATERLOGGED, false));
    }

    public static boolean isShaft(BlockState state) {
        return AllBlocks.SHAFT.has(state);
    }

    @Override
    public PushReaction getPushReaction(BlockState state) {
        return PushReaction.NORMAL;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return AllTileEntities.SIMPLE_KINETIC.create();
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return AllShapes.SIX_VOXEL_POLE.get(state.get(AXIS));
    }

    @Override
    public float getParticleTargetRadius() {
        return .25f;
    }

    @Override
    public float getParticleInitialRadius() {
        return 0f;
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        super.fillItemGroup(group, items);
    }

    @Override
    public ActionResultType onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult p_225533_6_) {
        if (player.isSneaking() || !player.isAllowEdit())
            return ActionResultType.PASS;

        ItemStack heldItem = player.getHeldItem(hand);

        for (EncasedShaftBlock.Casing casing : EncasedShaftBlock.Casing.values()) {
            if (casing.getCasingEntry().isIn(heldItem)) {
                if (world.isRemote)
                    return ActionResultType.SUCCESS;

                KineticTileEntity.switchToBlockState(world, pos, AllBlocks.ENCASED_SHAFT.getDefaultState().with(EncasedShaftBlock.CASING, casing).with(AXIS, state.get(AXIS)));
                return ActionResultType.SUCCESS;
            }
        }

        return ActionResultType.PASS;
    }

    // IRotate:

    @Override
    public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.get(AXIS);
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.get(AXIS);
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

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        IFluidState ifluidstate = context.getWorld().getFluidState(context.getPos());
        return super.getStateForPlacement(context).with(BlockStateProperties.WATERLOGGED, Boolean.valueOf(ifluidstate.getFluid() == Fluids.WATER));
    }
}
