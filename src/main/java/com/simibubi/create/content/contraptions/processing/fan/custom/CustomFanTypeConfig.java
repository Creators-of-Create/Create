package com.simibubi.create.content.contraptions.processing.fan.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.contraptions.particle.AirFlowParticle;
import com.simibubi.create.content.contraptions.processing.InWorldProcessing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Config for datapack fan processing types
 * - priority: required/ Higher value means this fan type is checked first, if multiple fan types conflicts
 * - {} block: required. Block predicate to test blocks
 * | ? [] blocks: optional. List of block candidates. Use block_states if some states of this block shouldn't be used
 * | ? [()] block_states: optional. List of block state candidates. See documentation
 * | ? [] fluids: optional. List of fluid candidates.
 * ? entity_effect: optional. MobEffects to apply to LivingEntity
 * | ? damage: optional.
 * | ? is_fire: optional.
 * | ? [{}] mob_effects: optional. List of mob effects to apply
 * | | - id: required. mob effect id
 * | | ? duration: optional.
 * | | ? level: optional.
 * - [{()}] processing_particle: required. Particle effects when item is processed. Without it players can't know if it works.
 * - {} morph: required. Particle effects of the air current. Without it players can't know if it works.
 * | - color_1: required. In hex format. Example: FFFFFF for white
 * | - color_2: required. The real color of particle is randomly interpolated between 1 and 2
 * | - alpha: required. Transparency in 0~1
 * | - sprite_length: required. Length of sprite to use. Usually 3 or 1
 * | ? [{}] particles: optional. Extra particles to fall from air current.
 * | | - id: required. ID for particle. Must be simple particle
 * | | - chance: required. Chance to generate
 * | | - speed: required. Speed factor from the air current speed
 */
public record CustomFanTypeConfig(int priority, BlockPredicateConfig block,
								  EffectEntityConfig entityEffect,
								  List<ProcessingParticleConfig> processingParticle, MorphConfig morph) {

	public record BlockPredicateConfig(List<Block> blocks, List<BlockStatePredicate> blockStates,
									   List<Fluid> fluids) {

		public static final Codec<BlockPredicateConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
						Codec.list(ResourceLocation.CODEC).optionalFieldOf("blocks").forGetter(e ->
								Optional.ofNullable(e.blocks).map(x -> x.stream().map(ForgeRegistryEntry::getRegistryName).toList())),
						Codec.list(Codec.STRING).optionalFieldOf("block_states").forGetter(e ->
								Optional.ofNullable(e.blockStates.stream().map(BlockStatePredicate::toString).toList())),
						Codec.list(ResourceLocation.CODEC).optionalFieldOf("fluids").forGetter(e ->
								Optional.ofNullable(e.fluids).map(x -> x.stream().map(ForgeRegistryEntry::getRegistryName).toList())))
				.apply(i, (blocks, block_states, fluids) -> new BlockPredicateConfig(
						blocks.map(e -> e.stream().map(BlockPredicateConfig::checkBlock).filter(Objects::nonNull).toList()).orElse(null),
						block_states.map(e -> e.stream().map(BlockStatePredicate::new).toList()).orElse(null),
						fluids.map(e -> e.stream().map(BlockPredicateConfig::checkFluid).filter(Objects::nonNull).toList()).orElse(null))));

		private static Block checkBlock(ResourceLocation id) {
			if (!ForgeRegistries.BLOCKS.containsKey(id))
				throw new IllegalArgumentException(id + " is not a block");
			return ForgeRegistries.BLOCKS.getValue(id);
		}

		private static Fluid checkFluid(ResourceLocation id) {
			if (!ForgeRegistries.FLUIDS.containsKey(id))
				throw new IllegalArgumentException(id + " is not a fluid");
			return ForgeRegistries.FLUIDS.getValue(id);
		}

		public boolean isApplicable(BlockGetter reader, BlockPos pos, ResourceLocation name) {
			FluidState fluidState = reader.getFluidState(pos);
			BlockState blockState = reader.getBlockState(pos);
			if ((blocks == null || blocks.size() == 0) && (blockStates == null || blockStates.size() == 0) && (fluids == null || fluids.size() == 0)) {
				throw new IllegalArgumentException("block predicate must have at least one of the following predicates: [blocks, blockStates, fluids]. They need to be in the list form. Error in custom fan processing recipe: " + name);
			}
			if (blocks != null) {
				for (Block block : blocks) {
					if (blockState.is(block)) {
						return true;
					}
				}
			}
			if (blockStates != null) {
				for (BlockStatePredicate pred : blockStates) {
					if (pred.testBlockState(blockState, name)) {
						return true;
					}
				}
			}
			if (fluids != null) {
				for (Fluid fluid : fluids) {
					if (fluidState.is(fluid)) {
						return true;
					}
				}
			}
			return false;
		}

		public BlockState getBlockForDisplay() {
			if (blocks != null && blocks.size() > 0) {
				return blocks.get(0).defaultBlockState();
			}
			if (blockStates != null && blockStates.size() > 0) {
				return blockStates.get(0).getDisplay();
			}
			if (fluids != null && fluids.size() > 0) {
				return fluids.get(0).defaultFluidState().createLegacyBlock();
			}
			return Blocks.AIR.defaultBlockState();
		}

	}

	public record EffectEntityConfig(float damage, boolean isFire,
									 List<EffectEntityConfig.MobEffectConfig> mobEffects) {

		public record MobEffectConfig(MobEffect effect, int duration, int level) {

			public static final Codec<EffectEntityConfig.MobEffectConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
					ResourceLocation.CODEC.fieldOf("id").forGetter(e -> e.effect.getRegistryName()),
					Codec.INT.optionalFieldOf("duration").forGetter(e -> Optional.of(e.duration)),
					Codec.INT.optionalFieldOf("level").forGetter(e -> Optional.of(e.level))
			).apply(i, (id, duration, level) -> new EffectEntityConfig.MobEffectConfig(
					checkMobEffect(id), duration.orElse(0), level.orElse(0))));

			private static MobEffect checkMobEffect(ResourceLocation id) {
				if (!ForgeRegistries.MOB_EFFECTS.containsKey(id))
					throw new IllegalArgumentException(id + " is not a valid mob effect");
				MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(id);
				if (effect.isInstantenous()) {
					throw new IllegalArgumentException("instantenous effects are not supported");
				}
				return effect;
			}

		}

		public static final Codec<EffectEntityConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.FLOAT.optionalFieldOf("damage").forGetter(e -> Optional.of(e.damage)),
				Codec.BOOL.optionalFieldOf("is_fire").forGetter(e -> Optional.of(e.isFire)),
				Codec.list(EffectEntityConfig.MobEffectConfig.CODEC).optionalFieldOf("mob_effects").forGetter(e -> Optional.ofNullable(e.mobEffects))
		).apply(i, (damage, is_fire, mob_effects) -> new EffectEntityConfig(damage.orElse(0f), is_fire.orElse(true), mob_effects.orElse(null))));

		public void affectEntity(Entity entity, Level level, ResourceLocation name) {
			if (level.isClientSide)
				return;
			if (mobEffects != null && entity instanceof LivingEntity livingEntity) {
				for (EffectEntityConfig.MobEffectConfig eff : mobEffects) {
					MobEffect effect = eff.effect;
					livingEntity.addEffect(new MobEffectInstance(effect, eff.duration, eff.level, false, false));
				}
			}
			if (damage > 0f && (!isFire || !entity.fireImmune())) {
				entity.hurt(InWorldProcessing.FIRE_DAMAGE_SOURCE, damage);
			}
		}

	}

	public record MorphConfig(String color1, String color2, float alpha, int spriteLength,
							  List<MorphConfig.ParticleConfig> particles) {

		public record ParticleConfig(SimpleParticleType simple, float chance, float speed) {

			public static final Codec<MorphConfig.ParticleConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
					ResourceLocation.CODEC.fieldOf("id").forGetter(e -> e.simple.getRegistryName()),
					Codec.FLOAT.fieldOf("chance").forGetter(e -> e.chance),
					Codec.FLOAT.fieldOf("speed").forGetter(e -> e.speed)
			).apply(i, (id, chance, speed) -> new MorphConfig.ParticleConfig(checkType(id), chance, speed)));

			public static SimpleParticleType checkType(ResourceLocation id) {
				if (!ForgeRegistries.PARTICLE_TYPES.containsKey(id))
					throw new IllegalArgumentException(id + " is not a valid particle type");
				ParticleType<?> type = ForgeRegistries.PARTICLE_TYPES.getValue(id);
				if (type instanceof SimpleParticleType simple) {
					return simple;
				}
				throw new IllegalArgumentException("particle type " + id + " is not simple particle type");
			}

			public void addParticle(AirFlowParticle particle) {
				particle.addParticle(simple, chance, speed);
			}

		}

		public static final Codec<MorphConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.STRING.fieldOf("color_1").forGetter(e -> e.color1),
				Codec.STRING.fieldOf("color_2").forGetter(e -> e.color2),
				Codec.FLOAT.fieldOf("alpha").forGetter(e -> e.alpha),
				Codec.INT.fieldOf("sprite_length").forGetter(e -> e.spriteLength),
				Codec.list(MorphConfig.ParticleConfig.CODEC).optionalFieldOf("particles").forGetter(e -> Optional.ofNullable(e.particles))
		).apply(i, (color_1, color_2, alpha, sprite_length, particles) -> new MorphConfig(
				checkColor(color_1), checkColor(color_2), alpha, sprite_length, particles.orElse(null))));

		public static String checkColor(String color) {
			// check if color can be parsed
			Integer.parseInt(color, 16);
			return color;
		}

		public void morphType(AirFlowParticle particle) {
			particle.setProperties(Integer.parseInt(color1, 16), Integer.parseInt(color2, 16), alpha, spriteLength);
			for (MorphConfig.ParticleConfig c : particles) {
				c.addParticle(particle);
			}
		}

	}

	public static final Codec<CustomFanTypeConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.INT.optionalFieldOf("priority").forGetter(e -> Optional.of(e.priority)),
			BlockPredicateConfig.CODEC.fieldOf("block").forGetter(e -> e.block),
			EffectEntityConfig.CODEC.optionalFieldOf("entity_effect").forGetter(e -> Optional.ofNullable(e.entityEffect)),
			Codec.list(ProcessingParticleConfig.CODEC).fieldOf("processing_particles").forGetter(e -> e.processingParticle),
			MorphConfig.CODEC.fieldOf("morph").forGetter(e -> e.morph)
	).apply(i, (priority, block, entity_effect, processing_particles, morph) -> new CustomFanTypeConfig(priority.orElse(0), block,
			entity_effect.orElse(null), processing_particles, morph)));

}
