package com.simibubi.create.content.kinetics.fan.processingTypes;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.fan.AirFlowParticle;
import com.simibubi.create.content.kinetics.fan.AbstractFanProcessingType;
import com.simibubi.create.content.kinetics.fan.HauntingRecipe;
import com.simibubi.create.foundation.recipe.RecipeApplier;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import java.util.List;
import java.util.Optional;

public class HauntingType extends AbstractFanProcessingType {
	public static final HauntingWrapper HAUNTING_WRAPPER = new HauntingWrapper();

	public HauntingType() {
		super("HAUNTING");
	}

	public static boolean isHauntable(ItemStack stack, Level world) {
		HAUNTING_WRAPPER.setItem(0, stack);
		Optional<HauntingRecipe> recipe = AllRecipeTypes.HAUNTING.find(HAUNTING_WRAPPER, world);
		return recipe.isPresent();
	}

	@Override
	public void spawnParticlesForProcessing(Level level, Vec3 pos) {
		if (level.random.nextInt(8) != 0)
			return;
		pos = pos.add(VecHelper.offsetRandomly(Vec3.ZERO, level.random, 1)
				.multiply(1, 0.05f, 1)
				.normalize()
				.scale(0.15f));
		level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, pos.x, pos.y + .45f, pos.z, 0, 0, 0);
		if (level.random.nextInt(2) == 0)
			level.addParticle(ParticleTypes.SMOKE, pos.x, pos.y + .25f, pos.z, 0, 0, 0);
	}

	@Override
	public void affectEntity(Entity entity, Level level) {
		if (level.isClientSide) {
			if (entity instanceof Horse) {
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
			return;
		}

		if (entity instanceof LivingEntity livingEntity) {
			livingEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0, false, false));
			livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 1, false, false));
		}
		if (entity instanceof Horse horse) {
			int progress = horse.getPersistentData()
					.getInt("CreateHaunting");
			if (progress < 100) {
				if (progress % 10 == 0) {
					level.playSound(null, entity.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.NEUTRAL,
							1f, 1.5f * progress / 100f);
				}
				horse.getPersistentData()
						.putInt("CreateHaunting", progress + 1);
				return;
			}

			level.playSound(null, entity.blockPosition(), SoundEvents.GENERIC_EXTINGUISH_FIRE,
					SoundSource.NEUTRAL, 1.25f, 0.65f);

			SkeletonHorse skeletonHorse = EntityType.SKELETON_HORSE.create(level);
			CompoundTag serializeNBT = horse.saveWithoutId(new CompoundTag());
			serializeNBT.remove("UUID");
			if (!horse.getArmor()
					.isEmpty())
				horse.spawnAtLocation(horse.getArmor());

			skeletonHorse.deserializeNBT(serializeNBT);
			skeletonHorse.setPos(horse.getPosition(0));
			level.addFreshEntity(skeletonHorse);
			horse.discard();
		}
	}

	@Override
	public void particleMorphType(AirFlowParticle particle) {
		particle.setProperties(0x0, 0x126568, 1f, 3);
		particle.addParticle(ParticleTypes.SOUL_FIRE_FLAME, 1 / 128f, .125f);
		particle.addParticle(ParticleTypes.SMOKE, 1 / 32f, .125f);
	}

	@Override
	public List<ItemStack> process(ItemStack stack, AbstractFanProcessingType type, Level world) {
		HAUNTING_WRAPPER.setItem(0, stack);
		Optional<HauntingRecipe> recipe = AllRecipeTypes.HAUNTING.find(HAUNTING_WRAPPER, world);
		if (recipe.isPresent())
			return RecipeApplier.applyRecipeOn(stack, recipe.get());
		return null;
	}

	@Override
	public boolean canProcess(ItemStack stack, Level level) {
		return isHauntable(stack, level);
	}

	public static class HauntingWrapper extends RecipeWrapper {
		public HauntingWrapper() {
			super(new ItemStackHandler(1));
		}
	}
}
