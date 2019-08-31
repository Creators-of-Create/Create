package com.simibubi.create.modules.logistics.block;

import java.util.Optional;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.IWithTileEntity;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.relays.belt.AllBeltAttachments.BeltAttachmentState;
import com.simibubi.create.modules.contraptions.relays.belt.AllBeltAttachments.IBeltAttachment;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class BeltFunnelBlock extends HorizontalBlock implements IBeltAttachment, IWithTileEntity<BeltFunnelTileEntity> {

	public static final VoxelShape SHAPE_NORTH = makeCuboidShape(4, 2, -1, 12, 10, 5),
			SHAPE_SOUTH = makeCuboidShape(4, 2, 11, 12, 10, 17), SHAPE_WEST = makeCuboidShape(-1, 2, 4, 5, 10, 12),
			SHAPE_EAST = makeCuboidShape(11, 2, 4, 17, 10, 12);

	public BeltFunnelBlock() {
		super(Properties.from(Blocks.ANDESITE));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new BeltFunnelTileEntity();
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_FACING);
		super.fillStateContainer(builder);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState state = getDefaultState();

		if (context.getFace().getAxis().isHorizontal()) {
			state = state.with(HORIZONTAL_FACING, context.getFace().getOpposite());
		} else {
			state = state.with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing());
		}

		return state;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		Direction facing = state.get(HORIZONTAL_FACING);

		if (facing == Direction.EAST)
			return SHAPE_EAST;
		if (facing == Direction.WEST)
			return SHAPE_WEST;
		if (facing == Direction.SOUTH)
			return SHAPE_SOUTH;
		if (facing == Direction.NORTH)
			return SHAPE_NORTH;

		return VoxelShapes.empty();
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		onAttachmentPlaced(worldIn, pos, state);
		updateObservedInventory(state, worldIn, pos);
	}

	@Override
	public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor) {
		if (!neighbor.equals(pos.offset(state.get(HORIZONTAL_FACING))))
			return;
		updateObservedInventory(state, world, pos);
	}

	private void updateObservedInventory(BlockState state, IWorldReader world, BlockPos pos) {
		IInventoryManipulator te = (IInventoryManipulator) world.getTileEntity(pos);
		if (te == null)
			return;
		te.neighborChanged();
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		onAttachmentRemoved(worldIn, pos, state);
		if (state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
			worldIn.removeTileEntity(pos);
		}
	}

	@Override
	public Optional<BlockPos> getValidAttachmentFor(BeltTileEntity te) {
		BlockPos validPos = te.getPos().up();
		BlockState blockState = te.getWorld().getBlockState(validPos);
		if (blockState.getBlock() != this
				|| blockState.get(HORIZONTAL_FACING).getAxis() != te.getBlockState().get(HORIZONTAL_FACING).getAxis())
			return Optional.empty();
		return Optional.of(validPos);
	}

	@Override
	public Optional<BlockPos> getValidBeltPositionFor(IWorld world, BlockPos pos, BlockState state) {
		BlockPos validPos = pos.down();
		BlockState blockState = world.getBlockState(validPos);
		if (!AllBlocks.BELT.typeOf(blockState)
				|| blockState.get(HORIZONTAL_FACING).getAxis() != state.get(HORIZONTAL_FACING).getAxis())
			return Optional.empty();
		return Optional.of(validPos);
	}

	@Override
	public boolean handleEntity(BeltTileEntity te, Entity entity, BeltAttachmentState state) {
		if (!(entity instanceof ItemEntity))
			return false;
		if (entity.getPositionVec().distanceTo(VecHelper.getCenterOf(te.getPos())) > .4f)
			return false;

		entity.setMotion(Vec3d.ZERO);
		withTileEntityDo(te.getWorld(), state.attachmentPos, funnelTE -> {
			funnelTE.tryToInsert((ItemEntity) entity);
		});

		return true;
	}

}
