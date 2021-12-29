package com.simibubi.create.content.contraptions.solver;

import com.simibubi.create.foundation.utility.DirectionHelper;
import com.simibubi.create.foundation.utility.LazyMap;
import com.simibubi.create.content.contraptions.solver.KineticConnections.Type;
import com.simibubi.create.content.contraptions.solver.KineticConnections.Entry;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;

import java.util.LinkedList;
import java.util.List;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS;

public class AllConnections {

	public static record ValueType(Object value) implements Type {
		@Override
		public boolean compatible(Type other) {
			return this == other;
		}

		public static <T> LazyMap<T, Type> map() {
			return new LazyMap<>(ValueType::new);
		}
	}

	public static final LazyMap<Axis, Type>
			TYPE_SHAFT = ValueType.map(),
			TYPE_LARGE_COG = ValueType.map(),
			TYPE_SMALL_COG = ValueType.map();


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
		return new Entry(diff, TYPE_LARGE_COG.apply(from), TYPE_LARGE_COG.apply(to), ratio);
	}


	public static final KineticConnections EMPTY = new KineticConnections();

	public static final LazyMap<Direction, KineticConnections>
			HALF_SHAFT = new LazyMap<>(dir ->
				new KineticConnections(new Entry(dir.getNormal(), TYPE_SHAFT.apply(dir.getAxis()))));

	public static final LazyMap<Axis, KineticConnections>
			FULL_SHAFT = new LazyMap<>(axis -> HALF_SHAFT.apply(pos(axis)).merge(HALF_SHAFT.apply(neg(axis)))),

			LARGE_COG = new LazyMap<>(axis -> {
				Type large = TYPE_LARGE_COG.apply(axis);
				Type small = TYPE_SMALL_COG.apply(axis);
				List<Entry> out = new LinkedList<>();
				Direction cur = DirectionHelper.getPositivePerpendicular(axis);
				for (int i = 0; i < 4; i++) {
					Direction next = DirectionHelper.rotateAround(cur, axis);
					out.add(largeToLarge(cur.getNormal().relative(pos(axis)), axis, cur.getAxis()));
					out.add(largeToLarge(cur.getNormal().relative(neg(axis)), axis, cur.getAxis()));
					out.add(new Entry(cur.getNormal().relative(next), large, small, -2));
					cur = next;
				}
				return new KineticConnections(out);
			}),

			SMALL_COG = new LazyMap<>(axis -> {
				Type large = TYPE_LARGE_COG.apply(axis);
				Type small = TYPE_SMALL_COG.apply(axis);
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

			SMALL_COG_FULL_SHAFT = new LazyMap<>(axis -> SMALL_COG.apply(axis).merge(FULL_SHAFT.apply(axis)));

}
