package com.simibubi.create.foundation.fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.simibubi.create.foundation.utility.RegisteredObjects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public abstract class FluidIngredient implements Predicate<FluidStack> {

	public static final FluidIngredient EMPTY = new FluidStackIngredient();

	public List<FluidStack> matchingFluidStacks;

	public static FluidIngredient fromTag(TagKey<Fluid> tag, int amount) {
		FluidTagIngredient ingredient = new FluidTagIngredient();
		ingredient.tag = tag;
		ingredient.amountRequired = amount;
		return ingredient;
	}

	public static FluidIngredient fromFluid(Fluid fluid, int amount) {
		FluidStackIngredient ingredient = new FluidStackIngredient();
		ingredient.fluid = fluid;
		ingredient.amountRequired = amount;
		ingredient.fixFlowing();
		return ingredient;
	}

	public static FluidIngredient fromFluidStack(FluidStack fluidStack) {
		FluidStackIngredient ingredient = new FluidStackIngredient();
		ingredient.fluid = fluidStack.getFluid();
		ingredient.amountRequired = fluidStack.getAmount();
		ingredient.fixFlowing();
		if (fluidStack.hasTag())
			ingredient.tagToMatch = fluidStack.getTag();
		return ingredient;
	}

	protected int amountRequired;

	protected abstract boolean testInternal(FluidStack t);

	protected abstract void readInternal(FriendlyByteBuf buffer);

	protected abstract void writeInternal(FriendlyByteBuf buffer);

	protected abstract void readInternal(JsonObject json);

	protected abstract void writeInternal(JsonObject json);

	protected abstract List<FluidStack> determineMatchingFluidStacks();

	public int getRequiredAmount() {
		return amountRequired;
	}

	public List<FluidStack> getMatchingFluidStacks() {
		if (matchingFluidStacks != null)
			return matchingFluidStacks;
		return matchingFluidStacks = determineMatchingFluidStacks();
	}

	@Override
	public boolean test(FluidStack t) {
		if (t == null)
			throw new IllegalArgumentException("FluidStack cannot be null");
		return testInternal(t);
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeBoolean(this instanceof FluidTagIngredient);
		buffer.writeVarInt(amountRequired);
		writeInternal(buffer);
	}

	public static FluidIngredient read(FriendlyByteBuf buffer) {
		boolean isTagIngredient = buffer.readBoolean();
		FluidIngredient ingredient = isTagIngredient ? new FluidTagIngredient() : new FluidStackIngredient();
		ingredient.amountRequired = buffer.readVarInt();
		ingredient.readInternal(buffer);
		return ingredient;
	}

	public JsonObject serialize() {
		JsonObject json = new JsonObject();
		writeInternal(json);
		json.addProperty("amount", amountRequired);
		return json;
	}

	public static boolean isFluidIngredient(@Nullable JsonElement je) {
		if (je == null || je.isJsonNull())
			return false;
		if (!je.isJsonObject())
			return false;
		JsonObject json = je.getAsJsonObject();
		if (json.has("fluidTag"))
			return true;
		else if (json.has("fluid"))
			return true;
		return false;
	}

	public static FluidIngredient deserialize(@Nullable JsonElement je) {
		if (!isFluidIngredient(je))
			throw new JsonSyntaxException("Invalid fluid ingredient: " + Objects.toString(je));

		JsonObject json = je.getAsJsonObject();
		FluidIngredient ingredient = json.has("fluidTag") ? new FluidTagIngredient() : new FluidStackIngredient();
		ingredient.readInternal(json);

		if (!json.has("amount"))
			throw new JsonSyntaxException("Fluid ingredient has to define an amount");
		ingredient.amountRequired = GsonHelper.getAsInt(json, "amount");
		return ingredient;
	}

	public static class FluidStackIngredient extends FluidIngredient {

		protected Fluid fluid;
		protected CompoundTag tagToMatch;

		public FluidStackIngredient() {
			tagToMatch = new CompoundTag();
		}

		void fixFlowing() {
			if (fluid instanceof FlowingFluid)
				fluid = ((FlowingFluid) fluid).getSource();
		}

		@Override
		protected boolean testInternal(FluidStack t) {
			if (!FluidHelper.isSame(t, fluid))
				return false;
			if (tagToMatch.isEmpty())
				return true;
			CompoundTag tag = t.getOrCreateTag();
			return tag.copy()
				.merge(tagToMatch)
				.equals(tag);
		}

		@Override
		protected void readInternal(FriendlyByteBuf buffer) {
			fluid = buffer.readRegistryId();
			tagToMatch = buffer.readNbt();
		}

		@Override
		protected void writeInternal(FriendlyByteBuf buffer) {
			buffer.writeRegistryId(fluid);
			buffer.writeNbt(tagToMatch);
		}

		@Override
		protected void readInternal(JsonObject json) {
			FluidStack stack = FluidHelper.deserializeFluidStack(json);
			fluid = stack.getFluid();
			tagToMatch = stack.getOrCreateTag();
		}

		@Override
		protected void writeInternal(JsonObject json) {
			json.addProperty("fluid", RegisteredObjects.getKeyOrThrow(fluid)
				.toString());
			json.add("nbt", JsonParser.parseString(tagToMatch.toString()));
		}

		@Override
		protected List<FluidStack> determineMatchingFluidStacks() {
			return ImmutableList.of(tagToMatch.isEmpty() ? new FluidStack(fluid, amountRequired)
				: new FluidStack(fluid, amountRequired, tagToMatch));
		}

	}

	public static class FluidTagIngredient extends FluidIngredient {

		protected TagKey<Fluid> tag;

		@Override
		protected boolean testInternal(FluidStack t) {
			if (tag != null)
				return FluidHelper.isTag(t, tag);
			for (FluidStack accepted : getMatchingFluidStacks())
				if (FluidHelper.isSame(accepted, t))
					return true;
			return false;
		}

		@Override
		protected void readInternal(FriendlyByteBuf buffer) {
			int size = buffer.readVarInt();
			matchingFluidStacks = new ArrayList<>(size);
			for (int i = 0; i < size; i++)
				matchingFluidStacks.add(buffer.readFluidStack());
		}

		@Override
		protected void writeInternal(FriendlyByteBuf buffer) {
			// Tag has to be resolved on the server before sending
			List<FluidStack> matchingFluidStacks = getMatchingFluidStacks();
			buffer.writeVarInt(matchingFluidStacks.size());
			matchingFluidStacks.stream()
				.forEach(buffer::writeFluidStack);
		}

		@Override
		protected void readInternal(JsonObject json) {
			ResourceLocation name = new ResourceLocation(GsonHelper.getAsString(json, "fluidTag"));
			tag = FluidTags.create(name);
		}

		@Override
		protected void writeInternal(JsonObject json) {
			json.addProperty("fluidTag", tag.location()
				.toString());
		}

		@Override
		protected List<FluidStack> determineMatchingFluidStacks() {
			return ForgeRegistries.FLUIDS.tags()
				.getTag(tag)
				.stream()
				.map(f -> {
					if (f instanceof FlowingFluid)
						return ((FlowingFluid) f).getSource();
					return f;
				})
				.distinct()
				.map(f -> new FluidStack(f, amountRequired))
				.collect(Collectors.toList());
		}

	}

}
