package com.simibubi.create;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import com.simibubi.create.content.contraptions.processing.fan.custom.CustomFanProcessingRecipe;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableSet;
import com.simibubi.create.compat.jei.ConversionRecipe;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCraftingRecipe;
import com.simibubi.create.content.contraptions.components.crusher.CrushingRecipe;
import com.simibubi.create.content.contraptions.components.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.contraptions.components.deployer.ManualApplicationRecipe;
import com.simibubi.create.content.contraptions.processing.fan.HauntingRecipe;
import com.simibubi.create.content.contraptions.processing.fan.SplashingRecipe;
import com.simibubi.create.content.contraptions.components.millstone.MillingRecipe;
import com.simibubi.create.content.contraptions.components.mixer.CompactingRecipe;
import com.simibubi.create.content.contraptions.components.mixer.MixingRecipe;
import com.simibubi.create.content.contraptions.components.press.PressingRecipe;
import com.simibubi.create.content.contraptions.components.saw.CuttingRecipe;
import com.simibubi.create.content.contraptions.fluids.actors.FillingRecipe;
import com.simibubi.create.content.contraptions.itemAssembly.SequencedAssemblyRecipeSerializer;
import com.simibubi.create.content.contraptions.processing.BasinRecipe;
import com.simibubi.create.content.contraptions.processing.EmptyingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder.ProcessingRecipeFactory;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeSerializer;
import com.simibubi.create.content.curiosities.toolbox.ToolboxDyeingRecipe;
import com.simibubi.create.content.curiosities.tools.SandPaperPolishingRecipe;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.RegisteredObjects;
import com.simibubi.create.foundation.utility.recipe.IRecipeTypeInfo;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public enum AllRecipeTypes implements IRecipeTypeInfo {

	CONVERSION(ConversionRecipe::new),
	CRUSHING(CrushingRecipe::new),
	CUTTING(CuttingRecipe::new),
	MILLING(MillingRecipe::new),
	BASIN(BasinRecipe::new),
	MIXING(MixingRecipe::new),
	COMPACTING(CompactingRecipe::new),
	PRESSING(PressingRecipe::new),
	SANDPAPER_POLISHING(SandPaperPolishingRecipe::new),
	SPLASHING(SplashingRecipe::new),
	HAUNTING(HauntingRecipe::new),
	CUSTOM_FAN(CustomFanProcessingRecipe.Serializer::new),
	DEPLOYING(DeployerApplicationRecipe::new),
	FILLING(FillingRecipe::new),
	EMPTYING(EmptyingRecipe::new),
	ITEM_APPLICATION(ManualApplicationRecipe::new),

	MECHANICAL_CRAFTING(MechanicalCraftingRecipe.Serializer::new),
	SEQUENCED_ASSEMBLY(SequencedAssemblyRecipeSerializer::new),

	TOOLBOX_DYEING(() -> new SimpleRecipeSerializer<>(ToolboxDyeingRecipe::new), () -> RecipeType.CRAFTING, false);

	private final ResourceLocation id;
	private final RegistryObject<RecipeSerializer<?>> serializerObject;
	@Nullable
	private final RegistryObject<RecipeType<?>> typeObject;
	private final Supplier<RecipeType<?>> type;

	AllRecipeTypes(Supplier<RecipeSerializer<?>> serializerSupplier, Supplier<RecipeType<?>> typeSupplier, boolean registerType) {
		String name = Lang.asId(name());
		id = Create.asResource(name);
		serializerObject = Registers.SERIALIZER_REGISTER.register(name, serializerSupplier);
		if (registerType) {
			typeObject = Registers.TYPE_REGISTER.register(name, typeSupplier);
			type = typeObject;
		} else {
			typeObject = null;
			type = typeSupplier;
		}
	}

	AllRecipeTypes(Supplier<RecipeSerializer<?>> serializerSupplier) {
		String name = Lang.asId(name());
		id = Create.asResource(name);
		serializerObject = Registers.SERIALIZER_REGISTER.register(name, serializerSupplier);
		typeObject = Registers.TYPE_REGISTER.register(name, () -> simpleType(id));
		type = typeObject;
	}

	AllRecipeTypes(ProcessingRecipeFactory<?> processingFactory) {
		this(() -> new ProcessingRecipeSerializer<>(processingFactory));
	}

	public static <T extends Recipe<?>> RecipeType<T> simpleType(ResourceLocation id) {
		String stringId = id.toString();
		return new RecipeType<T>() {
			@Override
			public String toString() {
				return stringId;
			}
		};
	}

	public static void register(IEventBus modEventBus) {
		ShapedRecipe.setCraftingSize(9, 9);
		Registers.SERIALIZER_REGISTER.register(modEventBus);
		Registers.TYPE_REGISTER.register(modEventBus);
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends RecipeSerializer<?>> T getSerializer() {
		return (T) serializerObject.get();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends RecipeType<?>> T getType() {
		return (T) type.get();
	}

	public <C extends Container, T extends Recipe<C>> Optional<T> find(C inv, Level world) {
		return world.getRecipeManager()
			.getRecipeFor(getType(), inv, world);
	}

	public static final Set<ResourceLocation> RECIPE_DENY_SET =
		ImmutableSet.of(new ResourceLocation("occultism", "spirit_trade"), new ResourceLocation("occultism", "ritual"));

	public static boolean shouldIgnoreInAutomation(Recipe<?> recipe) {
		RecipeSerializer<?> serializer = recipe.getSerializer();
		if (serializer != null && RECIPE_DENY_SET.contains(RegisteredObjects.getKeyOrThrow(serializer)))
			return true;
		return recipe.getId()
			.getPath()
			.endsWith("_manual_only");
	}

	private static class Registers {
		private static final DeferredRegister<RecipeSerializer<?>> SERIALIZER_REGISTER = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Create.ID);
		private static final DeferredRegister<RecipeType<?>> TYPE_REGISTER = DeferredRegister.create(Registry.RECIPE_TYPE_REGISTRY, Create.ID);
	}

}
