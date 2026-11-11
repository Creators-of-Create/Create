package com.simibubi.create.content.equipment.clipboard.ui;

import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public class ClipboardLayout {
	
	public static final int PAGE_TEXT_WIDTH = 150;
	public static final int PAGE_TEXT_HEIGHT = 185;
	public static final int PAGE_LIMIT = 50; // actually +1

	public static final int LINE_HEIGHT = 9;
	public static final int ENTRY_PADDING = 3;
	
	public static final int ICON_OFFSET_WIDTH = 16;
	public static final int ASSUMED_CHARACTER_WIDTH = 8;

	public static Font getClipboardFont() {
		return Minecraft.getInstance().font;
	}

	public static int getIconOffset(ItemStack icon) {
		return icon.isEmpty() ? 0 : ICON_OFFSET_WIDTH;
	}

	protected static int calculateHeight(MutableComponent text, int maxWidth, boolean clientSide) {
		if (clientSide) {
			return Math.max(LINE_HEIGHT + ENTRY_PADDING, getClipboardFont().split(text, maxWidth).size() * LINE_HEIGHT + ENTRY_PADDING);
		}
		int lineCharLimit = Math.max(1, maxWidth / ASSUMED_CHARACTER_WIDTH);
		int lines = 1 + (text.getString().length() - 1) / lineCharLimit;
		return lines * LINE_HEIGHT + ENTRY_PADDING;
	}

	public static int getHeightUniversal(MutableComponent text, boolean clientSide) {
		return calculateHeight(text, PAGE_TEXT_WIDTH, clientSide);
	}

	public static int getHeight(ClipboardEntry entry, boolean clientSide) {
		int maxWidth = entry.icon.isEmpty() ? PAGE_TEXT_WIDTH : PAGE_TEXT_WIDTH - getIconOffset(entry.icon);
		int height = calculateHeight(entry.text, maxWidth, clientSide);
		if (entry.itemAmount != 0)
			height += LINE_HEIGHT;
		return height;
	}
}
