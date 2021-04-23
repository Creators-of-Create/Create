package com.simibubi.create.foundation.config.ui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.electronwill.nightconfig.core.AbstractConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.config.ui.entries.BooleanEntry;
import com.simibubi.create.foundation.config.ui.entries.EnumEntry;
import com.simibubi.create.foundation.config.ui.entries.NumberEntry;
import com.simibubi.create.foundation.config.ui.entries.SubMenuEntry;
import com.simibubi.create.foundation.config.ui.entries.ValueEntry;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.ConfirmationScreen;
import com.simibubi.create.foundation.gui.DelegatedStencilElement;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.gui.TextStencilElement;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.ponder.ui.PonderButton;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public class SubMenuConfigScreen extends ConfigScreen {

	public final ModConfig.Type type;
	protected ForgeConfigSpec spec;
	protected UnmodifiableConfig configGroup;
	protected ConfigScreenList list;

	protected PonderButton resetAll;
	protected PonderButton saveChanges;
	protected PonderButton discardChanges;
	protected PonderButton goBack;
	protected PonderButton serverLocked;
	protected int listWidth;
	protected String title;


	public SubMenuConfigScreen(Screen parent, String title, ModConfig.Type type, ForgeConfigSpec configSpec, UnmodifiableConfig configGroup) {
		super(parent);
		this.type = type;
		this.spec = configSpec;
		this.title = title;
		this.configGroup = configGroup;
	}

	public SubMenuConfigScreen(Screen parent, ModConfig.Type type, ForgeConfigSpec configSpec) {
		super(parent);
		this.type = type;
		this.spec = configSpec;
		this.title = "root";
		this.configGroup = configSpec.getValues();
	}

	protected void clearChanges() {
		changes.clear();
		list.children()
				.stream()
				.filter(e -> e instanceof ValueEntry)
				.forEach(e -> ((ValueEntry<?>) e).onValueChange());
	}

	protected void saveChanges() {
		UnmodifiableConfig values = spec.getValues();
		changes.forEach((path, value) -> {
			ForgeConfigSpec.ConfigValue configValue = values.get(path);
			configValue.set(value);
		});
		clearChanges();
	}

	protected void resetConfig(UnmodifiableConfig values) {
		values.valueMap().forEach((key, obj) -> {
			if (obj instanceof AbstractConfig) {
				resetConfig((UnmodifiableConfig) obj);
			} else if (obj instanceof ForgeConfigSpec.ConfigValue<?>) {
				ForgeConfigSpec.ConfigValue<?> configValue = (ForgeConfigSpec.ConfigValue<?>) obj;
				ForgeConfigSpec.ValueSpec valueSpec = spec.getRaw(configValue.getPath());

				if (!configValue.get().equals(valueSpec.getDefault()))
					changes.put(String.join(".", configValue.getPath()), valueSpec.getDefault());
			}
		});

		list.children()
				.stream()
				.filter(e -> e instanceof ValueEntry)
				.forEach(e -> ((ValueEntry<?>) e).onValueChange());
	}

	@Override
	public void tick() {
		super.tick();
		list.tick();
	}

	@Override
	protected void init() {
		widgets.clear();
		super.init();

		//leave 40px on either side and dont be wider than 500px
		listWidth = Math.min(width - 80, 500);

		int yCenter = height / 2;
		int listL = this.width / 2 - listWidth / 2;
		int listR = this.width / 2 + listWidth / 2;

		resetAll = new PonderButton(listR + 10, yCenter - 25, (x, y) -> {
			new ConfirmationScreen()
					.at(x, y)
					.withText(ITextProperties.plain("You are about to reset all settings for the " + type.toString() + " config. Are you sure?"))
					.withAction(success -> {
						if (success)
							resetConfig(spec.getValues());
					})
					.open(this);
		})
				.showing(AllIcons.I_CONFIG_RESET.asStencil());
		resetAll.fade(1);
		resetAll.getToolTip().add(new StringTextComponent("Reset All"));
		resetAll.getToolTip().addAll(TooltipHelper.cutStringTextComponent("Click here to reset all configs to their default value.", TextFormatting.GRAY, TextFormatting.GRAY));

		saveChanges = new PonderButton(listL - 30, yCenter - 25, (x, y) -> {
			if (changes.isEmpty())
				return;

			new ConfirmationScreen()
					.at(x, y)
					.withText(ITextProperties.plain("You are about to change " + changes.size() + " values. Are you sure?"))
					.withAction(success -> {
						if (success)
							saveChanges();
					})
					.open(this);
		})
				.showing(AllIcons.I_CONFIG_SAVE.asStencil());
		saveChanges.fade(1);
		saveChanges.getToolTip().add(new StringTextComponent("Save Changes"));
		saveChanges.getToolTip().addAll(TooltipHelper.cutStringTextComponent("Click here to save your current changes.", TextFormatting.GRAY, TextFormatting.GRAY));

		discardChanges = new PonderButton(listL - 30, yCenter + 5, (x, y) -> {
			if (changes.isEmpty())
				return;

			new ConfirmationScreen()
					.at(x, y)
					.withText(ITextProperties.plain("You are about to discard " + changes.size() + " unsaved changes. Are you sure?"))
					.withAction(success -> {
						if (success)
							clearChanges();
					})
					.open(this);
		})
				.showing(AllIcons.I_CONFIG_DISCARD.asStencil());
		discardChanges.fade(1);
		discardChanges.getToolTip().add(new StringTextComponent("Discard Changes"));
		discardChanges.getToolTip().addAll(TooltipHelper.cutStringTextComponent("Click here to discard all the changes you made.", TextFormatting.GRAY, TextFormatting.GRAY));

		goBack = new PonderButton(listL - 30, yCenter + 65, this::attemptBackstep)
				.showing(AllIcons.I_CONFIG_BACK);
		goBack.fade(1);
		goBack.getToolTip().add(new StringTextComponent("Go Back"));

		widgets.add(resetAll);
		widgets.add(saveChanges);
		widgets.add(discardChanges);
		widgets.add(goBack);

		list = new ConfigScreenList(client, listWidth, height - 60, 45, height - 15, 50);
		list.setLeftPos(this.width / 2 - list.getWidth() / 2);

		children.add(list);

		configGroup.valueMap().forEach((key, obj) -> {
			String humanKey = toHumanReadable(key);

			if (obj instanceof AbstractConfig) {
				SubMenuEntry entry = new SubMenuEntry(this, humanKey, spec, (UnmodifiableConfig) obj);
				list.children().add(entry);

			} else if (obj instanceof ForgeConfigSpec.ConfigValue<?>) {
				ForgeConfigSpec.ConfigValue<?> configValue = (ForgeConfigSpec.ConfigValue<?>) obj;
				ForgeConfigSpec.ValueSpec valueSpec = spec.getRaw(configValue.getPath());
				Object value = configValue.get();

				if (value instanceof Boolean) {
					BooleanEntry entry = new BooleanEntry(humanKey, (ForgeConfigSpec.ConfigValue<Boolean>) configValue, valueSpec);
					list.children().add(entry);
				} else if (value instanceof Enum) {
					EnumEntry entry = new EnumEntry(humanKey, (ForgeConfigSpec.ConfigValue<Enum<?>>) configValue, valueSpec);
					list.children().add(entry);
				} else if (value instanceof Number) {
					NumberEntry<? extends Number> entry = NumberEntry.create(value, humanKey, configValue, valueSpec);
					if (entry != null) {
						list.children().add(entry);
					} else {
						list.children().add(new ConfigScreenList.LabeledEntry("n-" + obj.getClass().getSimpleName() + "  " + humanKey + " : " + value));
					}
				} else {
					list.children().add(new ConfigScreenList.LabeledEntry(humanKey + " : " + value));
				}
			}
		});

		//extras for sever configs
		if (type != ModConfig.Type.SERVER)
			return;

		list.isForServer = true;
		boolean canEdit = client != null && client.player != null && client.player.hasPermissionLevel(2);

		int colRed1 = 0xff_f78888;
		int colRed2 = 0xff_cc2020;
		int colGreen1 = 0xff_88f788;
		int colGreen2 = 0xff_20cc20;

		DelegatedStencilElement stencil = new DelegatedStencilElement();

		serverLocked = new PonderButton(listR + 10, yCenter + 5, () -> {})
				.showing(stencil);
		serverLocked.fade(1);

		if (!canEdit) {
			list.children().forEach(e -> e.setEditable(false));
			resetAll.active = false;
			stencil.withStencilRenderer((ms, w, h) -> AllIcons.I_CONFIG_LOCKED.draw(ms, 0, 0));
			stencil.withElementRenderer((ms, w, h) -> UIRenderHelper.angledGradient(ms, 90, 8, 0, 16, 16, colRed1, colRed2));
			serverLocked.customColors(colRed1, colRed2);
			serverLocked.getToolTip().add(new StringTextComponent("Locked").formatted(TextFormatting.BOLD));
			serverLocked.getToolTip().addAll(TooltipHelper.cutStringTextComponent("You don't have enough permissions to edit the server config. You can still look at the current values here though.", TextFormatting.GRAY, TextFormatting.GRAY));
		} else {
			stencil.withStencilRenderer((ms, w, h) -> AllIcons.I_CONFIG_UNLOCKED.draw(ms, 0, 0));
			stencil.withElementRenderer((ms, w, h) -> UIRenderHelper.angledGradient(ms, 90, 8, 0, 16, 16, colGreen1, colGreen2));
			serverLocked.customColors(colGreen1, colGreen2);
			serverLocked.getToolTip().add(new StringTextComponent("Unlocked").formatted(TextFormatting.BOLD));
			serverLocked.getToolTip().addAll(TooltipHelper.cutStringTextComponent("You have enough permissions to edit the server config. Changes you make here will be synced with the server once you saved them.", TextFormatting.GRAY, TextFormatting.GRAY));
		}

		widgets.add(serverLocked);
	}

	@Override
	protected void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		super.renderWindow(ms, mouseX, mouseY, partialTicks);

		int x = width/2;
		drawCenteredString(ms, client.fontRenderer, "Editing config: " + type.toString() + "@" + title, x, 15, 0xff_eaeaea);

		list.render(ms, mouseX, mouseY, partialTicks);
	}

	@Override
	protected void renderWindowForeground(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		super.renderWindowForeground(ms, mouseX, mouseY, partialTicks);
	}

	@Override
	public void resize(@Nonnull Minecraft client, int width, int height) {
		double scroll = list.getScrollAmount();
		init(client, width, height);
		list.setScrollAmount(scroll);
	}

	@Nullable
	@Override
	public IGuiEventListener getFocused() {
		if (ConfigScreenList.currentText != null)
			return ConfigScreenList.currentText;

		return super.getFocused();
	}

	@Override
	public boolean keyPressed(int code, int p_keyPressed_2_, int p_keyPressed_3_) {
		if (super.keyPressed(code, p_keyPressed_2_, p_keyPressed_3_))
			return true;

		if (code == GLFW.GLFW_KEY_BACKSPACE) {
			attemptBackstep();
		}

		return false;
	}

	private void attemptBackstep() {
		if (!changes.isEmpty() && parent instanceof BaseConfigScreen) {
			new ConfirmationScreen()
					.centered()
					.addText(ITextProperties.plain("You still have " + changes.size() + " unsaved changes for this config."))
					.addText(ITextProperties.plain("Leaving this screen will discard them without saving. Are you sure?"))
					.withAction(success -> {
						if (!success)
							return;

						changes.clear();
						ScreenOpener.open(parent);
					})
					.open(this);
		} else {
			ScreenOpener.open(parent);
		}
	}

	@Override
	public void onClose() {
		if (changes.isEmpty()) {
			super.onClose();
			return;
		}

		new ConfirmationScreen()
				.centered()
				.addText(ITextProperties.plain("You still have " + changes.size() + " unsaved changes for this config."))
				.addText(ITextProperties.plain("Leaving this screen will discard them without saving. Are you sure?"))
				.withAction(success -> {
					if (!success)
						return;

					changes.clear();
					super.onClose();
				})
				.open(this);
	}
}
