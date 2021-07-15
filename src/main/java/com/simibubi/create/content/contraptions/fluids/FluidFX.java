package com.simibubi.create.content.contraptions.fluids;

import java.util.Random;

import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.content.contraptions.fluids.particle.FluidParticleData;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public class FluidFX {

	static Random r = new Random();

	public static void splash(BlockPos pos, FluidStack fluidStack) {
		Fluid fluid = fluidStack.getFluid();
		if (fluid == Fluids.EMPTY)
			return;

		FluidState defaultState = fluid.defaultFluidState();
		if (defaultState == null || defaultState.isEmpty()) {
			return;
		}

		BlockParticleData blockParticleData = new BlockParticleData(ParticleTypes.BLOCK, defaultState.createLegacyBlock());
		Vector3d center = VecHelper.getCenterOf(pos);

		for (int i = 0; i < 20; i++) {
			Vector3d v = VecHelper.offsetRandomly(Vector3d.ZERO, r, .25f);
			particle(blockParticleData, center.add(v), v);
		}

	}

	public static IParticleData getFluidParticle(FluidStack fluid) {
		return new FluidParticleData(AllParticleTypes.FLUID_PARTICLE.get(), fluid);
	}

	public static IParticleData getDrippingParticle(FluidStack fluid) {
		IParticleData particle = null;
		if (FluidHelper.isWater(fluid.getFluid()))
			particle = ParticleTypes.DRIPPING_WATER;
		if (FluidHelper.isLava(fluid.getFluid()))
			particle = ParticleTypes.DRIPPING_LAVA;
		if (particle == null)
			particle = new FluidParticleData(AllParticleTypes.FLUID_DRIP.get(), fluid);
		return particle;
	}

	public static void spawnRimParticles(World world, BlockPos pos, Direction side, int amount, IParticleData particle,
		float rimRadius) {
		Vector3d directionVec = Vector3d.atLowerCornerOf(side.getNormal());
		for (int i = 0; i < amount; i++) {
			Vector3d vec = VecHelper.offsetRandomly(Vector3d.ZERO, r, 1)
				.normalize();
			vec = VecHelper.clampComponentWise(vec, rimRadius)
				.multiply(VecHelper.axisAlingedPlaneOf(directionVec))
				.add(directionVec.scale(.45 + r.nextFloat() / 16f));
			Vector3d m = vec.scale(.05f);
			vec = vec.add(VecHelper.getCenterOf(pos));

			world.addAlwaysVisibleParticle(particle, vec.x, vec.y - 1 / 16f, vec.z, m.x, m.y, m.z);
		}
	}

	public static void spawnPouringLiquid(World world, BlockPos pos, int amount, IParticleData particle,
		float rimRadius, Vector3d directionVec, boolean inbound) {
		for (int i = 0; i < amount; i++) {
			Vector3d vec = VecHelper.offsetRandomly(Vector3d.ZERO, r, rimRadius * .75f);
			vec = vec.multiply(VecHelper.axisAlingedPlaneOf(directionVec))
				.add(directionVec.scale(.5 + r.nextFloat() / 4f));
			Vector3d m = vec.scale(1 / 4f);
			Vector3d centerOf = VecHelper.getCenterOf(pos);
			vec = vec.add(centerOf);
			if (inbound) {
				vec = vec.add(m);
				m = centerOf.add(directionVec.scale(.5))
					.subtract(vec)
					.scale(1 / 16f);
			}
			world.addAlwaysVisibleParticle(particle, vec.x, vec.y - 1 / 16f, vec.z, m.x, m.y, m.z);
		}
	}

	private static void particle(IParticleData data, Vector3d pos, Vector3d motion) {
		world().addParticle(data, pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
	}

	private static World world() {
		return Minecraft.getInstance().level;
	}

}
