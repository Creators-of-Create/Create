package com.simibubi.create.modules.logistics.block.belts;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.AllShapes;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.modules.logistics.block.IExtractor;
import com.simibubi.create.modules.logistics.block.IHaveFilterSlot;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class ExtractorBlock extends HorizontalBlock implements IHaveFilterSlot {

	public static BooleanProperty POWERED = BlockStateProperties.POWERED;
	protected static final List<Vec3d> filterLocations = new ArrayList<>();

	public ExtractorBlock() {
		super(Properties.from(Blocks.ANDESITE));
		setDefaultState(getDefaultState().with(POWERED, false));
		cacheFilterLocations();
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_FACING, POWERED);
		super.fillStateContainer(builder);
	}

	@Override
	public boolean showsCount() {
		return true;
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new ExtractorTileEntity();
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		return handleActivatedFilterSlots(state, worldIn, pos, player, handIn, hit);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState state = getDefaultState();

		if (context.getFace().getAxis().isHorizontal()) {
			state = state.with(HORIZONTAL_FACING, context.getFace().getOpposite());
		} else {
			state = AllBlocks.VERTICAL_EXTRACTOR.get().getDefaultState();
			state = state.with(VerticalExtractorBlock.UPWARD, context.getFace() != Direction.UP);
			state = state.with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing());
		}

		return state.with(POWERED, Boolean.valueOf(context.getWorld().isBlockPowered(context.getPos())));
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		updateObservedInventory(state, worldIn, pos);
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		Direction facing = getBlockFacing(state);
		BlockPos neighbourPos = pos.offset(facing);
		BlockState neighbour = worldIn.getBlockState(neighbourPos);

		if (AllBlocks.BELT.typeOf(neighbour)) {
			return BeltBlock.canAccessFromSide(facing, neighbour);
		}

		return !neighbour.getShape(worldIn, pos).isEmpty();
	}

	@Override
	public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor) {
		if (world.isRemote())
			return;
		if (!isObserving(state, pos, neighbor))
			return;
		updateObservedInventory(state, world, pos);
	}

	private void updateObservedInventory(BlockState state, IWorldReader world, BlockPos pos) {
		IExtractor extractor = (IExtractor) world.getTileEntity(pos);
		if (extractor == null)
			return;
		extractor.neighborChanged();
	}

	private boolean isObserving(BlockState state, BlockPos pos, BlockPos observing) {
		return observing.equals(pos.offset(getBlockFacing(state)));
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (worldIn.isRemote)
			return;

		Direction blockFacing = getBlockFacing(state);
		if (fromPos.equals(pos.offset(blockFacing))) {
			if (!isValidPosition(state, worldIn, pos)) {
				worldIn.destroyBlock(pos, true);
				return;
			}
		}

		boolean previouslyPowered = state.get(POWERED);
		if (previouslyPowered != worldIn.isBlockPowered(pos)) {
			worldIn.setBlockState(pos, state.cycle(POWERED), 2);
			IExtractor extractor = (IExtractor) worldIn.getTileEntity(pos);
			if (extractor == null)
				return;
			extractor.setLocked(!previouslyPowered);
		}
	}

	public Direction getBlockFacing(BlockState state) {
		return state.get(HORIZONTAL_FACING);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.EXTRACTOR.get(getBlockFacing(state));
	}

	private void cacheFilterLocations() {
		filterLocations.clear();
		float e = 1 / 128f;
		Vec3d offsetForHorizontal = new Vec3d(8f / 16f, 10.5f / 16f + e, 2f / 16f);
		Vec3d offsetForUpward = new Vec3d(8f / 16f, 14.15f / 16f - e, 12.75f / 16f);
		Vec3d offsetForDownward = new Vec3d(8f / 16f, 1.85f / 16f + e, 12.75f / 16f);

		for (Vec3d offset : new Vec3d[] { offsetForHorizontal, offsetForUpward, offsetForDownward }) {
			for (int i = 0; i < 4; i++) {
				Direction facing = Direction.byHorizontalIndex(i);
				float angle = AngleHelper.horizontalAngle(facing);
				filterLocations.add(VecHelper.rotateCentered(offset, angle, Axis.Y));
			}
		}
	}

	@Override
	public float getItemHitboxScale() {
		return 1.76f / 16f;
	}

	@Override
	public Vec3d getFilterPosition(BlockState state) {
		Direction facing = state.get(HORIZONTAL_FACING).getOpposite();
		return filterLocations.get(facing.getHorizontalIndex());
	}

	@Override
	public Direction getFilterFacing(BlockState state) {
		return state.get(HORIZONTAL_FACING).getOpposite();
	}

	@Override
	public PushReaction getPushReaction(BlockState state) {
		return PushReaction.BLOCK;
	}

	public float getFilterAngle(BlockState state) {
		return getBlockFacing(state).getAxis().isHorizontal() ? 0 : 90;
	}

}
