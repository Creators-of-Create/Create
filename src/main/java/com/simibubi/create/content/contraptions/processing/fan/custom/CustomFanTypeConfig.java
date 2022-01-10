package com.simibubi.create.content.contraptions.processing.fan.custom;

import java.util.List;
import java.util.Optional;

import com.mojang.math.Vector3f;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.contraptions.particle.AirFlowParticle;
import com.simibubi.create.content.contraptions.processing.InWorldProcessing;
import com.simibubi.create.foundation.utility.Color;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
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
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public record CustomFanTypeConfig(int priority, String name, BlockPredicateConfig block,
								  EffectEntityConfig entity_effect,
								  List<ProcessingParticleConfig> processing_particle, MorphConfig morph) {

	public record BlockPredicateConfig(List<String> blocks, List<String> block_states, List<String> fluids,
									   List<String> tags) {

		public static final Codec<BlockPredicateConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
						Codec.list(Codec.STRING).optionalFieldOf("blocks").forGetter(e -> Optional.ofNullable(e.blocks)),
						Codec.list(Codec.STRING).optionalFieldOf("block_states").forGetter(e -> Optional.ofNullable(e.block_states)),
						Codec.list(Codec.STRING).optionalFieldOf("fluids").forGetter(e -> Optional.ofNullable(e.fluids)),
						Codec.list(Codec.STRING).optionalFieldOf("tags").forGetter(e -> Optional.ofNullable(e.tags)))
				.apply(i, (blocks, block_states, fluids, tags) -> new BlockPredicateConfig(
						blocks.orElse(null),
						block_states.orElse(null),
						fluids.orElse(null),
						tags.orElse(null))));

		public boolean isApplicable(BlockGetter reader, BlockPos pos, String name) {
			FluidState fluidState = reader.getFluidState(pos);
			BlockState blockState = reader.getBlockState(pos);
			if ((blocks == null || blocks.size() == 0) && (block_states == null || block_states.size() == 0) && (fluids == null || fluids.size() == 0) && (tags == null || tags.size() == 0)) {
				throw new IllegalArgumentException("block predicate must have at least one of the following predicates: [blocks, block_states, fluids, tags]. They need to be in the list form. Error in custom fan processing recipe: " + name);
			}
			if (blocks != null) {
				for (String str : blocks) {
					ResourceLocation id = new ResourceLocation(str);
					Block block = ForgeRegistries.BLOCKS.getValue(id);
					if (block != null && blockState.is(block)) {
						return true;
					}
				}
			}
			if (block_states != null) {
				for (String str : block_states) {
					if (testBlockState(blockState, str, name)) {
						return true;
					}
				}
			}
			if (fluids != null) {
				for (String str : fluids) {
					ResourceLocation id = new ResourceLocation(str);
					Fluid fluid = ForgeRegistries.FLUIDS.getValue(id);
					if (fluidState.is(fluid)) {
						return true;
					}
				}
			}
			if (tags != null) {
				for (String str : tags) {
					ResourceLocation id = new ResourceLocation(str);
					Tag<Block> block_tag = BlockTags.getAllTags().getTag(id);
					if (block_tag != null && block_tag.contains(blockState.getBlock())) {
						return true;
					}
					Tag<Fluid> fluid_tag = FluidTags.getAllTags().getTag(id);
					if (fluid_tag != null && fluid_tag.contains(fluidState.getType())) {
						return true;
					}

				}
			}
			return false;
		}

		public BlockState getBlockForDisplay() {
			if (blocks != null && blocks.size() > 0) {
				return Optional.ofNullable(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blocks.get(0)))).orElse(Blocks.AIR).defaultBlockState();
			}
			if (block_states != null && block_states.size() > 0) {
				String str = block_states.get(0);
				String[] parts = str.split("\\&");
				BlockState state = null;
				for (String part : parts) {
					if (state == null) {
						Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(part));
						if (block == null) {
							return Blocks.AIR.defaultBlockState();
						}
						state = block.defaultBlockState();
					} else {
						String[] equation = part.split("=");
						for (Property<?> property : state.getProperties()) {
							if (property.getName().equals(equation[0])) {
								state = altBlockState(state, property, equation[1]);
							}
						}
					}
				}
				return state;
			}
			if (fluids != null && fluids.size() > 0) {
				return Optional.ofNullable(ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluids.get(0)))).map(e -> e.defaultFluidState().createLegacyBlock()).orElse(Blocks.AIR.defaultBlockState());
			}
			if (tags != null && tags.size() > 0) {
				ResourceLocation tag = new ResourceLocation(tags.get(0));
				return Optional.ofNullable(BlockTags.getAllTags().getTag(tag))
						.map(e -> e.getValues().size() > 0 ? e.getValues().get(0).defaultBlockState() : null)
						.or(() -> Optional.ofNullable(FluidTags.getAllTags().getTag(tag))
								.map(e -> e.getValues().size() > 0 ? e.getValues().get(0).defaultFluidState().createLegacyBlock() : null))
						.orElse(Blocks.AIR.defaultBlockState());
			}
			return null;
		}

		private static boolean testBlockState(BlockState blockState, String str, String name) {
			String[] parts = str.split("\\&");
			if (parts.length < 2) {
				throw new IllegalArgumentException("Block State format: '<block id>&<property>=<value>&...'. Error in custom fan processing type " + name);
			}
			Block block = null;
			for (String part : parts) {
				if (block == null) {
					ResourceLocation id = new ResourceLocation(parts[0]);
					block = ForgeRegistries.BLOCKS.getValue(id);
					if (block == null || !blockState.is(block)) {
						return false;
					}
				} else {
					String[] equation = part.split("=");
					if (equation.length != 2) {
						throw new IllegalArgumentException("property predicate does not have exactly one '='. '" + part + "' in '" + str + "' in custom fan processing type " + name);
					}
					if (!testProperty(blockState, equation[0], equation[1])) {
						return false;
					}
				}
			}
			return true;
		}

		private static boolean testProperty(BlockState blockState, String prop, String value) {
			for (Property<?> property : blockState.getProperties()) {
				if (property.getName().equals(prop)) {
					Optional<?> op = property.getValue(value);
					return op.isPresent() && op.get() == blockState.getValue(property);
				}
			}
			return false;
		}

		private static <T extends Comparable<T>> BlockState altBlockState(BlockState state, Property<T> property, String value) {
			return state.setValue(property, property.getValue(value).get());
		}

	}

	public record EffectEntityConfig(float damage, boolean is_fire,
									 List<EffectEntityConfig.MobEffectConfig> mob_effects) {

		public record MobEffectConfig(String id, int duration, int level) {

			public static final Codec<EffectEntityConfig.MobEffectConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
					Codec.STRING.fieldOf("id").forGetter(e -> e.id),
					Codec.INT.optionalFieldOf("duration").forGetter(e -> Optional.of(e.duration)),
					Codec.INT.optionalFieldOf("level").forGetter(e -> Optional.of(e.level))
			).apply(i, (id, duration, level) -> new EffectEntityConfig.MobEffectConfig(id, duration.orElse(0), level.orElse(0))));

		}

		public static final Codec<EffectEntityConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.FLOAT.optionalFieldOf("damage").forGetter(e -> Optional.of(e.damage)),
				Codec.BOOL.optionalFieldOf("is_fire").forGetter(e -> Optional.of(e.is_fire)),
				Codec.list(EffectEntityConfig.MobEffectConfig.CODEC).optionalFieldOf("mob_effects").forGetter(e -> Optional.ofNullable(e.mob_effects))
		).apply(i, (damage, is_fire, mob_effects) -> new EffectEntityConfig(damage.orElse(0f), is_fire.orElse(true), mob_effects.orElse(null))));

		public void affectEntity(Entity entity, Level level, String name) {
			if (level.isClientSide)
				return;
			if (mob_effects != null && entity instanceof LivingEntity livingEntity) {
				for (EffectEntityConfig.MobEffectConfig eff : mob_effects) {
					ResourceLocation id = new ResourceLocation(eff.id);
					MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(id);
					if (effect == null) {
						throw new IllegalArgumentException("effect " + eff.id + " does not exist. Error in custom fan processing type " + name);
					}
					livingEntity.addEffect(new MobEffectInstance(effect, eff.duration, eff.level, false, false));
				}
			}
			if (damage > 0f && (!is_fire || !entity.fireImmune())) {
				entity.hurt(InWorldProcessing.FIRE_DAMAGE_SOURCE, damage);
			}
		}

	}

	public record MorphConfig(String color_1, String color_2, float alpha, int sprite_length,
							  List<MorphConfig.ParticleConfig> particles) {

		public record ParticleConfig(String id, float chance, float speed) {

			public static final Codec<MorphConfig.ParticleConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
					Codec.STRING.fieldOf("id").forGetter(e -> e.id),
					Codec.FLOAT.fieldOf("chance").forGetter(e -> e.chance),
					Codec.FLOAT.fieldOf("speed").forGetter(e -> e.speed)
			).apply(i, MorphConfig.ParticleConfig::new));

			public void addParticle(AirFlowParticle particle) {
				ParticleType<?> type = ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation(id));
				if (type instanceof SimpleParticleType simple) {
					particle.addParticle(simple, chance, speed);
				} else throw new IllegalArgumentException("particle type " + id + " is not simple particle type");
			}

		}

		public static final Codec<MorphConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.STRING.fieldOf("color_1").forGetter(e -> e.color_1),
				Codec.STRING.fieldOf("color_2").forGetter(e -> e.color_2),
				Codec.FLOAT.fieldOf("alpha").forGetter(e -> e.alpha),
				Codec.INT.fieldOf("sprite_length").forGetter(e -> e.sprite_length),
				Codec.list(MorphConfig.ParticleConfig.CODEC).optionalFieldOf("particles").forGetter(e -> Optional.ofNullable(e.particles))
		).apply(i, (color_1, color_2, alpha, sprite_length, particles) -> new MorphConfig(color_1, color_2, alpha, sprite_length, particles.orElse(null))));

		public void morphType(AirFlowParticle particle) {
			particle.setProperties(Integer.parseInt(color_1, 16), Integer.parseInt(color_2, 16), alpha, sprite_length);
			for (MorphConfig.ParticleConfig c : particles) {
				c.addParticle(particle);
			}
		}

	}

	public record ProcessingParticleConfig(int chance, String col, String id,
										   ProcessingParticleConfig.OffsetConfig base_offset,
										   ProcessingParticleConfig.OffsetConfig random_offset,
										   ProcessingParticleConfig.OffsetConfig speed) {

		public record OffsetConfig(float x, float y, float z) {

			public static final Codec<ProcessingParticleConfig.OffsetConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
					Codec.FLOAT.fieldOf("x").forGetter(e -> e.x),
					Codec.FLOAT.fieldOf("y").forGetter(e -> e.y),
					Codec.FLOAT.fieldOf("z").forGetter(e -> e.z)
			).apply(i, ProcessingParticleConfig.OffsetConfig::new));

		}

		public static final Codec<ProcessingParticleConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.INT.fieldOf("chance").forGetter(e -> e.chance),
				Codec.STRING.fieldOf("color").forGetter(e -> e.col),
				Codec.STRING.fieldOf("id").forGetter(e -> e.id),
				ProcessingParticleConfig.OffsetConfig.CODEC.optionalFieldOf("base_offset").forGetter(e -> Optional.of(e.base_offset)),
				ProcessingParticleConfig.OffsetConfig.CODEC.optionalFieldOf("random_offset").forGetter(e -> Optional.of(e.random_offset)),
				ProcessingParticleConfig.OffsetConfig.CODEC.optionalFieldOf("speed").forGetter(e -> Optional.of(e.speed))
		).apply(i, (chance, col, id, base_offset, random_offset, speed) -> new ProcessingParticleConfig(chance, col, id,
				base_offset.orElse(new ProcessingParticleConfig.OffsetConfig(0, 0, 0)),
				random_offset.orElse(new ProcessingParticleConfig.OffsetConfig(0, 0, 0)),
				speed.orElse(new ProcessingParticleConfig.OffsetConfig(0, 0, 0)))));

		public void spawnParticlesForProcessing(Level level, Vec3 pos) {
			if (level.random.nextInt(chance) != 0)
				return;
			ParticleOptions option = null;
			if (id != null && id.length() > 0) {
				ParticleType<?> type = ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation(id));
				if (type instanceof SimpleParticleType simple) {
					option = simple;
				} else throw new IllegalArgumentException("particle type " + id + " is not simple particle type");
			} else if (col != null && col.length() > 0) {
				Vector3f color = new Color(Integer.parseInt(col, 16)).asVectorF();
				option = new DustParticleOptions(color, 1);
			}
			double x = pos.x + base_offset.x + (level.random.nextFloat() - .5f) * random_offset.x;
			double y = pos.y + base_offset.y + (level.random.nextFloat() - .5f) * random_offset.y;
			double z = pos.z + base_offset.z + (level.random.nextFloat() - .5f) * random_offset.z;
			if (option != null) {
				level.addParticle(option, x, y, z, speed.x, speed.y, speed.z);
			}
		}

	}

	public static final Codec<CustomFanTypeConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.INT.optionalFieldOf("priority").forGetter(e -> Optional.of(e.priority)),
			Codec.STRING.fieldOf("name").forGetter(e -> e.name),
			BlockPredicateConfig.CODEC.fieldOf("block").forGetter(e -> e.block),
			EffectEntityConfig.CODEC.optionalFieldOf("entity_effect").forGetter(e -> Optional.ofNullable(e.entity_effect)),
			Codec.list(ProcessingParticleConfig.CODEC).optionalFieldOf("processing_particles").forGetter(e -> Optional.ofNullable(e.processing_particle)),
			MorphConfig.CODEC.optionalFieldOf("morph").forGetter(e -> Optional.ofNullable(e.morph))
	).apply(i, (priority, name, block, entity_effect, processing_particles, morph) -> new CustomFanTypeConfig(priority.orElse(0), name, block,
			entity_effect.orElse(null), processing_particles.orElse(null), morph.orElse(null))));

}
