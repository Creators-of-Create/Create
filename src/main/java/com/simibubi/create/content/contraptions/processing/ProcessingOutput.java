package com.simibubi.create.content.contraptions.processing;

import java.util.Random;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.simibubi.create.Create;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class ProcessingOutput {

	public static final ProcessingOutput EMPTY = new ProcessingOutput(ItemStack.EMPTY, 1);

	private static final Random r = new Random();
	private final ItemStack stack;
	private final float chance;

	public ProcessingOutput(ItemStack stack, float chance) {
		this.stack = stack;
		this.chance = chance;
	}

	public ItemStack getStack() {
		return stack;
	}

	public float getChance() {
		return chance;
	}

	public ItemStack rollOutput() {
		int outputAmount = stack.getCount();
		for (int roll = 0; roll < stack.getCount(); roll++)
			if (r.nextFloat() > chance)
				outputAmount--;
		return outputAmount > 0 ? new ItemStack(stack.getItem(), outputAmount) : ItemStack.EMPTY;
	}

	public JsonElement serialize() {
		JsonObject json = new JsonObject();
		json.addProperty("item", stack.getItem()
			.getRegistryName()
			.toString());
		json.addProperty("count", stack.getCount());
		if (stack.hasTag())
			json.addProperty("nbt", stack.getTag()
				.toString());
		if (chance != 1)
			json.addProperty("chance", chance);
		return json;
	}

	public static ProcessingOutput deserialize(JsonElement je) {
		if (!je.isJsonObject())
			throw new JsonSyntaxException("ProcessingOutput must be a json object");

		JsonObject json = je.getAsJsonObject();
		String itemId = JSONUtils.getString(json, "item");
		int count = JSONUtils.getInt(json, "count");
		float chance = JSONUtils.hasField(json, "chance") ? JSONUtils.getFloat(json, "chance") : 1;
		ItemStack itemstack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId)), count);

		if (JSONUtils.hasField(json, "nbt")) {
			try {
				JsonElement element = json.get("nbt");
				itemstack.setTag(JsonToNBT.getTagFromJson(
					element.isJsonObject() ? Create.GSON.toJson(element) : JSONUtils.getString(element, "nbt")));
			} catch (CommandSyntaxException e) {
				e.printStackTrace();
			}
		}

		return new ProcessingOutput(itemstack, chance);
	}

	public void write(PacketBuffer buf) {
		buf.writeItemStack(getStack());
		buf.writeFloat(getChance());
	}

	public static ProcessingOutput read(PacketBuffer buf) {
		return new ProcessingOutput(buf.readItemStack(), buf.readFloat());
	}

}
