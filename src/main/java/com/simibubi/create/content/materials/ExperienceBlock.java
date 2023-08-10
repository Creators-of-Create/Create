package com.simibubi.create.content.materials;

import java.util.Random;

import net.createmod.catnip.utility.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ExperienceBlock extends Block {

	public ExperienceBlock(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, Random pRand) {
		if (pRand.nextInt(5) != 0)
			return;
		Vec3 vec3 = VecHelper.clampComponentWise(VecHelper.offsetRandomly(Vec3.ZERO, pRand, .75f), .55f)
			.add(VecHelper.getCenterOf(pPos));
		pLevel.addParticle(ParticleTypes.END_ROD, vec3.x, vec3.y, vec3.z, pRand.nextGaussian() * 0.005D,
			pRand.nextGaussian() * 0.005D, pRand.nextGaussian() * 0.005D);
	}

}
