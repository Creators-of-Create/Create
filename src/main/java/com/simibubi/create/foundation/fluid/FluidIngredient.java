package com.simibubi.create.foundation.fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public abstract class FluidIngredient implements Predicate<FluidStack> {

	public static final FluidIngredient EMPTY = new FluidStackIngredient();

	public List<FluidStack> matchingFluidStacks;

	public static FluidIngredient fromTag(ITag.INamedTag<Fluid> tag, int amount) {
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

	protected abstract void readInternal(PacketBuffer buffer);

	protected abstract void writeInternal(PacketBuffer buffer);

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

	public void write(PacketBuffer buffer) {
		buffer.writeBoolean(this instanceof FluidTagIngredient);
		buffer.writeVarInt(amountRequired);
		writeInternal(buffer);
	}

	public static FluidIngredient read(PacketBuffer buffer) {
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
		ingredient.amountRequired = JSONUtils.getInt(json, "amount");
		return ingredient;
	}

	public static class FluidStackIngredient extends FluidIngredient {

		protected Fluid fluid;
		protected CompoundNBT tagToMatch;

		public FluidStackIngredient() {
			tagToMatch = new CompoundNBT();
		}

		void fixFlowing() {
			if (fluid instanceof FlowingFluid)
				fluid = ((FlowingFluid) fluid).getStillFluid();
		}

		@Override
		protected boolean testInternal(FluidStack t) {
			if (!t.getFluid()
				.isEquivalentTo(fluid))
				return false;
			if (tagToMatch.isEmpty())
				return true;
			CompoundNBT tag = t.getOrCreateTag();
			return tag.copy()
				.merge(tagToMatch)
				.equals(tag);
		}

		@Override
		protected void readInternal(PacketBuffer buffer) {
			fluid = buffer.readRegistryId();
			tagToMatch = buffer.readCompoundTag();
		}

		@Override
		protected void writeInternal(PacketBuffer buffer) {
			buffer.writeRegistryId(fluid);
			buffer.writeCompoundTag(tagToMatch);
		}

		@Override
		protected void readInternal(JsonObject json) {
			FluidStack stack = FluidHelper.deserializeFluidStack(json);
			fluid = stack.getFluid();
			tagToMatch = stack.getOrCreateTag();
		}

		@Override
		protected void writeInternal(JsonObject json) {
			json.addProperty("fluid", fluid.getRegistryName()
				.toString());
			json.add("nbt", new JsonParser().parse(tagToMatch.toString()));
		}

		@Override
		protected List<FluidStack> determineMatchingFluidStacks() {
			return ImmutableList.of(tagToMatch.isEmpty() ? new FluidStack(fluid, amountRequired)
				: new FluidStack(fluid, amountRequired, tagToMatch));
		}

	}

	public static class FluidTagIngredient extends FluidIngredient {

		protected ITag.INamedTag<Fluid> tag;

		@Override
		protected boolean testInternal(FluidStack t) {
			if (tag == null)
				for (FluidStack accepted : getMatchingFluidStacks())
					if (accepted.getFluid()
						.isEquivalentTo(t.getFluid()))
						return true;
			return t.getFluid()
				.isIn(tag);
		}

		@Override
		protected void readInternal(PacketBuffer buffer) {
			int size = buffer.readVarInt();
			matchingFluidStacks = new ArrayList<>(size);
			for (int i = 0; i < size; i++)
				matchingFluidStacks.add(buffer.readFluidStack());
		}

		@Override
		protected void writeInternal(PacketBuffer buffer) {
			// Tag has to be resolved on the server before sending
			List<FluidStack> matchingFluidStacks = getMatchingFluidStacks();
			buffer.writeVarInt(matchingFluidStacks.size());
			matchingFluidStacks.stream()
				.forEach(buffer::writeFluidStack);
		}

		@Override
		protected void readInternal(JsonObject json) {
			ResourceLocation id = new ResourceLocation(JSONUtils.getString(json, "fluidTag"));
			Optional<? extends ITag.INamedTag<Fluid>> optionalINamedTag = FluidTags.getRequiredTags()
				.stream()
				.filter(fluidINamedTag -> fluidINamedTag.getId()
					.equals(id))
				.findFirst(); // fixme
			if (!optionalINamedTag.isPresent())
				throw new JsonSyntaxException("Unknown fluid tag '" + id + "'");
			tag = optionalINamedTag.get();
		}

		@Override
		protected void writeInternal(JsonObject json) {
			json.addProperty("fluidTag", tag.getId()
				.toString());
		}

		@Override
		protected List<FluidStack> determineMatchingFluidStacks() {
			return tag.values()
				.stream()
				.map(f -> {
					if (f instanceof FlowingFluid)
						return ((FlowingFluid) f).getStillFluid();
					return f;
				})
				.distinct()
				.map(f -> new FluidStack(f, amountRequired))
				.collect(Collectors.toList());
		}

	}

}
