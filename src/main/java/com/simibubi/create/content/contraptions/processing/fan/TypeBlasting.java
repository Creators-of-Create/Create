package com.simibubi.create.content.contraptions.processing.fan;

import static com.simibubi.create.content.contraptions.processing.InWorldProcessing.RECIPE_WRAPPER;
import static com.simibubi.create.content.contraptions.processing.InWorldProcessing.applyRecipeOn;
import static com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock.getHeatLevelOf;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.contraptions.particle.AirFlowParticle;
import com.simibubi.create.content.contraptions.processing.InWorldProcessing;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TypeBlasting extends AbstractFanProcessingType {

	public TypeBlasting(int priority, ResourceLocation name) {
		super(priority, name);
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
			entity.hurt(InWorldProcessing.LAVA_DAMAGE_SOURCE, 4);
		}
	}

	@Override
	public boolean isApplicable(BlockGetter reader, BlockPos pos) {
		BlockState blockState = reader.getBlockState(pos);
		Block block = blockState.getBlock();
		return block == Blocks.LAVA || getHeatLevelOf(blockState).isAtLeast(BlazeBurnerBlock.HeatLevel.FADING);
	}

	@Nullable
	@Override
	public List<ItemStack> process(ItemStack stack, Level world) {

		RECIPE_WRAPPER.setItem(0, stack);
		Optional<SmokingRecipe> smokingRecipe = world.getRecipeManager()
				.getRecipeFor(RecipeType.SMOKING, RECIPE_WRAPPER, world);
		if (!smokingRecipe.isPresent()) {
			RECIPE_WRAPPER.setItem(0, stack);
			Optional<SmeltingRecipe> smeltingRecipe = world.getRecipeManager()
					.getRecipeFor(RecipeType.SMELTING, RECIPE_WRAPPER, world);

			if (smeltingRecipe.isPresent())
				return applyRecipeOn(stack, smeltingRecipe.get());

			RECIPE_WRAPPER.setItem(0, stack);
			Optional<BlastingRecipe> blastingRecipe = world.getRecipeManager()
					.getRecipeFor(RecipeType.BLASTING, RECIPE_WRAPPER, world);

			if (blastingRecipe.isPresent())
				return applyRecipeOn(stack, blastingRecipe.get());
		}

		return Collections.emptyList();
	}

	@Override
	public void morphType(AirFlowParticle particle) {
		particle.setProperties(0xFF4400, 0xFF8855, .5f, 3);
		particle.addParticle(ParticleTypes.FLAME, 1 / 32f, .25f);
		particle.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.LAVA.defaultBlockState()), 1 / 16f, .25f);
	}

	@Override
	public boolean canProcess(ItemStack stack, Level level) {
		RECIPE_WRAPPER.setItem(0, stack);
		Optional<SmeltingRecipe> smeltingRecipe = level.getRecipeManager()
				.getRecipeFor(RecipeType.SMELTING, RECIPE_WRAPPER, level);

		if (smeltingRecipe.isPresent())
			return true;

		RECIPE_WRAPPER.setItem(0, stack);
		Optional<BlastingRecipe> blastingRecipe = level.getRecipeManager()
				.getRecipeFor(RecipeType.BLASTING, RECIPE_WRAPPER, level);

		if (blastingRecipe.isPresent())
			return true;

		return !stack.getItem()
				.isFireResistant();
	}
}
