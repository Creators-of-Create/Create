package com.simibubi.create.content.optics;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.BeaconHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.DyeColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
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
		Vector3d direction = VecHelper.step(beamDirection).normalize();
		Vector3d testPos = VecHelper.getCenterOf(testBlockPos);


		BeamSegment segment = new BeamSegment(this, segmentColor, testPos, direction);
		beam.add(segment);

		BlockPos lastChecked = testBlockPos;
		Vector3d rayEnd = testPos.add(direction.scale(getMaxScanRange()));

		for (int i = 0; i < getMaxScanRange(); i++) {
			testPos = testPos.add(direction); // check next block

			BlockRayTraceResult raytrace = world
					.rayTraceBlocks(new RayTraceContext(testPos, rayEnd, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, null));

			testBlockPos = raytrace.getPos();
			if (raytrace.getType() == RayTraceResult.Type.MISS || !world.isBlockPresent(testBlockPos)) {
				segment.setLength(getMaxScanRange());
				break;
			}

			double scale =Math.sqrt(lastChecked.distanceSq(testBlockPos.getX(), testBlockPos.getY(), testBlockPos.getZ(), false));
			testPos = testPos.add(direction.scale(Math.max(0, scale - 1)));
			segment.setLength(segment.getLength() + Math.max(scale / direction.length() - 1, 0));

			lastChecked = testBlockPos;

			BlockState testState = world.getBlockState(testBlockPos);
			Block testBlock = testState.getBlock();
			float[] newColor = BeaconHelper.getBeaconColorFor(testState.getBlock());

			ILightHandler lightHandler = null;

			// if possible, don't read for tile entities as often. Only read TEs that we actually know exist and are relevant.
			if (testBlock instanceof ITE && ILightHandlerProvider.class.isAssignableFrom(((ITE<?>) testBlock).getTileEntityClass())) {
				TileEntity te = world.getTileEntity(testBlockPos);
				if (te instanceof ILightHandlerProvider)
					lightHandler = ((ILightHandlerProvider) te).getHandler();
			}

			if (lightHandler != this)
				beam.addListener(lightHandler);

			if (newColor == null) {
				if (testState.getOpacity(world, testBlockPos) >= 15 && testBlock != Blocks.BEDROCK || (lightHandler != null && lightHandler.absorbsLight())) {
					break;
				}
			} else if (!Arrays.equals(segmentColor, newColor)) {
				segmentColor = new float[]{(segment.colors[0] + newColor[0]) / 2.0F, (segment.colors[1] + newColor[1]) / 2.0F, (segment.colors[2] + newColor[2]) / 2.0F};
				segment = new BeamSegment(this, newColor, segment.getStart().add(segment.getDirection().scale(segment.getLength())), direction);
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
