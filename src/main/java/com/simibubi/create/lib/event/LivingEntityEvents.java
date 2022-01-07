package com.simibubi.create.lib.event;

import java.util.Collection;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class LivingEntityEvents {
	public static final Event<ExperienceDrop> EXPERIENCE_DROP = EventFactory.createArrayBacked(ExperienceDrop.class, callbacks -> (amount, player) -> {
		for (ExperienceDrop callback : callbacks) {
			int newAmount = callback.onLivingEntityExperienceDrop(amount, player);
			if (newAmount != amount) return newAmount;
		}

		return amount;
	});

	public static final Event<KnockBackStrength> KNOCKBACK_STRENGTH = EventFactory.createArrayBacked(KnockBackStrength.class, callbacks -> (strength, player) -> {
		for (KnockBackStrength callback : callbacks) {
			double newStrength = callback.onLivingEntityTakeKnockback(strength, player);
			if (newStrength != strength) return newStrength;
		}

		return strength;
	});

	public static final Event<Drops> DROPS = EventFactory.createArrayBacked(Drops.class, callbacks -> (source, drops) -> {
		for (Drops callback : callbacks) {
			if (callback.onLivingEntityDrops(source, drops)) return true;
		}

		return false;
	});

	public static final Event<LootingLevel> LOOTING_LEVEL = EventFactory.createArrayBacked(LootingLevel.class, callbacks -> (source) -> {
		for (LootingLevel callback : callbacks) {
			int lootingLevel = callback.modifyLootingLevel(source);
			if (lootingLevel != 0) {
				return lootingLevel;
			}
		}

		return 0;
	});

	public static final Event<Tick> TICK = EventFactory.createArrayBacked(Tick.class, callbacks -> (entity) -> {
		for (Tick callback : callbacks) {
			callback.onLivingEntityTick(entity);
		}
	});

	public static final Event<Hurt> HURT = EventFactory.createArrayBacked(Hurt.class, callbacks -> (source, amount) -> {
		for (Hurt callback : callbacks) {
			float newAmount = callback.onHurt(source, amount);
			if (newAmount != amount) return newAmount;
		}
		return amount;
	});

	@FunctionalInterface
	public interface Hurt {
		float onHurt(DamageSource source, float amount);
	}

	@FunctionalInterface
	public interface ExperienceDrop {
		int onLivingEntityExperienceDrop(int amount, Player player);
	}

	@FunctionalInterface
	public interface KnockBackStrength {
		double onLivingEntityTakeKnockback(double strength, Player player);
	}

	@FunctionalInterface
	public interface Drops {
		boolean onLivingEntityDrops(DamageSource source, Collection<ItemEntity> drops);
	}

	@FunctionalInterface
	public interface LootingLevel {
		int modifyLootingLevel(DamageSource source);
	}

	@FunctionalInterface
	public interface Tick {
		void onLivingEntityTick(LivingEntity entity);
	}
}
