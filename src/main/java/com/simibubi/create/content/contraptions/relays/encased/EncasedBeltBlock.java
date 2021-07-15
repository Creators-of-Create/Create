package com.simibubi.create.content.contraptions.relays.encased;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;

import net.minecraft.block.AbstractBlock.Properties;

public class EncasedBeltBlock extends RotatedPillarKineticBlock {

	public static final Property<Part> PART = EnumProperty.create("part", Part.class);
	public static final BooleanProperty CONNECTED_ALONG_FIRST_COORDINATE =
		DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;

	public EncasedBeltBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(PART, Part.NONE));
	}

	@Override
	public boolean shouldCheckWeakPower(BlockState state, IWorldReader world, BlockPos pos, Direction side) {
		return false;
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.NORMAL;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(PART, CONNECTED_ALONG_FIRST_COORDINATE));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Axis placedAxis = context.getNearestLookingDirection()
			.getAxis();
		Axis axis = context.getPlayer() != null && context.getPlayer()
			.isShiftKeyDown() ? placedAxis : getPreferredAxis(context);
		if (axis == null)
			axis = placedAxis;

		BlockState state = defaultBlockState().setValue(AXIS, axis);
		for (Direction facing : Iterate.directions) {
			if (facing.getAxis() == axis)
				continue;
			BlockPos pos = context.getClickedPos();
			BlockPos offset = pos.relative(facing);
			state = updateShape(state, facing, context.getLevel()
				.getBlockState(offset), context.getLevel(), pos, offset);
		}
		return state;
	}

	@Override
	public BlockState updateShape(BlockState stateIn, Direction face, BlockState neighbour, IWorld worldIn,
		BlockPos currentPos, BlockPos facingPos) {
		Part part = stateIn.getValue(PART);
		Axis axis = stateIn.getValue(AXIS);
		boolean connectionAlongFirst = stateIn.getValue(CONNECTED_ALONG_FIRST_COORDINATE);
		Axis connectionAxis =
			connectionAlongFirst ? (axis == Axis.X ? Axis.Y : Axis.X) : (axis == Axis.Z ? Axis.Y : Axis.Z);

		Axis faceAxis = face.getAxis();
		boolean facingAlongFirst = axis == Axis.X ? faceAxis.isVertical() : faceAxis == Axis.X;
		boolean positive = face.getAxisDirection() == AxisDirection.POSITIVE;

		if (axis == faceAxis)
			return stateIn;

		if (!(neighbour.getBlock() instanceof EncasedBeltBlock)) {
			if (facingAlongFirst != connectionAlongFirst || part == Part.NONE)
				return stateIn;
			if (part == Part.MIDDLE)
				return stateIn.setValue(PART, positive ? Part.END : Part.START);
			if ((part == Part.START) == positive)
				return stateIn.setValue(PART, Part.NONE);
			return stateIn;
		}

		Part otherPart = neighbour.getValue(PART);
		Axis otherAxis = neighbour.getValue(AXIS);
		boolean otherConnection = neighbour.getValue(CONNECTED_ALONG_FIRST_COORDINATE);
		Axis otherConnectionAxis =
			otherConnection ? (otherAxis == Axis.X ? Axis.Y : Axis.X) : (otherAxis == Axis.Z ? Axis.Y : Axis.Z);

		if (neighbour.getValue(AXIS) == faceAxis)
			return stateIn;
		if (otherPart != Part.NONE && otherConnectionAxis != faceAxis)
			return stateIn;

		if (part == Part.NONE) {
			part = positive ? Part.START : Part.END;
			connectionAlongFirst = axis == Axis.X ? faceAxis.isVertical() : faceAxis == Axis.X;
		} else if (connectionAxis != faceAxis) {
			return stateIn;
		}

		if ((part == Part.START) != positive)
			part = Part.MIDDLE;

		return stateIn.setValue(PART, part)
			.setValue(CONNECTED_ALONG_FIRST_COORDINATE, connectionAlongFirst);
	}

	@Override
	public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
		if (originalState.getValue(PART) == Part.NONE)
			return super.getRotatedBlockState(originalState, targetedFace);
		return super.getRotatedBlockState(originalState,
			Direction.get(AxisDirection.POSITIVE, getConnectionAxis(originalState)));
	}

	@Override
	public BlockState updateAfterWrenched(BlockState newState, ItemUseContext context) {
//		Blocks.AIR.getDefaultState()
//			.updateNeighbors(context.getWorld(), context.getPos(), 1);
		Axis axis = newState.getValue(AXIS);
		newState = defaultBlockState().setValue(AXIS, axis);
		if (newState.hasProperty(BlockStateProperties.POWERED))
			newState = newState.setValue(BlockStateProperties.POWERED, context.getLevel()
				.hasNeighborSignal(context.getClickedPos()));
		for (Direction facing : Iterate.directions) {
			if (facing.getAxis() == axis)
				continue;
			BlockPos pos = context.getClickedPos();
			BlockPos offset = pos.relative(facing);
			newState = updateShape(newState, facing, context.getLevel()
				.getBlockState(offset), context.getLevel(), pos, offset);
		}
//		newState.updateNeighbors(context.getWorld(), context.getPos(), 1 | 2);
		return newState;
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == state.getValue(AXIS);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(AXIS);
	}

	public static boolean areBlocksConnected(BlockState state, BlockState other, Direction facing) {
		Part part = state.getValue(PART);
		Axis connectionAxis = getConnectionAxis(state);
		Axis otherConnectionAxis = getConnectionAxis(other);

		if (otherConnectionAxis != connectionAxis)
			return false;
		if (facing.getAxis() != connectionAxis)
			return false;
		if (facing.getAxisDirection() == AxisDirection.POSITIVE && (part == Part.MIDDLE || part == Part.START))
			return true;
		if (facing.getAxisDirection() == AxisDirection.NEGATIVE && (part == Part.MIDDLE || part == Part.END))
			return true;

		return false;
	}

	protected static Axis getConnectionAxis(BlockState state) {
		Axis axis = state.getValue(AXIS);
		boolean connectionAlongFirst = state.getValue(CONNECTED_ALONG_FIRST_COORDINATE);
		Axis connectionAxis =
			connectionAlongFirst ? (axis == Axis.X ? Axis.Y : Axis.X) : (axis == Axis.Z ? Axis.Y : Axis.Z);
		return connectionAxis;
	}

	public static float getRotationSpeedModifier(KineticTileEntity from, KineticTileEntity to) {
		float fromMod = 1;
		float toMod = 1;
		if (from instanceof AdjustablePulleyTileEntity)
			fromMod = ((AdjustablePulleyTileEntity) from).getModifier();
		if (to instanceof AdjustablePulleyTileEntity)
			toMod = ((AdjustablePulleyTileEntity) to).getModifier();
		return fromMod / toMod;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.ENCASED_SHAFT.create();
	}

	public enum Part implements IStringSerializable {
		START, MIDDLE, END, NONE;

		@Override
		public String getSerializedName() {
			return Lang.asId(name());
		}
	}

}
