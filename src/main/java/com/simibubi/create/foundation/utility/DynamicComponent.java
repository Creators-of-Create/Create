package com.simibubi.create.foundation.utility;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.simibubi.create.Create;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.ResolutionContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class DynamicComponent {

	private JsonElement rawCustomText;
	private Component parsedCustomText;

	public DynamicComponent() {}

	public void displayCustomText(Level level, BlockPos pos, String tagElement) {
		if (tagElement == null)
			return;

		rawCustomText = getJsonFromString(tagElement);
		parsedCustomText = parseCustomText(level, pos, rawCustomText);
	}

	public boolean sameAs(String tagElement) {
		return isValid() && rawCustomText.equals(getJsonFromString(tagElement));
	}

	public boolean isValid() {
		return parsedCustomText != null && rawCustomText != null;
	}

	public String resolve() {
		return parsedCustomText.getString();
	}

	public MutableComponent get() {
		return parsedCustomText == null ? Component.empty() : parsedCustomText.copy();
	}

	public void read(BlockPos pos, CompoundTag nbt, HolderLookup.Provider registries) {
		rawCustomText = getJsonFromString(nbt.getStringOr("RawCustomText", ""));
		parsedCustomText = LegacyComponentSerializationBridge.fromJson(nbt.getStringOr("CustomText", ""), registries);
	}

	public void write(CompoundTag nbt, HolderLookup.Provider registries) {
		if (!isValid())
			return;

		nbt.putString("RawCustomText", rawCustomText.toString());
		nbt.putString("CustomText", LegacyComponentSerializationBridge.toJson(parsedCustomText, registries));
	}

	public static JsonElement getJsonFromString(String string) {
		try {
			return JsonParser.parseString(string);
		} catch (JsonParseException e) {
			return null;
		}
	}

	public static Component parseCustomText(Level level, BlockPos pos, JsonElement customText) {
		if (!(level instanceof ServerLevel serverLevel))
			return null;
		try {
			return ComponentUtils.resolve(ResolutionContext.create(getCommandSource(serverLevel, pos)),
				LegacyComponentSerializationBridge.fromJson(customText.toString(), level.registryAccess()), 0);
		} catch (CommandSyntaxException e) {
			return null;
		}
	}

	// FIXME 1.21: checkover if it's still needed
	public static Component parseCustomText(Level level, BlockPos pos, Component customText) {
		if (!(level instanceof ServerLevel serverLevel))
			return null;
		try {
			return ComponentUtils.resolve(ResolutionContext.create(getCommandSource(serverLevel, pos)), customText, 0);
		} catch (CommandSyntaxException e) {
			return null;
		}
	}

	public static CommandSourceStack getCommandSource(ServerLevel level, BlockPos pos) {
		return new CommandSourceStack(CommandSource.NULL, Vec3.atCenterOf(pos), Vec2.ZERO, level, PermissionSet.ALL_PERMISSIONS, Create.ID,
			Component.literal(Create.ID), level.getServer(), null);
	}

}
