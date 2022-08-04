package com.simibubi.create.content.curiosities.weapons;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public record ProjectileEffect(MobEffect effect, int level, int seconds) {
	public MobEffectInstance getMEI(){
		return new MobEffectInstance(effect, seconds*20, level - 1);
	}

	@Override
	public String toString() {
		return "ProjectileEffect{" +
				"effect=" + effect.getDisplayName() +
				", level=" + level +
				", seconds=" + seconds +
				'}';
	}
}
