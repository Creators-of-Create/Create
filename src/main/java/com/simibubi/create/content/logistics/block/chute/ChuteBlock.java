package com.simibubi.create.content.logistics.block.chute;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.logistics.block.funnel.FunnelBlock;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class ChuteBlock extends AbstractChuteBlock {
	
	public static final Property<Shape> SHAPE = EnumProperty.create("shape", Shape.class);
	public static final DirectionProperty FACING = BlockStateProperties.FACING_EXCEPT_UP;

	public ChuteBlock(Properties p_i48440_1_) {
		super(p_i48440_1_);
		setDefaultState(getDefaultState().with(SHAPE, Shape.NORMAL)
			.with(FACING, Direction.DOWN));
	}

	public enum Shape implements IStringSerializable {
		INTERSECTION, WINDOW, NORMAL;

		@Override
		public String getString() {
			return Lang.asId(name());
		}
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.CHUTE.create();
	}

	@Override
	public Direction getFacing(BlockState state) {
		return state.get(FACING);
	}

	@Override
	public boolean isOpen(BlockState state) {
		return state.get(FACING) == Direction.DOWN || state.get(SHAPE) == Shape.INTERSECTION;
	}

	@Override
	public boolean isTransparent(BlockState state) {
		return state.get(SHAPE) == Shape.WINDOW;
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		Shape shape = state.get(SHAPE);
		boolean down = state.get(FACING) == Direction.DOWN;
		if (!context.getWorld().isRemote && down && shape != Shape.INTERSECTION) {
			context.getWorld()
				.setBlockState(context.getPos(),
					state.with(SHAPE, shape == Shape.WINDOW ? Shape.NORMAL : Shape.WINDOW));
		}
		return ActionResultType.SUCCESS;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext ctx) {
		BlockState state = super.getStateForPlacement(ctx);
		Direction face = ctx.getFace();
		if (face.getAxis()
			.isHorizontal() && !ctx.shouldCancelInteraction()) {
			World world = ctx.getWorld();
			BlockPos pos = ctx.getPos();
			return updateChuteState(state.with(FACING, face), world.getBlockState(pos.up()), world, pos);
		}
		return state;
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> p_206840_1_) {
		super.fillStateContainer(p_206840_1_.add(SHAPE, FACING));
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
		BlockState above = world.getBlockState(pos.up());
		return !isChute(above) || getChuteFacing(above) == Direction.DOWN;
	}

	@Override
	public BlockState updateChuteState(BlockState state, BlockState above, IBlockReader world, BlockPos pos) {
		if (!(state.getBlock() instanceof ChuteBlock))
			return state;

		Map<Direction, Boolean> connections = new HashMap<>();
		int amtConnections = 0;
		Direction facing = state.get(FACING);
		boolean vertical = facing == Direction.DOWN;

		if (!vertical) {
			BlockState target = world.getBlockState(pos.down()
				.offset(facing.getOpposite()));
			if (!isChute(target))
				return state.with(FACING, Direction.DOWN)
					.with(SHAPE, Shape.NORMAL);
		}

		for (Direction direction : Iterate.horizontalDirections) {
			BlockState diagonalInputChute = world.getBlockState(pos.up()
				.offset(direction));
			boolean value =
				diagonalInputChute.getBlock() instanceof ChuteBlock && diagonalInputChute.get(FACING) == direction;
			connections.put(direction, value);
			if (value)
				amtConnections++;
		}

		boolean noConnections = amtConnections == 0;
		if (vertical)
			return state.with(SHAPE,
				noConnections ? state.get(SHAPE) == Shape.WINDOW ? Shape.WINDOW : Shape.NORMAL : Shape.INTERSECTION);
		if (noConnections)
			return state.with(SHAPE, Shape.INTERSECTION);
		if (connections.get(Direction.NORTH) && connections.get(Direction.SOUTH))
			return state.with(SHAPE, Shape.INTERSECTION);
		if (connections.get(Direction.EAST) && connections.get(Direction.WEST))
			return state.with(SHAPE, Shape.INTERSECTION);
		if (amtConnections == 1 && connections.get(facing) && !(getChuteFacing(above) == Direction.DOWN)
			&& !(above.getBlock() instanceof FunnelBlock && FunnelBlock.getFunnelFacing(above) == Direction.DOWN))
			return state.with(SHAPE, Shape.NORMAL);
		return state.with(SHAPE, Shape.INTERSECTION);
	}
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
		return false;
	}

}
