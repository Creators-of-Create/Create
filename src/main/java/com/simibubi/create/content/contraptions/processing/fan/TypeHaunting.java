package com.simibubi.create.content.contraptions.processing.fan;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.particle.AirFlowParticle;
import com.simibubi.create.content.contraptions.processing.InWorldProcessing;
import com.simibubi.create.content.contraptions.processing.burner.LitBlazeBurnerBlock;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static com.simibubi.create.content.contraptions.processing.InWorldProcessing.RECIPE_WRAPPER;
import static com.simibubi.create.content.contraptions.processing.InWorldProcessing.applyRecipeOn;

public class TypeHaunting extends AbstractRecipeFanType<HauntingRecipe> {

	public TypeHaunting(int priority, String name) {
		super(priority, name, AllRecipeTypes.HAUNTING::find);
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
	public boolean isApplicable(BlockGetter reader, BlockPos pos) {
		BlockState blockState = reader.getBlockState(pos);
		Block block = blockState.getBlock();
		return block == Blocks.SOUL_FIRE
				|| block == Blocks.SOUL_CAMPFIRE && blockState.getOptionalValue(CampfireBlock.LIT)
				.orElse(false)
				|| AllBlocks.LIT_BLAZE_BURNER.has(blockState) && blockState.getOptionalValue(LitBlazeBurnerBlock.FLAME_TYPE)
				.map(flame -> flame == LitBlazeBurnerBlock.FlameType.SOUL).orElse(false);
	}

	@Override
	public void morphType(AirFlowParticle particle) {
		particle.setProperties(0x0, 0x126568, 1f, 3);
		particle.addParticle(ParticleTypes.SOUL_FIRE_FLAME, 1 / 128f, .125f);
		particle.addParticle(ParticleTypes.SMOKE, 1 / 32f, .125f);
	}

}
