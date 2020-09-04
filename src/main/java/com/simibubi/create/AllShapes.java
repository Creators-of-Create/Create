package com.simibubi.create;

import static net.minecraft.util.Direction.NORTH;
import static net.minecraft.util.Direction.SOUTH;
import static net.minecraft.util.Direction.UP;

import java.util.function.BiFunction;

import com.simibubi.create.content.logistics.block.chute.ChuteShapes;
import com.simibubi.create.foundation.utility.VoxelShaper;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

public class AllShapes {

	// Independent Shapers
	public static final VoxelShaper

	CASING_14PX = shape(0, 0, 0, 16, 14, 16).forDirectional(),
		CASING_12PX = shape(0, 0, 0, 16, 12, 16).forDirectional(),
		CASING_11PX = shape(0, 0, 0, 16, 11, 16).forDirectional(),
		MOTOR_BLOCK = shape(3, 0, 3, 13, 14, 13).forDirectional(),
		FOUR_VOXEL_POLE = shape(6, 0, 6, 10, 16, 10).forAxis(), SIX_VOXEL_POLE = shape(5, 0, 5, 11, 16, 11).forAxis(),
		EIGHT_VOXEL_POLE = shape(4, 0, 4, 12, 16, 12).forAxis(),
		EXTRACTOR = shape(4, 2, 11, 12, 10, 17).forDirectional(SOUTH)
			.withVerticalShapes(cuboid(4, 11, 4, 12, 17, 12)),
		TRANSPOSER = shape(4, 4, -1, 12, 12, 1).add(5, 5, 0, 11, 11, 16)
			.add(4, 4, 11, 12, 12, 17)
			.forDirectional(SOUTH),
		FURNACE_ENGINE = shape(1, 1, 0, 15, 15, 16).add(0, 0, 9, 16, 16, 14)
			.forHorizontal(Direction.SOUTH),
		PORTABLE_STORAGE_INTERFACE = shape(0, 0, 0, 16, 12, 16).add(3, 12, 3, 13, 16, 13)
			.forDirectional(),
		PULLEY = shape(0, 0, 0, 16, 16, 2).add(1, 1, 2, 15, 15, 14)
			.add(0, 0, 14, 16, 16, 16)
			.forHorizontalAxis(),
		SPEED_CONTROLLER = shape(0, 0, 0, 16, 2, 16).add(1, 1, 1, 15, 15, 15)
			.erase(0, 8, 5, 16, 16, 11)
			.add(2, 9, 2, 14, 14, 14)
			.erase(6, 11, 0, 10, 16, 16)
			.forHorizontalAxis(),
		HARVESTER_BASE = shape(0, 2, 0, 16, 14, 3).forDirectional(SOUTH),
		NOZZLE = shape(2, 0, 2, 14, 14, 14).add(1, 13, 1, 15, 15, 15)
			.erase(3, 13, 3, 13, 15, 13)
			.forDirectional(),
		CRANK = shape(5, 0, 5, 11, 6, 11).add(1, 3, 1, 15, 8, 15)
			.forDirectional(),
		CART_ASSEMBLER = shape(0, 12, 0, 16, 16, 16).add(-2, 0, 1, 18, 14, 15)
			.forHorizontalAxis(),
		STOCKPILE_SWITCH = shape(0, 0, 0, 16, 2, 16).add(1, 0, 1, 15, 16, 15)
			.add(0, 14, 0, 16, 16, 16)
			.add(3, 3, -1, 13, 13, 2)
			.forHorizontal(NORTH),
		NIXIE_TUBE = shape(0, 0, 0, 16, 4, 16).add(9, 0, 5, 15, 15, 11)
			.add(1, 0, 5, 7, 15, 11)
			.forHorizontalAxis(),
		NIXIE_TUBE_CEILING = shape(0, 12, 0, 16, 16, 16).add(9, 1, 5, 15, 16, 11)
			.add(1, 1, 5, 7, 16, 11)
			.forHorizontalAxis(),
		FUNNEL = shape(3, -2, 3, 13, 2, 13).add(2, 2, 2, 14, 6, 14)
			.add(1, 6, 1, 15, 10, 15)
			.add(0, 10, 0, 16, 16, 16)
			.forDirectional(UP),
		FUNNEL_COLLISION = shape(3, -2, 3, 13, 2, 13).add(2, 2, 2, 14, 6, 14)
			.add(1, 6, 1, 15, 10, 15)
			.add(0, 10, 0, 16, 13, 16)
			.forDirectional(UP),
		CHUTE_FUNNEL = shape(3, -2, 3, 13, 2, 13).add(2, 2, 2, 14, 6, 14)
			.add(0, 8, 0, 16, 14, 16)
			.add(1, 5, 1, 15, 18, 15)
			.forDirectional(UP),
		BELT_FUNNEL_RETRACTED = shape(3, -5, 14, 13, 13, 19).add(0, -5, 8, 16, 16, 14)
			.forHorizontal(NORTH),
		BELT_FUNNEL_EXTENDED = shape(3, -4, 6, 13, 13, 17).add(2, -4, 10, 14, 14, 14)
			.add(1, -4, 6, 15, 15, 10)
			.add(0, -5, 0, 16, 16, 6)
			.forHorizontal(NORTH),
		PUMP = shape(2, 0, 2, 14, 5, 14).add(4, 0, 4, 12, 16, 12)
			.add(3, 12, 3, 13, 16, 13)
			.forDirectional(Direction.UP)

	;

	// Internally Shared Shapes
	private static final VoxelShape

	PISTON_HEAD = Blocks.PISTON_HEAD.getDefaultState()
		.with(DirectionalBlock.FACING, UP)
		.with(PistonHeadBlock.SHORT, true)
		.getShape(null, null), PISTON_EXTENDED =
			shape(CASING_12PX.get(UP)).add(FOUR_VOXEL_POLE.get(Axis.Y))
				.build(),
		SMALL_GEAR_SHAPE = cuboid(2, 6, 2, 14, 10, 14), LARGE_GEAR_SHAPE = cuboid(0, 6, 0, 16, 10, 16),
		VERTICAL_TABLET_SHAPE = cuboid(3, 1, -1, 13, 15, 3), SQUARE_TABLET_SHAPE = cuboid(2, 2, -1, 14, 14, 3),
		LOGISTICS_TABLE_SLOPE = shape(0, 10, 15, 16, 14, 10.667).add(0, 12, 10.667, 16, 16, 6.333)
			.add(0, 14, 6.333, 16, 18, 2)
			.build(),
		SCHEMATICS_TABLE_SLOPE = shape(0, 10, 16, 16, 14, 11).add(0, 12, 11, 16, 16, 6)
			.add(0, 14, 6, 16, 18, 1)
			.build(),
		TANK_BOTTOM_LID = shape(0, 0, 0, 16, 4, 16).build(), TANK_TOP_LID = shape(0, 12, 0, 16, 16, 16).build()

	;

	// Static Block Shapes
	public static final VoxelShape

	BASIN_BLOCK_SHAPE = shape(0, 2, 0, 16, 16, 16).erase(2, 2, 2, 14, 16, 14)
		.add(2, 0, 2, 14, 2, 14)
		.build(), BASIN_COLLISION_SHAPE =
			shape(0, 2, 0, 16, 13, 16).erase(2, 5, 2, 14, 16, 14)
				.add(2, 0, 2, 14, 2, 14)
				.build(),
		HEATER_BLOCK_SHAPE = shape(2, 0, 2, 14, 14, 14).add(0, 0, 0, 16, 4, 16)
			.build(),
		HEATER_BLOCK_SPECIAL_COLLISION_SHAPE = shape(0, 0, 0, 16, 4, 16).build(),
		CRUSHING_WHEEL_COLLISION_SHAPE = cuboid(0, 0, 0, 16, 22, 16), SEAT = cuboid(0, 0, 0, 16, 8, 16),
		SEAT_COLLISION = cuboid(0, 0, 0, 16, 6, 16),
		MECHANICAL_PROCESSOR_SHAPE = shape(VoxelShapes.fullCube()).erase(4, 0, 4, 12, 16, 12)
			.build(),
		TURNTABLE_SHAPE = shape(1, 4, 1, 15, 8, 15).add(5, 0, 5, 11, 4, 11)
			.build(),
		CRATE_BLOCK_SHAPE = cuboid(1, 0, 1, 15, 14, 15),
		TABLE_POLE_SHAPE = shape(4, 0, 4, 12, 2, 12).add(5, 2, 5, 11, 14, 11)
			.build(),
		BELT_COLLISION_MASK = cuboid(0, 0, 0, 16, 19, 16),
		SCHEMATICANNON_SHAPE = shape(1, 0, 1, 15, 8, 15).add(0.5, 8, 0.5, 15.5, 11, 15.5)
			.build(),
		PULLEY_MAGNET = shape(3, 0, 3, 13, 2, 13).add(FOUR_VOXEL_POLE.get(UP))
			.build(),
		SPOUT = shape(1, 2, 1, 15, 14, 15).add(2, 0, 2, 14, 16, 14)
			.build(),
		MILLSTONE = shape(0, 0, 0, 16, 6, 16).add(2, 6, 2, 14, 13, 14)
			.add(3, 13, 3, 13, 16, 13)
			.build(),
		CUCKOO_CLOCK = shape(1, 0, 1, 15, 19, 15).build(),
		GAUGE_SHAPE_UP = shape(1, 0, 0, 15, 2, 16).add(2, 2, 1, 14, 14, 15)
			.build(),
		MECHANICAL_ARM = shape(2, 0, 2, 14, 10, 14).add(3, 0, 3, 13, 14, 13)
			.build(),
		MECHANICAL_ARM_CEILING = shape(2, 6, 2, 14, 16, 14).add(3, 2, 3, 13, 16, 13)
			.build(),
		CHUTE = shape(1, 8, 1, 15, 16, 15).add(2, 0, 2, 14, 8, 14)
			.build(),
		TANK = shape(1, 0, 1, 15, 16, 15).build(), TANK_TOP = shape(TANK_TOP_LID).add(TANK)
			.build(),
		TANK_BOTTOM = shape(TANK_BOTTOM_LID).add(TANK)
			.build(),
		TANK_TOP_BOTTOM = shape(TANK_BOTTOM_LID).add(TANK_TOP_LID)
			.add(TANK)
			.build(),
		DEPOT = shape(CASING_11PX.get(Direction.UP)).add(1, 11, 1, 15, 13, 15)
			.build()

	;

	// More Shapers
	public static final VoxelShaper

	MECHANICAL_PISTON_HEAD = shape(PISTON_HEAD).forDirectional(), MECHANICAL_PISTON = CASING_12PX,
		MECHANICAL_PISTON_EXTENDED = shape(PISTON_EXTENDED).forDirectional(),
		SMALL_GEAR = shape(SMALL_GEAR_SHAPE).add(SIX_VOXEL_POLE.get(Axis.Y))
			.forAxis(),
		LARGE_GEAR = shape(LARGE_GEAR_SHAPE).add(SIX_VOXEL_POLE.get(Axis.Y))
			.forAxis(),
		LOGISTICAL_CONTROLLER = shape(SQUARE_TABLET_SHAPE).forDirectional(SOUTH),
		REDSTONE_BRIDGE = shape(VERTICAL_TABLET_SHAPE).forDirectional(SOUTH)
			.withVerticalShapes(LOGISTICAL_CONTROLLER.get(UP)),
		LOGISTICS_TABLE = shape(TABLE_POLE_SHAPE).add(LOGISTICS_TABLE_SLOPE)
			.forHorizontal(SOUTH),
		SCHEMATICS_TABLE = shape(TABLE_POLE_SHAPE).add(SCHEMATICS_TABLE_SLOPE)
			.forDirectional(SOUTH),
		CHUTE_SLOPE = shape(ChuteShapes.createSlope()).forHorizontal(SOUTH)

	;

	private static Builder shape(VoxelShape shape) {
		return new Builder(shape);
	}

	private static Builder shape(double x1, double y1, double z1, double x2, double y2, double z2) {
		return shape(cuboid(x1, y1, z1, x2, y2, z2));
	}

	private static VoxelShape cuboid(double x1, double y1, double z1, double x2, double y2, double z2) {
		return Block.makeCuboidShape(x1, y1, z1, x2, y2, z2);
	}

	private static class Builder {
		VoxelShape shape;

		public Builder(VoxelShape shape) {
			this.shape = shape;
		}

		Builder add(VoxelShape shape) {
			this.shape = VoxelShapes.or(this.shape, shape);
			return this;
		}

		Builder add(double x1, double y1, double z1, double x2, double y2, double z2) {
			return add(cuboid(x1, y1, z1, x2, y2, z2));
		}

		Builder erase(double x1, double y1, double z1, double x2, double y2, double z2) {
			this.shape =
				VoxelShapes.combineAndSimplify(shape, cuboid(x1, y1, z1, x2, y2, z2), IBooleanFunction.ONLY_FIRST);
			return this;
		}

		VoxelShape build() {
			return shape;
		}

		VoxelShaper build(BiFunction<VoxelShape, Direction, VoxelShaper> factory, Direction direction) {
			return factory.apply(shape, direction);
		}

		VoxelShaper build(BiFunction<VoxelShape, Axis, VoxelShaper> factory, Axis axis) {
			return factory.apply(shape, axis);
		}

		VoxelShaper forDirectional(Direction direction) {
			return build(VoxelShaper::forDirectional, direction);
		}

		VoxelShaper forAxis() {
			return build(VoxelShaper::forAxis, Axis.Y);
		}

		VoxelShaper forHorizontalAxis() {
			return build(VoxelShaper::forHorizontalAxis, Axis.Z);
		}

		VoxelShaper forHorizontal(Direction direction) {
			return build(VoxelShaper::forHorizontal, direction);
		}

		VoxelShaper forDirectional() {
			return forDirectional(UP);
		}

	}

}
