package com.simibubi.create.modules.logistics.block.belts;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.logistics.block.IBlockWithFrequency;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class LinkedExtractorBlock extends ExtractorBlock implements IBlockWithFrequency {

	private static final List<Pair<Vec3d, Vec3d>> linkItemLocations = new ArrayList<>();

	public LinkedExtractorBlock() {
		super();
		cacheLinkItemLocations();
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new LinkedExtractorTileEntity();
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState state = getDefaultState();

		if (context.getFace().getAxis().isHorizontal()) {
			state = state.with(HORIZONTAL_FACING, context.getFace().getOpposite());
		} else {
			state = AllBlocks.VERTICAL_LINKED_EXTRACTOR.get().getDefaultState();
			state = state.with(VerticalExtractorBlock.UPWARD, context.getFace() != Direction.UP);
			state = state.with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing());
		}

		return state.with(POWERED, Boolean.valueOf(false));
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
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		return super.onBlockActivated(state, worldIn, pos, player, handIn, hit)
				|| handleActivatedFrequencySlots(state, worldIn, pos, player, handIn, hit);
	}

	private void cacheLinkItemLocations() {
		linkItemLocations.clear();

		float zFightOffset = 1 / 128f;
		Vec3d first = new Vec3d(11.5f / 16f + zFightOffset, 4f / 16f, 14f / 16f);
		Vec3d second = new Vec3d(11.5f / 16f + zFightOffset, 8f / 16f, 14f / 16f);

		Vec3d firstUpward = new Vec3d(10f / 16f + zFightOffset, 14f / 16f, 11.5f / 16f);
		Vec3d secondUpward = new Vec3d(6f / 16f + zFightOffset, 14f / 16f, 11.5f / 16f);
		Vec3d firstDownward = new Vec3d(10f / 16f + zFightOffset, 2f / 16f, 11.5f / 16f);
		Vec3d secondDownward = new Vec3d(6f / 16f + zFightOffset, 2f / 16f, 11.5f / 16f);

		cacheForAllSides(first, second);
		cacheForAllSides(firstUpward, secondUpward);
		cacheForAllSides(firstDownward, secondDownward);
	}

	private void cacheForAllSides(Vec3d first, Vec3d second) {
		for (int i = 0; i < 4; i++) {
			Direction facing = Direction.byHorizontalIndex(i);
			float angle = AngleHelper.horizontalAngle(facing);
			linkItemLocations.add(Pair.of(VecHelper.rotateCentered(first, angle, Axis.Y),
					VecHelper.rotateCentered(second, angle, Axis.Y)));
		}
	}

	@Override
	public float getItemHitboxScale() {
		return 3 / 32f;
	}

	@Override
	public Pair<Vec3d, Vec3d> getFrequencyItemPositions(BlockState state) {
		Direction facing = state.get(HORIZONTAL_FACING);
		Direction extractorFacing = getBlockFacing(state);
		int groupOffset = extractorFacing == Direction.UP ? 4 : extractorFacing == Direction.DOWN ? 8 : 0;
		return linkItemLocations.get(groupOffset + facing.getHorizontalIndex());
	}

	@Override
	public Direction getFrequencyItemFacing(BlockState state) {
		if (getBlockFacing(state).getAxis().isHorizontal())
			return state.get(HORIZONTAL_FACING).rotateYCCW();
		return state.get(HORIZONTAL_FACING);
	}

}
