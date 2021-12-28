package com.simibubi.create.content.contraptions.solver;

import com.simibubi.create.foundation.utility.DirectionHelper;
import com.simibubi.create.foundation.utility.LazyMap;
import com.simibubi.create.content.contraptions.solver.KineticConnections.Type;
import com.simibubi.create.content.contraptions.solver.KineticConnections.Entry;

import net.minecraft.core.Direction;

import java.util.LinkedList;
import java.util.List;

public class AllConnections {

	private static Direction pos(Direction.Axis axis) {
		return Direction.get(Direction.AxisDirection.POSITIVE, axis);
	}

	private static Direction neg(Direction.Axis axis) {
		return Direction.get(Direction.AxisDirection.NEGATIVE, axis);
	}

	public static final KineticConnections EMPTY = new KineticConnections();

	public static final LazyMap<Direction, KineticConnections> HALF_SHAFT
			= new LazyMap<>(dir -> new KineticConnections(new Entry(dir.getNormal(), Type.SHAFT)));

	public static final LazyMap<Direction.Axis, KineticConnections> FULL_SHAFT
			= new LazyMap<>(axis -> HALF_SHAFT.apply(pos(axis)).merge(HALF_SHAFT.apply(neg(axis))));

	public static final LazyMap<Direction.Axis, KineticConnections> LARGE_COG
			= new LazyMap<>(axis -> {
				List<Entry> out = new LinkedList<>();
				Direction cur = DirectionHelper.getPositivePerpendicular(axis);
				for (int i = 0; i < 4; i++) {
					Direction next = DirectionHelper.rotateAround(cur, axis);
					out.add(new Entry(cur.getNormal().multiply(2), Type.LARGE_COG, -1));
					out.add(new Entry(cur.getNormal().relative(pos(axis)), Type.LARGE_COG, -1));
					out.add(new Entry(cur.getNormal().relative(neg(axis)), Type.LARGE_COG, -1));
					out.add(new Entry(cur.getNormal().relative(next), Type.LARGE_COG, Type.SMALL_COG, -2));
					cur = next;
				}
				return new KineticConnections(out);
			});

	public static final LazyMap<Direction.Axis, KineticConnections> SMALL_COG
			= new LazyMap<>(axis -> {
				List<Entry> out = new LinkedList<>();
				Direction cur = DirectionHelper.getPositivePerpendicular(axis);
				for (int i = 0; i < 4; i++) {
					Direction next = DirectionHelper.rotateAround(cur, axis);
					out.add(new Entry(cur.getNormal(), Type.SMALL_COG, -1));
					out.add(new Entry(cur.getNormal().relative(next), Type.SMALL_COG, Type.LARGE_COG, -0.5f));
					cur = next;
				}
				return new KineticConnections(out);
			});

	public static final LazyMap<Direction.Axis, KineticConnections> LARGE_COG_FULL_SHAFT
			= new LazyMap<>(axis -> LARGE_COG.apply(axis).merge(FULL_SHAFT.apply(axis)));

	public static final LazyMap<Direction.Axis, KineticConnections> SMALL_COG_FULL_SHAFT
			= new LazyMap<>(axis -> SMALL_COG.apply(axis).merge(FULL_SHAFT.apply(axis)));

}
