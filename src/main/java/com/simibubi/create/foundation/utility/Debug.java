package com.simibubi.create.foundation.utility;

import com.simibubi.create.Create;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/** Deprecated so simi doensn't forget to remove debug calls **/
@Environment(value = EnvType.CLIENT)
public class Debug {

	@Deprecated
	public static void debugChat(String message) {
		if (Minecraft.getInstance().player != null)
			Minecraft.getInstance().player.displayClientMessage(new TextComponent(message), false);
	}

	@Deprecated
	public static void debugChatAndShowStack(String message, int depth) {
		if (Minecraft.getInstance().player != null)
			Minecraft.getInstance().player.displayClientMessage(new TextComponent(message).append("@")
				.append(debugStack(depth)), false);
	}

	@Deprecated
	public static void debugMessage(String message) {
		if (Minecraft.getInstance().player != null)
			Minecraft.getInstance().player.displayClientMessage(new TextComponent(message), true);
	}

	@Deprecated
	public static void log(String message) {
		Create.LOGGER.info(message);
	}

	@Deprecated
	public static String getLogicalSide() {
		return Minecraft.getInstance().level // only called on client, this is safe (but completely redundant)
			.isClientSide() ? "CL" : "SV";
	}

	@Deprecated
	public static Component debugStack(int depth) {
		StackTraceElement[] stackTraceElements = Thread.currentThread()
			.getStackTrace();
		MutableComponent text = new TextComponent("[")
			.append(new TextComponent(getLogicalSide()).withStyle(ChatFormatting.GOLD))
			.append("] ");
		for (int i = 1; i < depth + 2 && i < stackTraceElements.length; i++) {
			StackTraceElement e = stackTraceElements[i];
			if (e.getClassName()
				.equals(Debug.class.getName()))
				continue;
			text.append(new TextComponent(e.getMethodName()).withStyle(ChatFormatting.YELLOW))
				.append(", ");
		}
		return text.append(new TextComponent(" ...").withStyle(ChatFormatting.GRAY));
	}

	@Deprecated
	public static void markTemporary() {}

}
