package com.simibubi.create.foundation.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.simibubi.create.Create;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Lang {

	public static String translate(String key, Object... args) {
		return getTranslationComponent(key, args).getFormattedText();
	}

	private static TranslationTextComponent getTranslationComponent(String key, Object... args) {
		return new TranslationTextComponent(Create.ID + "." + key, args);
	}

	public static void sendStatus(PlayerEntity player, String key, Object... args) {
		player.sendStatusMessage(getTranslationComponent(key, args), true);
	}

	// Deprecated so simi doensn't forget to remove debug calls
	@OnlyIn(value = Dist.CLIENT)
	@Deprecated
	public static void debugChat(String message) {
		if (Minecraft.getInstance().player != null)
			Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(message), false);
	}

	@OnlyIn(value = Dist.CLIENT)
	@Deprecated
	public static void debugMessage(String message) {
		if (Minecraft.getInstance().player != null)
			Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(message), true);
	}

	public static List<String> translatedOptions(String prefix, String... keys) {
		List<String> result = new ArrayList<>(keys.length);
		for (String key : keys) {
			result.add(translate(prefix + "." + key));
		}
		return result;
	}

	public static String asId(String name) {
		return name.toLowerCase(Locale.ENGLISH);
	}

}
