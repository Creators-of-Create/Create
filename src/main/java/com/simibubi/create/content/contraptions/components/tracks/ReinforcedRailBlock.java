package com.simibubi.create.content.contraptions.components.tracks;

import javax.annotation.Nonnull;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class ReinforcedRailBlock extends AbstractRailBlock {

    public static Property<RailShape> RAIL_SHAPE =
            EnumProperty.create("shape", RailShape.class, RailShape.EAST_WEST, RailShape.NORTH_SOUTH);

    public static Property<Boolean> CONNECTS_N = BooleanProperty.create("connects_n");
    public static Property<Boolean> CONNECTS_S = BooleanProperty.create("connects_s");

    public ReinforcedRailBlock(Properties properties) {
        super(true, properties);
    }

    @Override
    public void fillItemGroup(ItemGroup p_149666_1_, NonNullList<ItemStack> p_149666_2_) {
    	// TODO re-add when finished
    }

    @Nonnull
    @Override
    public Property<RailShape> getShapeProperty() {
        return RAIL_SHAPE;
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        builder.add(RAIL_SHAPE, CONNECTS_N, CONNECTS_S);
        super.fillStateContainer(builder);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        boolean alongX = context.getPlacementHorizontalFacing().getAxis() == Axis.X;
        return super.getStateForPlacement(context).with(RAIL_SHAPE, alongX ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH).with(CONNECTS_N, false).with(CONNECTS_S, false);
    }

    @Override
    public boolean canMakeSlopes(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        return false;
    }

    @Override
    protected void updateState(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Block block) {
        super.updateState(state, world, pos, block);
        world.setBlockState(pos, getUpdatedState(world, pos, state, true));
    }

    @Override
    @Nonnull
    protected BlockState getUpdatedState(@Nonnull World world, BlockPos pos, BlockState state,
                                         boolean p_208489_4_) {

        boolean alongX = state.get(RAIL_SHAPE) == RailShape.EAST_WEST;
        BlockPos sPos = pos.add(alongX ? -1 : 0, 0, alongX ? 0 : 1);
        BlockPos nPos = pos.add(alongX ? 1 : 0, 0, alongX ? 0 : -1);

        return super.getUpdatedState(world, pos, state, p_208489_4_).with(CONNECTS_S, world.getBlockState(sPos).getBlock() instanceof ReinforcedRailBlock &&
                (world.getBlockState(sPos).get(RAIL_SHAPE) == state.get(RAIL_SHAPE)))
                .with(CONNECTS_N, world.getBlockState(nPos).getBlock() instanceof ReinforcedRailBlock &&
                        (world.getBlockState(nPos).get(RAIL_SHAPE) == state.get(RAIL_SHAPE)));
    }

    @Override
    @Nonnull
    public VoxelShape getCollisionShape(@Nonnull BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos,
                                        ISelectionContext context) {    //FIXME
        if (context.getEntity() instanceof AbstractMinecartEntity)
            return VoxelShapes.empty();
        return getShape(state, worldIn, pos, null);
    }

    @Override
    @Nonnull
    public VoxelShape getShape(BlockState state, @Nonnull IBlockReader reader, @Nonnull BlockPos pos, ISelectionContext context) {
        boolean alongX = state.get(RAIL_SHAPE) == RailShape.EAST_WEST;
        return VoxelShapes.or(makeCuboidShape(0, -2, 0, 16, 2, 16), VoxelShapes.or(makeCuboidShape(0, -2, 0, alongX ? 16 : -1, 12, alongX ? -1 : 16), makeCuboidShape(alongX ? 0 : 17, -2, alongX ? 17 : 0, 16, 12, 16)));
    }

    @Override
    @Nonnull
    public PushReaction getPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    /* FIXME: Same thing as before, does this still matter? If so, what is the new way of doing it?
    @Override
    public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return false;
    }*/

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
        return !(world.getBlockState(pos.down()).getBlock() instanceof AbstractRailBlock || world.getBlockState(pos.up()).getBlock() instanceof AbstractRailBlock);
    }

    @Override
    public void neighborChanged(@Nonnull BlockState state, World world, @Nonnull BlockPos pos, @Nonnull Block block, @Nonnull BlockPos pos2, boolean p_220069_6_) {
        if (!world.isRemote) {
            if ((world.getBlockState(pos.down()).getBlock() instanceof AbstractRailBlock)) {
                if (!p_220069_6_) {
                    spawnDrops(state, world, pos);
                }
                world.removeBlock(pos, false);
            } else {
                this.updateState(state, world, pos, block);
            }
        }
    }
}
