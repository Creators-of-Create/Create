package com.simibubi.create.modules.contraptions.relays;

import com.google.common.base.Predicates;
import com.simibubi.create.modules.contraptions.base.RotatedPillarKineticBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class EncasedBeltBlock extends RotatedPillarKineticBlock {

	public static final BooleanProperty CONNECTED = BooleanProperty.create("attached");
	public static final DirectionProperty CONNECTED_FACE = DirectionProperty.create("attach_face",
			Predicates.alwaysTrue());

	public EncasedBeltBlock() {
		super(Properties.from(Blocks.ANDESITE));
		setDefaultState(getDefaultState().with(CONNECTED, false));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder);
		builder.add(CONNECTED, CONNECTED_FACE);
	}

	@Override
	public boolean isSolid(BlockState state) {
		return false;
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState state = getDefaultState().with(AXIS, context.getNearestLookingDirection().getAxis());
				
		for (Direction face : Direction.values()) {
			BlockState neighbour = context.getWorld().getBlockState(context.getPos().offset(face));
			if (neighbour.getBlock() != this || neighbour.get(CONNECTED))
				continue;
			if (neighbour.get(AXIS) == face.getAxis())
				continue;
			if (state.get(AXIS) == face.getAxis())
				continue;

			return state.with(CONNECTED, true).with(CONNECTED_FACE, face);
		}
		return state;
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction face, BlockState neighbour, IWorld worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		if (neighbour.getBlock() != this || !neighbour.get(CONNECTED))
			return stateIn;
		if (neighbour.get(CONNECTED_FACE) != face.getOpposite())
			return stateIn;
		if (neighbour.get(AXIS) == face.getAxis())
			return stateIn;

		return stateIn.with(CONNECTED, true).with(CONNECTED_FACE, face);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		super.onReplaced(state, worldIn, pos, newState, isMoving);
		if (!state.get(CONNECTED))
			return;
		BlockPos attached = pos.offset(state.get(CONNECTED_FACE));
		BlockState attachedState = worldIn.getBlockState(attached);
		if (attachedState.getBlock() == this)
			worldIn.setBlockState(attached, attachedState.with(CONNECTED, false), 3);
	}

	@Override
	public boolean isAxisTowards(World world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == state.get(AXIS);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(AXIS);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new AxisTunnelTileEntity();
	}

	@Override
	protected boolean hasStaticPart() {
		return true;
	}

}
