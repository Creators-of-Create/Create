package com.simibubi.create.modules.logistics.block.belts;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

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

	private static final List<Pair<Vec3d, Vec3d>> itemPositions = new ArrayList<>(Direction.values().length);

	public LinkedExtractorBlock() {
		super();
		cacheItemPositions();
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
		return super.getStateForPlacement(context).with(POWERED, false);
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		return super.onBlockActivated(state, worldIn, pos, player, handIn, hit)
				|| handleActivatedFrequencySlots(state, worldIn, pos, player, handIn, hit);
	}

	private void cacheItemPositions() {
		if (!itemPositions.isEmpty())
			return;

		Vec3d first = Vec3d.ZERO;
		Vec3d second = Vec3d.ZERO;
		Vec3d shift = VecHelper.getCenterOf(BlockPos.ZERO);
		float zFightOffset = 1 / 128f;

		for (int i = 0; i < 4; i++) {
			Direction facing = Direction.byHorizontalIndex(i);
			first = new Vec3d(11.5f / 16f + zFightOffset, 4f / 16f, 14f / 16f);
			second = new Vec3d(11.5f / 16f + zFightOffset, 8f / 16f, 14f / 16f);

			float angle = facing.getHorizontalAngle();
			if (facing.getAxis() == Axis.X)
				angle = -angle;

			first = VecHelper.rotate(first.subtract(shift), angle, Axis.Y).add(shift);
			second = VecHelper.rotate(second.subtract(shift), angle, Axis.Y).add(shift);

			itemPositions.add(Pair.of(first, second));
		}

	}

	@Override
	public float getItemHitboxScale() {
		return 3 / 32f;
	}

	@Override
	public Pair<Vec3d, Vec3d> getFrequencyItemPositions(BlockState state) {
		Direction facing = state.get(HORIZONTAL_FACING);
		return itemPositions.get(facing.getHorizontalIndex());
	}

	@Override
	public Direction getFrequencyItemFacing(BlockState state) {
		return state.get(HORIZONTAL_FACING).rotateYCCW();
	}

}
