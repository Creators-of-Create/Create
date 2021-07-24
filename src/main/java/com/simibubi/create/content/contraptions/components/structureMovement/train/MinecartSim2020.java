package com.simibubi.create.content.contraptions.components.structureMovement.train;

import static net.minecraft.entity.Entity.getHorizontalDistanceSqr;

import java.util.Map;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.CapabilityMinecartController;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.MinecartController;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.item.minecart.FurnaceMinecartEntity;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.common.util.LazyOptional;

/**
 * Useful methods for dealing with Minecarts
 *
 */
public class MinecartSim2020 {
	private static final Map<RailShape, Pair<Vector3i, Vector3i>> MATRIX =
		Util.make(Maps.newEnumMap(RailShape.class), (map) -> {
			Vector3i west = Direction.WEST.getNormal();
			Vector3i east = Direction.EAST.getNormal();
			Vector3i north = Direction.NORTH.getNormal();
			Vector3i south = Direction.SOUTH.getNormal();
			map.put(RailShape.NORTH_SOUTH, Pair.of(north, south));
			map.put(RailShape.EAST_WEST, Pair.of(west, east));
			map.put(RailShape.ASCENDING_EAST, Pair.of(west.below(), east));
			map.put(RailShape.ASCENDING_WEST, Pair.of(west, east.below()));
			map.put(RailShape.ASCENDING_NORTH, Pair.of(north, south.below()));
			map.put(RailShape.ASCENDING_SOUTH, Pair.of(north.below(), south));
			map.put(RailShape.SOUTH_EAST, Pair.of(south, east));
			map.put(RailShape.SOUTH_WEST, Pair.of(south, west));
			map.put(RailShape.NORTH_WEST, Pair.of(north, west));
			map.put(RailShape.NORTH_EAST, Pair.of(north, east));
		});

	public static Vector3d predictNextPositionOf(AbstractMinecartEntity cart) {
		Vector3d position = cart.position();
		Vector3d motion = VecHelper.clamp(cart.getDeltaMovement(), 1f);
		return position.add(motion);
	}

	public static boolean canAddMotion(AbstractMinecartEntity c) {
		if (c instanceof FurnaceMinecartEntity)
			return MathHelper.equal(((FurnaceMinecartEntity) c).xPush, 0)
				&& MathHelper.equal(((FurnaceMinecartEntity) c).zPush, 0);
		LazyOptional<MinecartController> capability =
			c.getCapability(CapabilityMinecartController.MINECART_CONTROLLER_CAPABILITY);
		if (capability.isPresent() && capability.orElse(null)
			.isStalled())
			return false;
		return true;
	}

	public static void moveCartAlongTrack(AbstractMinecartEntity cart, Vector3d forcedMovement, BlockPos cartPos,
		BlockState trackState) {

		if (forcedMovement.equals(Vector3d.ZERO))
			return;

		Vector3d previousMotion = cart.getDeltaMovement();
		cart.fallDistance = 0.0F;

		double x = cart.getX();
		double y = cart.getY();
		double z = cart.getZ();

		double actualX = x;
		double actualY = y;
		double actualZ = z;

		Vector3d actualVec = cart.getPos(actualX, actualY, actualZ);
		actualY = cartPos.getY() + 1;

		AbstractRailBlock abstractrailblock = (AbstractRailBlock) trackState.getBlock();
		RailShape railshape = abstractrailblock.getRailDirection(trackState, cart.level, cartPos, cart);
		switch (railshape) {
		case ASCENDING_EAST:
			forcedMovement = forcedMovement.add(-1 * cart.getSlopeAdjustment(), 0.0D, 0.0D);
			actualY++;
			break;
		case ASCENDING_WEST:
			forcedMovement = forcedMovement.add(cart.getSlopeAdjustment(), 0.0D, 0.0D);
			actualY++;
			break;
		case ASCENDING_NORTH:
			forcedMovement = forcedMovement.add(0.0D, 0.0D, cart.getSlopeAdjustment());
			actualY++;
			break;
		case ASCENDING_SOUTH:
			forcedMovement = forcedMovement.add(0.0D, 0.0D, -1 * cart.getSlopeAdjustment());
			actualY++;
		default:
			break;
		}

		Pair<Vector3i, Vector3i> pair = MATRIX.get(railshape);
		Vector3i Vector3i = pair.getFirst();
		Vector3i Vector3i1 = pair.getSecond();
		double d4 = (double) (Vector3i1.getX() - Vector3i.getX());
		double d5 = (double) (Vector3i1.getZ() - Vector3i.getZ());
//		double d6 = Math.sqrt(d4 * d4 + d5 * d5);
		double d7 = forcedMovement.x * d4 + forcedMovement.z * d5;
		if (d7 < 0.0D) {
			d4 = -d4;
			d5 = -d5;
		}

		double d23 = (double) cartPos.getX() + 0.5D + (double) Vector3i.getX() * 0.5D;
		double d10 = (double) cartPos.getZ() + 0.5D + (double) Vector3i.getZ() * 0.5D;
		double d12 = (double) cartPos.getX() + 0.5D + (double) Vector3i1.getX() * 0.5D;
		double d13 = (double) cartPos.getZ() + 0.5D + (double) Vector3i1.getZ() * 0.5D;
		d4 = d12 - d23;
		d5 = d13 - d10;
		double d14;
		if (d4 == 0.0D) {
			d14 = actualZ - (double) cartPos.getZ();
		} else if (d5 == 0.0D) {
			d14 = actualX - (double) cartPos.getX();
		} else {
			double d15 = actualX - d23;
			double d16 = actualZ - d10;
			d14 = (d15 * d4 + d16 * d5) * 2.0D;
		}

		actualX = d23 + d4 * d14;
		actualZ = d10 + d5 * d14;

		cart.setPos(actualX, actualY, actualZ);
		cart.setDeltaMovement(forcedMovement);
		cart.moveMinecartOnRail(cartPos);

		x = cart.getX();
		y = cart.getY();
		z = cart.getZ();

		if (Vector3i.getY() != 0 && MathHelper.floor(x) - cartPos.getX() == Vector3i.getX()
			&& MathHelper.floor(z) - cartPos.getZ() == Vector3i.getZ()) {
			cart.setPos(x, y + (double) Vector3i.getY(), z);
		} else if (Vector3i1.getY() != 0 && MathHelper.floor(x) - cartPos.getX() == Vector3i1.getX()
			&& MathHelper.floor(z) - cartPos.getZ() == Vector3i1.getZ()) {
			cart.setPos(x, y + (double) Vector3i1.getY(), z);
		}

		x = cart.getX();
		y = cart.getY();
		z = cart.getZ();

		Vector3d Vector3d3 = cart.getPos(x, y, z);
		if (Vector3d3 != null && actualVec != null) {
			double d17 = (actualVec.y - Vector3d3.y) * 0.05D;
			Vector3d Vector3d4 = cart.getDeltaMovement();
			double d18 = Math.sqrt(getHorizontalDistanceSqr(Vector3d4));
			if (d18 > 0.0D) {
				cart.setDeltaMovement(Vector3d4.multiply((d18 + d17) / d18, 1.0D, (d18 + d17) / d18));
			}

			cart.setPos(x, Vector3d3.y, z);
		}

		x = cart.getX();
		y = cart.getY();
		z = cart.getZ();

		int j = MathHelper.floor(x);
		int i = MathHelper.floor(z);
		if (j != cartPos.getX() || i != cartPos.getZ()) {
			Vector3d Vector3d5 = cart.getDeltaMovement();
			double d26 = Math.sqrt(getHorizontalDistanceSqr(Vector3d5));
			cart.setDeltaMovement(d26 * (double) (j - cartPos.getX()), Vector3d5.y, d26 * (double) (i - cartPos.getZ()));
		}

		cart.setDeltaMovement(previousMotion);
	}

	public static Vector3d getRailVec(RailShape shape) {
		switch (shape) {
		case ASCENDING_NORTH:
		case ASCENDING_SOUTH:
		case NORTH_SOUTH:
			return new Vector3d(0, 0, 1);
		case ASCENDING_EAST:
		case ASCENDING_WEST:
		case EAST_WEST:
			return new Vector3d(1, 0, 0);
		case NORTH_EAST:
		case SOUTH_WEST:
			return new Vector3d(1, 0, 1).normalize();
		case NORTH_WEST:
		case SOUTH_EAST:
			return new Vector3d(1, 0, -1).normalize();
		default:
			return new Vector3d(0, 1, 0);
		}
	}

}
