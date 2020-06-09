package com.simibubi.create.content.contraptions.components.tracks;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class ReinforcedRailBlock extends AbstractRailBlock{
	
	public static IProperty<RailShape> RAIL_SHAPE =
			EnumProperty.create("shape", RailShape.class, RailShape.EAST_WEST, RailShape.NORTH_SOUTH);
	
	public static IProperty<Boolean> CONNECTS_N = BooleanProperty.create("connects_n");
	public static IProperty<Boolean> CONNECTS_S = BooleanProperty.create("connects_s");

	public ReinforcedRailBlock(Properties properties) {
		super(true, properties);
	}

	@Override
	public IProperty<RailShape> getShapeProperty() {
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
	public boolean canMakeSlopes(BlockState state, IBlockReader world, BlockPos pos) {
		return false;
	}
	
	@Override
	protected void updateState(BlockState state, World world, BlockPos pos, Block block) {
		super.updateState(state, world, pos, block);
		world.setBlockState(pos, getUpdatedState(world, pos, state, true));
	}
	
	@Override
	protected BlockState getUpdatedState(World world, BlockPos pos, BlockState state,
			boolean p_208489_4_) {
		
		boolean alongX = state.get(RAIL_SHAPE) == RailShape.EAST_WEST;
		BlockPos sPos = pos.add(alongX  ? -1 : 0, 0, alongX ? 0 : 1);
		BlockPos nPos = pos.add(alongX ? 1 : 0, 0, alongX ? 0 : -1);
		
		return super.getUpdatedState(world, pos, state, p_208489_4_).with(CONNECTS_S, world.getBlockState(sPos).getBlock() instanceof ReinforcedRailBlock &&
						(world.getBlockState(sPos).get(RAIL_SHAPE) == state.get(RAIL_SHAPE)))
					.with(CONNECTS_N, world.getBlockState(nPos).getBlock() instanceof ReinforcedRailBlock && 
						(world.getBlockState(nPos).get(RAIL_SHAPE) == state.get(RAIL_SHAPE)));
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos,
		ISelectionContext context) {	//FIXME
		if (context.getEntity() instanceof AbstractMinecartEntity)
			return VoxelShapes.empty();
		return VoxelShapes.fullCube();
	}
	
	@Override
	public PushReaction getPushReaction(BlockState state) {
		return PushReaction.BLOCK;
	}
	
	@Override
	public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return false;
	}
	
	@Override
	public boolean isValidPosition(BlockState p_196260_1_, IWorldReader p_196260_2_, BlockPos p_196260_3_) {
	      return true;
	   }
	
	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos pos2, boolean p_220069_6_) {
		if (!world.isRemote) {
			this.updateState(state, world, pos, block);
		}
	}
}
