package com.simibubi.create.content.contraptions.minecart;

import java.util.Map;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.AllAttachmentTypes;
import com.simibubi.create.content.contraptions.minecart.capability.MinecartController;

import net.createmod.catnip.api.math.VecHelper;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

/**
 * Useful methods for dealing with Minecarts
 *
 */
public class MinecartSim2020 {
	private static final Map<RailShape, Pair<Vec3i, Vec3i>> MATRIX =
		Util.make(Maps.newEnumMap(RailShape.class), (map) -> {
			Vec3i west = Direction.WEST.getUnitVec3i();
			Vec3i east = Direction.EAST.getUnitVec3i();
			Vec3i north = Direction.NORTH.getUnitVec3i();
			Vec3i south = Direction.SOUTH.getUnitVec3i();
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

	public static Vec3 predictNextPositionOf(AbstractMinecart cart) {
		Vec3 position = cart.position();
		Vec3 motion = VecHelper.clamp(cart.getDeltaMovement(), 1f);
		return position.add(motion);
	}

	public static boolean canAddMotion(AbstractMinecart c) {
		if (c instanceof MinecartFurnace furnace)
			return Mth.equal(furnace.push.x, 0)
				&& Mth.equal(furnace.push.z, 0);

		MinecartController controller = c.getData(AllAttachmentTypes.MINECART_CONTROLLER);
		if (controller.isPresent())
			return !controller.isStalled();
		return true;
	}

	public static void moveCartAlongTrack(AbstractMinecart cart, Vec3 forcedMovement, BlockPos cartPos,
		BlockState trackState) {

		if (forcedMovement.equals(Vec3.ZERO))
			return;

		Vec3 previousMotion = cart.getDeltaMovement();
		cart.fallDistance = 0.0F;
		cart.setDeltaMovement(forcedMovement);
		cart.move(net.minecraft.world.entity.MoverType.SELF, forcedMovement);
		cart.setDeltaMovement(previousMotion);
	}

	public static Vec3 getRailVec(RailShape shape) {
		switch (shape) {
		case ASCENDING_NORTH:
		case ASCENDING_SOUTH:
		case NORTH_SOUTH:
			return new Vec3(0, 0, 1);
		case ASCENDING_EAST:
		case ASCENDING_WEST:
		case EAST_WEST:
			return new Vec3(1, 0, 0);
		case NORTH_EAST:
		case SOUTH_WEST:
			return new Vec3(1, 0, 1).normalize();
		case NORTH_WEST:
		case SOUTH_EAST:
			return new Vec3(1, 0, -1).normalize();
		default:
			return new Vec3(0, 1, 0);
		}
	}

}
