package com.simibubi.create.content.optics;

import com.simibubi.create.foundation.utility.BeaconHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.DyeColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public interface ILightHandler<T extends TileEntity & ILightHandler<T>> {
	default Collection<BeamSegment> constructOutBeam(Vector3d beamDirection) {
		ArrayList<BeamSegment> beam = new ArrayList<>();
		float[] segmentColor = getSegmentStartColor();
		World world = getTile().getWorld();
		if (world == null)
			return beam;
		Vector3d direction = VecHelper.step(beamDirection);
		Vector3d testPos = VecHelper.getCenterOf(getTile().getPos());

		BeamSegment segment = new BeamSegment(this, segmentColor, testPos, direction);
		beam.add(segment);

		for (int i = 0; i < 128; i++) {
			testPos = testPos.add(direction); // check next block
			BlockPos testBlockPos = new BlockPos(testPos.x, testPos.y, testPos.z);
			BlockState testState = world.getBlockState(testBlockPos);
			float[] newColor = BeaconHelper.getBeaconColorAt(testState.getBlock());
			if (newColor == null) {
				TileEntity te = testState.hasTileEntity() ? world.getTileEntity(testBlockPos) : null;
				if (testState.getOpacity(world, testBlockPos) >= 15 && testState.getBlock() != Blocks.BEDROCK || te instanceof ILightHandler) {
					if (te instanceof ILightHandler) {
						((ILightHandler<?>) te).setColor(segmentColor);
					}
					break;
				}
			} else if (!Arrays.equals(segmentColor, newColor)) {
				segmentColor = new float[]{(segment.colors[0] + newColor[0]) / 2.0F, (segment.colors[1] + newColor[1]) / 2.0F, (segment.colors[2] + newColor[2]) / 2.0F};
				segment = new BeamSegment(this, newColor, testPos, direction);
				beam.add(segment);
				continue;
			}
			segment.incrementLength();
		}
		return beam;
	}

	T getTile();

	default void setColor(float[] segmentColor) {
	}

	default float[] getSegmentStartColor() {
		return DyeColor.WHITE.getColorComponentValues();
	}

	@Nullable
	default Direction getBeamRotationAround() {
		return null;
	}

}
