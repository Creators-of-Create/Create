package com.simibubi.create.content.optics;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
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

public interface ILightHandler<T extends SmartTileEntity & ILightHandler<T>> {
	@Nullable
	default Beam constructOutBeam(@Nullable Beam parent, Vector3d beamDirection) {
		return constructOutBeam(parent, beamDirection, getTile().getPos());
	}

	@Nullable
	default Beam constructOutBeam(@Nullable Beam parent, Vector3d beamDirection, BlockPos testBlockPos) {

		float[] segmentColor = parent == null ? DyeColor.WHITE.getColorComponentValues() : parent.getColorAt(testBlockPos);
		World world = getTile().getWorld();
		if (world == null)
			return null;
		Vector3d direction = VecHelper.step(beamDirection);
		Beam beam = new Beam(parent, direction);
		Vector3d testPos = VecHelper.getCenterOf(testBlockPos);

		BeamSegment segment = new BeamSegment(this, segmentColor, testPos, direction);
		beam.add(segment);

		for (int i = 0; i < 128; i++) {
			testPos = testPos.add(direction); // check next block
			testBlockPos = new BlockPos(testPos.x, testPos.y, testPos.z);
			BlockState testState = world.getBlockState(testBlockPos);
			float[] newColor = BeaconHelper.getBeaconColorAt(testState.getBlock());

			TileEntity te = testState.hasTileEntity() ? world.getTileEntity(testBlockPos) : null;
			ILightHandler<?> lightHandler = te instanceof ILightHandler ? (ILightHandler<?>) te : null;
			if (lightHandler != this)
				beam.addListener(lightHandler);

			if (newColor == null) {
				if (testState.getOpacity(world, testBlockPos) >= 15 && testState.getBlock() != Blocks.BEDROCK || (lightHandler != null && !lightHandler.canLightPass())) {
					if (lightHandler != null) {
						lightHandler.setColor(segmentColor);
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

	@Nullable
	default Direction getBeamRotationAround() {
		return null;
	}

	default Stream<Beam> constructSubBeams(Beam beam) {
		return Stream.empty();
	}

	default Iterator<Beam> getRenderBeams() {
		return Collections.emptyIterator();
	}

	default boolean canLightPass() {
		return false;
	}

	default Collection<Beam> getOutBeams() {
		return Collections.emptySet();
	}
}
