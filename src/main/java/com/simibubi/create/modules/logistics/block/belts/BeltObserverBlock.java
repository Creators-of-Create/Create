package com.simibubi.create.modules.logistics.block.belts;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.block.IWithTileEntity;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.relays.belt.AllBeltAttachments.BeltAttachmentState;
import com.simibubi.create.modules.contraptions.relays.belt.AllBeltAttachments.IBeltAttachment;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock.Part;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock.Slope;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.modules.contraptions.relays.belt.TransportedItemStack;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class BeltObserverBlock extends HorizontalBlock
		implements IWithTileEntity<BeltObserverTileEntity>, IBeltAttachment {

	public static BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static BooleanProperty BELT = BooleanProperty.create("belt");

	public BeltObserverBlock() {
		super(Properties.from(Blocks.ANDESITE));
		setDefaultState(getDefaultState().with(POWERED, false).with(BELT, false));
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new BeltObserverTileEntity();
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
		builder.add(POWERED, HORIZONTAL_FACING, BELT);
		super.fillStateContainer(builder);
	}

	private boolean shouldHaveExtension(BlockState state, IWorld world, BlockPos pos) {
		Direction direction = state.get(HORIZONTAL_FACING);
		BlockState blockState = world.getBlockState(pos.offset(direction));

		if (!AllBlocks.BELT.typeOf(blockState))
			return false;
		if (blockState.get(BeltBlock.SLOPE) != Slope.HORIZONTAL)
			return false;
		if (blockState.get(BeltBlock.PART) != Part.MIDDLE)
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
			if (AllBlocks.BELT.typeOf(blockState)
					&& blockState.get(BlockStateProperties.HORIZONTAL_FACING).getAxis() != face.getAxis()
					&& blockState.get(BeltBlock.SLOPE) == Slope.HORIZONTAL)
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
	public List<BlockPos> getPotentialAttachmentPositions(IWorld world, BlockPos pos, BlockState beltState) {
		Direction side = beltState.get(BeltBlock.HORIZONTAL_FACING).rotateY();
		return Arrays.asList(pos.offset(side), pos.offset(side.getOpposite()));
	}

	@Override
	public BlockPos getBeltPositionForAttachment(IWorld world, BlockPos pos, BlockState state) {
		return pos.offset(state.get(HORIZONTAL_FACING));
	}

	@Override
	public boolean isAttachedCorrectly(IWorld world, BlockPos attachmentPos, BlockPos beltPos,
			BlockState attachmentState, BlockState beltState) {
		return attachmentState.get(BELT);
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
		if (newState.getBlock() != this || newState.with(POWERED, false) != state.with(POWERED, false))
			onAttachmentRemoved(worldIn, pos, state);
		if (state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
			worldIn.removeTileEntity(pos);
		}
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (oldState.getBlock() != this || oldState.with(POWERED, false) != state.with(POWERED, false))
			onAttachmentPlaced(worldIn, pos, state);
	}

	@Override
	public boolean startProcessingItem(BeltTileEntity te, TransportedItemStack transported, BeltAttachmentState state) {
		state.processingDuration = 0;
		FilteringBehaviour behaviour = TileEntityBehaviour.get(te.getWorld(), state.attachmentPos,
				FilteringBehaviour.TYPE);
		if (behaviour != null) {
			if (!behaviour.test(transported.stack)) {
				state.processingDuration = -1;
				return false;
			}
		}

		World world = te.getWorld();
		BlockState blockState = world.getBlockState(state.attachmentPos);
		if (state.processingDuration == 0) {
			world.setBlockState(state.attachmentPos, blockState.with(POWERED, true));
			world.getPendingBlockTicks().scheduleTick(state.attachmentPos, this, 6);
			world.notifyNeighborsOfStateChange(state.attachmentPos, this);
			return true;
		}
		return false;
	}

	@Override
	public boolean processEntity(BeltTileEntity te, Entity entity, BeltAttachmentState state) {
		if (te.getWorld().isRemote)
			return false;

		if (state.processingEntity != entity) {
			state.processingEntity = entity;
			state.processingDuration = 0;
		}

		if (entity.getPositionVec().distanceTo(VecHelper.getCenterOf(te.getPos())) > .5f)
			return false;
		if (state.processingDuration == -1) {
			return false;
		}

		World world = te.getWorld();
		BlockState blockState = world.getBlockState(state.attachmentPos);
		if (blockState.get(POWERED))
			return false;

		state.processingDuration = -1;
		world.setBlockState(state.attachmentPos, blockState.with(POWERED, true));
		world.getPendingBlockTicks().scheduleTick(state.attachmentPos, this, 6);
		world.notifyNeighborsOfStateChange(state.attachmentPos, this);
		return false;
	}

	@Override
	public void tick(BlockState state, World worldIn, BlockPos pos, Random random) {
		worldIn.setBlockState(pos, state.with(POWERED, false), 2);
		worldIn.notifyNeighborsOfStateChange(pos, this);
	}

}
