package com.simibubi.create.foundation.config.ui;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.gui.mainMenu.CreateMainMenuScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

public class OpenCreateMenuButton extends Button {

	public static ItemStack icon = AllItems.GOGGLES.asStack();

	public OpenCreateMenuButton(int x, int y) {
		super(x, y, 20, 20, StringTextComponent.EMPTY, OpenCreateMenuButton::click);
	}

	@Override
	public void render(MatrixStack mstack, int mouseX, int mouseY, float pticks) {
		super.render(mstack, mouseX, mouseY, pticks);
		Minecraft.getInstance().getItemRenderer().renderItemIntoGUI(icon, x + 2, y + 2);
	}

	public static void click(Button b) {
		ScreenOpener.open(new CreateMainMenuScreen(Minecraft.getInstance().currentScreen));
	}

	public static class SingleMenuRow {
		public final String left, right;
		public SingleMenuRow(String left, String right) {
			this.left = I18n.format(left);
			this.right = I18n.format(right);
		}
		public SingleMenuRow(String center) {
			this(center, center);
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

		protected final List<String> leftButtons, rightButtons;

		public MenuRows(List<SingleMenuRow> variants) {
			leftButtons = variants.stream().map(r -> r.left).collect(Collectors.toList());
			rightButtons = variants.stream().map(r -> r.right).collect(Collectors.toList());
		}
	}

	@EventBusSubscriber(value = Dist.CLIENT)
	public static class OpenConfigButtonHandler {

		@SubscribeEvent
		public static void onGuiInit(GuiScreenEvent.InitGuiEvent event) {
			Screen gui = event.getGui();

			MenuRows menu = null;
			int rowIdx = 0, offsetX = 0;
			if (gui instanceof MainMenuScreen) {
				menu = MenuRows.MAIN_MENU;
				rowIdx = AllConfigs.CLIENT.mainMenuConfigButtonRow.get();
				offsetX = AllConfigs.CLIENT.mainMenuConfigButtonOffsetX.get();
			} else if (gui instanceof IngameMenuScreen) {
				menu = MenuRows.INGAME_MENU;
				rowIdx = AllConfigs.CLIENT.ingameMenuConfigButtonRow.get();
				offsetX = AllConfigs.CLIENT.ingameMenuConfigButtonOffsetX.get();
			}

			if (rowIdx != 0 && menu != null) {
				boolean onLeft = offsetX < 0;
				String target = (onLeft ? menu.leftButtons : menu.rightButtons).get(rowIdx - 1);

				int offsetX_ = offsetX;
				event.getWidgetList().stream()
					.filter(w -> w.getMessage().getString().equals(target))
					.findFirst()
					.ifPresent(w -> event.addWidget(
							new OpenCreateMenuButton(w.x + offsetX_ + (onLeft ? -20 : w.getWidth()), w.y)
					));
			}
		}

	}

}
