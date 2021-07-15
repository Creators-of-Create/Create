package com.simibubi.create.content.contraptions.components.structureMovement.train;

import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.MinecartController;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.state.properties.RailShape;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class CouplingPhysics {

	public static void tick(World world) {
		CouplingHandler.forEachLoadedCoupling(world, c -> tickCoupling(world, c));
	}

	public static void tickCoupling(World world, Couple<MinecartController> c) {
		Couple<AbstractMinecartEntity> carts = c.map(MinecartController::cart);
		float couplingLength = c.getFirst()
			.getCouplingLength(true);
		softCollisionStep(world, carts, couplingLength);
		if (world.isClientSide)
			return;
		hardCollisionStep(world, carts, couplingLength);
	}

	public static void hardCollisionStep(World world, Couple<AbstractMinecartEntity> carts, double couplingLength) {
		if (!MinecartSim2020.canAddMotion(carts.get(false)) && MinecartSim2020.canAddMotion(carts.get(true)))
			carts = carts.swap();

		Couple<Vector3d> corrections = Couple.create(null, null);
		Couple<Float> maxSpeed = carts.map(AbstractMinecartEntity::getMaxCartSpeedOnRail);
		boolean firstLoop = true;
		for (boolean current : new boolean[] { true, false, true }) {
			AbstractMinecartEntity cart = carts.get(current);
			AbstractMinecartEntity otherCart = carts.get(!current);

			float stress = (float) (couplingLength - cart.position()
				.distanceTo(otherCart.position()));

			if (Math.abs(stress) < 1 / 8f)
				continue;

			RailShape shape = null;
			BlockPos railPosition = cart.getCurrentRailPosition();
			BlockState railState = world.getBlockState(railPosition.above());

			if (railState.getBlock() instanceof AbstractRailBlock) {
				AbstractRailBlock block = (AbstractRailBlock) railState.getBlock();
				shape = block.getRailDirection(railState, world, railPosition, cart);
			}

			Vector3d correction = Vector3d.ZERO;
			Vector3d pos = cart.position();
			Vector3d link = otherCart.position()
				.subtract(pos);
			float correctionMagnitude = firstLoop ? -stress / 2f : -stress;
			
			if (!MinecartSim2020.canAddMotion(cart))
				correctionMagnitude /= 2;
			
			correction = shape != null
				? followLinkOnRail(link, pos, correctionMagnitude, MinecartSim2020.getRailVec(shape)).subtract(pos)
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
				cart.setDeltaMovement(cart.getDeltaMovement()
					.scale(0.95f));
			}
			firstLoop = false;
		}
	}

	public static void softCollisionStep(World world, Couple<AbstractMinecartEntity> carts, double couplingLength) {
		Couple<Float> maxSpeed = carts.map(AbstractMinecartEntity::getMaxCartSpeedOnRail);
		Couple<Boolean> canAddmotion = carts.map(MinecartSim2020::canAddMotion);
		
		// Assuming Minecarts will never move faster than 1 block/tick
		Couple<Vector3d> motions = carts.map(Entity::getDeltaMovement);
		motions.replaceWithParams(VecHelper::clamp, Couple.create(1f, 1f));
		Couple<Vector3d> nextPositions = carts.map(MinecartSim2020::predictNextPositionOf);

		Couple<RailShape> shapes = carts.mapWithContext((cart, current) -> {
			AbstractMinecartEntity minecart = cart.getMinecart();
			Vector3d vec = nextPositions.get(current);
			int x = MathHelper.floor(vec.x());
	        int y = MathHelper.floor(vec.y());
	        int z = MathHelper.floor(vec.z());
	        BlockPos pos = new BlockPos(x, y - 1, z);
	        if (minecart.level.getBlockState(pos).is(BlockTags.RAILS)) pos = pos.below();
			BlockPos railPosition = pos;
			BlockState railState = world.getBlockState(railPosition.above());
			if (!(railState.getBlock() instanceof AbstractRailBlock))
				return null;
			AbstractRailBlock block = (AbstractRailBlock) railState.getBlock();
			return block.getRailDirection(railState, world, railPosition, cart);
		});

		float futureStress = (float) (couplingLength - nextPositions.getFirst()
			.distanceTo(nextPositions.getSecond()));
		if (MathHelper.equal(futureStress, 0D))
			return;

		for (boolean current : Iterate.trueAndFalse) {
			Vector3d correction = Vector3d.ZERO;
			Vector3d pos = nextPositions.get(current);
			Vector3d link = nextPositions.get(!current)
				.subtract(pos);
			float correctionMagnitude = -futureStress / 2f;

			if (canAddmotion.get(current) != canAddmotion.get(!current))
				correctionMagnitude = !canAddmotion.get(current) ? 0 : correctionMagnitude * 2;
			if (!canAddmotion.get(current))
				continue;

			RailShape shape = shapes.get(current);
			if (shape != null) {
				Vector3d railVec = MinecartSim2020.getRailVec(shape);
				correction = followLinkOnRail(link, pos, correctionMagnitude, railVec).subtract(pos);
			} else
				correction = link.normalize()
					.scale(correctionMagnitude);

			correction = VecHelper.clamp(correction, maxSpeed.get(current));
			
			motions.set(current, motions.get(current)
				.add(correction));
		}

		motions.replaceWithParams(VecHelper::clamp, maxSpeed);
		carts.forEachWithParams(Entity::setDeltaMovement, motions);
	}

	public static Vector3d followLinkOnRail(Vector3d link, Vector3d cart, float diffToReduce, Vector3d railAxis) {
		double dotProduct = railAxis.dot(link);
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

}
