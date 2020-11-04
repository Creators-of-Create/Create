package com.simibubi.create.content.contraptions.fluids;

import java.util.Random;

import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.content.contraptions.fluids.particle.FluidParticleData;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public class FluidFX {

	static Random r = new Random();

	public static void splash(BlockPos pos, FluidStack fluidStack) {
		Fluid fluid = fluidStack.getFluid();
		if (fluid == Fluids.EMPTY)
			return;

		IFluidState defaultState = fluid.getDefaultState();
		if (defaultState == null || defaultState.isEmpty()) {
			return;
		}

		BlockParticleData blockParticleData = new BlockParticleData(ParticleTypes.BLOCK, defaultState.getBlockState());
		Vec3d center = VecHelper.getCenterOf(pos);

		for (int i = 0; i < 20; i++) {
			Vec3d v = VecHelper.offsetRandomly(Vec3d.ZERO, r, .25f);
			particle(blockParticleData, center.add(v), v);
		}

	}

	public static IParticleData getFluidParticle(FluidStack fluid) {
		if (FluidHelper.hasBlockState(fluid.getFluid()))
			return new BlockParticleData(ParticleTypes.BLOCK, fluid.getFluid()
				.getDefaultState()
				.getBlockState());
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
		Vec3d directionVec = new Vec3d(side.getDirectionVec());
		for (int i = 0; i < amount; i++) {
			Vec3d vec = VecHelper.offsetRandomly(Vec3d.ZERO, r, 1)
				.normalize();
			vec = VecHelper.clampComponentWise(vec, rimRadius)
				.mul(VecHelper.axisAlingedPlaneOf(directionVec))
				.add(directionVec.scale(.45 + r.nextFloat() / 16f));
			Vec3d m = vec;
			vec = vec.add(VecHelper.getCenterOf(pos));

			world.addOptionalParticle(particle, vec.x, vec.y - 1 / 16f, vec.z, m.x, m.y, m.z);
		}
	}

	public static void spawnPouringLiquid(World world, BlockPos pos, int amount, IParticleData particle,
		float rimRadius, Vec3d directionVec, boolean inbound) {
		for (int i = 0; i < amount; i++) {
			Vec3d vec = VecHelper.offsetRandomly(Vec3d.ZERO, r, rimRadius);
			vec = vec.mul(VecHelper.axisAlingedPlaneOf(directionVec))
				.add(directionVec.scale(.5 + r.nextFloat() / 4f));
			Vec3d m = vec;
			Vec3d centerOf = VecHelper.getCenterOf(pos);
			vec = vec.add(centerOf);
			if (inbound) {
				vec = vec.add(m);
				m = centerOf.add(directionVec.scale(.5))
					.subtract(vec)
					.scale(3);
			}
			world.addOptionalParticle(particle, vec.x, vec.y - 1 / 16f, vec.z, m.x, m.y, m.z);
		}
	}

	private static void particle(IParticleData data, Vec3d pos, Vec3d motion) {
		world().addParticle(data, pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
	}

	private static World world() {
		return Minecraft.getInstance().world;
	}

}
