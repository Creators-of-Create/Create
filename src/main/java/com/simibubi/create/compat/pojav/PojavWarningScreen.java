package com.simibubi.create.compat.pojav;

import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.WarningScreen;
import net.minecraft.network.chat.Component;

/**
 * @see TitleScreen
 */
public class PojavWarningScreen extends WarningScreen {
	public static final Component TITLE = CreateLang.translateDirect("gui.pojav.title").withStyle(ChatFormatting.RED);
	public static final Component CONTENT = CreateLang.translateDirect("gui.pojav.content");
	public static final Component NARRATION = TITLE.copy().append("\n").append(CONTENT);
	public static final Component CONTINUE = CreateLang.translateDirect("gui.pojav.continue");
	public static final Component QUIT = Component.translatable("menu.quit");

	private final TitleScreen titleScreen;

	public PojavWarningScreen(TitleScreen titleScreen) {
		super(TITLE, CONTENT, null, NARRATION);
		this.titleScreen = titleScreen;
	}

	@Override
	protected Layout addFooterButtons() {
		LinearLayout layout = LinearLayout.horizontal().spacing(8);
		layout.addChild(Button.builder(CONTINUE, button -> this.minecraft.setScreen(this.titleScreen)).build());
		layout.addChild(Button.builder(QUIT, button -> this.minecraft.stop()).build());
		return layout;
	}
}
