package com.simibubi.create;

import static com.simibubi.create.AllTags.NameSpace.COMMON;
import static com.simibubi.create.AllTags.NameSpace.CURIOS;
import static com.simibubi.create.AllTags.NameSpace.MOD;
import static com.simibubi.create.AllTags.NameSpace.QUARK;
import static com.simibubi.create.AllTags.NameSpace.TIC;

import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.contraption.ContraptionType;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorage;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.registry.CreateDataMaps;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.decoration.palettes.AllPaletteStoneTypes;
import com.simibubi.create.foundation.data.recipe.CommonMetal;

import net.createmod.catnip.lang.Lang;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

public class AllTags {
	@ScheduledForRemoval(inVersion = "1.21.1+ Port")
	@Deprecated(since = "6.0.7", forRemoval = true)
	public static <T> TagKey<T> optionalTag(Registry<T> registry, ResourceLocation id) {
		return TagKey.create(registry.key(), id);
	}

	@ScheduledForRemoval(inVersion = "1.21.1+ Port")
	@Deprecated(since = "6.0.7", forRemoval = true)
	public static <T> TagKey<T> commonTag(Registry<T> registry, String path) {
		return optionalTag(registry, ResourceLocation.fromNamespaceAndPath("c", path));
	}

	@ScheduledForRemoval(inVersion = "1.21.1+ Port")
	@Deprecated(since = "6.0.7", forRemoval = true)
	public static TagKey<Block> commonBlockTag(String path) {
		return commonTag(BuiltInRegistries.BLOCK, path);
	}

	@ScheduledForRemoval(inVersion = "1.21.1+ Port")
	@Deprecated(since = "6.0.7", forRemoval = true)
	public static TagKey<Item> commonItemTag(String path) {
		return commonTag(BuiltInRegistries.ITEM, path);
	}

	@ScheduledForRemoval(inVersion = "1.21.1+ Port")
	@Deprecated(since = "6.0.7", forRemoval = true)
	public static TagKey<Fluid> commonFluidTag(String path) {
		return commonTag(BuiltInRegistries.FLUID, path);
	}

	public enum NameSpace {
		MOD(Create.ID),
		COMMON("c"),
		TIC("tconstruct"),
		QUARK("quark"),
		GS("galosphere"),
		CURIOS("curios");

		public final String id;

		NameSpace(String id) {
			this.id = id;
		}

		public ResourceLocation id(String path) {
			return ResourceLocation.fromNamespaceAndPath(this.id, path);
		}

		public ResourceLocation id(Enum<?> entry, @Nullable String pathOverride) {
			return this.id(pathOverride != null ? pathOverride : Lang.asId(entry.name()));
		}
	}

	public enum AllBlockTags {
		BRITTLE,
		CASING,
		COPYCAT_ALLOW,
		COPYCAT_DENY,
		FAN_PROCESSING_CATALYSTS_BLASTING(MOD, "fan_processing_catalysts/blasting"),
		FAN_PROCESSING_CATALYSTS_HAUNTING(MOD, "fan_processing_catalysts/haunting"),
		FAN_PROCESSING_CATALYSTS_SMOKING(MOD, "fan_processing_catalysts/smoking"),
		FAN_PROCESSING_CATALYSTS_SPLASHING(MOD, "fan_processing_catalysts/splashing"),
		FAN_TRANSPARENT,
		GIRDABLE_TRACKS,
		MOVABLE_EMPTY_COLLIDER,
		NON_MOVABLE,
		NON_BREAKABLE,
		PASSIVE_BOILER_HEATERS,
		SAFE_NBT,
		SEATS,
		POSTBOXES,
		TABLE_CLOTHS,
		TOOLBOXES,
		TRACKS,
		TREE_ATTACHMENTS,
		VALVE_HANDLES,
		WINDMILL_SAILS,
		WRENCH_PICKUP,
		CHEST_MOUNTED_STORAGE,
		SIMPLE_MOUNTED_STORAGE,
		FALLBACK_MOUNTED_STORAGE_BLACKLIST,
		ROOTS,
		SUGAR_CANE_VARIANTS,
		NON_HARVESTABLE,
		SINGLE_BLOCK_INVENTORIES,
		CARDBOARD_STORAGE_BLOCKS(COMMON, "storage_blocks/cardboard"),
		ANDESITE_ALLOY_STORAGE_BLOCKS(COMMON, "storage_blocks/andesite_alloy"),

		CORALS,

		SLIMY_LOGS(TIC),
		NON_DOUBLE_DOOR(QUARK),
		;

		public final TagKey<Block> tag;

		AllBlockTags() {
			this(MOD);
		}

		AllBlockTags(NameSpace namespace) {
			this(namespace, null);
		}

		AllBlockTags(NameSpace namespace, @Nullable String pathOverride) {
			this.tag = TagKey.create(Registries.BLOCK, namespace.id(this, pathOverride));
		}

		@SuppressWarnings("deprecation")
		public boolean matches(Block block) {
			return block.builtInRegistryHolder()
				.is(tag);
		}

		public boolean matches(ItemStack stack) {
			return stack != null && stack.getItem() instanceof BlockItem blockItem && matches(blockItem.getBlock());
		}

		public boolean matches(BlockState state) {
			return state.is(tag);
		}

	}

	/**
	 * Despite the name, not truly all item tags.
	 *
	 * @see CommonMetal
	 * @see AllPaletteStoneTypes#materialTag
	 */
	public enum AllItemTags {
		/**
		 * @deprecated <p> Use {@link NeoForgeDataMaps#FURNACE_FUELS} or {@link CreateDataMaps#REGULAR_BLAZE_BURNER_FUELS} instead.
		 */
		@ScheduledForRemoval(inVersion = "1.21.1+ Port")
		@Deprecated(since = "6.0.7", forRemoval = true)
		BLAZE_BURNER_FUEL_REGULAR(MOD, "blaze_burner_fuel/regular"),
		/**
		 * @deprecated <p> Use {@link CreateDataMaps#SUPERHEATED_BLAZE_BURNER_FUELS} instead.
		 */
		@ScheduledForRemoval(inVersion = "1.21.1+ Port")
		@Deprecated(since = "6.0.7", forRemoval = true)
		BLAZE_BURNER_FUEL_SPECIAL(MOD, "blaze_burner_fuel/special"),
		CASING,
		CONTRAPTION_CONTROLLED,
		CREATE_INGOTS,
		CRUSHED_RAW_MATERIALS,
		INVALID_FOR_TRACK_PAVING,
		DEPLOYABLE_DRINK,
		PRESSURIZED_AIR_SOURCES,
		SANDPAPER,
		SEATS,
		POSTBOXES,
		TABLE_CLOTHS,
		DYED_TABLE_CLOTHS,
		PULPIFIABLE,
		SLEEPERS,
		TOOLBOXES,
		PACKAGES,
		CHAIN_RIDEABLE,
		TRACKS,
		UPRIGHT_ON_BELT,
		NOT_UPRIGHT_ON_BELT,
		NOT_POTION,
		VALVE_HANDLES,
		DISPENSE_BEHAVIOR_WRAP_BLACKLIST,

		OBSIDIAN_DUST(COMMON, "dusts/obsidian"),

		PLATES(COMMON),
		OBSIDIAN_PLATES(COMMON, "plates/obsidian"),
		CARDBOARD_PLATES(COMMON, "plates/cardboard"),

		ALLURITE(MOD, "stone_types/galosphere/allurite"),
		AMETHYST(MOD, "stone_types/galosphere/amethyst"),
		LUMIERE(MOD, "stone_types/galosphere/lumiere"),

		CERTUS_QUARTZ(COMMON, "gems/certus_quartz"),

		AMETRINE_ORES(COMMON, "ores/ametrine"),
		ANTHRACITE_ORES(COMMON, "ores/anthracite"),
		EMERALDITE_ORES(COMMON, "ores/emeraldite"),
		LIGNITE_ORES(COMMON, "ores/lignite"),

		CARDBOARD_STORAGE_BLOCKS(COMMON, "storage_blocks/cardboard"),
		ANDESITE_ALLOY_STORAGE_BLOCKS(COMMON, "storage_blocks/andesite_alloy"),

		CHOCOLATE_BUCKETS(COMMON, "buckets/chocolate"),
		HONEY_BUCKETS(COMMON, "buckets/honey"),

		FOODS_CHOCOLATE(COMMON, "foods/chocolate"),

		DRINKS_TEA(COMMON, "drinks/tea"),

		FLOURS(COMMON),
		WHEAT_FLOURS(COMMON, "flours/wheat"),

		DOUGHS(COMMON),
		WHEAT_DOUGHS(COMMON, "doughs/wheat"),

		UA_CORAL(MOD, "upgrade_aquatic/coral"),
		CURIOS_HEAD(CURIOS, "head");

		public final TagKey<Item> tag;

		AllItemTags() {
			this(MOD);
		}

		AllItemTags(NameSpace namespace) {
			this(namespace, null);
		}

		AllItemTags(NameSpace namespace, @Nullable String pathOverride) {
			this.tag = TagKey.create(Registries.ITEM, namespace.id(this, pathOverride));
		}

		@SuppressWarnings("deprecation")
		public boolean matches(Item item) {
			return item.builtInRegistryHolder()
				.is(tag);
		}

		public boolean matches(ItemStack stack) {
			return stack.is(tag);
		}
	}

	public enum AllFluidTags {
		BOTTOMLESS_ALLOW(MOD, "bottomless/allow"),
		BOTTOMLESS_DENY(MOD, "bottomless/deny"),
		FAN_PROCESSING_CATALYSTS_BLASTING(MOD, "fan_processing_catalysts/blasting"),
		FAN_PROCESSING_CATALYSTS_HAUNTING(MOD, "fan_processing_catalysts/haunting"),
		FAN_PROCESSING_CATALYSTS_SMOKING(MOD, "fan_processing_catalysts/smoking"),
		FAN_PROCESSING_CATALYSTS_SPLASHING(MOD, "fan_processing_catalysts/splashing"),

		TEA(COMMON),
		CHOCOLATE(COMMON),

		CREOSOTE(COMMON);

		public final TagKey<Fluid> tag;

		AllFluidTags() {
			this(MOD);
		}

		AllFluidTags(NameSpace namespace) {
			this(namespace, null);
		}

		AllFluidTags(NameSpace namespace, @Nullable String pathOverride) {
			this.tag = TagKey.create(Registries.FLUID, namespace.id(this, pathOverride));
		}

		@SuppressWarnings("deprecation")
		public boolean matches(Fluid fluid) {
			return fluid.is(tag);
		}

		public boolean matches(FluidState state) {
			return state.is(tag);
		}
	}

	public enum AllEntityTags {
		BLAZE_BURNER_CAPTURABLE,
		IGNORE_SEAT;

		public final TagKey<EntityType<?>> tag;

		AllEntityTags() {
			this(MOD);
		}

		AllEntityTags(NameSpace namespace) {
			this(namespace, null);
		}

		AllEntityTags(NameSpace namespace, @Nullable String pathOverride) {
			this.tag = TagKey.create(Registries.ENTITY_TYPE, namespace.id(this, pathOverride));
		}

		public boolean matches(EntityType<?> type) {
			return type.is(tag);
		}

		public boolean matches(Entity entity) {
			return matches(entity.getType());
		}
	}

	public enum AllRecipeSerializerTags {
		AUTOMATION_IGNORE;

		public final TagKey<RecipeSerializer<?>> tag;

		AllRecipeSerializerTags() {
			this(MOD);
		}

		AllRecipeSerializerTags(NameSpace namespace) {
			this(namespace, null);
		}

		AllRecipeSerializerTags(NameSpace namespace, @Nullable String pathOverride) {
			this.tag = TagKey.create(Registries.RECIPE_SERIALIZER, namespace.id(this, pathOverride));
		}

		public boolean matches(RecipeSerializer<?> recipeSerializer) {
			ResourceKey<RecipeSerializer<?>> key = BuiltInRegistries.RECIPE_SERIALIZER.getResourceKey(recipeSerializer).orElseThrow();
			return BuiltInRegistries.RECIPE_SERIALIZER.getHolder(key).orElseThrow().is(tag);
		}
	}

	public enum AllContraptionTypeTags {
		OPENS_CONTROLS,
		REQUIRES_VEHICLE_FOR_RENDER;

		public final TagKey<ContraptionType> tag;

		AllContraptionTypeTags() {
			this(MOD);
		}

		AllContraptionTypeTags(NameSpace namespace) {
			this(namespace, null);
		}

		AllContraptionTypeTags(NameSpace namespace, @Nullable String pathOverride) {
			this.tag = TagKey.create(CreateRegistries.CONTRAPTION_TYPE, namespace.id(this, pathOverride));
		}

		public boolean matches(ContraptionType type) {
			return type.is(this.tag);
		}
	}

	public enum AllMountedItemStorageTypeTags {
		INTERNAL,
		FUEL_BLACKLIST;

		public final TagKey<MountedItemStorageType<?>> tag;

		AllMountedItemStorageTypeTags() {
			this(MOD);
		}

		AllMountedItemStorageTypeTags(NameSpace namespace) {
			this(namespace, null);
		}

		AllMountedItemStorageTypeTags(NameSpace namespace, @Nullable String pathOverride) {
			this.tag = TagKey.create(CreateRegistries.MOUNTED_ITEM_STORAGE_TYPE, namespace.id(this, pathOverride));
		}

		public boolean matches(MountedItemStorage storage) {
			return this.matches(storage.type);
		}

		public boolean matches(MountedItemStorageType<?> type) {
			return type.is(this.tag);
		}
	}
}
