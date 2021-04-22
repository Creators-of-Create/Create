package com.simibubi.create.content.optics;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;

import javax.annotation.Nullable;

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

public interface ILightHandler {
	default Beam constructOutBeam(Vector3d beamDirection) {
		return constructOutBeam(null, beamDirection);
	}

	default Beam constructOutBeam(@Nullable Beam parent, Vector3d beamDirection) {
		return constructOutBeam(parent, beamDirection, getBlockPos(), DyeColor.WHITE.getColorComponentValues());
	}

	default Beam constructOutBeam(@Nullable Beam parent, Vector3d beamDirection, BlockPos testBlockPos, float[] colorComponentValues) {
		Beam beam = new Beam(parent, getHandlerWorld());
		World world = getHandlerWorld();
		if (world == null)
			return beam;

		float[] segmentColor = parent == null ? colorComponentValues : parent.getColorAt(testBlockPos);
		Vector3d direction = VecHelper.step(beamDirection);

		Vector3d testPos = VecHelper.getCenterOf(testBlockPos);

		BeamSegment segment = new BeamSegment(this, segmentColor, testPos, direction);
		beam.add(segment);

		for (int i = 0; i < getMaxScanRange(); i++) {
			testPos = testPos.add(direction); // check next block
			testBlockPos = new BlockPos(testPos.x, testPos.y, testPos.z);
			BlockState testState = world.getBlockState(testBlockPos);
			float[] newColor = BeaconHelper.getBeaconColorFor(testState.getBlock());

			TileEntity te = testState.hasTileEntity() ? world.getTileEntity(testBlockPos) : null;
			ILightHandler lightHandler = te instanceof ILightHandlerProvider ? ((ILightHandlerProvider) te).getHandler() : null;
			if (lightHandler != this)
				beam.addListener(lightHandler);

			if (newColor == null) {
				if (testState.getOpacity(world, testBlockPos) >= 15 && testState.getBlock() != Blocks.BEDROCK || (lightHandler != null && lightHandler.absorbsLight())) {
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

	default int getMaxScanRange() {
		return 128;
	}

	default World getHandlerWorld() {
		return getTile().getWorld();
	}

	default BlockPos getBlockPos() {
		return getTile().getPos();
	}

	TileEntity getTile();

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

	default boolean absorbsLight() {
		return true;
	}

	void updateBeams();

	@FunctionalInterface
	interface ILightHandlerProvider {
		ILightHandler getHandler();
	}
}
