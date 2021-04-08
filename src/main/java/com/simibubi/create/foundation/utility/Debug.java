package com.simibubi.create.foundation.utility;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.thread.EffectiveSide;

/** Deprecated so simi doensn't forget to remove debug calls **/
@OnlyIn(value = Dist.CLIENT)
public class Debug {

	@Deprecated
	public static void debugChat(ITextComponent message) {
		if (Minecraft.getInstance().player != null)
			Minecraft.getInstance().player.sendStatusMessage(message, false);
	}

	@Deprecated
	public static void debugChatAndShowStack(ITextComponent message, int depth) {
		if (Minecraft.getInstance().player != null)
			Minecraft.getInstance().player.sendStatusMessage(message.copy()
				.append("@")
				.append(debugStack(depth)), false);
	}

	@Deprecated
	public static void debugMessage(ITextComponent message) {
		if (Minecraft.getInstance().player != null)
			Minecraft.getInstance().player.sendStatusMessage(message, true);
	}

	@Deprecated
	public static String getLogicalSide() {
		return EffectiveSide.get()
			.isClient() ? "CL" : "SV";
	}

	@Deprecated
	public static ITextComponent debugStack(int depth) {
		StackTraceElement[] stackTraceElements = Thread.currentThread()
			.getStackTrace();
		IFormattableTextComponent text = new StringTextComponent("[")
			.append(new StringTextComponent(getLogicalSide()).formatted(TextFormatting.GOLD))
			.append("] ");
		for (int i = 1; i < depth + 2 && i < stackTraceElements.length; i++) {
			StackTraceElement e = stackTraceElements[i];
			if (e.getClassName()
				.equals(Debug.class.getName()))
				continue;
			text.append(new StringTextComponent(e.getMethodName()).formatted(TextFormatting.YELLOW))
				.append(", ");
		}
		return text.append(new StringTextComponent(" ...").formatted(TextFormatting.GRAY));
	}

	@Deprecated
	public static void markTemporary() {}

}
