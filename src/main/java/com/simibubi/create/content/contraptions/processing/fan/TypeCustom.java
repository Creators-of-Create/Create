package com.simibubi.create.content.contraptions.processing.fan;

import java.util.List;
import java.util.Optional;

import com.simibubi.create.AllRecipeTypes;

import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import org.jetbrains.annotations.Nullable;

import com.mojang.math.Vector3f;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import static com.simibubi.create.content.contraptions.processing.InWorldProcessing.applyRecipeOn;

public class TypeCustom extends AbstractFanProcessingType {

	public record Config(int priority, String name, BlockPredicateConfig block, EffectEntityConfig entity_effect,
						 ProcessingParticleConfig[] processing_particle,
						 MorphConfig morph) {

		public record BlockPredicateConfig(String[] blocks, String[] block_states, String[] fluids,
										   String[] tags) {

			public boolean isApplicable(BlockGetter reader, BlockPos pos, String name) {
				FluidState fluidState = reader.getFluidState(pos);
				BlockState blockState = reader.getBlockState(pos);
				if ((blocks == null || blocks.length == 0) && (block_states == null || block_states.length == 0) && (fluids == null || fluids.length == 0) && (tags == null || tags.length == 0)) {
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

		}

		public record EffectEntityConfig(float damage, boolean is_fire, MobEffectConfig[] mob_effects) {

			public record MobEffectConfig(String id, int duration, int level) {

			}

			public void affectEntity(Entity entity, Level level, String name) {
				if (level.isClientSide)
					return;
				if (mob_effects != null && entity instanceof LivingEntity livingEntity) {
					for (MobEffectConfig eff : mob_effects) {
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
								  ParticleConfig[] particles) {

			public record ParticleConfig(String id, float chance, float speed) {

				public void addParticle(AirFlowParticle particle) {
					ParticleType<?> type = ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation(id));
					if (type instanceof SimpleParticleType simple) {
						particle.addParticle(simple, chance, speed);
					} else throw new IllegalArgumentException("particle type " + id + " is not simple particle type");
				}

			}

			public void morphType(AirFlowParticle particle) {
				particle.setProperties(Integer.parseInt(color_1, 16), Integer.parseInt(color_2, 16), alpha, sprite_length);
				for (ParticleConfig c : particles) {
					c.addParticle(particle);
				}
			}

		}

		public record ProcessingParticleConfig(int chance, String col, String id, OffsetConfig base_offset,
											   OffsetConfig random_offset, OffsetConfig speed) {

			public record OffsetConfig(float x, float y, float z) {

			}

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

	}

	public static final CustomRecipeWrapper RECIPE_WRAPPER = new CustomRecipeWrapper();

	private final Config config;

	public TypeCustom(Config config) {
		super(config.priority, config.name);
		this.config = config;
	}

	@Override
	public boolean canProcess(ItemStack stack, Level level) {
		RECIPE_WRAPPER.setItem(0, stack);
		RECIPE_WRAPPER.type = this;
		Optional<HauntingRecipe> recipe = AllRecipeTypes.CUSTOM_FAN.find(RECIPE_WRAPPER, level);
		return recipe.isPresent();
	}

	@Override
	public void spawnParticlesForProcessing(Level level, Vec3 pos) {
		if (config.processing_particle != null && config.processing_particle.length > 0) {
			for (Config.ProcessingParticleConfig ppc : config.processing_particle)
				ppc.spawnParticlesForProcessing(level, pos);
		}
	}

	@Override
	public void affectEntity(Entity entity, Level level) {
		if (config.entity_effect != null) {
			config.entity_effect.affectEntity(entity, level, config.name);
		}
	}

	@Override
	public boolean isApplicable(BlockGetter reader, BlockPos pos) {
		return config.block.isApplicable(reader, pos, config.name);
	}

	@Nullable
	@Override
	public List<ItemStack> process(ItemStack stack, Level world) {
		RECIPE_WRAPPER.setItem(0, stack);
		RECIPE_WRAPPER.type = this;
		Optional<CustomFanProcessingRecipe> recipe = AllRecipeTypes.CUSTOM_FAN.find(RECIPE_WRAPPER, world);
		if (recipe.isPresent())
			return applyRecipeOn(stack, recipe.get());
		return null;
	}

	@Override
	public void morphType(AirFlowParticle particle) {
		Config.MorphConfig morph = config.morph;
		if (morph != null) {
			morph.morphType(particle);
		}

	}

	public static class CustomRecipeWrapper extends RecipeWrapper {

		public TypeCustom type = null;

		public CustomRecipeWrapper() {
			super(new ItemStackHandler(1));
		}
	}

}
