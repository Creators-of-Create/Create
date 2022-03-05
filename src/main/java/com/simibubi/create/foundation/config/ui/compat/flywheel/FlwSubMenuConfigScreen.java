package com.simibubi.create.foundation.config.ui.compat.flywheel;

import java.util.Collections;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.config.Option;
import com.simibubi.create.foundation.config.ui.ConfigHelper;
import com.simibubi.create.foundation.config.ui.ConfigScreenList;
import com.simibubi.create.foundation.config.ui.ConfigTextField;
import com.simibubi.create.foundation.config.ui.SubMenuConfigScreen;
import com.simibubi.create.foundation.config.ui.entries.SubMenuEntry;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.ConfirmationScreen;
import com.simibubi.create.foundation.gui.Theme;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.gui.element.DelegatedStencilElement;
import com.simibubi.create.foundation.gui.widget.BoxWidget;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Couple;
import io.github.fabricators_of_create.porting_lib.mixin.client.accessor.AbstractSelectionListAccessor;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.fml.config.ModConfig;

public class FlwSubMenuConfigScreen extends SubMenuConfigScreen {

	private final FlwConfig flwConfig;

	public FlwSubMenuConfigScreen(Screen parent, String title, ModConfig.Type type, FlwConfig flwConfig) {
		super(parent, title, type, null, null);
		this.flwConfig = flwConfig;
	}

	public FlwSubMenuConfigScreen(Screen parent, ModConfig.Type type, FlwConfig flwConfig) {
		this(parent, "root", type, flwConfig);
	}

	@Override
	protected void saveChanges() {
		flwConfig.save();
	}

	@Override
	protected void resetConfig(UnmodifiableConfig values) {

	}

	@Override
	protected void init() {
		guiLeft = (width - windowWidth) / 2;
		guiTop = (height - windowHeight) / 2;
		guiLeft += windowXOffset;
		guiTop += windowYOffset;

		listWidth = Math.min(width - 80, 300);

		int yCenter = height / 2;
		int listL = this.width / 2 - listWidth / 2;
		int listR = this.width / 2 + listWidth / 2;

		resetAll = new BoxWidget(listR + 10, yCenter - 25, 20, 20)
				.withPadding(2, 2)
				.withCallback((x, y) ->
						new ConfirmationScreen()
								.centered()
								.withText(FormattedText.of("Resetting this config is not supported!"))
//								.withAction(success -> {
//									if (success)
//										resetConfig(spec.getValues());
//								})
								.open(this)
				);

		resetAll.showingElement(AllIcons.I_CONFIG_RESET.asStencil().withElementRenderer(BoxWidget.gradientFactory.apply(resetAll)));
		resetAll.getToolTip().add(new TextComponent("Reset All"));
		resetAll.getToolTip().addAll(TooltipHelper.cutStringTextComponent("Click here to reset all settings to their default value.", ChatFormatting.GRAY, ChatFormatting.GRAY));

		saveChanges = new BoxWidget(listL - 30, yCenter - 25, 20, 20)
				.withPadding(2, 2)
				.withCallback((x, y) -> {
					if (ConfigHelper.changes.isEmpty())
						return;

					ConfirmationScreen confirm = new ConfirmationScreen()
							.centered()
							.withText(FormattedText.of("Saving " + ConfigHelper.changes.size() + " changed value" + (ConfigHelper.changes.size() != 1 ? "s" : "") + ""))
							.withAction(success -> {
								if (success)
									saveChanges();
							});

					addAnnotationsToConfirm(confirm).open(this);
				});
		saveChanges.showingElement(AllIcons.I_CONFIG_SAVE.asStencil().withElementRenderer(BoxWidget.gradientFactory.apply(saveChanges)));
		saveChanges.getToolTip().add(new TextComponent("Save Changes"));
		saveChanges.getToolTip().addAll(TooltipHelper.cutStringTextComponent("Click here to save your current changes.", ChatFormatting.GRAY, ChatFormatting.GRAY));

		discardChanges = new BoxWidget(listL - 30, yCenter + 5, 20, 20)
				.withPadding(2, 2)
				.withCallback((x, y) -> {
					if (ConfigHelper.changes.isEmpty())
						return;

					new ConfirmationScreen()
							.centered()
							.withText(FormattedText.of("Discarding " + ConfigHelper.changes.size() + " unsaved change" + (ConfigHelper.changes.size() != 1 ? "s" : "") + ""))
							.withAction(success -> {
								if (success)
									clearChanges();
							})
							.open(this);
				});
		discardChanges.showingElement(AllIcons.I_CONFIG_DISCARD.asStencil().withElementRenderer(BoxWidget.gradientFactory.apply(discardChanges)));
		discardChanges.getToolTip().add(new TextComponent("Discard Changes"));
		discardChanges.getToolTip().addAll(TooltipHelper.cutStringTextComponent("Click here to discard all the changes you made.", ChatFormatting.GRAY, ChatFormatting.GRAY));

		goBack = new BoxWidget(listL - 30, yCenter + 65, 20, 20)
				.withPadding(2, 2)
				.withCallback(this::attemptBackstep);
		goBack.showingElement(AllIcons.I_CONFIG_BACK.asStencil().withElementRenderer(BoxWidget.gradientFactory.apply(goBack)));
		goBack.getToolTip().add(new TextComponent("Go Back"));

		addRenderableWidget(resetAll);
		addRenderableWidget(saveChanges);
		addRenderableWidget(discardChanges);
		addRenderableWidget(goBack);

		list = new ConfigScreenList(minecraft, listWidth, height - 80, 35, height - 45, 40);
		list.setLeftPos(this.width / 2 - ((AbstractSelectionListAccessor) list).create$getWidth() / 2);

		addRenderableWidget(list);

		search = new ConfigTextField(font, width / 2 - listWidth / 2, height - 35, listWidth, 20);
		search.setResponder(this::updateFilter);
		search.setHint("Search..");
		search.moveCursorToStart();
		addRenderableWidget(search);

		flwConfig.getOptionMapView().forEach((key, option) -> {
			String humanKey = toHumanReadable(key);

			Object value = option.get();
			ConfigScreenList.Entry entry = null;
			if (value instanceof Boolean) {
				entry = new FlwBooleanEntry(humanKey, (Option<Boolean>) option);
			} else if (value instanceof Enum) {
				entry = new FlwEnumEntry(humanKey, (Option<Enum<?>>) option);
			}

			if (entry == null)
				entry = new ConfigScreenList.LabeledEntry("Impl missing - " + option.get().getClass().getSimpleName() + "  " + humanKey + " : " + value);

			if (highlights.contains(key))
				entry.annotations.put("highlight", ":)");

			list.children().add(entry);
		});

		Collections.sort(list.children(),
				(e, e2) -> {
					int group = (e2 instanceof SubMenuEntry ? 1 : 0) - (e instanceof SubMenuEntry ? 1 : 0);
					if (group == 0 && e instanceof ConfigScreenList.LabeledEntry && e2 instanceof ConfigScreenList.LabeledEntry) {
						ConfigScreenList.LabeledEntry le = (ConfigScreenList.LabeledEntry) e;
						ConfigScreenList.LabeledEntry le2 = (ConfigScreenList.LabeledEntry) e2;
						return le.label.getComponent()
								.getString()
								.compareTo(le2.label.getComponent()
										.getString());
					}
					return group;
				});

		list.search(highlights.stream().findFirst().orElse(""));

		//extras for server configs
		if (type != ModConfig.Type.SERVER)
			return;
		if (minecraft.hasSingleplayerServer())
			return;

		boolean canEdit = minecraft != null && minecraft.player != null && minecraft.player.hasPermissions(2);

		Couple<Color> red = Theme.p(Theme.Key.BUTTON_FAIL);
		Couple<Color> green = Theme.p(Theme.Key.BUTTON_SUCCESS);

		DelegatedStencilElement stencil = new DelegatedStencilElement();

		serverLocked = new BoxWidget(listR + 10, yCenter + 5, 20, 20)
				.withPadding(2, 2)
				.showingElement(stencil);


		if (!canEdit) {
			list.children().forEach(e -> e.setEditable(false));
			resetAll.active = false;
			stencil.withStencilRenderer((ms, w, h, alpha) -> AllIcons.I_CONFIG_LOCKED.render(ms, 0, 0));
			stencil.withElementRenderer((ms, w, h, alpha) -> UIRenderHelper.angledGradient(ms, 90, 8, 0, 16, 16, red));
			serverLocked.withBorderColors(red);
			serverLocked.getToolTip().add(new TextComponent("Locked").withStyle(ChatFormatting.BOLD));
			serverLocked.getToolTip().addAll(TooltipHelper.cutStringTextComponent("You do not have enough permissions to edit the server config. You can still look at the current values here though.", ChatFormatting.GRAY, ChatFormatting.GRAY));
		} else {
			stencil.withStencilRenderer((ms, w, h, alpha) -> AllIcons.I_CONFIG_UNLOCKED.render(ms, 0, 0));
			stencil.withElementRenderer((ms, w, h, alpha) -> UIRenderHelper.angledGradient(ms, 90, 8, 0, 16, 16, green));
			serverLocked.withBorderColors(green);
			serverLocked.getToolTip().add(new TextComponent("Unlocked").withStyle(ChatFormatting.BOLD));
			serverLocked.getToolTip().addAll(TooltipHelper.cutStringTextComponent("You have enough permissions to edit the server config. Changes you make here will be synced with the server when you save them.", ChatFormatting.GRAY, ChatFormatting.GRAY));
		}

		addRenderableWidget(serverLocked);
	}
}
