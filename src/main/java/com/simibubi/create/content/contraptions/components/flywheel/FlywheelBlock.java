package com.simibubi.create.content.contraptions.components.flywheel;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.content.contraptions.components.flywheel.engine.EngineTileEntity;
import com.simibubi.create.content.contraptions.components.flywheel.engine.FurnaceEngineBlock;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import net.minecraft.block.AbstractBlock.Properties;

public class FlywheelBlock extends HorizontalKineticBlock {

	public static EnumProperty<ConnectionState> CONNECTION = EnumProperty.create("connection", ConnectionState.class);

	public FlywheelBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(CONNECTION, ConnectionState.NONE));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(CONNECTION));
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.FLYWHEEL.create();
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction preferred = getPreferredHorizontalFacing(context);
		if (preferred != null)
			return defaultBlockState().setValue(HORIZONTAL_FACING, preferred.getOpposite());
		return this.defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection());
	}

	public static boolean isConnected(BlockState state) {
		return getConnection(state) != null;
	}

	public static Direction getConnection(BlockState state) {
		Direction facing = state.getValue(HORIZONTAL_FACING);
		ConnectionState connection = state.getValue(CONNECTION);

		if (connection == ConnectionState.LEFT)
			return facing.getCounterClockWise();
		if (connection == ConnectionState.RIGHT)
			return facing.getClockWise();
		return null;
	}

	public static void setConnection(World world, BlockPos pos, BlockState state, Direction direction) {
		Direction facing = state.getValue(HORIZONTAL_FACING);
		ConnectionState connection = ConnectionState.NONE;

		if (direction == facing.getClockWise())
			connection = ConnectionState.RIGHT;
		if (direction == facing.getCounterClockWise())
			connection = ConnectionState.LEFT;

		world.setBlock(pos, state.setValue(CONNECTION, connection), 18);
		AllTriggers.triggerForNearbyPlayers(AllTriggers.FLYWHEEL, world, pos, 4);
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return face == state.getValue(HORIZONTAL_FACING).getOpposite();
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(HORIZONTAL_FACING).getAxis();
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		Direction connection = getConnection(state);
		if (connection == null)
			return super.onWrenched(state ,context);

		if (context.getClickedFace().getAxis() == state.getValue(HORIZONTAL_FACING).getAxis())
			return ActionResultType.PASS;

		World world = context.getLevel();
		BlockPos enginePos = context.getClickedPos().relative(connection, 2);
		BlockState engine = world.getBlockState(enginePos);
		if (engine.getBlock() instanceof FurnaceEngineBlock)
			((FurnaceEngineBlock) engine.getBlock()).withTileEntityDo(world, enginePos, EngineTileEntity::detachWheel);

		return super.onWrenched(state.setValue(CONNECTION, ConnectionState.NONE), context);
	}

	public enum ConnectionState implements IStringSerializable {
		NONE, LEFT, RIGHT;

		@Override
		public String getSerializedName() {
			return Lang.asId(name());
		}
	}

}
