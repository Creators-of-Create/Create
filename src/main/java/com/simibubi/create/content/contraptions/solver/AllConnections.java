package com.simibubi.create.content.contraptions.solver;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.simibubi.create.foundation.utility.DirectionHelper;
import com.simibubi.create.foundation.utility.LazyMap;
import com.simibubi.create.content.contraptions.solver.KineticConnections.Type;
import com.simibubi.create.content.contraptions.solver.KineticConnections.Types;
import com.simibubi.create.content.contraptions.solver.KineticConnections.Entry;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.util.StringRepresentable;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class AllConnections {

	private static Direction pos(Axis axis) {
		return Direction.get(Direction.AxisDirection.POSITIVE, axis);
	}

	private static Direction neg(Axis axis) {
		return Direction.get(Direction.AxisDirection.NEGATIVE, axis);
	}

	private static Entry largeToLarge(Vec3i diff, Axis from, Axis to) {
		int fromDiff = from.choose(diff.getX(), diff.getY(), diff.getZ());
		int toDiff = to.choose(diff.getX(), diff.getY(), diff.getZ());
		float ratio = fromDiff > 0 ^ toDiff > 0 ? -1 : 1;
		return new Entry(diff, Type.of(Types.LARGE_COG, from), Type.of(Types.LARGE_COG, to), ratio);
	}

	private static Optional<Axis> oppAxis(Axis axis) {
		return switch (axis) {
			case X -> Optional.of(Axis.Z);
			case Z -> Optional.of(Axis.X);
			default -> Optional.empty();
		};
	}


	public static final KineticConnections EMPTY = new KineticConnections();

	public static final LazyMap<Direction, KineticConnections>
			HALF_SHAFT = new LazyMap<>(dir ->
				new KineticConnections(new Entry(dir.getNormal(), Type.of(Types.SHAFT, dir.getAxis()))));

	public static final LazyMap<Axis, KineticConnections>
			FULL_SHAFT = new LazyMap<>(axis -> HALF_SHAFT.apply(pos(axis)).merge(HALF_SHAFT.apply(neg(axis)))),

			LARGE_COG = new LazyMap<>(axis -> {
				Type large = Type.of(Types.LARGE_COG, axis);
				Type small = Type.of(Types.SMALL_COG, axis);

				List<Entry> out = new LinkedList<>();
				Direction cur = DirectionHelper.getPositivePerpendicular(axis);
				for (int i = 0; i < 4; i++) {
					Direction next = DirectionHelper.rotateAround(cur, axis);
					out.add(largeToLarge(cur.getNormal().relative(pos(axis)), axis, cur.getAxis()));
					out.add(largeToLarge(cur.getNormal().relative(neg(axis)), axis, cur.getAxis()));
					out.add(new Entry(cur.getNormal().relative(next), large, small, -2));
					cur = next;
				}

				oppAxis(axis).ifPresent(opp -> {
					Type sc = Type.of(Types.SPEED_CONTROLLER_TOP, opp);
					out.add(new Entry(Direction.DOWN.getNormal(), large, sc).stressOnly());
				});
				return new KineticConnections(out);
			}),

			SMALL_COG = new LazyMap<>(axis -> {
				Type large = Type.of(Types.LARGE_COG, axis);
				Type small = Type.of(Types.SMALL_COG, axis);

				List<Entry> out = new LinkedList<>();
				Direction cur = DirectionHelper.getPositivePerpendicular(axis);
				for (int i = 0; i < 4; i++) {
					Direction next = DirectionHelper.rotateAround(cur, axis);
					out.add(new Entry(cur.getNormal(), small, -1));
					out.add(new Entry(cur.getNormal().relative(next), small, large, -0.5f));
					cur = next;
				}

				return new KineticConnections(out);
			}),

			LARGE_COG_FULL_SHAFT = new LazyMap<>(axis -> LARGE_COG.apply(axis).merge(FULL_SHAFT.apply(axis))),

			SMALL_COG_FULL_SHAFT = new LazyMap<>(axis -> SMALL_COG.apply(axis).merge(FULL_SHAFT.apply(axis))),

			SPEED_CONTROLLER = new LazyMap<>(axis -> {
				Type sc = Type.of(Types.SPEED_CONTROLLER_TOP, axis);
				Type large = Type.of(Types.LARGE_COG, oppAxis(axis).get());
				Vec3i up = Direction.UP.getNormal();
				return new KineticConnections(new Entry(up, sc, large).stressOnly()).merge(FULL_SHAFT.apply(axis));
			});

}
