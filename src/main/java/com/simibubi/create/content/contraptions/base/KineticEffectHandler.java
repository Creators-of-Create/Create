package com.simibubi.create.content.contraptions.base;

import java.util.Random;

import com.simibubi.create.content.contraptions.base.IRotate.SpeedLevel;
import com.simibubi.create.content.contraptions.particle.RotationIndicatorParticleData;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class KineticEffectHandler {

	int overStressedTime;
	float overStressedEffect;
	int particleSpawnCountdown;
	KineticTileEntity kte;

	public KineticEffectHandler(KineticTileEntity kte) {
		this.kte = kte;
	}

	public void tick() {
		World world = kte.getLevel();

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

	public void spawnEffect(IParticleData particle, float maxMotion, int amount) {
		World world = kte.getLevel();
		if (world == null)
			return;
		if (!world.isClientSide)
			return;
		Random r = world.random;
		for (int i = 0; i < amount; i++) {
			Vector3d motion = VecHelper.offsetRandomly(Vector3d.ZERO, r, maxMotion);
			Vector3d position = VecHelper.getCenterOf(kte.getBlockPos());
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
		World world = kte.getLevel();
		if (axis == null)
			return;
		if (world == null)
			return;

		char axisChar = axis.name().charAt(0);
		Vector3d vec = VecHelper.getCenterOf(pos);
		SpeedLevel speedLevel = SpeedLevel.of(speed);
		int color = speedLevel.getColor();
		int particleSpeed = speedLevel.getParticleSpeed();
		particleSpeed *= Math.signum(speed);

		if (world instanceof ServerWorld) {
			AllTriggers.triggerForNearbyPlayers(AllTriggers.ROTATION, world, pos, 5);
			RotationIndicatorParticleData particleData =
				new RotationIndicatorParticleData(color, particleSpeed, radius1, radius2, 10, axisChar);
			((ServerWorld) world).sendParticles(particleData, vec.x, vec.y, vec.z, 20, 0, 0, 0, 1);
		}
	}

	public void triggerOverStressedEffect() {
		overStressedTime = overStressedTime == 0 ? 2 : 0;
	}

}
