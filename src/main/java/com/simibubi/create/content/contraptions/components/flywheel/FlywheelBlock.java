package com.simibubi.create.content.contraptions.components.flywheel;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.content.contraptions.components.flywheel.engine.EngineTileEntity;
import com.simibubi.create.content.contraptions.components.flywheel.engine.FurnaceEngineBlock;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class FlywheelBlock extends HorizontalKineticBlock implements ITE<FlywheelTileEntity> {

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
	public BlockState getStateForPlacement(BlockPlaceContext context) {
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

	public static void setConnection(Level world, BlockPos pos, BlockState state, Direction direction) {
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
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return face == state.getValue(HORIZONTAL_FACING).getOpposite();
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(HORIZONTAL_FACING).getAxis();
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		Direction connection = getConnection(state);
		if (connection == null)
			return super.onWrenched(state ,context);

		if (context.getClickedFace().getAxis() == state.getValue(HORIZONTAL_FACING).getAxis())
			return InteractionResult.PASS;

		Level world = context.getLevel();
		BlockPos enginePos = context.getClickedPos().relative(connection, 2);
		BlockState engine = world.getBlockState(enginePos);
		if (engine.getBlock() instanceof FurnaceEngineBlock)
			((FurnaceEngineBlock) engine.getBlock()).withTileEntityDo(world, enginePos, EngineTileEntity::detachWheel);

		return super.onWrenched(state.setValue(CONNECTION, ConnectionState.NONE), context);
	}

	public enum ConnectionState implements StringRepresentable {
		NONE, LEFT, RIGHT;

		@Override
		public String getSerializedName() {
			return Lang.asId(name());
		}
	}

	@Override
	public Class<FlywheelTileEntity> getTileEntityClass() {
		return FlywheelTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends FlywheelTileEntity> getTileEntityType() {
		return AllTileEntities.FLYWHEEL.get();
	}

}
