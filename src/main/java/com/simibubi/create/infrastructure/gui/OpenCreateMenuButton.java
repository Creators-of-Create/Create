package com.simibubi.create.infrastructure.gui;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.MutableObject;

import com.simibubi.create.AllItems;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.gui.ScreenOpener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.world.item.ItemStack;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

public class OpenCreateMenuButton extends Button {

	public OpenCreateMenuButton(int x, int y) {
		super(x, y, 20, 20, CommonComponents.EMPTY, OpenCreateMenuButton::click, DEFAULT_NARRATION);
	}

	@Override
	public void renderString(GuiGraphics graphics, Font pFont, int pColor) {
		ItemStack icon = AllItems.GOGGLES.asStack();
		BakedModel bakedmodel = Minecraft.getInstance()
			.getItemRenderer()
			.getModel(icon, Minecraft.getInstance().level, Minecraft.getInstance().player, 0);
		if (bakedmodel == null)
			return;
		
		graphics.renderItem(icon, getX() + 2, getY() + 2);
	}

	public static void click(Button b) {
		ScreenOpener.open(new CreateMainMenuScreen(Minecraft.getInstance().screen));
	}

	public record SingleMenuRow(String leftTextKey, String rightTextKey) {
		public SingleMenuRow(String centerTextKey) {
			this(centerTextKey, centerTextKey);
		}
	}

	public static class MenuRows {
		public static final MenuRows MAIN_MENU = new MenuRows(Arrays.asList(
			new SingleMenuRow("menu.singleplayer"),
			new SingleMenuRow("menu.multiplayer"),
			new SingleMenuRow("fml.menu.mods", "menu.online"),
			new SingleMenuRow("narrator.button.language", "narrator.button.accessibility")
		));

		public static final MenuRows INGAME_MENU = new MenuRows(Arrays.asList(
			new SingleMenuRow("menu.returnToGame"),
			new SingleMenuRow("gui.advancements", "gui.stats"),
			new SingleMenuRow("menu.sendFeedback", "menu.reportBugs"),
			new SingleMenuRow("menu.options", "menu.shareToLan"),
			new SingleMenuRow("menu.returnToMenu")
		));

		protected final List<String> leftTextKeys, rightTextKeys;

		public MenuRows(List<SingleMenuRow> rows) {
			leftTextKeys = rows.stream().map(SingleMenuRow::leftTextKey).collect(Collectors.toList());
			rightTextKeys = rows.stream().map(SingleMenuRow::rightTextKey).collect(Collectors.toList());
		}
	}

	@EventBusSubscriber(value = Dist.CLIENT)
	public static class OpenConfigButtonHandler {

		@SubscribeEvent
		public static void onGuiInit(ScreenEvent.Init.Post event) {
			Screen screen = event.getScreen();

			MenuRows menu;
			int rowIdx;
			int offsetX;
			if (screen instanceof TitleScreen) {
				menu = MenuRows.MAIN_MENU;
				rowIdx = AllConfigs.client().mainMenuConfigButtonRow.get();
				offsetX = AllConfigs.client().mainMenuConfigButtonOffsetX.get();
			} else if (screen instanceof PauseScreen) {
				menu = MenuRows.INGAME_MENU;
				rowIdx = AllConfigs.client().ingameMenuConfigButtonRow.get();
				offsetX = AllConfigs.client().ingameMenuConfigButtonOffsetX.get();
			} else {
				return;
			}

			if (rowIdx == 0) {
				return;
			}

			boolean onLeft = offsetX < 0;
			String targetMessage = I18n.get((onLeft ? menu.leftTextKeys : menu.rightTextKeys).get(rowIdx - 1));

			int offsetX_ = offsetX;
			MutableObject<GuiEventListener> toAdd = new MutableObject<>(null);
			event.getListenersList()
				.stream()
				.filter(w -> w instanceof AbstractWidget)
				.map(w -> (AbstractWidget) w)
				.filter(w -> w.getMessage()
					.getString()
					.equals(targetMessage))
				.findFirst()
				.ifPresent(w -> toAdd
					.setValue(new OpenCreateMenuButton(w.getX() + offsetX_ + (onLeft ? -20 : w.getWidth()), w.getY())));
			if (toAdd.getValue() != null)
				event.addListener(toAdd.getValue());
		}

	}

}
