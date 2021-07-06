package com.simibubi.create.foundation.ponder;

import java.util.List;

import com.google.common.base.Strings;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderTooltipEvent;

public class PonderTooltipHandler {

	static LerpedFloat holdWProgress = LerpedFloat.linear()
		.startWithValue(0);
	static ItemStack hoveredStack = ItemStack.EMPTY;
	static ItemStack trackingStack = ItemStack.EMPTY;
	static boolean subject = false;
	static boolean deferTick = false;

	public static final String HOLD_TO_PONDER = PonderLocalization.LANG_PREFIX + "hold_to_ponder";
	public static final String SUBJECT = PonderLocalization.LANG_PREFIX + "subject";

	public static void tick() {
		deferTick = true;
	}

	public static void deferredTick() {
		deferTick = false;
		if (hoveredStack.isEmpty() || trackingStack.isEmpty()) {
			trackingStack = ItemStack.EMPTY;
			holdWProgress.startWithValue(0);
			return;
		}

		Minecraft instance = Minecraft.getInstance();
		Screen currentScreen = instance.currentScreen;
		float value = holdWProgress.getValue();
		int keyCode = ponderKeybind().getKey()
			.getKeyCode();
		long window = instance.getWindow()
			.getHandle();

		if (!subject && InputMappings.isKeyDown(window, keyCode)) {
			if (value >= 1) {
				if (currentScreen instanceof NavigatableSimiScreen)
					((NavigatableSimiScreen) currentScreen).centerScalingOnMouse();
				ScreenOpener.transitionTo(PonderUI.of(trackingStack));
				holdWProgress.startWithValue(0);
				return;
			}
			holdWProgress.setValue(Math.min(1, value + Math.max(.25f, value) * .25f));
		} else
			holdWProgress.setValue(Math.max(0, value - .05f));

		hoveredStack = ItemStack.EMPTY;
	}

	public static void addToTooltip(List<ITextComponent> toolTip, ItemStack stack) {
		updateHovered(stack);

		if (deferTick)
			deferredTick();

		if (trackingStack != stack)
			return;

		float renderPartialTicks = Minecraft.getInstance()
			.getRenderPartialTicks();
		ITextComponent component = subject ? Lang.createTranslationTextComponent(SUBJECT)
			.formatted(TextFormatting.GREEN)
			: makeProgressBar(Math.min(1, holdWProgress.getValue(renderPartialTicks) * 8 / 7f));
		if (toolTip.size() < 2)
			toolTip.add(component);
		else
			toolTip.add(1, component);
	}

	protected static void updateHovered(ItemStack stack) {
		Minecraft instance = Minecraft.getInstance();
		Screen currentScreen = instance.currentScreen;
		ItemStack prevStack = trackingStack;
		hoveredStack = ItemStack.EMPTY;
		subject = false;

		if (currentScreen instanceof PonderUI) {
			PonderUI ponderUI = (PonderUI) currentScreen;
			if (stack.isItemEqual(ponderUI.getSubject()))
				subject = true;
		}

		if (stack.isEmpty())
			return;
		if (!PonderRegistry.all.containsKey(stack.getItem()
			.getRegistryName()))
			return;

		if (prevStack.isEmpty() || !prevStack.isItemEqual(stack))
			holdWProgress.startWithValue(0);

		hoveredStack = stack;
		trackingStack = stack;
	}

	public static void handleTooltipColor(RenderTooltipEvent.Color event) {
		if (trackingStack != event.getStack())
			return;
		if (holdWProgress.getValue() == 0)
			return;
		float renderPartialTicks = Minecraft.getInstance()
			.getRenderPartialTicks();
		int start = event.getOriginalBorderStart();
		int end = event.getOriginalBorderEnd();
		float progress = Math.min(1, holdWProgress.getValue(renderPartialTicks) * 8 / 7f);

		start = getSmoothColorForProgress(progress);
		end = getSmoothColorForProgress((progress));

		event.setBorderStart(start | 0xa0000000);
		event.setBorderEnd(end | 0xa0000000);
	}

	private static int getSmoothColorForProgress(float progress) {
		if (progress < .5f)
			return ColorHelper.mixColors(0x5000FF, 5592575, progress * 2);
		return ColorHelper.mixColors(5592575, 0xffffff, (progress - .5f) * 2);
	}

	private static ITextComponent makeProgressBar(float progress) {
		IFormattableTextComponent holdW = Lang
			.translate(HOLD_TO_PONDER,
				((IFormattableTextComponent) ponderKeybind().getBoundKeyLocalizedText()).formatted(TextFormatting.GRAY))
			.formatted(TextFormatting.DARK_GRAY);

		FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
		float charWidth = fontRenderer.getStringWidth("|");
		float tipWidth = fontRenderer.getWidth(holdW);

		int total = (int) (tipWidth / charWidth);
		int current = (int) (progress * total);

		if (progress > 0) {
			String bars = "";
			bars += TextFormatting.GRAY + Strings.repeat("|", current);
			if (progress < 1)
				bars += TextFormatting.DARK_GRAY + Strings.repeat("|", total - current);
			return new StringTextComponent(bars);
		}

		return holdW;
	}

	protected static KeyBinding ponderKeybind() {
		return Minecraft.getInstance().gameSettings.keyBindForward;
	}

}
