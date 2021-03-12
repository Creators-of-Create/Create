package com.simibubi.create.foundation.ponder;

import java.util.List;

import com.google.common.base.Strings;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.ponder.content.PonderIndexScreen;
import com.simibubi.create.foundation.ponder.content.PonderTagScreen;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.LerpedFloat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderTooltipEvent;

public class PonderTooltipHandler {

	static LerpedFloat holdWProgress = LerpedFloat.linear()
		.startWithValue(0);
	static ItemStack lastHoveredStack = ItemStack.EMPTY;
	static boolean subject = false;

	public static final String HOLD_TO_PONDER = PonderLocalization.LANG_PREFIX + "hold_to_ponder";
	public static final String SUBJECT = PonderLocalization.LANG_PREFIX + "subject";

	public static void tick() {
		Minecraft instance = Minecraft.getInstance();
		Screen currentScreen = instance.currentScreen;
		ItemStack stack = ItemStack.EMPTY;
		ItemStack prevStack = lastHoveredStack;
		lastHoveredStack = ItemStack.EMPTY;
		subject = false;

		if (currentScreen instanceof ContainerScreen) {
			ContainerScreen<?> cs = (ContainerScreen<?>) currentScreen;
			Slot slotUnderMouse = cs.getSlotUnderMouse();
			if (slotUnderMouse == null || !slotUnderMouse.getHasStack())
				return;
			stack = slotUnderMouse.getStack();
		} else if (currentScreen instanceof PonderUI) {
			PonderUI ponderUI = (PonderUI) currentScreen;
			stack = ponderUI.getHoveredTooltipItem();
			if (stack.isItemEqual(ponderUI.getSubject()))
				subject = true;
		} else if (currentScreen instanceof PonderTagScreen) {
			PonderTagScreen tagScreen = (PonderTagScreen) currentScreen;
			stack = tagScreen.getHoveredTooltipItem();
		} else if (currentScreen instanceof PonderIndexScreen) {
			PonderIndexScreen indexScreen = (PonderIndexScreen) currentScreen;
			stack = indexScreen.getHoveredTooltipItem();
		} else
			return;

		if (stack.isEmpty())
			return;
		if (!PonderRegistry.all.containsKey(stack.getItem()
			.getRegistryName()))
			return;

		if (prevStack.isEmpty() || !prevStack.isItemEqual(stack))
			holdWProgress.startWithValue(0);

		float value = holdWProgress.getValue();
		int keyCode = ponderKeybind().getKey()
			.getKeyCode();
		long window = instance.getWindow()
			.getHandle();

		if (!subject && InputMappings.isKeyDown(window, keyCode)) {
			if (value >= 1) {
				if (currentScreen instanceof AbstractSimiScreen)
					((AbstractSimiScreen) currentScreen).centerScalingOnMouse();

				ScreenOpener.transitionTo(PonderUI.of(stack));
				holdWProgress.startWithValue(0);
				return;
			}
			holdWProgress.setValue(Math.min(1, value + Math.max(.25f, value) * .25f));
		} else
			holdWProgress.setValue(Math.max(0, value - .05f));

		lastHoveredStack = stack;
	}

	public static void addToTooltip(List<ITextComponent> toolTip, ItemStack stack) {
		float renderPartialTicks = Minecraft.getInstance()
			.getRenderPartialTicks();
		if (lastHoveredStack != stack)
			return;
		ITextComponent component = subject ? Lang.createTranslationTextComponent(SUBJECT)
			.applyTextStyle(TextFormatting.GREEN)
			: makeProgressBar(Math.min(1, holdWProgress.getValue(renderPartialTicks) * 8 / 7f));
		if (toolTip.size() < 2)
			toolTip.add(component);
		else
			toolTip.set(1, component);
	}

	public static void handleTooltipColor(RenderTooltipEvent.Color event) {
		if (lastHoveredStack != event.getStack())
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
		String holdW = Lang
			.createTranslationTextComponent(HOLD_TO_PONDER, new StringTextComponent(ponderKeybind().getKeyBinding()
				.getLocalizedName()).applyTextStyle(TextFormatting.WHITE))
			.applyTextStyle(TextFormatting.GRAY)
			.getFormattedText();

		FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
		float charWidth = fontRenderer.getStringWidth("|");
		float tipWidth = fontRenderer.getStringWidth(holdW);

		int total = (int) (tipWidth / charWidth);
		int current = (int) (progress * total);

		if (progress > 0) {
			String bars = "";
			bars += TextFormatting.WHITE + Strings.repeat("|", current);
			if (progress < 1)
				bars += TextFormatting.GRAY + Strings.repeat("|", total - current);
			return new StringTextComponent(bars);
		}

		return new StringTextComponent(holdW);
	}

	protected static KeyBinding ponderKeybind() {
		return Minecraft.getInstance().gameSettings.keyBindForward;
	}

}
