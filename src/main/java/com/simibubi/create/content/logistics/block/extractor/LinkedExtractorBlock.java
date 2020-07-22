package com.simibubi.create.content.logistics.block.extractor;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.logistics.block.AttachedLogisticalBlock;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;

public class LinkedExtractorBlock extends ExtractorBlock {

	public LinkedExtractorBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected BlockState getVerticalDefaultState() {
		return AllBlocks.VERTICAL_LINKED_EXTRACTOR.getDefaultState();
	}

	@Override
	protected BlockState getHorizontalDefaultState() {
		return AllBlocks.LINKED_EXTRACTOR.getDefaultState();
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.LINKED_EXTRACTOR.create();
	}

	@Override
	protected boolean reactsToRedstone() {
		return false;
	}

	public static Pair<Vector3d, Vector3d> getFrequencySlotPosition(BlockState state) {
		float verticalOffset = (state.getBlock() instanceof ExtractorBlock) ? 4f : 6f;

		Vector3d first = VecHelper.voxelSpace(11.5f, verticalOffset, 14f);
		Vector3d second = VecHelper.voxelSpace(11.5f, 4f + verticalOffset, 14f);

		Vector3d firstUpward = VecHelper.voxelSpace(10f, 14f, 11.5f);
		Vector3d secondUpward = VecHelper.voxelSpace(6f, 14f, 11.5f);
		Vector3d firstDownward = VecHelper.voxelSpace(10f, 2f, 11.5f);
		Vector3d secondDownward = VecHelper.voxelSpace(6f, 2f, 11.5f);

		float yRot = AngleHelper.horizontalAngle(state.get(ExtractorBlock.HORIZONTAL_FACING));
		if (AttachedLogisticalBlock.isVertical(state)) {
			Boolean up = state.get(AttachedLogisticalBlock.UPWARD);
			first = up ? firstUpward : firstDownward;
			second = up ? secondUpward : secondDownward;
		}

		first = VecHelper.rotateCentered(first, yRot, Axis.Y);
		second = VecHelper.rotateCentered(second, yRot, Axis.Y);
		return Pair.of(first, second);
	}

	public static Vector3d getFrequencySlotOrientation(BlockState state) {
		boolean vertical = AttachedLogisticalBlock.isVertical(state);
		float horizontalAngle = AngleHelper.horizontalAngle(state.get(ExtractorBlock.HORIZONTAL_FACING));

		float xRot = vertical ? (state.get(UPWARD) ? 90 : 270) : 0;
		float yRot = vertical ? horizontalAngle + 180 : horizontalAngle + 270;
		float zRot = vertical ? 0 : 0;

		return new Vector3d(xRot, yRot, zRot);
	}

	public static class Vertical extends LinkedExtractorBlock {
		public Vertical(Properties properties) {
			super(properties);
		}

		@Override
		protected boolean isVertical() {
			return true;
		}
	}

}
