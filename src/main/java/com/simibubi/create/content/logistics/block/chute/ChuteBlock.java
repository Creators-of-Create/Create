package com.simibubi.create.content.logistics.block.chute;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class ChuteBlock extends Block implements IWrenchable, ITE<ChuteTileEntity> {
	public static final Property<Shape> SHAPE = EnumProperty.create("shape", Shape.class);
	public static final DirectionProperty FACING = BlockStateProperties.FACING_EXCEPT_UP;

	public enum Shape implements IStringSerializable {
		START(), WINDOW_STRAIGHT(), NORMAL();

		@Override
		public String getString() {
			return Lang.asId(name());
		}
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.CHUTE.create();
	}

	public ChuteBlock(Properties p_i48440_1_) {
		super(p_i48440_1_);
		setDefaultState(getDefaultState().with(SHAPE, Shape.NORMAL)
			.with(FACING, Direction.DOWN));
	}

	@Override
	public void onLanded(IBlockReader worldIn, Entity entityIn) {
		super.onLanded(worldIn, entityIn);
		if (!(entityIn instanceof ItemEntity))
			return;
		if (entityIn.world.isRemote)
			return;
		DirectBeltInputBehaviour input = TileEntityBehaviour.get(entityIn.world, new BlockPos(entityIn.getPositionVec()
			.add(0, 0.5f, 0)).down(), DirectBeltInputBehaviour.TYPE);
		if (input == null)
			return;
		if (!input.canInsertFromSide(Direction.UP))
			return;

		ItemEntity itemEntity = (ItemEntity) entityIn;
		ItemStack toInsert = itemEntity.getItem();
		ItemStack remainder = input.handleInsertion(toInsert, Direction.UP, false);

		if (remainder.isEmpty())
			itemEntity.remove();
		if (remainder.getCount() < toInsert.getCount())
			itemEntity.setItem(remainder);
	}

	@Override
	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState p_220082_4_, boolean p_220082_5_) {
		withTileEntityDo(world, pos, ChuteTileEntity::onAdded);
		if (p_220082_5_)
			return;
		updateDiagonalNeighbour(state, world, pos);
	}

	protected void updateDiagonalNeighbour(BlockState state, World world, BlockPos pos) {
		Direction facing = state.get(FACING);
		BlockPos toUpdate = pos.down();
		if (facing.getAxis()
			.isHorizontal())
			toUpdate = toUpdate.offset(facing.getOpposite());

		BlockState stateToUpdate = world.getBlockState(toUpdate);
		BlockState updated = updateDiagonalState(stateToUpdate, world.getBlockState(toUpdate.up()), world, toUpdate);
		if (stateToUpdate != updated && !world.isRemote)
			world.setBlockState(toUpdate, updated);
	}

	@Override
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState p_196243_4_, boolean p_196243_5_) {
		boolean differentBlock = state.getBlock() != p_196243_4_.getBlock();
		if (state.hasTileEntity() && (differentBlock || !p_196243_4_.hasTileEntity())) {
			withTileEntityDo(world, pos, c -> c.onRemoved(state));
			world.removeTileEntity(pos);
		}
		if (p_196243_5_ || !differentBlock)
			return;

		updateDiagonalNeighbour(state, world, pos);

		for (Direction direction : Iterate.horizontalDirections) {
			BlockPos toUpdate = pos.up()
				.offset(direction);
			BlockState stateToUpdate = world.getBlockState(toUpdate);
			BlockState updated =
				updateDiagonalState(stateToUpdate, world.getBlockState(toUpdate.up()), world, toUpdate);
			if (stateToUpdate != updated && !world.isRemote)
				world.setBlockState(toUpdate, updated);
		}
	}

	@Override
	public BlockState updatePostPlacement(BlockState state, Direction direction, BlockState above, IWorld world,
		BlockPos pos, BlockPos p_196271_6_) {
		if (direction != Direction.UP)
			return state;
		return updateDiagonalState(state, above, world, pos);
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
		BlockState above = world.getBlockState(pos.up());
		return !(above.getBlock() instanceof ChuteBlock) || above.get(FACING) == Direction.DOWN;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext ctx) {
		BlockState state = super.getStateForPlacement(ctx);
		Direction face = ctx.getFace();
		if (face.getAxis()
			.isHorizontal() && !ctx.shouldCancelInteraction()) {
			World world = ctx.getWorld();
			BlockPos pos = ctx.getPos();
			return updateDiagonalState(state.with(FACING, face), world.getBlockState(pos.up()), world, pos);
		}
		return state;
	}

	public static BlockState updateDiagonalState(BlockState state, BlockState above, IBlockReader world, BlockPos pos) {
		if (!(state.getBlock() instanceof ChuteBlock))
			return state;

		Map<Direction, Boolean> connections = new HashMap<>();
		int amtConnections = 0;
		Direction facing = state.get(FACING);

		if (facing == Direction.DOWN)
			return state;
		BlockState target = world.getBlockState(pos.down()
			.offset(facing.getOpposite()));
		if (!(target.getBlock() instanceof ChuteBlock))
			return state.with(FACING, Direction.DOWN)
				.with(SHAPE, Shape.NORMAL);

		for (Direction direction : Iterate.horizontalDirections) {
			BlockState diagonalInputChute = world.getBlockState(pos.up()
				.offset(direction));
			boolean value =
				diagonalInputChute.getBlock() instanceof ChuteBlock && diagonalInputChute.get(FACING) == direction;
			connections.put(direction, value);
			if (value)
				amtConnections++;
		}

		if (amtConnections == 0)
			return state.with(SHAPE, Shape.START);
		if (connections.get(Direction.NORTH) && connections.get(Direction.SOUTH))
			return state.with(SHAPE, Shape.START);
		if (connections.get(Direction.EAST) && connections.get(Direction.WEST))
			return state.with(SHAPE, Shape.START);
		if (amtConnections == 1 && connections.get(facing)
			&& !(above.getBlock() instanceof ChuteBlock && above.get(FACING) == Direction.DOWN))
			return state.with(SHAPE, Shape.WINDOW_STRAIGHT);
		return state.with(SHAPE, Shape.NORMAL);
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		if (!context.getWorld().isRemote && state.get(FACING) == Direction.DOWN)
			context.getWorld()
				.setBlockState(context.getPos(), state.with(SHAPE,
					state.get(SHAPE) == Shape.WINDOW_STRAIGHT ? Shape.NORMAL : Shape.WINDOW_STRAIGHT));
		return ActionResultType.SUCCESS;
	}

	@Override
	public VoxelShape getShape(BlockState p_220053_1_, IBlockReader p_220053_2_, BlockPos p_220053_3_,
		ISelectionContext p_220053_4_) {
		return AllShapes.CHUTE;
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> p_206840_1_) {
		super.fillStateContainer(p_206840_1_.add(SHAPE, FACING));
	}

	@Override
	public Class<ChuteTileEntity> getTileEntityClass() {
		return ChuteTileEntity.class;
	}

	@Override
	public ActionResultType onUse(BlockState p_225533_1_, World world, BlockPos pos, PlayerEntity player, Hand hand,
		BlockRayTraceResult p_225533_6_) {
		if (!player.getHeldItem(hand)
			.isEmpty())
			return ActionResultType.PASS;
		if (world.isRemote)
			return ActionResultType.SUCCESS;
		try {
			ChuteTileEntity te = getTileEntity(world, pos);
			if (te == null)
				return ActionResultType.PASS;
			if (te.item.isEmpty())
				return ActionResultType.PASS;
			player.inventory.placeItemBackInInventory(world, te.item);
			te.setItem(ItemStack.EMPTY);
			return ActionResultType.SUCCESS;

		} catch (TileEntityException e) {
			e.printStackTrace();
		}
		return ActionResultType.PASS;
	}

}
