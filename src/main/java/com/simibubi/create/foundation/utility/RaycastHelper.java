package com.simibubi.create.foundation.utility;

import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class RaycastHelper {

	public static BlockHitResult rayTraceRange(Level worldIn, Player playerIn, double range) {
		Vec3 origin = getTraceOrigin(playerIn);
		Vec3 target = getTraceTarget(playerIn, range, origin);
		ClipContext context = new ClipContext(origin, target, Block.COLLIDER, Fluid.NONE, playerIn);
		return worldIn.clip(context);
	}

	public static PredicateTraceResult rayTraceUntil(Player playerIn, double range,
		Predicate<BlockPos> predicate) {
		Vec3 origin = getTraceOrigin(playerIn);
		Vec3 target = getTraceTarget(playerIn, range, origin);
		return rayTraceUntil(origin, target, predicate);
	}

	public static Vec3 getTraceTarget(Player playerIn, double range, Vec3 origin) {
		float f = playerIn.getXRot();
		float f1 = playerIn.getYRot();
		float f2 = Mth.cos(-f1 * 0.017453292F - (float) Math.PI);
		float f3 = Mth.sin(-f1 * 0.017453292F - (float) Math.PI);
		float f4 = -Mth.cos(-f * 0.017453292F);
		float f5 = Mth.sin(-f * 0.017453292F);
		float f6 = f3 * f4;
		float f7 = f2 * f4;
		double d3 = range;
		Vec3 Vector3d1 = origin.add((double) f6 * d3, (double) f5 * d3, (double) f7 * d3);
		return Vector3d1;
	}

	public static Vec3 getTraceOrigin(Player playerIn) {
		double d0 = playerIn.getX();
		double d1 = playerIn.getY() + (double) playerIn.getEyeHeight();
		double d2 = playerIn.getZ();
		Vec3 Vector3d = new Vec3(d0, d1, d2);
		return Vector3d;
	}

	public static PredicateTraceResult rayTraceUntil(Vec3 start, Vec3 end, Predicate<BlockPos> predicate) {
		if (Double.isNaN(start.x) || Double.isNaN(start.y) || Double.isNaN(start.z))
			return null;
		if (Double.isNaN(end.x) || Double.isNaN(end.y) || Double.isNaN(end.z))
			return null;

		int dx = Mth.floor(end.x);
		int dy = Mth.floor(end.y);
		int dz = Mth.floor(end.z);
		int x = Mth.floor(start.x);
		int y = Mth.floor(start.y);
		int z = Mth.floor(start.z);

		MutableBlockPos currentPos = new BlockPos(x, y, z).mutable();

		if (predicate.test(currentPos))
			return new PredicateTraceResult(currentPos.immutable(), Direction.getNearest(dx - x, dy - y, dz - z));

		int remainingDistance = 200;

		while (remainingDistance-- >= 0) {
			if (Double.isNaN(start.x) || Double.isNaN(start.y) || Double.isNaN(start.z)) {
				return null;
			}

			if (x == dx && y == dy && z == dz) {
				return new PredicateTraceResult();
			}

			boolean flag2 = true;
			boolean flag = true;
			boolean flag1 = true;
			double d0 = 999.0D;
			double d1 = 999.0D;
			double d2 = 999.0D;

			if (dx > x) {
				d0 = (double) x + 1.0D;
			} else if (dx < x) {
				d0 = (double) x + 0.0D;
			} else {
				flag2 = false;
			}

			if (dy > y) {
				d1 = (double) y + 1.0D;
			} else if (dy < y) {
				d1 = (double) y + 0.0D;
			} else {
				flag = false;
			}

			if (dz > z) {
				d2 = (double) z + 1.0D;
			} else if (dz < z) {
				d2 = (double) z + 0.0D;
			} else {
				flag1 = false;
			}

			double d3 = 999.0D;
			double d4 = 999.0D;
			double d5 = 999.0D;
			double d6 = end.x - start.x;
			double d7 = end.y - start.y;
			double d8 = end.z - start.z;

			if (flag2) {
				d3 = (d0 - start.x) / d6;
			}

			if (flag) {
				d4 = (d1 - start.y) / d7;
			}

			if (flag1) {
				d5 = (d2 - start.z) / d8;
			}

			if (d3 == -0.0D) {
				d3 = -1.0E-4D;
			}

			if (d4 == -0.0D) {
				d4 = -1.0E-4D;
			}

			if (d5 == -0.0D) {
				d5 = -1.0E-4D;
			}

			Direction enumfacing;

			if (d3 < d4 && d3 < d5) {
				enumfacing = dx > x ? Direction.WEST : Direction.EAST;
				start = new Vec3(d0, start.y + d7 * d3, start.z + d8 * d3);
			} else if (d4 < d5) {
				enumfacing = dy > y ? Direction.DOWN : Direction.UP;
				start = new Vec3(start.x + d6 * d4, d1, start.z + d8 * d4);
			} else {
				enumfacing = dz > z ? Direction.NORTH : Direction.SOUTH;
				start = new Vec3(start.x + d6 * d5, start.y + d7 * d5, d2);
			}

			x = Mth.floor(start.x) - (enumfacing == Direction.EAST ? 1 : 0);
			y = Mth.floor(start.y) - (enumfacing == Direction.UP ? 1 : 0);
			z = Mth.floor(start.z) - (enumfacing == Direction.SOUTH ? 1 : 0);
			currentPos.set(x, y, z);

			if (predicate.test(currentPos))
				return new PredicateTraceResult(currentPos.immutable(), enumfacing);
		}

		return new PredicateTraceResult();
	}

	public static class PredicateTraceResult {
		private BlockPos pos;
		private Direction facing;

		public PredicateTraceResult(BlockPos pos, Direction facing) {
			this.pos = pos;
			this.facing = facing;
		}

		public PredicateTraceResult() {
			// missed, no result
		}

		public Direction getFacing() {
			return facing;
		}

		public BlockPos getPos() {
			return pos;
		}

		public boolean missed() {
			return this.pos == null;
		}
	}

}
