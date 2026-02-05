package com.simibubi.create.foundation.codec;

import java.util.function.Function;

import com.mojang.serialization.Lifecycle;
import com.simibubi.create.Create;

import net.minecraft.ResourceLocationException;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.foundation.fluid.FluidIngredientOld;
import com.simibubi.create.foundation.item.ItemSlots;

import net.minecraft.util.ExtraCodecs;

import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class CreateCodecs {
	public static final Codec<Integer> INT_STR = Codec.STRING.comapFlatMap(
		string -> {
			try {
				return DataResult.success(Integer.parseInt(string));
			} catch (NumberFormatException ignored) {
				return DataResult.error(() -> "Not an integer: " + string);
			}
		},
		String::valueOf
	);

	public static final Codec<ResourceLocation> RESOURCE_LOCATION_WITH_CREATE_DEFAULT = Codec.STRING.comapFlatMap(location -> {
		try {
			int separatorIndex = location.indexOf(":");
			if (separatorIndex >= 0) {
				String path = location.substring(separatorIndex + 1);
				if (separatorIndex != 0) {
					String namespace = location.substring(0, separatorIndex);
					return DataResult.success(ResourceLocation.fromNamespaceAndPath(namespace, path));
				} else {
					return DataResult.success(Create.asResource(path));
				}
			} else {
				return DataResult.success(Create.asResource(location));
			}
		} catch (ResourceLocationException resourcelocationexception) {
			return DataResult.error(() -> "Not a valid resource location: " + location + " " + resourcelocationexception.getMessage());
		}
	}, ResourceLocation::toString).stable();



	public static final Codec<ItemStackHandler> ITEM_STACK_HANDLER = Codec.lazyInitialized(() -> ItemSlots.CODEC.xmap(
		slots -> slots.toHandler(ItemStackHandler::new), ItemSlots::fromHandler
	));

	public static Codec<Integer> boundedIntStr(int min) {
		return INT_STR.validate(i -> i >= min ? DataResult.success(i) : DataResult.error(() -> "Value under minimum of " + min));
	}

	public static final Codec<Double> NON_NEGATIVE_DOUBLE = doubleRangeWithMessage(0, Double.MAX_VALUE,
		i -> "Value must be non-negative: " + i);
	public static final Codec<Double> POSITIVE_DOUBLE = doubleRangeWithMessage(1, Double.MAX_VALUE,
		i -> "Value must be positive: " + i);

	private static Codec<Double> doubleRangeWithMessage(double min, double max, Function<Double, String> errorMessage) {
		return Codec.DOUBLE.validate(i ->
			i.compareTo(min) >= 0 && i.compareTo(max) <= 0 ? DataResult.success(i) : DataResult.error(() ->
				errorMessage.apply(i)
			)
		);
	}

	public static Codec<SizedFluidIngredient> FLAT_SIZED_FLUID_INGREDIENT_WITH_TYPE = RecordCodecBuilder.create(instance -> instance.group(
		NeoForgeRegistries.FLUID_INGREDIENT_TYPES.byNameCodec().fieldOf("type").forGetter(i -> i.ingredient().getType()),
		FluidIngredient.MAP_CODEC_NONEMPTY.forGetter(SizedFluidIngredient::ingredient),
		NeoForgeExtraCodecs.optionalFieldAlwaysWrite(ExtraCodecs.POSITIVE_INT, "amount", 1000).forGetter(SizedFluidIngredient::amount)
	).apply(instance, (type, ingredient, amount) -> new SizedFluidIngredient(ingredient, amount)));

	@ScheduledForRemoval(inVersion = "1.21.1+ Port")
	@Deprecated(since = "6.0.7", forRemoval = true)
	public static Codec<SizedFluidIngredient> SIZED_FLUID_INGREDIENT = Codec.withAlternative(FLAT_SIZED_FLUID_INGREDIENT_WITH_TYPE, FluidIngredientOld.CODEC);

	private static <T> Codec<Holder.Reference<T>> registryReferenceHolderWithLifecycleAndCreateDefault(Registry<T> registry) {
		Codec<Holder.Reference<T>> codec = RESOURCE_LOCATION_WITH_CREATE_DEFAULT.comapFlatMap((location) -> registry.getHolder(location).map(DataResult::success).orElseGet(() -> DataResult.error(() -> {
			String keyName = String.valueOf(registry.key());
			return "Unknown registry key in " + keyName + ": " + location;
		})), (p_325513_) -> p_325513_.key().location());
		return ExtraCodecs.overrideLifecycle(codec, (p_325514_) -> registry.registrationInfo(p_325514_.key()).map(RegistrationInfo::lifecycle).orElse(Lifecycle.experimental()));
	}

	public static <T> Codec<T> byNameCodecWithCreateDefault(Registry<T> registry) {
		return registryReferenceHolderWithLifecycleAndCreateDefault(registry).flatComapMap(Holder.Reference::value, (p_325515_) -> registry.safeCastToReference(registry.wrapAsHolder(p_325515_)));
	}
}
