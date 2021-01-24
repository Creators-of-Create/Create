package com.simibubi.create.foundation.metadoc;

import java.util.List;

import com.google.common.base.Strings;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.LerpedFloat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderTooltipEvent;

public class MetaDocHandler {

	static LerpedFloat holdWProgress = LerpedFloat.linear()
		.startWithValue(0);
	static ItemStack lastHoveredStack = null;

	public static void tick() {
		Minecraft instance = Minecraft.getInstance();
		Screen currentScreen = instance.currentScreen;
		if (!(currentScreen instanceof ContainerScreen))
			return;
		ContainerScreen<?> cs = (ContainerScreen<?>) currentScreen;

		ItemStack prevStack = lastHoveredStack;
		lastHoveredStack = null;
		Slot slotUnderMouse = cs.getSlotUnderMouse();
		if (slotUnderMouse == null || !slotUnderMouse.getHasStack())
			return;

		ItemStack stack = slotUnderMouse.getStack();
		if (prevStack != stack)
			holdWProgress.startWithValue(0);

		float value = holdWProgress.getValue();
		if (InputMappings.isKeyDown(instance.getWindow()
			.getHandle(),
			instance.gameSettings.keyBindForward.getKey()
				.getKeyCode())) {
//		if (AllKeys.altDown()) {
			if (value >= 1)
				ScreenOpener.open(new MetaDocScreen());
			holdWProgress.setValue(Math.min(1, value + Math.max(.25f, value) * .25f));
		} else {
			holdWProgress.setValue(Math.max(0, value - .05f));
		}

		lastHoveredStack = stack;
	}

	public static void addToTooltip(List<ITextComponent> toolTip, ItemStack stack) {
		if (lastHoveredStack != stack)
			return;
		float renderPartialTicks = Minecraft.getInstance()
			.getRenderPartialTicks();
		toolTip.add(makeProgressBar(Math.min(1, holdWProgress.getValue(renderPartialTicks) * 8 / 7f)));
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
//		if (progress < .75f)
//			return ColorHelper.mixColors(16733695, 5636095, (progress - .5f) * 4);
		return ColorHelper.mixColors(5592575, 5636095, (progress - .5f) * 2);
	}

	private static ITextComponent makeProgressBar(float progress) {
		String bar = "";
		int filledLength = (int) (12 * progress);
		bar += Strings.repeat("\u2588", filledLength);
		if (progress < 1)
			bar += Strings.repeat("\u2592", 12 - filledLength);

		TextFormatting color = TextFormatting.GRAY;
		if (progress > 0)
			color = TextFormatting.BLUE;
		if (progress == 1f)
			color = TextFormatting.AQUA;

		ITextComponent leftBr = new StringTextComponent("").applyTextStyle(TextFormatting.WHITE);
		ITextComponent rightBr = new StringTextComponent("").applyTextStyle(TextFormatting.WHITE);
		ITextComponent barComponent = new StringTextComponent(bar).applyTextStyle(color);
		return leftBr.appendSibling(barComponent)
			.appendSibling(rightBr);
	}

}
