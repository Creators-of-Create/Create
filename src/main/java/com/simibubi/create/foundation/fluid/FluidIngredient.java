package com.simibubi.create.foundation.fluid;

import java.util.Objects;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public abstract class FluidIngredient implements Predicate<FluidStack> {

	public static final FluidIngredient EMPTY = new FluidStackIngredient();

	public static FluidIngredient fromTag(Tag<Fluid> tag, int amount) {
		FluidTagIngredient ingredient = new FluidTagIngredient();
		ingredient.tag = tag;
		ingredient.amountRequired = amount;
		return ingredient;
	}
	
	public static FluidIngredient fromFluid(Fluid fluid, int amount) {
		FluidStackIngredient ingredient = new FluidStackIngredient();
		ingredient.fluid = fluid;
		ingredient.amountRequired = amount;
		return ingredient;
	}
	
	protected int amountRequired;

	protected abstract boolean testInternal(FluidStack t);

	protected abstract void readInternal(PacketBuffer buffer);

	protected abstract void writeInternal(PacketBuffer buffer);

	protected abstract void readInternal(JsonObject json);

	protected abstract void writeInternal(JsonObject json);

	public int getRequiredAmount() {
		return amountRequired;
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

		@Override
		protected boolean testInternal(FluidStack t) {
			if (!t.getFluid()
				.isEquivalentTo(fluid))
				return false;
			CompoundNBT tag = t.getTag()
				.copy();
			return tag.merge(tagToMatch)
				.equals(t.getTag());
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
			json.addProperty("nbt", tagToMatch.toString());
		}

	}

	public static class FluidTagIngredient extends FluidIngredient {

		protected Tag<Fluid> tag;

		@Override
		protected boolean testInternal(FluidStack t) {
			return t.getFluid()
				.isIn(tag);
		}

		@Override
		protected void readInternal(PacketBuffer buffer) {
			ResourceLocation resourcelocation = buffer.readResourceLocation();
			tag = FluidTags.getContainer()
				.get(resourcelocation);
		}

		@Override
		protected void writeInternal(PacketBuffer buffer) {
			buffer.writeResourceLocation(tag.getId());
		}

		@Override
		protected void readInternal(JsonObject json) {
			ResourceLocation id = new ResourceLocation(JSONUtils.getString(json, "fluidTag"));
			tag = FluidTags.getContainer()
				.get(id);
			if (tag == null)
				throw new JsonSyntaxException("Unknown fluid tag '" + id + "'");
		}

		@Override
		protected void writeInternal(JsonObject json) {
			json.addProperty("fluidTag", tag.getId()
				.toString());
		}

	}

}
