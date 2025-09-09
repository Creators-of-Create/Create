package com.simibubi.create.foundation.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.common.base.Suppliers;
import com.google.common.primitives.UnsignedBytes;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.foundation.item.ItemSlots;
import com.simibubi.create.foundation.mixin.accessor.MobEffectInstanceAccessor;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectInstance.FactorData;
import net.minecraft.world.food.FoodProperties;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemStackHandler;

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

	public static final Codec<ItemStackHandler> ITEM_STACK_HANDLER = ExtraCodecs.lazyInitializedCodec(
		() -> ItemSlots.CODEC.xmap(slots -> slots.toHandler(ItemStackHandler::new), ItemSlots::fromHandler)
	);

	public static Codec<Integer> boundedIntStr(int min) {
		return ExtraCodecs.validate(
			INT_STR,
			i -> i >= min ? DataResult.success(i) : DataResult.error(() -> "Value under minimum of " + min)
		);
	}

	public static final Codec<Double> NON_NEGATIVE_DOUBLE = doubleRangeWithMessage(0, Double.MAX_VALUE,
		i -> "Value must be non-negative: " + i);
	public static final Codec<Double> POSITIVE_DOUBLE = doubleRangeWithMessage(1, Double.MAX_VALUE,
		i -> "Value must be positive: " + i);

	private static Codec<Double> doubleRangeWithMessage(double min, double max, Function<Double, String> errorMessage) {
		return ExtraCodecs.validate(Codec.DOUBLE, i ->
			i.compareTo(min) >= 0 && i.compareTo(max) <= 0 ? DataResult.success(i) : DataResult.error(() ->
				errorMessage.apply(i)
			)
		);
	}

	public static final Codec<Integer> UNSIGNED_BYTE = Codec.BYTE
		.flatComapMap(
			UnsignedBytes::toInt,
			p_324632_ -> p_324632_ > 255
				? DataResult.error(() -> "Unsigned byte was too large: " + p_324632_ + " > 255")
				: DataResult.success(p_324632_.byteValue())
		);

	public static final Codec<FluidStack> FLUID_STACK_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
			BuiltInRegistries.FLUID.byNameCodec().fieldOf("FluidName").forGetter(FluidStack::getFluid),
			Codec.INT.fieldOf("Amount").forGetter(FluidStack::getAmount),
			CompoundTag.CODEC.optionalFieldOf("Tag").forGetter(stack -> Optional.ofNullable(stack.getTag()))
		).apply(instance, (fluid, amount, tag) -> {
			FluidStack stack = new FluidStack(fluid, amount);
			if (!stack.isEmpty())
				tag.ifPresent(stack::setTag);
			return stack;
		})
	);

	public static final MapCodec<MobEffectInstance> MOB_EFFECT_INSTANCE = recursive(
		"MobEffectInstance",
		codec -> RecordCodecBuilder.mapCodec(
			instance -> instance.group(
					BuiltInRegistries.MOB_EFFECT.byNameCodec().fieldOf("effect").forGetter(MobEffectInstance::getEffect),
					Codec.INT.optionalFieldOf("duration", 0).forGetter(MobEffectInstance::getDuration),
					UNSIGNED_BYTE.optionalFieldOf("amplifier", 0).forGetter(MobEffectInstance::getAmplifier),
					Codec.BOOL.optionalFieldOf("ambient", false).forGetter(MobEffectInstance::isAmbient),
					Codec.BOOL.optionalFieldOf("show_particles", true).forGetter(MobEffectInstance::isVisible),
					Codec.BOOL.optionalFieldOf("show_icon", true).forGetter(MobEffectInstance::showIcon),
					codec.optionalFieldOf("hidden_effect").forGetter(i -> Optional.ofNullable(((MobEffectInstanceAccessor) i).create$getHiddenEffect())),
					FactorData.CODEC.optionalFieldOf("factor_data").forGetter(MobEffectInstance::getFactorData)
				)
				.apply(instance, (effect, duration, amplifier, isAmbient, showParticles, showIcon, hiddenEffect, factorData) ->
					new MobEffectInstance(effect, duration, amplifier, isAmbient, showParticles, showIcon, hiddenEffect.orElse(null), factorData))
		)
	);

	public static final Codec<FoodProperties> FOOD_PROPERTIES = RecordCodecBuilder.create(instance -> instance.group(
		ExtraCodecs.NON_NEGATIVE_INT.fieldOf("nutrition").forGetter(FoodProperties::getNutrition),
		Codec.FLOAT.fieldOf("saturation_modifier").forGetter(FoodProperties::getSaturationModifier),
		Codec.BOOL.optionalFieldOf("is_meat", false).forGetter(FoodProperties::isMeat),
		Codec.BOOL.optionalFieldOf("can_always_eat", false).forGetter(FoodProperties::canAlwaysEat),
		Codec.BOOL.optionalFieldOf("is_fast_food", false).forGetter(FoodProperties::isFastFood),
		FoodEffect.CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter(i -> {
			List<FoodEffect> effects = new ArrayList<>();
			for (Pair<MobEffectInstance, Float> pair : i.getEffects())
				effects.add(new FoodEffect(pair.getFirst(), pair.getSecond()));
			return effects;
		})
	).apply(instance, (nutrition, saturationModifier, isMeat, canAlwaysEat, isFastFood, effects) -> {
		FoodProperties.Builder builder = new FoodProperties.Builder()
			.nutrition(nutrition)
			.saturationMod(saturationModifier);

		if (isMeat) builder.meat();
		if (canAlwaysEat) builder.alwaysEat();
		if (isFastFood) builder.fast();
		for (FoodEffect effect : effects) builder.effect(effect.effectSupplier(), effect.probability());

		return builder.build();
	}));

	public record FoodEffect(Supplier<MobEffectInstance> effectSupplier, float probability) {
		public static final Codec<FoodEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			MOB_EFFECT_INSTANCE.fieldOf("effect").forGetter(FoodEffect::effect),
			Codec.FLOAT.fieldOf("probability").forGetter(FoodEffect::probability)
		).apply(instance, FoodEffect::new));

		private FoodEffect(MobEffectInstance effect, float probability) {
			this(() -> effect, probability);
		}

		public MobEffectInstance effect() {
			return new MobEffectInstance(this.effectSupplier.get());
		}
	}

	public static <A> MapCodec<A> recursive(final String name, final Function<Codec<A>, MapCodec<A>> wrapped) {
		return new RecursiveMapCodec<>(name, wrapped);
	}

	private static class RecursiveMapCodec<A> extends MapCodec<A> {
		private final String name;
		private final Supplier<MapCodec<A>> wrapped;

		private RecursiveMapCodec(final String name, final Function<Codec<A>, MapCodec<A>> wrapped) {
			this.name = name;
			this.wrapped = Suppliers.memoize(() -> wrapped.apply(codec()));
		}

		@Override
		public <T> RecordBuilder<T> encode(final A input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
			return wrapped.get().encode(input, ops, prefix);
		}

		@Override
		public <T> DataResult<A> decode(final DynamicOps<T> ops, final MapLike<T> input) {
			return wrapped.get().decode(ops, input);
		}

		@Override
		public <T> Stream<T> keys(final DynamicOps<T> ops) {
			return wrapped.get().keys(ops);
		}

		@Override
		public String toString() {
			return "RecursiveMapCodec[" + name + ']';
		}
	}
}
