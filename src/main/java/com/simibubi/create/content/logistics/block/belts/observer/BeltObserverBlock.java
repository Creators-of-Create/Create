package com.simibubi.create.content.logistics.block.belts.observer;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltPart;
import com.simibubi.create.content.contraptions.relays.belt.BeltSlope;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class BeltObserverBlock extends HorizontalBlock
		implements ITE<BeltObserverTileEntity>, IWrenchable {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final BooleanProperty BELT = BooleanProperty.create("belt");
	public static final EnumProperty<Mode> MODE = EnumProperty.create("mode", Mode.class);

	public BeltObserverBlock(Properties properties) {
		super(properties);
		setDefaultState(getDefaultState().with(POWERED, false).with(BELT, false));
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.BELT_OBSERVER.create();
	}
	
	@Override
	public PushReaction getPushReaction(BlockState state) {
		return PushReaction.BLOCK;
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		if (facing == stateIn.get(HORIZONTAL_FACING))
			stateIn = stateIn.with(BELT, shouldHaveExtension(stateIn, worldIn, currentPos));
		return stateIn;
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(POWERED, HORIZONTAL_FACING, BELT, MODE);
		super.fillStateContainer(builder);
	}

	private boolean shouldHaveExtension(BlockState state, IWorld world, BlockPos pos) {
		Direction direction = state.get(HORIZONTAL_FACING);
		BlockState blockState = world.getBlockState(pos.offset(direction));

		if (!AllBlocks.BELT.has(blockState))
			return false;
		if (blockState.get(BeltBlock.SLOPE) != BeltSlope.HORIZONTAL)
			return false;
		if (blockState.get(BeltBlock.PART) != BeltPart.MIDDLE)
			return false;
		if (blockState.get(BeltBlock.HORIZONTAL_FACING).getAxis() == direction.getAxis())
			return false;

		return true;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState state = getDefaultState();

		Direction preferredFacing = null;
		for (Direction face : Direction.values()) {
			if (face.getAxis().isVertical())
				continue;

			BlockState blockState = context.getWorld().getBlockState(context.getPos().offset(face));
			if (AllBlocks.BELT.has(blockState)
					&& blockState.get(BlockStateProperties.HORIZONTAL_FACING).getAxis() != face.getAxis()
					&& blockState.get(BeltBlock.SLOPE) == BeltSlope.HORIZONTAL)
				if (preferredFacing == null)
					preferredFacing = face;
				else {
					preferredFacing = null;
					break;
				}
		}

		if (preferredFacing != null) {
			state = state.with(HORIZONTAL_FACING, preferredFacing);
		} else if (context.getFace().getAxis().isHorizontal()) {
			state = state.with(HORIZONTAL_FACING, context.getFace());
		} else {
			state = state.with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing());
		}

		state = state.with(BELT, shouldHaveExtension(state, context.getWorld(), context.getPos()));

		return state;
	}

	@Override
	public boolean canProvidePower(BlockState state) {
		return state.get(POWERED);
	}

	@Override
	public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return canProvidePower(blockState) ? 15 : 0;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
		return side != state.get(HORIZONTAL_FACING).getOpposite();
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
			TileEntityBehaviour.destroy(worldIn, pos, FilteringBehaviour.TYPE);
			worldIn.removeTileEntity(pos);
		}
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		World world = context.getWorld();
		if (!world.isRemote) {
			world.setBlockState(context.getPos(), state.with(POWERED, false).cycle(MODE), 3);
			world.notifyNeighborsOfStateChange(context.getPos(), this);
		}
		return ActionResultType.SUCCESS;
	}

	public enum Mode implements IStringSerializable {
		DETECT, PULSE, EJECT, SPLIT;

		@Override
		public String getString() {
			return Lang.asId(name());
		}
	}

	@Override
	public Class<BeltObserverTileEntity> getTileEntityClass() {
		return BeltObserverTileEntity.class;
	}

}
