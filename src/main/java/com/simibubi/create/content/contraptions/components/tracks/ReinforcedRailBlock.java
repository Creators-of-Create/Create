package com.simibubi.create.content.contraptions.components.tracks;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ReinforcedRailBlock extends BaseRailBlock {

    public static Property<RailShape> RAIL_SHAPE =
            EnumProperty.create("shape", RailShape.class, RailShape.EAST_WEST, RailShape.NORTH_SOUTH);

    public static Property<Boolean> CONNECTS_N = BooleanProperty.create("connects_n");
    public static Property<Boolean> CONNECTS_S = BooleanProperty.create("connects_s");

    public ReinforcedRailBlock(Properties properties) {
        super(true, properties);
    }

    @Override
    public void fillItemCategory(CreativeModeTab p_149666_1_, NonNullList<ItemStack> p_149666_2_) {
    	// TODO re-add when finished
    }

    @Nonnull
    @Override
    public Property<RailShape> getShapeProperty() {
        return RAIL_SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(RAIL_SHAPE, CONNECTS_N, CONNECTS_S);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean alongX = context.getHorizontalDirection().getAxis() == Axis.X;
        return super.getStateForPlacement(context).setValue(RAIL_SHAPE, alongX ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH).setValue(CONNECTS_N, false).setValue(CONNECTS_S, false);
    }

    @Override
    public boolean canMakeSlopes(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos) {
        return false;
    }

    @Override
    protected void updateState(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Block block) {
        super.updateState(state, world, pos, block);
        world.setBlockAndUpdate(pos, updateDir(world, pos, state, true));
    }

    @Override
    @Nonnull
    protected BlockState updateDir(@Nonnull Level world, BlockPos pos, BlockState state,
                                         boolean p_208489_4_) {

        boolean alongX = state.getValue(RAIL_SHAPE) == RailShape.EAST_WEST;
        BlockPos sPos = pos.offset(alongX ? -1 : 0, 0, alongX ? 0 : 1);
        BlockPos nPos = pos.offset(alongX ? 1 : 0, 0, alongX ? 0 : -1);

        return super.updateDir(world, pos, state, p_208489_4_).setValue(CONNECTS_S, world.getBlockState(sPos).getBlock() instanceof ReinforcedRailBlock &&
                (world.getBlockState(sPos).getValue(RAIL_SHAPE) == state.getValue(RAIL_SHAPE)))
                .setValue(CONNECTS_N, world.getBlockState(nPos).getBlock() instanceof ReinforcedRailBlock &&
                        (world.getBlockState(nPos).getValue(RAIL_SHAPE) == state.getValue(RAIL_SHAPE)));
    }

    @Override
    @Nonnull
    public VoxelShape getCollisionShape(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos,
                                        CollisionContext context) {    //FIXME
        if (context.getEntity() instanceof AbstractMinecart)
            return Shapes.empty();
        return getShape(state, worldIn, pos, null);
    }

    @Override
    @Nonnull
    public VoxelShape getShape(BlockState state, @Nonnull BlockGetter reader, @Nonnull BlockPos pos, CollisionContext context) {
        boolean alongX = state.getValue(RAIL_SHAPE) == RailShape.EAST_WEST;
        return Shapes.or(box(0, -2, 0, 16, 2, 16), Shapes.or(box(0, -2, 0, alongX ? 16 : -1, 12, alongX ? -1 : 16), box(alongX ? 0 : 17, -2, alongX ? 17 : 0, 16, 12, 16)));
    }

    @Override
    @Nonnull
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    /* FIXME: Same thing as before, does this still matter? If so, what is the new way of doing it?
    @Override
    public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return false;
    }*/

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return !(world.getBlockState(pos.below()).getBlock() instanceof BaseRailBlock || world.getBlockState(pos.above()).getBlock() instanceof BaseRailBlock);
    }

    @Override
    public void neighborChanged(@Nonnull BlockState state, Level world, @Nonnull BlockPos pos, @Nonnull Block block, @Nonnull BlockPos pos2, boolean p_220069_6_) {
        if (!world.isClientSide) {
            if ((world.getBlockState(pos.below()).getBlock() instanceof BaseRailBlock)) {
                if (!p_220069_6_) {
                    dropResources(state, world, pos);
                }
                world.removeBlock(pos, false);
            } else {
                this.updateState(state, world, pos, block);
            }
        }
    }
}
