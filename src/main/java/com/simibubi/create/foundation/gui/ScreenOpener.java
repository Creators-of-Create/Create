package com.simibubi.create.foundation.gui;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.utility.LerpedFloat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;

public class ScreenOpener {

	private static final Deque<Screen> backStack = new ArrayDeque<>();
	private static Screen backSteppedFrom = null;

	public static void open(Screen screen) {
		open(Minecraft.getInstance().currentScreen, screen);
	}

	public static void open(@Nullable Screen current, Screen toOpen) {
		backSteppedFrom = null;
		if (current != null) {
			if (backStack.size() >= 15) // don't go deeper than 15 steps
				backStack.pollLast();

			backStack.push(current);
		} else
			backStack.clear();

		openScreen(toOpen);
	}

	public static void openPreviousScreen(Screen current) {
		if (backStack.isEmpty())
			return;
		backSteppedFrom = current;
		Screen previousScreen = backStack.pop();
		if (previousScreen instanceof AbstractSimiScreen)
			((AbstractSimiScreen) previousScreen).transition.startWithValue(-0.1)
				.chase(-1, .4f, LerpedFloat.Chaser.EXP);
		openScreen(previousScreen);
	}

	// transitions are only supported in simiScreens atm. they take care of all the
	// rendering for it
	public static void transitionTo(AbstractSimiScreen screen) {
		if (tryBackTracking(screen))
			return;
		screen.transition.startWithValue(0.1)
			.chase(1, .4f, LerpedFloat.Chaser.EXP);
		open(screen);
	}

	private static boolean tryBackTracking(AbstractSimiScreen screen) {
		List<Screen> screenHistory = getScreenHistory();
		if (screenHistory.isEmpty())
			return false;
		Screen previouslyRenderedScreen = screenHistory.get(0);
		if (!(previouslyRenderedScreen instanceof AbstractSimiScreen))
			return false;
		if (!screen.isEquivalentTo((AbstractSimiScreen) previouslyRenderedScreen))
			return false;

		openPreviousScreen(Minecraft.getInstance().currentScreen);
		return true;
	}

	public static void clearStack() {
		backStack.clear();
	}

	public static List<Screen> getScreenHistory() {
		return new ArrayList<>(backStack);
	}

	@Nullable
	public static Screen getPreviouslyRenderedScreen() {
		return backSteppedFrom != null ? backSteppedFrom : backStack.peek();
	}

	private static void openScreen(Screen screen) {
		Minecraft.getInstance()
			.enqueue(() -> Minecraft.getInstance()
				.displayGuiScreen(screen));
	}

}
