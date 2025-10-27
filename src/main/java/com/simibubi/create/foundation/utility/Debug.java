package com.simibubi.create.foundation.utility;

import com.simibubi.create.Create;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.util.thread.EffectiveSide;

/** Deprecated so simi doensn't forget to remove debug calls **/
@OnlyIn(value = Dist.CLIENT)
public class Debug {

	@Deprecated
	public static void debugChat(String message) {
		if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(Component.literal(message), false);
        }
	}

	@Deprecated
	public static void debugChatAndShowStack(String message, int depth) {
		if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(Component.literal(message).append("@")
                .append(debugStack(depth)), false);
        }
	}

	@Deprecated
	public static void debugMessage(String message) {
		if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(Component.literal(message), true);
        }
	}

	@Deprecated
	public static void log(String message) {
		Create.LOGGER.info(message);
	}

	@Deprecated
	public static String getLogicalSide() {
		return EffectiveSide.get()
			.isClient() ? "CL" : "SV";
	}

	@Deprecated
	public static Component debugStack(int depth) {
		StackTraceElement[] stackTraceElements = Thread.currentThread()
			.getStackTrace();
		MutableComponent text = Component.literal("[")
			.append(Component.literal(getLogicalSide()).withStyle(ChatFormatting.GOLD))
			.append("] ");
		for (int i = 1; i < depth + 2 && i < stackTraceElements.length; i++) {
			StackTraceElement e = stackTraceElements[i];
			if (e.getClassName()
				.equals(Debug.class.getName()))
				continue;
			text.append(Component.literal(e.getMethodName()).withStyle(ChatFormatting.YELLOW))
				.append(", ");
		}
		return text.append(Component.literal(" ...").withStyle(ChatFormatting.GRAY));
	}

	@Deprecated
	public static void markTemporary() {}

}
