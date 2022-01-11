package com.simibubi.create.content.contraptions.solver;

import com.simibubi.create.content.contraptions.components.structureMovement.gantry.GantryCarriageTileEntity;
import com.simibubi.create.foundation.utility.DirectionHelper;

import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class AllConnections {

	public static void register() {
		Shafts.registerTypes();
		Directional.registerTypes();
		Axial.registerTypes();
		DirAxial.registerTypes();

		Shafts.registerRatios();
		Directional.registerRatios();
		Axial.registerRatios();
		DirAxial.registerRatios();
	}

	private static record Entry(Vec3i offset, String to, float ratio) {}

	public enum Shafts {
		SHAFT("shaft", 1),
		SHAFT_REV("shaft_rev", -1),
		SHAFT_X2("shaft_x2", 2),
		SHAFT_REV_X2("shaft_rev_x2", -2);

		public final String prefix;
		public final float ratio;
		Shafts(String prefix, float ratio) {
			this.prefix = prefix;
			this.ratio = ratio;
		}

		public String type(Direction dir) {
			return prefix + "." + dir;
		}

		public static void registerTypes() {
			for (Shafts value : values()) {
				for (Direction dir : Direction.values()) {
					KineticConnectionsRegistry.registerConnectionType(value.type(dir));
				}
			}
		}

		public static void registerRatios() {
			for (Shafts from : values()) {
				for (Shafts to : values()) {
					for (Direction dir : Direction.values()) {
						KineticConnectionsRegistry.registerConnectionRatio(
								KineticConnectionsRegistry.getConnectionType(from.type(dir)).get(),
								KineticConnectionsRegistry.getConnectionType(to.type(dir.getOpposite())).get(),
								dir.getNormal(),
								from.ratio / to.ratio
						);
					}
				}
			}
		}
	}

	public enum Directional {
		GANTRY_RACK("gantry_rack") {
			@Override
			public List<Entry> genConnections(Direction dir) { return List.of(); }
		};

		public final String prefix;

		Directional(String prefix) {
			this.prefix = prefix;
		}

		public abstract List<Entry> genConnections(Direction dir);

		public String type(Direction dir) {
			return prefix + "." + dir;
		}

		public static void registerTypes() {
			for (Directional value : values()) {
				for (Direction dir : Direction.values()) {
					KineticConnectionsRegistry.registerConnectionType(value.type(dir));
				}
			}
		}

		public static void registerRatios() {
			for (Directional from : values()) {
				for (Direction dir : Direction.values()) {
					String fromType = from.type(dir);
					from.genConnections(dir).forEach(entry -> KineticConnectionsRegistry.registerConnectionRatio(
							KineticConnectionsRegistry.getConnectionType(fromType).get(),
							KineticConnectionsRegistry.getConnectionType(entry.to).get(),
							entry.offset,
							entry.ratio
					));
				}
			}
		}
	}

	public enum Axial {
		SMALL_COG("small_cog") {
			@Override
			public List<Entry> genConnections(Axis axis) {
				List<Entry> out = new LinkedList<>();
				for (Direction cur : Iterate.directionsPerpendicularTo(axis)) {
					Direction next = rot(cur, axis);
					out.add(new Entry(cur.getNormal(), SMALL_COG.type(axis), -1));
					out.add(new Entry(cur.getNormal().relative(next), LARGE_COG.type(axis), -0.5f));
				}
				return out;
			}
		},

		LARGE_COG("large_cog") {
			@Override
			public List<Entry> genConnections(Axis axis) {
				List<Entry> out = new LinkedList<>();
				for (Direction cur : Iterate.directionsPerpendicularTo(axis)) {
					out.add(largeToLarge(cur.getNormal().relative(pos(axis)), axis, cur.getAxis()));
					out.add(largeToLarge(cur.getNormal().relative(neg(axis)), axis, cur.getAxis()));
				}
				return out;
			}
		},

		SPEED_CONTROLLER_TOP("speed_controller_top") {
			@Override
			public List<Entry> genConnections(Axis axis) {
				return oppAxis(axis)
					.map(opp -> List.of(new Entry(Direction.UP.getNormal(), LARGE_COG.type(opp), 0)))
					.orElseGet(List::of);
			}
		};

		public final String prefix;

		Axial(String prefix) {
			this.prefix = prefix;
		}

		public abstract List<Entry> genConnections(Axis axis);

		public String type(Axis axis) {
			return prefix + "." + axis;
		}

		public static void registerTypes() {
			for (Axial value : values()) {
				for (Axis axis : Axis.values()) {
					KineticConnectionsRegistry.registerConnectionType(value.type(axis));
				}
			}
		}

		public static void registerRatios() {
			for (Axial from : values()) {
				for (Axis axis : Axis.values()) {
					String fromType = from.type(axis);
					from.genConnections(axis).forEach(entry -> KineticConnectionsRegistry.registerConnectionRatio(
							KineticConnectionsRegistry.getConnectionType(fromType).get(),
							KineticConnectionsRegistry.getConnectionType(entry.to).get(),
							entry.offset,
							entry.ratio
					));
				}
			}
		}
	}

	public enum DirAxial {
		GANTRY_PINION("gantry_pinion") {
			@Override
			public List<Entry> genConnections(Direction dir, boolean first) {
				Axis shaftAxis = switch (dir.getAxis()) {
					case X -> first ? Axis.Z : Axis.Y;
					case Y -> first ? Axis.Z : Axis.X;
					case Z -> first ? Axis.Y : Axis.X;
				};

				List<Entry> out = new LinkedList<>();
				for (Direction shaftDir : Iterate.directionsInAxis(shaftAxis)) {
					float ratio = GantryCarriageTileEntity.getGantryPinionModifier(shaftDir, dir);
					out.add(new Entry(dir.getOpposite().getNormal(), Directional.GANTRY_RACK.type(shaftDir), ratio));
				}
				return out;
			}
		};

		public final String prefix;

		DirAxial(String prefix) {
			this.prefix = prefix;
		}

		public abstract List<Entry> genConnections(Direction dir, boolean first);

		public String type(Direction dir, boolean first) {
			return prefix + "." + dir + "_" + (first ? "0" : "1");
		}

		public static void registerTypes() {
			for (DirAxial value : values()) {
				for (Direction dir : Direction.values()) {
					for (boolean first : Iterate.trueAndFalse) {
						KineticConnectionsRegistry.registerConnectionType(value.type(dir, first));
					}
				}
			}
		}

		public static void registerRatios() {
			for (DirAxial from : values()) {
				for (Direction dir : Direction.values()) {
					for (boolean first : Iterate.trueAndFalse) {
						String fromType = from.type(dir, first);
						from.genConnections(dir, first).forEach(entry -> KineticConnectionsRegistry.registerConnectionRatio(
								KineticConnectionsRegistry.getConnectionType(fromType).get(),
								KineticConnectionsRegistry.getConnectionType(entry.to).get(),
								entry.offset,
								entry.ratio
						));
					}
				}
			}
		}
	}

	public static Direction pos(Axis axis) {
		return Direction.get(Direction.AxisDirection.POSITIVE, axis);
	}

	public static Direction neg(Axis axis) {
		return Direction.get(Direction.AxisDirection.NEGATIVE, axis);
	}

	public static Direction rot(Direction dir, Axis axis) {
		return DirectionHelper.rotateAround(dir, axis);
	}

	public static float perpendicularRatios(Vec3i diff, Axis from, Axis to) {
		int fromDiff = from.choose(diff.getX(), diff.getY(), diff.getZ());
		int toDiff = to.choose(diff.getX(), diff.getY(), diff.getZ());
		return fromDiff * toDiff;
	}

	private static Entry largeToLarge(Vec3i diff, Axis from, Axis to) {
		return new Entry(diff, Axial.LARGE_COG.type(to), perpendicularRatios(diff, from, to));
	}

	private static Optional<Axis> oppAxis(Axis axis) {
		return switch (axis) {
			case X -> Optional.of(Axis.Z);
			case Z -> Optional.of(Axis.X);
			default -> Optional.empty();
		};
	}

}
