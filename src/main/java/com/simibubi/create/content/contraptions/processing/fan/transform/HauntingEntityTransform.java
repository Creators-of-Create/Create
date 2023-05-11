package com.simibubi.create.content.contraptions.processing.fan.transform;

import java.util.function.Function;
import java.util.function.Predicate;

import com.simibubi.create.AllFanProcessingTypes;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class HauntingEntityTransform<A extends LivingEntity, B extends LivingEntity> extends EntityTransformHelper<A, B> {

	public HauntingEntityTransform(Class<A> cls, Predicate<A> predicate, Function<A, EntityType<B>> getter) {
		super("CreateHaunting", e -> e == AllFanProcessingTypes.HAUNTING, cls, predicate, getter);
	}

	@Override
	public void onProgress(Level level, A entity, int progress) {
		if (progress % 10 == 0) {
			level.playSound(null, entity.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.NEUTRAL,
					1f, 1.5f * progress / 100f);
		}
	}

	@Override
	public void onComplete(Level level, A entity) {
		level.playSound(null, entity.blockPosition(), SoundEvents.GENERIC_EXTINGUISH_FIRE,
				SoundSource.NEUTRAL, 1.25f, 0.65f);
	}

	@Override
	public void clientEffect(Level level, A entity) {
		Vec3 p = entity.getPosition(0);
		Vec3 v = p.add(0, 0.5f, 0)
				.add(VecHelper.offsetRandomly(Vec3.ZERO, level.random, 1)
						.multiply(1, 0.2f, 1)
						.normalize()
						.scale(1f));
		level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, v.x, v.y, v.z, 0, 0.1f, 0);
		if (level.random.nextInt(3) == 0)
			level.addParticle(ParticleTypes.LARGE_SMOKE, p.x, p.y + .5f, p.z,
					(level.random.nextFloat() - .5f) * .5f, 0.1f, (level.random.nextFloat() - .5f) * .5f);
	}
}
