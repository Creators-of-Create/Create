package com.simibubi.create.content.kinetics.fan.processingTypes;

import com.simibubi.create.content.kinetics.fan.AirFlowParticle;
import com.simibubi.create.content.kinetics.fan.FanProcessing;
import com.simibubi.create.content.kinetics.fan.AbstractFanProcessingType;
import com.simibubi.create.foundation.recipe.RecipeApplier;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BlastingType extends AbstractFanProcessingType {
	public BlastingType() {
		super("BLASTING");
	}

	@Override
	public void spawnParticlesForProcessing(Level level, Vec3 pos) {
		if (level.random.nextInt(8) != 0)
			return;
		level.addParticle(ParticleTypes.LARGE_SMOKE, pos.x, pos.y + .25f, pos.z, 0, 1 / 16f, 0);
	}

	@Override
	public void affectEntity(Entity entity, Level level) {
		if (level.isClientSide)
			return;

		if (!entity.fireImmune()) {
			entity.setSecondsOnFire(10);
			entity.hurt(FanProcessing.LAVA_DAMAGE_SOURCE, 4);
		}
	}

	@Override
	public void particleMorphType(AirFlowParticle particle) {
		particle.setProperties(0xFF4400, 0xFF8855, .5f, 3);
		particle.addParticle(ParticleTypes.FLAME, 1 / 128f, .25f);
		particle.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.LAVA.defaultBlockState()),
				1 / 16f, .25f);
	}

	@Override
	public List<ItemStack> process(ItemStack stack, AbstractFanProcessingType type, Level world) {
		FanProcessing.RECIPE_WRAPPER.setItem(0, stack);
		Optional<SmokingRecipe> smokingRecipe = world.getRecipeManager()
				.getRecipeFor(RecipeType.SMOKING, FanProcessing.RECIPE_WRAPPER, world);
		FanProcessing.RECIPE_WRAPPER.setItem(0, stack);
		Optional<? extends AbstractCookingRecipe> smeltingRecipe = world.getRecipeManager()
				.getRecipeFor(RecipeType.SMELTING, FanProcessing.RECIPE_WRAPPER, world);
		if (!smeltingRecipe.isPresent()) {
			FanProcessing.RECIPE_WRAPPER.setItem(0, stack);
			smeltingRecipe = world.getRecipeManager()
					.getRecipeFor(RecipeType.BLASTING, FanProcessing.RECIPE_WRAPPER, world);
		}

		if (smeltingRecipe.isPresent()) {
			if (!smokingRecipe.isPresent() || !ItemStack.isSame(smokingRecipe.get()
							.getResultItem(),
					smeltingRecipe.get()
							.getResultItem())) {
				return RecipeApplier.applyRecipeOn(stack, smeltingRecipe.get());
			}
		}

		return Collections.emptyList();
	}

	@Override
	public boolean canProcess(ItemStack stack, Level level) {
		FanProcessing.RECIPE_WRAPPER.setItem(0, stack);
		Optional<SmeltingRecipe> smeltingRecipe = level.getRecipeManager()
				.getRecipeFor(RecipeType.SMELTING, FanProcessing.RECIPE_WRAPPER, level);

		if (smeltingRecipe.isPresent())
			return true;

		FanProcessing.RECIPE_WRAPPER.setItem(0, stack);
		Optional<BlastingRecipe> blastingRecipe = level.getRecipeManager()
				.getRecipeFor(RecipeType.BLASTING, FanProcessing.RECIPE_WRAPPER, level);

		if (blastingRecipe.isPresent())
			return true;

		return !stack.getItem()
				.isFireResistant();
	}
}
