package com.simibubi.create.content.logistics.factoryBoard;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class FactoryPanelConnection {
	public static final Codec<FactoryPanelConnection> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		FactoryPanelPosition.CODEC.fieldOf("position").forGetter(i -> i.from),
		Codec.INT.fieldOf("amount").forGetter(i -> i.amount),
		Codec.INT.fieldOf("arrow_bending").forGetter(i -> i.arrowBendMode)
	).apply(instance, FactoryPanelConnection::new));

	public FactoryPanelPosition from;
	public int amount;
	public List<Direction> path;
	public int arrowBendMode;
	public boolean success;

	public WeakReference<Object> cachedSource;

	private int arrowBendModeCurrentPathUses;

	public FactoryPanelConnection(FactoryPanelPosition from, int amount) {
		this(from, amount, -1);
	}

	public FactoryPanelConnection(FactoryPanelPosition from, int amount, int arrowBendMode) {
		this.from = from;
		this.amount = amount;
		this.arrowBendMode = arrowBendMode;
		path = new ArrayList<>();
		success = true;
		arrowBendModeCurrentPathUses = 0;
		cachedSource = new WeakReference<>(null);
	}

	public List<Direction> getPath(Level level, BlockState state, FactoryPanelPosition to) {
		if (!path.isEmpty() && arrowBendModeCurrentPathUses == arrowBendMode)
			return path;

		boolean findSuitable = arrowBendMode == -1;
		arrowBendModeCurrentPathUses = arrowBendMode;

		FactoryPanelBehaviour fromBehaviour = FactoryPanelBehaviour.at(level, to);
		final Vec3 diff = calculatePathDiff(state, to);
		final Vec3 start = fromBehaviour != null ? fromBehaviour.getSlotPositioning()
			.getLocalOffset(level, to.pos(), state)
			.add(Vec3.atLowerCornerOf(to.pos())) : Vec3.ZERO;
		final float xRot = Mth.RAD_TO_DEG * FactoryPanelBlock.getXRot(state);
		final float yRot = Mth.RAD_TO_DEG * FactoryPanelBlock.getYRot(state);

		// When mode is not locked, find one that doesnt intersect with other gauges
		ModeFinder:
		for (int actualMode = 0; actualMode <= 4; actualMode++) {
			path.clear();
			if (!findSuitable && actualMode != arrowBendMode)
				continue;
			boolean desperateOption = actualMode == 4;

			BlockPos toTravelFirst = BlockPos.ZERO;
			BlockPos toTravelLast = BlockPos.containing(diff.scale(2)
				.add(0.1, 0.1, 0.1));

			if (actualMode > 1) {
				boolean flipX = diff.x > 0 ^ (actualMode % 2 == 1);
				boolean flipZ = diff.z > 0 ^ (actualMode % 2 == 0);
				int ceilX = Mth.positiveCeilDiv(toTravelLast.getX(), 2);
				int ceilZ = Mth.positiveCeilDiv(toTravelLast.getZ(), 2);
				int floorZ = Mth.floorDiv(toTravelLast.getZ(), 2);
				int floorX = Mth.floorDiv(toTravelLast.getX(), 2);
				toTravelFirst = new BlockPos(flipX ? floorX : ceilX, 0, flipZ ? floorZ : ceilZ);
				toTravelLast = new BlockPos(!flipX ? floorX : ceilX, 0, !flipZ ? floorZ : ceilZ);
			}

			Direction lastDirection = null;
			Direction currentDirection = null;

			for (BlockPos toTravel : List.of(toTravelFirst, toTravelLast)) {
				boolean zIsFarther = Math.abs(toTravel.getZ()) > Math.abs(toTravel.getX());
				boolean zIsPreferred = desperateOption ? zIsFarther : actualMode % 2 == 1;
				List<Direction> directionOrder =
					zIsPreferred ? List.of(Direction.SOUTH, Direction.NORTH, Direction.WEST, Direction.EAST)
						: List.of(Direction.WEST, Direction.EAST, Direction.SOUTH, Direction.NORTH);

				for (int i = 0; i < 100; i++) {
					if (toTravel.equals(BlockPos.ZERO))
						break;

					for (Direction d : directionOrder) {
						if (lastDirection != null && d == lastDirection.getOpposite())
							continue;
						if (currentDirection == null || toTravel.relative(d)
							.distManhattan(BlockPos.ZERO) < toTravel.relative(currentDirection)
							.distManhattan(BlockPos.ZERO))
							currentDirection = d;
					}

					lastDirection = currentDirection;
					toTravel = toTravel.relative(currentDirection);
					path.add(currentDirection);
				}
			}

			if (findSuitable && !desperateOption) {
				BlockPos travelled = BlockPos.ZERO;
				for (int i = 0; i < path.size() - 1; i++) {
					Direction d = path.get(i);
					travelled = travelled.relative(d);
					Vec3 testOffset = Vec3.atLowerCornerOf(travelled)
						.scale(0.5);
					testOffset = VecHelper.rotate(testOffset, 180, Axis.Y);
					testOffset = VecHelper.rotate(testOffset, xRot + 90, Axis.X);
					testOffset = VecHelper.rotate(testOffset, yRot, Axis.Y);
					Vec3 v = start.add(testOffset);
					if (!level.noCollision(new AABB(v, v).inflate(1 / 128f)))
						continue ModeFinder;
				}
			}

			break;
		}

		return path;
	}

	public Vec3 calculatePathDiff(BlockState state, FactoryPanelPosition to) {
		float xRot = Mth.RAD_TO_DEG * FactoryPanelBlock.getXRot(state);
		float yRot = Mth.RAD_TO_DEG * FactoryPanelBlock.getYRot(state);
		int slotDiffx = to.slot().xOffset - from.slot().xOffset;
		int slotDiffY = to.slot().yOffset - from.slot().yOffset;

		Vec3 diff = Vec3.atLowerCornerOf(to.pos()
			.subtract(from.pos()));
		diff = VecHelper.rotate(diff, -yRot, Axis.Y);
		diff = VecHelper.rotate(diff, -xRot - 90, Axis.X);
		diff = VecHelper.rotate(diff, -180, Axis.Y);
		diff = diff.add(slotDiffx * .5, 0, slotDiffY * .5);
		diff = diff.multiply(1, 0, 1);
		return diff;
	}

}
