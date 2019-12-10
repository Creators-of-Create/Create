package com.simibubi.create.modules.logistics.block.belts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.IWithTileEntity;
import com.simibubi.create.foundation.utility.AllShapes;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.relays.belt.AllBeltAttachments.BeltAttachmentState;
import com.simibubi.create.modules.contraptions.relays.belt.AllBeltAttachments.IBeltAttachment;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.modules.contraptions.relays.belt.TransportedItemStack;
import com.simibubi.create.modules.logistics.block.IBlockWithFilter;
import com.simibubi.create.modules.logistics.block.IInventoryManipulator;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class BeltFunnelBlock extends HorizontalBlock implements IBeltAttachment, IWithTileEntity<BeltFunnelTileEntity>, IBlockWithFilter {

	public BeltFunnelBlock() {
		super(Properties.from(Blocks.ANDESITE));
		cacheItemPositions();
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		Direction blockFacing = state.get(HORIZONTAL_FACING);
		if (fromPos.equals(pos.offset(blockFacing))) {
			if (!isValidPosition(state, worldIn, pos)) {
				worldIn.destroyBlock(pos, true);
				return;
			}
		}
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
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		BlockPos neighbourPos = pos.offset(state.get(HORIZONTAL_FACING));
		BlockState neighbour = worldIn.getBlockState(neighbourPos);

		if (AllBlocks.BELT.typeOf(neighbour)) {
			return BeltBlock.canAccessFromSide(state.get(HORIZONTAL_FACING), neighbour);
		}

		return !neighbour.getShape(worldIn, pos).isEmpty();
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
		return AllShapes.BELT_FUNNEL.get(state.get(HORIZONTAL_FACING));
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
	public List<BlockPos> getPotentialAttachmentPositions(IWorld world, BlockPos pos, BlockState beltState) {
		return Arrays.asList(pos.up());
	}

	@Override
	public BlockPos getBeltPositionForAttachment(IWorld world, BlockPos pos, BlockState state) {
		return pos.down();
	}

	@Override
	public boolean startProcessingItem(BeltTileEntity te, TransportedItemStack transported, BeltAttachmentState state) {
		return process(te, transported, state);
	}

	@Override
	public boolean processItem(BeltTileEntity te, TransportedItemStack transported, BeltAttachmentState state) {
		Direction movementFacing = te.getMovementFacing();
		if (movementFacing.getAxis() == Axis.Z)
			movementFacing = movementFacing.getOpposite();
		if (movementFacing != te.getWorld().getBlockState(state.attachmentPos)
				.get(HORIZONTAL_FACING))
			return false;
		return process(te, transported, state);
	}

	public boolean process(BeltTileEntity belt, TransportedItemStack transported, BeltAttachmentState state) {
		TileEntity te = belt.getWorld().getTileEntity(state.attachmentPos);
		if (te == null || !(te instanceof BeltFunnelTileEntity))
			return false;
		BeltFunnelTileEntity funnel = (BeltFunnelTileEntity) te;
		ItemStack stack = funnel.tryToInsert(transported.stack);
		transported.stack = stack;
		return true;
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		return handleActivatedFilterSlots(state, worldIn, pos, player, handIn, hit);
	}

	private static final List<Vec3d> itemPositions = new ArrayList<>(Direction.values().length);

	private void cacheItemPositions() {
		itemPositions.clear();

		Vec3d position = Vec3d.ZERO;
		Vec3d shift = VecHelper.getCenterOf(BlockPos.ZERO);
		float zFightOffset = 1 / 128f;

		for (int i = 0; i < 4; i++) {
			Direction facing = Direction.byHorizontalIndex(i);
			position = new Vec3d(8f / 16f + zFightOffset, 9f / 16f, 2.25f / 16f);

			float angle = facing.getHorizontalAngle();
			if (facing.getAxis() == Axis.X)
				angle = -angle;

			position = VecHelper.rotate(position.subtract(shift), angle, Axis.Y).add(shift);

			itemPositions.add(position);
		}
	}

	@Override
	public boolean showsCount() {
		return true;
	}
	
	@Override
	public float getItemHitboxScale() {
		return 1.76f / 16f;
	}

	@Override
	public Vec3d getFilterPosition(BlockState state) {
		Direction facing = state.get(HORIZONTAL_FACING).getOpposite();
		return itemPositions.get(facing.getHorizontalIndex());
	}

	@Override
	public Direction getFilterFacing(BlockState state) {
		return state.get(HORIZONTAL_FACING).getOpposite();
	}

}
