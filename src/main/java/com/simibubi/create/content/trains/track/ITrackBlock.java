package com.simibubi.create.content.trains.track;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.trains.graph.TrackNodeLocation;
import com.simibubi.create.content.trains.graph.TrackNodeLocation.DiscoveredLocation;
import com.simibubi.create.content.trains.track.TrackTargetingBehaviour.RenderedTrackOverlayType;

import net.createmod.catnip.utility.Iterate;
import net.createmod.catnip.utility.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ITrackBlock {

	public Vec3 getUpNormal(BlockGetter world, BlockPos pos, BlockState state);

	public List<Vec3> getTrackAxes(BlockGetter world, BlockPos pos, BlockState state);

	public Vec3 getCurveStart(BlockGetter world, BlockPos pos, BlockState state, Vec3 axis);

	public default int getYOffsetAt(BlockGetter world, BlockPos pos, BlockState state, Vec3 end) {
		return 0;
	}

	public BlockState getBogeyAnchor(BlockGetter world, BlockPos pos, BlockState state); // should be on bogey side

	public boolean trackEquals(BlockState state1, BlockState state2);

	public default BlockState overlay(BlockGetter world, BlockPos pos, BlockState existing, BlockState placed) {
		return existing;
	}

	public default double getElevationAtCenter(BlockGetter world, BlockPos pos, BlockState state) {
		return isSlope(world, pos, state) ? .5 : 0;
	}

	public static Collection<DiscoveredLocation> walkConnectedTracks(BlockGetter worldIn, TrackNodeLocation location,
		boolean linear) {
		BlockGetter world = location != null && worldIn instanceof ServerLevel sl ? sl.getServer()
			.getLevel(location.dimension) : worldIn;
		List<DiscoveredLocation> list = new ArrayList<>();
		for (BlockPos blockPos : location.allAdjacent()) {
			BlockState blockState = world.getBlockState(blockPos);
			if (blockState.getBlock()instanceof ITrackBlock track)
				list.addAll(track.getConnected(world, blockPos, blockState, linear, location));
		}
		return list;
	}

	public default Collection<DiscoveredLocation> getConnected(BlockGetter worldIn, BlockPos pos, BlockState state,
		boolean linear, @Nullable TrackNodeLocation connectedTo) {
		BlockGetter world = connectedTo != null && worldIn instanceof ServerLevel sl ? sl.getServer()
			.getLevel(connectedTo.dimension) : worldIn;
		Vec3 center = Vec3.atBottomCenterOf(pos)
			.add(0, getElevationAtCenter(world, pos, state), 0);
		List<DiscoveredLocation> list = new ArrayList<>();
		TrackShape shape = state.getValue(TrackBlock.SHAPE);
		List<Vec3> trackAxes = getTrackAxes(world, pos, state);

		trackAxes.forEach(axis -> {
			BiFunction<Double, Boolean, Vec3> offsetFactory = (d, b) -> axis.scale(b ? d : -d)
				.add(center);
			Function<Boolean, ResourceKey<Level>> dimensionFactory =
				b -> world instanceof Level l ? l.dimension() : Level.OVERWORLD;
			Function<Vec3, Integer> yOffsetFactory = v -> getYOffsetAt(world, pos, state, v);

			addToListIfConnected(connectedTo, list, offsetFactory, b -> shape.getNormal(), dimensionFactory,
				yOffsetFactory, axis, null, (b, v) -> getMaterialSimple(world, v));
		});

		return list;
	}

	public static TrackMaterial getMaterialSimple(BlockGetter world, Vec3 pos) {
		return getMaterialSimple(world, pos, TrackMaterial.ANDESITE);
	}

	public static TrackMaterial getMaterialSimple(BlockGetter world, Vec3 pos, TrackMaterial defaultMaterial) {
		if (defaultMaterial == null)
			defaultMaterial = TrackMaterial.ANDESITE;
		if (world != null) {
			Block block = world.getBlockState(BlockPos.containing(pos)).getBlock();
			if (block instanceof ITrackBlock track) {
				return track.getMaterial();
			}
		}
		return defaultMaterial;
	}

	public static void addToListIfConnected(@Nullable TrackNodeLocation fromEnd, Collection<DiscoveredLocation> list,
		BiFunction<Double, Boolean, Vec3> offsetFactory, Function<Boolean, Vec3> normalFactory,
		Function<Boolean, ResourceKey<Level>> dimensionFactory, Function<Vec3, Integer> yOffsetFactory, Vec3 axis,
		BezierConnection viaTurn, BiFunction<Boolean, Vec3, TrackMaterial> materialFactory) {

		Vec3 firstOffset = offsetFactory.apply(0.5d, true);
		DiscoveredLocation firstLocation =
			new DiscoveredLocation(dimensionFactory.apply(true), firstOffset).viaTurn(viaTurn)
				.materialA(materialFactory.apply(true, offsetFactory.apply(0.0d, true)))
				.materialB(materialFactory.apply(true, offsetFactory.apply(1.0d, true)))
				.withNormal(normalFactory.apply(true))
				.withDirection(axis)
				.withYOffset(yOffsetFactory.apply(firstOffset));

		Vec3 secondOffset = offsetFactory.apply(0.5d, false);
		DiscoveredLocation secondLocation =
			new DiscoveredLocation(dimensionFactory.apply(false), secondOffset).viaTurn(viaTurn)
				.materialA(materialFactory.apply(false, offsetFactory.apply(0.0d, false)))
				.materialB(materialFactory.apply(false, offsetFactory.apply(1.0d, false)))
				.withNormal(normalFactory.apply(false))
				.withDirection(axis)
				.withYOffset(yOffsetFactory.apply(secondOffset));

		if (!firstLocation.dimension.equals(secondLocation.dimension)) {
			firstLocation.forceNode();
			secondLocation.forceNode();
		}

		boolean skipFirst = false;
		boolean skipSecond = false;

		if (fromEnd != null) {
			boolean equalsFirst = firstLocation.equals(fromEnd);
			boolean equalsSecond = secondLocation.equals(fromEnd);

			// not reachable from this end
			if (!equalsFirst && !equalsSecond)
				return;

			if (equalsFirst)
				skipFirst = true;
			if (equalsSecond)
				skipSecond = true;
		}

		if (!skipFirst)
			list.add(firstLocation);
		if (!skipSecond)
			list.add(secondLocation);
	}

	@OnlyIn(Dist.CLIENT)
	public PartialModel prepareTrackOverlay(BlockGetter world, BlockPos pos, BlockState state,
		BezierTrackPointLocation bezierPoint, AxisDirection direction, PoseStack transform,
		RenderedTrackOverlayType type);

	@OnlyIn(Dist.CLIENT)
	public PartialModel prepareAssemblyOverlay(BlockGetter world, BlockPos pos, BlockState state, Direction direction,
		PoseStack ms);

	public default boolean isSlope(BlockGetter world, BlockPos pos, BlockState state) {
		return getTrackAxes(world, pos, state).get(0).y != 0;
	}

	public default Pair<Vec3, AxisDirection> getNearestTrackAxis(BlockGetter world, BlockPos pos, BlockState state,
		Vec3 lookVec) {
		Vec3 best = null;
		double bestDiff = Double.MAX_VALUE;
		for (Vec3 vec3 : getTrackAxes(world, pos, state)) {
			for (int opposite : Iterate.positiveAndNegative) {
				double distanceTo = vec3.normalize()
					.distanceTo(lookVec.scale(opposite));
				if (distanceTo > bestDiff)
					continue;
				bestDiff = distanceTo;
				best = vec3;
			}
		}
		return Pair.of(best, lookVec.dot(best.multiply(1, 0, 1)
			.normalize()) < 0 ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE);
	}

	TrackMaterial getMaterial();

}
