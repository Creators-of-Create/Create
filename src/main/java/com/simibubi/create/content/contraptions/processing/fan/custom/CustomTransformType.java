package com.simibubi.create.content.contraptions.processing.fan.custom;

import com.simibubi.create.content.contraptions.processing.fan.transform.EntityTransformHelper;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class CustomTransformType extends EntityTransformHelper<LivingEntity, LivingEntity> {

	public final CustomTransformConfig config;

	@SuppressWarnings({"raw_types", "unchecked", "unsafe"})
	public CustomTransformType(CustomTransformConfig config) {
		super("CreateCustomTransform", e -> e.name.equals(config.blockType()), LivingEntity.class, e -> e.getType() == config.oldType(), e -> (EntityType<LivingEntity>) config.newType());
		this.config = config;
	}

	@Override
	public void onProgress(Level level, LivingEntity entity, int progress) {
		CustomTransformConfig.ProgressionSoundConfig c = config.progression();
		if (c != null && progress % c.interval() == 0) {
			level.playSound(null, entity.blockPosition(), c.sound(), SoundSource.NEUTRAL,
					c.volume() + c.volumeSlope() * progress / 100f, c.pitch() + c.pitchSlope() * progress / 100f);
		}
	}

	@Override
	public void onComplete(Level level, LivingEntity entity) {
		CustomTransformConfig.CompletionSoundConfig c = config.completion();
		if (c != null) {
			level.playSound(null, entity.blockPosition(), c.sound(),
					SoundSource.NEUTRAL, c.volume(), c.pitch());
		}
	}

	@Override
	public void clientEffect(Level level, LivingEntity entity) {
		if (config.particles() != null) {
			for (ProcessingParticleConfig c : config.particles()) {
				c.spawnParticlesForProcessing(level, entity.getPosition(0));
			}
		}
	}

	@Override
	public void postTransform(LivingEntity livingEntity, LivingEntity livingEntity2) {

	}

}
