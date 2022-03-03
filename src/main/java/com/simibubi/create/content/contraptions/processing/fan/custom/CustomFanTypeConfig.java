package com.simibubi.create.content.contraptions.processing.fan.custom;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.contraptions.particle.AirFlowParticle;
import com.simibubi.create.content.contraptions.processing.InWorldProcessing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
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

/**
 * Config for datapack fan processing types
 * - priority: required/ Higher value means this fan type is checked first, if multiple fan types conflicts
 * - name: required. Name of this fan type, usually in full capital letters (NONE, BLASTING, SPLASHING, etc)
 * - {} block: required. Block predicate to test blocks
 * | ? [] blocks: optional. List of block candidates. Use block_states if some states of this block shouldn't be used
 * | ? [()] block_states: optional. List of block state candidates. See documentation
 * | ? [] fluids: optional. List of fluid candidates.
 * | ? [] tags: optional. List of block or fluid candidates
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
public record CustomFanTypeConfig(int priority, String name, BlockPredicateConfig block,
								  EffectEntityConfig entityEffect,
								  List<ProcessingParticleConfig> processingParticle, MorphConfig morph) {

	public record BlockPredicateConfig(List<Block> blocks, List<BlockStatePredicate> blockStates,
									   List<Fluid> fluids,
									   List<Either<Tag<Block>, Tag<Fluid>>> tags) {

		public static final Codec<BlockPredicateConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.list(ResourceLocation.CODEC).optionalFieldOf("blocks").forGetter(e ->
					Optional.ofNullable(e.blocks).map(x -> x.stream().map(ForgeRegistryEntry::getRegistryName).toList())),
				Codec.list(Codec.STRING).optionalFieldOf("block_states").forGetter(e ->
					Optional.ofNullable(e.blockStates.stream().map(BlockStatePredicate::toString).toList())),
				Codec.list(ResourceLocation.CODEC).optionalFieldOf("fluids").forGetter(e ->
					Optional.ofNullable(e.fluids).map(x -> x.stream().map(ForgeRegistryEntry::getRegistryName).toList())),
				Codec.list(ResourceLocation.CODEC).optionalFieldOf("tags").forGetter(e ->
					Optional.ofNullable(e.tags).map(x -> x.stream().map(y ->
						y.map(BlockTags.getAllTags()::getId, FluidTags.getAllTags()::getId)).toList())))
			.apply(i, (blocks, block_states, fluids, tags) -> new BlockPredicateConfig(
				blocks.map(e -> e.stream().map(ForgeRegistries.BLOCKS::getValue).filter(Objects::nonNull).toList()).orElse(null),
				block_states.map(e -> e.stream().map(BlockStatePredicate::new).toList()).orElse(null),
				fluids.map(e -> e.stream().map(ForgeRegistries.FLUIDS::getValue).filter(Objects::nonNull).toList()).orElse(null),
				tags.map(e -> e.stream().map(BlockPredicateConfig::checkTag).filter(Objects::nonNull).toList()).orElse(null))));

		public static Either<Tag<Block>, Tag<Fluid>> checkTag(ResourceLocation id) {
			Tag<Block> blockTag = BlockTags.getAllTags().getTag(id);
			if (blockTag != null) {
				return Either.left(blockTag);
			}
			Tag<Fluid> fluidTag = FluidTags.getAllTags().getTag(id);
			if (fluidTag != null) {
				return Either.right(fluidTag);
			}
			return null;
		}

		public boolean isApplicable(BlockGetter reader, BlockPos pos, String name) {
			FluidState fluidState = reader.getFluidState(pos);
			BlockState blockState = reader.getBlockState(pos);
			if ((blocks == null || blocks.size() == 0) && (blockStates == null || blockStates.size() == 0) && (fluids == null || fluids.size() == 0) && (tags == null || tags.size() == 0)) {
				throw new IllegalArgumentException("block predicate must have at least one of the following predicates: [blocks, blockStates, fluids, tags]. They need to be in the list form. Error in custom fan processing recipe: " + name);
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
			if (tags != null) {
				for (Either<Tag<Block>, Tag<Fluid>> tag : tags) {
					if (tag.map(a -> a.contains(blockState.getBlock()), b -> b.contains(fluidState.getType())))
						return true;
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
			if (tags != null && tags.size() > 0) {
				for (Either<Tag<Block>, Tag<Fluid>> tag : tags) {
					BlockState sample = tag.map(a -> a.getValues().size() > 0 ? a.getValues().get(0).defaultBlockState() : null,
						b -> b.getValues().size() > 0 ? b.getValues().get(0).defaultFluidState().createLegacyBlock() : null);
					if (sample != null)
						return sample;
				}
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
				Objects.requireNonNull(ForgeRegistries.MOB_EFFECTS.getValue(id)), duration.orElse(0), level.orElse(0))));

		}

		public static final Codec<EffectEntityConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.FLOAT.optionalFieldOf("damage").forGetter(e -> Optional.of(e.damage)),
			Codec.BOOL.optionalFieldOf("is_fire").forGetter(e -> Optional.of(e.isFire)),
			Codec.list(EffectEntityConfig.MobEffectConfig.CODEC).optionalFieldOf("mob_effects").forGetter(e -> Optional.ofNullable(e.mobEffects))
		).apply(i, (damage, is_fire, mob_effects) -> new EffectEntityConfig(damage.orElse(0f), is_fire.orElse(true), mob_effects.orElse(null))));

		public void affectEntity(Entity entity, Level level, String name) {
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
		Codec.STRING.fieldOf("name").forGetter(e -> e.name),
		BlockPredicateConfig.CODEC.fieldOf("block").forGetter(e -> e.block),
		EffectEntityConfig.CODEC.optionalFieldOf("entity_effect").forGetter(e -> Optional.ofNullable(e.entityEffect)),
		Codec.list(ProcessingParticleConfig.CODEC).fieldOf("processing_particles").forGetter(e -> e.processingParticle),
		MorphConfig.CODEC.fieldOf("morph").forGetter(e -> e.morph)
	).apply(i, (priority, name, block, entity_effect, processing_particles, morph) -> new CustomFanTypeConfig(priority.orElse(0), name, block,
		entity_effect.orElse(null), processing_particles, morph)));

}
