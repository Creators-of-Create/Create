package com.simibubi.create.content.kinetics.base;

import com.simibubi.create.content.kinetics.base.IRotate.SpeedLevel;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class KineticEffectHandler {

	int overStressedTime;
	float overStressedEffect;
	int particleSpawnCountdown;
	KineticBlockEntity kte;

	public KineticEffectHandler(KineticBlockEntity kte) {
		this.kte = kte;
	}

	public void tick() {
		Level world = kte.getLevel();

		if (world.isClientSide) {
			if (overStressedTime > 0)
				if (--overStressedTime == 0)
					if (kte.isOverStressed()) {
						overStressedEffect = 1;
						spawnEffect(ParticleTypes.SMOKE, 0.2f, 5);
					} else {
						overStressedEffect = -1;
						spawnEffect(ParticleTypes.CLOUD, .075f, 2);
					}

			if (overStressedEffect != 0) {
				overStressedEffect -= overStressedEffect * .1f;
				if (Math.abs(overStressedEffect) < 1 / 128f)
					overStressedEffect = 0;
			}

		} else if (particleSpawnCountdown > 0) {
			if (--particleSpawnCountdown == 0)
				spawnRotationIndicators();
		}
	}

	public void queueRotationIndicators() {
		particleSpawnCountdown = 2;
	}

	public void spawnEffect(ParticleOptions particle, float maxMotion, int amount) {
		Level world = kte.getLevel();
		if (world == null)
			return;
		if (!world.isClientSide)
			return;
		RandomSource r = world.random;
		for (int i = 0; i < amount; i++) {
			Vec3 motion = VecHelper.offsetRandomly(Vec3.ZERO, r, maxMotion);
			Vec3 position = VecHelper.getCenterOf(kte.getBlockPos());
			world.addParticle(particle, position.x, position.y, position.z, motion.x, motion.y, motion.z);
		}
	}

	public void spawnRotationIndicators() {
		float speed = kte.getSpeed();
		if (speed == 0)
			return;

		BlockState state = kte.getBlockState();
		Block block = state.getBlock();
		if (!(block instanceof KineticBlock))
			return;

		KineticBlock kb = (KineticBlock) block;
		float radius1 = kb.getParticleInitialRadius();
		float radius2 = kb.getParticleTargetRadius();

		Axis axis = kb.getRotationAxis(state);
		BlockPos pos = kte.getBlockPos();
		Level world = kte.getLevel();
		if (axis == null)
			return;
		if (world == null)
			return;

		char axisChar = axis.name().charAt(0);
		Vec3 vec = VecHelper.getCenterOf(pos);
		SpeedLevel speedLevel = SpeedLevel.of(speed);
		int color = speedLevel.getColor();
		int particleSpeed = speedLevel.getParticleSpeed();
		particleSpeed *= Math.signum(speed);

		if (world instanceof ServerLevel) {
			RotationIndicatorParticleData particleData =
				new RotationIndicatorParticleData(color, particleSpeed, radius1, radius2, 10, axisChar);
			((ServerLevel) world).sendParticles(particleData, vec.x, vec.y, vec.z, 20, 0, 0, 0, 1);
		}
	}

	public void triggerOverStressedEffect() {
		overStressedTime = overStressedTime == 0 ? 2 : 0;
	}

}
