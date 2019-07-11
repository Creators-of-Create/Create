package com.simibubi.create;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWScrollCallback;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;

public class ScrollFixer {

	private static List<Predicate<Double>> listeners;

	public static void init() {
		listeners = new ArrayList<>();
		Method method;
		try {
			MouseHelper mouseHelper = Minecraft.getInstance().mouseHelper;
			method = mouseHelper.getClass().getDeclaredMethod("scrollCallback", Long.TYPE,
					Double.TYPE, Double.TYPE);
			method.setAccessible(true);
			GLFW.glfwSetScrollCallback(Minecraft.getInstance().mainWindow.getHandle(), new GLFWScrollCallback() {
				@Override
				public void invoke(long win, double dx, double dy) {
					for (Predicate<Double> consumer : listeners) {
						if (consumer.test(dy))
							return;
					}
					try {
						method.invoke(mouseHelper, win, dx, dy);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			});
		} catch (NoSuchMethodException | SecurityException e1) {
			e1.printStackTrace();
		}
	}

	public static void addMouseWheelListener(Predicate<Double> callback) {
		listeners.add(callback);
	}

}
