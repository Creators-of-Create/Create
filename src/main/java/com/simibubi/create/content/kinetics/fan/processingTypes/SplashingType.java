package com.simibubi.create.content.kinetics.fan.processingTypes;

import com.mojang.math.Vector3f;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.fan.AirFlowParticle;
import com.simibubi.create.content.kinetics.fan.AbstractFanProcessingType;
import com.simibubi.create.content.kinetics.fan.SplashingRecipe;
import com.simibubi.create.foundation.recipe.RecipeApplier;
import com.simibubi.create.foundation.utility.Color;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import java.util.List;
import java.util.Optional;

public class SplashingType extends AbstractFanProcessingType {
	public static final SplashingWrapper SPLASHING_WRAPPER = new SplashingWrapper();

	public SplashingType() {
		super("SPLASHING");
	}

	public static boolean isWashable(ItemStack stack, Level world) {
		SPLASHING_WRAPPER.setItem(0, stack);
		Optional<SplashingRecipe> recipe = AllRecipeTypes.SPLASHING.find(SPLASHING_WRAPPER, world);
		return recipe.isPresent();
	}

	@Override
	public void spawnParticlesForProcessing(Level level, Vec3 pos) {
		if (level.random.nextInt(8) != 0)
			return;
		Vector3f color = new Color(0x0055FF).asVectorF();
		level.addParticle(new DustParticleOptions(color, 1), pos.x + (level.random.nextFloat() - .5f) * .5f,
				pos.y + .5f, pos.z + (level.random.nextFloat() - .5f) * .5f, 0, 1 / 8f, 0);
		level.addParticle(ParticleTypes.SPIT, pos.x + (level.random.nextFloat() - .5f) * .5f, pos.y + .5f,
				pos.z + (level.random.nextFloat() - .5f) * .5f, 0, 1 / 8f, 0);
	}

	@Override
	public void affectEntity(Entity entity, Level level) {
		if (level.isClientSide)
			return;

		if (entity instanceof EnderMan || entity.getType() == EntityType.SNOW_GOLEM
				|| entity.getType() == EntityType.BLAZE) {
			entity.hurt(DamageSource.DROWN, 2);
		}
		if (entity.isOnFire()) {
			entity.clearFire();
			level.playSound(null, entity.blockPosition(), SoundEvents.GENERIC_EXTINGUISH_FIRE,
					SoundSource.NEUTRAL, 0.7F, 1.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.4F);
		}
	}

	@Override
	public void particleMorphType(AirFlowParticle particle) {
		particle.setProperties(0x4499FF, 0x2277FF, 1f, 3);
		particle.addParticle(ParticleTypes.BUBBLE, 1 / 32f, .125f);
		particle.addParticle(ParticleTypes.BUBBLE_POP, 1 / 32f, .125f);
	}

	@Override
	public List<ItemStack> process(ItemStack stack, AbstractFanProcessingType type, Level world) {
		SPLASHING_WRAPPER.setItem(0, stack);
		Optional<SplashingRecipe> recipe = AllRecipeTypes.SPLASHING.find(SPLASHING_WRAPPER, world);
		if (recipe.isPresent())
			return RecipeApplier.applyRecipeOn(stack, recipe.get());
		return null;
	}

	@Override
	public boolean canProcess(ItemStack stack, Level level) {
		return isWashable(stack, level);
	}

	public static class SplashingWrapper extends RecipeWrapper {
		public SplashingWrapper() {
			super(new ItemStackHandler(1));
		}
	}
}
