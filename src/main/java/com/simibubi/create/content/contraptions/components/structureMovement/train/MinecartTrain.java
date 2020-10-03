package com.simibubi.create.content.contraptions.components.structureMovement.train;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionEntity;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class MinecartTrain {

	protected ArrayList<MinecartCoupling> couplings;
	protected double momentum;
	boolean complete;

	public MinecartTrain(MinecartCoupling coupling) {
		couplings = new ArrayList<>();
		couplings.add(coupling);
		tickOrder = 1; // start at most stressed
	}

	public void flip(World world) {
		Collections.reverse(couplings);
		couplings.forEach(c -> MinecartCouplingHandler.flipCoupling(world, c));
	}

	public void mergeOnto(World world, MinecartTrain other) {
		AbstractMinecartEntity trailingOfOther = other.couplings.get(other.couplings.size() - 1).connectedCart.get();
		AbstractMinecartEntity leadingOfThis = couplings.get(0).mainCart.get();

		if (trailingOfOther != leadingOfThis)
			flip(world);

		other.couplings.addAll(couplings);
	}

	public int tickOrder;

	public void tickCouplings(World world) {

		// SOFT collision - modify motion of carts with stressed links @t+1
		double sharedMotion = 0;
		int participants = 0;
		boolean stall = false;
		
		for (int i = 0; i < couplings.size(); i++) {
			MinecartCoupling minecartCoupling = couplings.get(i);
			boolean last = i + 1 == couplings.size();
			if (!minecartCoupling.areBothEndsPresent())
				continue;
			participants++;
			sharedMotion += minecartCoupling.mainCart.get()
				.getMotion()
				.length();
			
			List<Entity> passengers = minecartCoupling.mainCart.get().getPassengers();
			if (!passengers.isEmpty() && passengers.get(0) instanceof ContraptionEntity)
				if (((ContraptionEntity) passengers.get(0)).isStalled()) {
					stall = true;
					break;
				}
				

			if (last) {
				participants++;
				sharedMotion += minecartCoupling.connectedCart.get()
					.getMotion()
					.length();
			}
		}

		if (participants == 0)
			return;

		sharedMotion /= participants;

		/*
		 * Tick order testing: 0: start from motion outlier 1: start at most stressed
		 * coupling 2: start at front 3: start at back
		 */

		if (tickOrder == 0) {
			// Iterate starting from biggest outlier in motion
			double maxDiff = 0;
			int argMax = 0;
			for (int i = 0; i < couplings.size(); i++) {
				MinecartCoupling minecartCoupling = couplings.get(i);
				boolean last = i + 1 == couplings.size();
				if (!minecartCoupling.areBothEndsPresent())
					continue;

				double diff = Math.abs(minecartCoupling.mainCart.get()
					.getMotion()
					.length() - sharedMotion);
				if (diff > maxDiff) {
					maxDiff = diff;
					argMax = i;
				}

				if (last) {
					diff = Math.abs(minecartCoupling.connectedCart.get()
						.getMotion()
						.length() - sharedMotion);
					if (diff > maxDiff) {
						maxDiff = diff;
						argMax = i;
					}
				}
			}

			for (boolean hard : Iterate.trueAndFalse) {
				for (int i = argMax - 1; i >= 0; i--)
					if (couplings.get(i)
						.areBothEndsPresent())
						collisionStep(world, couplings.get(i)
							.asCouple()
							.swap(), couplings.get(i).length, hard);
				for (int i = argMax; i < couplings.size(); i++)
					if (couplings.get(i)
						.areBothEndsPresent())
						collisionStep(world, couplings.get(i)
							.asCouple()
							.swap(), couplings.get(i).length, hard);
			}
			return;
		}

		if (tickOrder == 1) {
			// Iterate starting from biggest stress
			double maxStress = 0;
			int argMax = 0;
			for (int i = 0; i < couplings.size(); i++) {
				MinecartCoupling minecartCoupling = couplings.get(i);
				if (!minecartCoupling.areBothEndsPresent())
					continue;
				
				if (stall) {
					minecartCoupling.asCouple().forEach(ame -> ame.setMotion(Vec3d.ZERO));
					continue;
				}
				
				double stress = getStressOfCoupling(minecartCoupling);
				if (stress > maxStress) {
					maxStress = stress;
					argMax = i;
				}
			}

			for (boolean hard : Iterate.trueAndFalse) {
				for (int i = argMax - 1; i >= 0; i--)
					if (couplings.get(i)
						.areBothEndsPresent())
						collisionStep(world, couplings.get(i)
							.asCouple()
							.swap(), couplings.get(i).length, hard);
				for (int i = argMax; i < couplings.size(); i++)
					if (couplings.get(i)
						.areBothEndsPresent())
						collisionStep(world, couplings.get(i)
							.asCouple()
							.swap(), couplings.get(i).length, hard);
			}
			return;
		}

		if (momentum >= 0 == (tickOrder == 2)) {
			// Iterate front to back
			for (boolean hard : Iterate.trueAndFalse)
				for (int i = 0; i < couplings.size(); i++)
					if (couplings.get(i)
						.areBothEndsPresent())
						collisionStep(world, couplings.get(i)
							.asCouple()
							.swap(), couplings.get(i).length, hard);

		} else {
			// Iterate back to front
			for (boolean hard : Iterate.trueAndFalse)
				for (int i = couplings.size() - 1; i >= 0; i--)
					if (couplings.get(i)
						.areBothEndsPresent())
						collisionStep(world, couplings.get(i)
							.asCouple()
							.swap(), couplings.get(i).length, hard);
		}

	}

	private float getStressOfCoupling(MinecartCoupling coupling) {
		if (!coupling.areBothEndsPresent())
			return 0;
		return (float) (coupling.length - coupling.mainCart.get()
			.getPositionVec()
			.distanceTo(coupling.connectedCart.get()
				.getPositionVec()));
	}

	public void collisionStep(World world, Couple<AbstractMinecartEntity> carts, double couplingLength, boolean hard) {
		if (hard)
			hardCollisionStep(world, carts, couplingLength);
		else
			softCollisionStep(world, carts, couplingLength);
	}

	public void hardCollisionStep(World world, Couple<AbstractMinecartEntity> carts, double couplingLength) {
		Couple<Vector3d> corrections = Couple.create(null, null);
		Couple<Float> maxSpeed = carts.map(AbstractMinecartEntity::getMaxCartSpeedOnRail);
		boolean firstLoop = true;
		for (boolean current : new boolean[] { true, false, true }) {
			AbstractMinecartEntity cart = carts.get(current);
			AbstractMinecartEntity otherCart = carts.get(!current);

			float stress = (float) (couplingLength - cart.getPositionVec()
				.distanceTo(otherCart.getPositionVec()));

			RailShape shape = null;
			BlockPos railPosition = cart.getCurrentRailPosition();
			BlockState railState = world.getBlockState(railPosition.up());

			if (railState.getBlock() instanceof AbstractRailBlock) {
				AbstractRailBlock block = (AbstractRailBlock) railState.getBlock();
				shape = block.getRailDirection(railState, world, railPosition, cart);
			}

			Vector3d correction = Vector3d.ZERO;
			Vector3d pos = cart.getPositionVec();
			Vector3d link = otherCart.getPositionVec()
				.subtract(pos);
			float correctionMagnitude = firstLoop ? -stress / 2f : -stress;
			correction = shape != null ? followLinkOnRail(link, pos, correctionMagnitude, shape).subtract(pos)
				: link.normalize()
					.scale(correctionMagnitude);

			float maxResolveSpeed = 1.75f;
			correction = VecHelper.clamp(correction, Math.min(maxResolveSpeed, maxSpeed.get(current)));

			if (corrections.get(current) == null)
				corrections.set(current, correction);

			if (shape != null)
				MinecartSim2020.moveCartAlongTrack(cart, correction, railPosition, railState);
			else {
				cart.move(MoverType.SELF, correction);
				cart.setMotion(cart.getMotion()
					.scale(0.5f));
			}
			firstLoop = false;
		}
	}

	public void softCollisionStep(World world, Couple<AbstractMinecartEntity> carts, double couplingLength) {

		Couple<Vector3d> positions = carts.map(Entity::getPositionVec);
		Couple<Float> maxSpeed = carts.map(AbstractMinecartEntity::getMaxCartSpeedOnRail);
		Couple<Boolean> canAddmotion = carts.map(MinecartSim2020::canAddMotion);

		Couple<RailShape> shapes = carts.map(current -> {
			BlockPos railPosition = current.getCurrentRailPosition();
			BlockState railState = world.getBlockState(railPosition.up());
			if (!(railState.getBlock() instanceof AbstractRailBlock))
				return null;
			AbstractRailBlock block = (AbstractRailBlock) railState.getBlock();
			return block.getRailDirection(railState, world, railPosition, current);
		});

		Couple<Vector3d> motions = carts.map(MinecartSim2020::predictMotionOf);
		Couple<Vector3d> nextPositions = positions.copy();
		nextPositions.replaceWithParams(Vector3d::add, motions);

		float futureStress = (float) (couplingLength - nextPositions.getFirst()
			.distanceTo(nextPositions.getSecond()));
		if (Math.abs(futureStress) < 1 / 128f)
			return;

		for (boolean current : Iterate.trueAndFalse) {
			Vector3d correction = Vector3d.ZERO;
			Vector3d pos = nextPositions.get(current);
			Vector3d link = nextPositions.get(!current)
				.subtract(pos);
			float correctionMagnitude = -futureStress / 2f;

			if (canAddmotion.get(current) != canAddmotion.get(!current))
				correctionMagnitude = !canAddmotion.get(current) ? 0 : correctionMagnitude * 2;

			RailShape shape = shapes.get(current);
			correction = shape != null ? followLinkOnRail(link, pos, correctionMagnitude, shape).subtract(pos)
				: link.normalize()
					.scale(correctionMagnitude);
			correction = VecHelper.clamp(correction, maxSpeed.get(current));
			motions.set(current, motions.get(current)
				.add(correction));
		}

		motions.replaceWithParams(VecHelper::clamp, maxSpeed);
		carts.forEachWithParams(Entity::setMotion, motions);
	}

	public static Vector3d followLinkOnRail(Vector3d link, Vector3d cart, float diffToReduce, RailShape shape) {
		Vector3d railAxis = getRailVec(shape);
		double dotProduct = railAxis.dotProduct(link);
		if (Double.isNaN(dotProduct) || dotProduct == 0 || diffToReduce == 0)
			return cart;

		Vector3d axis = railAxis.scale(-Math.signum(dotProduct));
		Vector3d center = cart.add(link);
		double radius = link.length() - diffToReduce;
		Vector3d intersectSphere = VecHelper.intersectSphere(cart, axis, center, radius);

		// Cannot satisfy on current rail vector
		if (intersectSphere == null)
			return cart.add(VecHelper.project(link, axis));

		return intersectSphere;
	}

	private static Vector3d getRailVec(RailShape shape) {
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

	public UUID getId() {
		return couplings.get(0)
			.getId();
	}

	public static void doDebugRender(World world, MinecartCoupling coupling, int index) {
		AbstractMinecartEntity mainCart = coupling.mainCart.get();
		AbstractMinecartEntity connectedCart = coupling.connectedCart.get();

		if (!coupling.areBothEndsPresent())
			return;

		int yOffset = 1;
		Vector3d mainCenter = mainCart.getPositionVec()
			.add(0, yOffset, 0);
		Vector3d connectedCenter = connectedCart.getPositionVec()
			.add(0, yOffset, 0);

		int color = ColorHelper.mixColors(0xabf0e9, 0xee8572,
			(float) MathHelper.clamp(Math.abs(coupling.length - connectedCenter.distanceTo(mainCenter)) * 8, 0, 1));

		CreateClient.outliner.showLine(coupling + "" + index, mainCenter, connectedCenter)
			.colored(color)
			.lineWidth(1 / 8f);

		Vector3d point = mainCart.getPositionVec()
			.add(0, yOffset, 0);
		CreateClient.outliner.showLine(coupling.getId() + "" + index, point, point.add(0, 1 / 128f, 0))
			.colored(0xffffff)
			.lineWidth(1 / 4f);
	}

}
