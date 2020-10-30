package com.simibubi.create.content.contraptions.fluids;

import java.util.Random;

import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
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
		
		FluidState defaultState = fluid.getDefaultState();
		if (defaultState == null || defaultState.isEmpty()) {
			return;
		}

		BlockParticleData blockParticleData = new BlockParticleData(ParticleTypes.BLOCK, defaultState.getBlockState());
		Vector3d center = VecHelper.getCenterOf(pos);
		
		for (int i = 0; i < 20; i++) {
			Vector3d v = VecHelper.offsetRandomly(Vector3d.ZERO, r, .25f);
			particle(blockParticleData, center.add(v), v);
		}
		
	}
	
	private static void particle(IParticleData data, Vector3d pos, Vector3d motion) {
		world().addParticle(data, pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
	}
	
	private static World world() {
		return Minecraft.getInstance().world;
	}

}
