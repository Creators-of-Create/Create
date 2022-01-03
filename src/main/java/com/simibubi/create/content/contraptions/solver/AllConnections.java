package com.simibubi.create.content.contraptions.solver;

import com.simibubi.create.foundation.utility.DirectionHelper;
import com.simibubi.create.foundation.utility.LazyMap;
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

	public static String type(String name, StringRepresentable ext) {
		return name + "." + ext;
	}

	public static final String
		TYPE_SHAFT = "shaft",
		TYPE_LARGE_COG = "large_cog",
		TYPE_SMALL_COG = "small_cog",
		TYPE_SPEED_CONTROLLER_TOP = "speed_controller_top";

	private static Vec3i rhr(Vec3i vec) {
		if (vec.getX() == 0) return new Vec3i(vec.getY(), vec.getZ(), 0);
		if (vec.getY() == 0) return new Vec3i(vec.getZ(), vec.getX(), 0);
		return new Vec3i(vec.getX(), vec.getY(), 0);
	}

	private static Entry largeToLarge(Vec3i diff, Axis from, Axis to) {
		Vec3i rhr = rhr(diff);
		float ratio = rhr.getX() == rhr.getY() ? 1 : rhr.getX();
		return new Entry(diff, type(TYPE_LARGE_COG, from), type(TYPE_LARGE_COG, to), ratio);
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
				new KineticConnections(
						new Entry(dir.getNormal(), type(TYPE_SHAFT, dir), type(TYPE_SHAFT, dir.getOpposite())))),

			HALF_SHAFT_REVERSER = new LazyMap<>(dir ->
					new KineticConnections(
							new Entry(dir.getNormal(), type(TYPE_SHAFT, dir), type(TYPE_SHAFT, dir.getOpposite()), -1))),

			FULL_SHAFT_REVERSER = new LazyMap<>(dir ->
					HALF_SHAFT.apply(dir).merge(HALF_SHAFT_REVERSER.apply(dir.getOpposite())));

	public static final LazyMap<Axis, KineticConnections>
			FULL_SHAFT = new LazyMap<>(axis -> HALF_SHAFT.apply(pos(axis)).merge(HALF_SHAFT.apply(neg(axis)))),

			LARGE_COG = new LazyMap<>(axis -> {
				String large = type(TYPE_LARGE_COG, axis);
				String small = type(TYPE_SMALL_COG, axis);

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
					String sc = type(TYPE_SPEED_CONTROLLER_TOP, opp);
					out.add(new Entry(Direction.DOWN.getNormal(), large, sc).stressOnly());
				});
				return new KineticConnections(out);
			}),

			SMALL_COG = new LazyMap<>(axis -> {
				String large = type(TYPE_LARGE_COG, axis);
				String small = type(TYPE_SMALL_COG, axis);

				List<Entry> out = new LinkedList<>();
				Direction cur = DirectionHelper.getPositivePerpendicular(axis);
				for (int i = 0; i < 4; i++) {
					Direction next = DirectionHelper.rotateAround(cur, axis);
					out.add(new Entry(cur.getNormal(), small, small, i < 2 ? -1 : 1));
					out.add(new Entry(cur.getNormal().relative(next), small, large));
					cur = next;
				}
				return new KineticConnections(out);
			}),

			LARGE_COG_FULL_SHAFT = new LazyMap<>(axis -> LARGE_COG.apply(axis).merge(FULL_SHAFT.apply(axis))),

			SMALL_COG_FULL_SHAFT = new LazyMap<>(axis -> SMALL_COG.apply(axis).merge(FULL_SHAFT.apply(axis))),

			SPEED_CONTROLLER = new LazyMap<>(axis -> {
				String sc = type(TYPE_SPEED_CONTROLLER_TOP, axis);
				String large = type(TYPE_LARGE_COG, oppAxis(axis).get());
				Vec3i up = Direction.UP.getNormal();
				return new KineticConnections(new Entry(up, sc, large).stressOnly()).merge(FULL_SHAFT.apply(axis));
			});

}
