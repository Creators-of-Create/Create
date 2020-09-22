package com.simibubi.create.content.contraptions.components.structureMovement.train;

import static net.minecraft.entity.Entity.horizontalMag;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionEntity;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.item.minecart.FurnaceMinecartEntity;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

public class MinecartSim2020 {

	private static final Map<RailShape, Pair<Vector3i, Vector3i>> MATRIX =
		Util.make(Maps.newEnumMap(RailShape.class), (p_226574_0_) -> {
			Vector3i vec3i = Direction.WEST.getDirectionVec();
			Vector3i vec3i1 = Direction.EAST.getDirectionVec();
			Vector3i vec3i2 = Direction.NORTH.getDirectionVec();
			Vector3i vec3i3 = Direction.SOUTH.getDirectionVec();
			Vector3i vec3i4 = vec3i.down();
			Vector3i vec3i5 = vec3i1.down();
			Vector3i vec3i6 = vec3i2.down();
			Vector3i vec3i7 = vec3i3.down();
			p_226574_0_.put(RailShape.NORTH_SOUTH, Pair.of(vec3i2, vec3i3));
			p_226574_0_.put(RailShape.EAST_WEST, Pair.of(vec3i, vec3i1));
			p_226574_0_.put(RailShape.ASCENDING_EAST, Pair.of(vec3i4, vec3i1));
			p_226574_0_.put(RailShape.ASCENDING_WEST, Pair.of(vec3i, vec3i5));
			p_226574_0_.put(RailShape.ASCENDING_NORTH, Pair.of(vec3i2, vec3i7));
			p_226574_0_.put(RailShape.ASCENDING_SOUTH, Pair.of(vec3i6, vec3i3));
			p_226574_0_.put(RailShape.SOUTH_EAST, Pair.of(vec3i3, vec3i1));
			p_226574_0_.put(RailShape.SOUTH_WEST, Pair.of(vec3i3, vec3i));
			p_226574_0_.put(RailShape.NORTH_WEST, Pair.of(vec3i2, vec3i));
			p_226574_0_.put(RailShape.NORTH_EAST, Pair.of(vec3i2, vec3i1));
		});

	public static Vector3d predictMotionOf(AbstractMinecartEntity cart) {
		if (cart instanceof FurnaceMinecartEntity) {
			return cart.getPositionVec()
				.subtract(cart.lastTickPosX, cart.lastTickPosY, cart.lastTickPosZ);
		}
		return cart.getMotion().scale(1.03f);
//		if (cart instanceof ContainerMinecartEntity) {
//			ContainerMinecartEntity containerCart = (ContainerMinecartEntity) cart;
//			float f = 0.98F;
//			if (containerCart.isEmpty())
//				return cart.getMotion()
//					.mul(f, 0.0D, f);
//			int i = 15 - Container.calcRedstoneFromInventory(containerCart);
//			f += (float) i * 0.001F;
//			return cart.getMotion()
//				.mul(f, 0.0D, f);
//		}
//		return cart.getMotion()
//			.scale(cart.isBeingRidden() ? 0.997D : 0.96D);
	}

	public static boolean canAddMotion(AbstractMinecartEntity c) {
		if (c instanceof FurnaceMinecartEntity)
			return MathHelper.epsilonEquals(((FurnaceMinecartEntity) c).pushX, 0)
				&& MathHelper.epsilonEquals(((FurnaceMinecartEntity) c).pushZ, 0);
		List<Entity> passengers = c.getPassengers();
		if (passengers.isEmpty())
			return true;
		for (Entity entity : passengers) {
			if (entity instanceof ContraptionEntity) {
				ContraptionEntity contraptionEntity = (ContraptionEntity) entity;
				return !contraptionEntity.isStalled();
			}
		}
		return true;
	}

	public static void moveCartAlongTrack(AbstractMinecartEntity cart, Vector3d forcedMovement, BlockPos cartPos,
		BlockState trackState) {

		if (forcedMovement.equals(Vector3d.ZERO))
			return;

		Vector3d previousMotion = cart.getMotion();
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
		RailShape railshape = abstractrailblock.getRailDirection(trackState, cart.world, cartPos, cart);
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
		Vector3i vec3i = pair.getFirst();
		Vector3i vec3i1 = pair.getSecond();
		double d4 = (double) (vec3i1.getX() - vec3i.getX());
		double d5 = (double) (vec3i1.getZ() - vec3i.getZ());
//		double d6 = Math.sqrt(d4 * d4 + d5 * d5);
		double d7 = forcedMovement.x * d4 + forcedMovement.z * d5;
		if (d7 < 0.0D) {
			d4 = -d4;
			d5 = -d5;
		}

		double d23 = (double) cartPos.getX() + 0.5D + (double) vec3i.getX() * 0.5D;
		double d10 = (double) cartPos.getZ() + 0.5D + (double) vec3i.getZ() * 0.5D;
		double d12 = (double) cartPos.getX() + 0.5D + (double) vec3i1.getX() * 0.5D;
		double d13 = (double) cartPos.getZ() + 0.5D + (double) vec3i1.getZ() * 0.5D;
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

		cart.setPosition(actualX, actualY, actualZ);
		cart.setMotion(forcedMovement);
		cart.moveMinecartOnRail(cartPos);

		x = cart.getX();
		y = cart.getY();
		z = cart.getZ();

		if (vec3i.getY() != 0 && MathHelper.floor(x) - cartPos.getX() == vec3i.getX()
			&& MathHelper.floor(z) - cartPos.getZ() == vec3i.getZ()) {
			cart.setPosition(x, y + (double) vec3i.getY(), z);
		} else if (vec3i1.getY() != 0 && MathHelper.floor(x) - cartPos.getX() == vec3i1.getX()
			&& MathHelper.floor(z) - cartPos.getZ() == vec3i1.getZ()) {
			cart.setPosition(x, y + (double) vec3i1.getY(), z);
		}

		x = cart.getX();
		y = cart.getY();
		z = cart.getZ();

		Vector3d Vector3d3 = cart.getPos(x, y, z);
		if (Vector3d3 != null && actualVec != null) {
			double d17 = (actualVec.y - Vector3d3.y) * 0.05D;
			Vector3d Vector3d4 = cart.getMotion();
			double d18 = Math.sqrt(horizontalMag(Vector3d4));
			if (d18 > 0.0D) {
				cart.setMotion(Vector3d4.mul((d18 + d17) / d18, 1.0D, (d18 + d17) / d18));
			}

			cart.setPosition(x, Vector3d3.y, z);
		}

		x = cart.getX();
		y = cart.getY();
		z = cart.getZ();

		int j = MathHelper.floor(x);
		int i = MathHelper.floor(z);
		if (j != cartPos.getX() || i != cartPos.getZ()) {
			Vector3d Vector3d5 = cart.getMotion();
			double d26 = Math.sqrt(horizontalMag(Vector3d5));
			cart.setMotion(d26 * (double) (j - cartPos.getX()), Vector3d5.y, d26 * (double) (i - cartPos.getZ()));
		}

		cart.setMotion(previousMotion);

		if (cart instanceof FurnaceMinecartEntity) {
//			FurnaceMinecartEntity furnaceCart = (FurnaceMinecartEntity) cart;
//			Vector3d Vector3d = cart.getMotion();
//			double d2 = horizontalMag(Vector3d);
//			double d3 = furnaceCart.pushX * furnaceCart.pushX + furnaceCart.pushZ * furnaceCart.pushZ;
//			if (d3 > 1.0E-4D && d2 > 0.001D) {
//				double d40 = (double) MathHelper.sqrt(d2);
//				double d50 = (double) MathHelper.sqrt(d3);
//				furnaceCart.pushX = Vector3d.x / d40 * d50;
//				furnaceCart.pushZ = Vector3d.z / d40 * d50;
//				furnaceCart.setMotion(Vector3d.mul(0.8D, 0.0D, 0.8D)
//					.add(furnaceCart.pushX, 0.0D, furnaceCart.pushZ));
//			}
		}
	}

}
