package com.simibubi.create.foundation.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class ScreenOpener {

	private static final Deque<Screen> backStack = new ArrayDeque<>();

	public static void open(Screen screen) {
		open(Minecraft.getInstance().currentScreen, screen);
	}

	public static void open(@Nullable Screen current, Screen toOpen) {
		if (current != null) {
			if (backStack.size() >= 15) //don't go deeper than 15 steps
				backStack.pollLast();

			backStack.push(current);
		} else
			backStack.clear();

		openScreen(toOpen);
	}

	public static void openLastScreen() {
		if (backStack.isEmpty())
			return;

		openScreen(backStack.pop());
	}

	//transitions are only supported in simiScreens atm. they take care of all the rendering for it
	public static void transitionTo(AbstractSimiScreen screen) {
		screen.transition.updateChaseTarget(1);
		open(screen);
	}

	public static void transitionToLast() {
		if (backStack.isEmpty())
			return;

		Screen currentScreen = Minecraft.getInstance().currentScreen;
		if (currentScreen instanceof AbstractSimiScreen)
			((AbstractSimiScreen) currentScreen).transition.updateChaseTarget(-1);
		else
			openLastScreen();
	}

	public static void clearStack() {
		backStack.clear();
	}

	public static List<Screen> getScreenHistory() {
		return new ArrayList<>(backStack);
	}

	@Nullable
	public static Screen getLastScreen() {
		return backStack.peek();
	}

	private static void openScreen(Screen screen) {
		Minecraft.getInstance().enqueue(() -> Minecraft.getInstance().displayGuiScreen(screen));
	}

}
