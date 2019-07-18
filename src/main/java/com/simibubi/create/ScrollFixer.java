package com.simibubi.create;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWScrollCallback;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraftforge.client.event.GuiScreenEvent.MouseScrollEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class ScrollFixer {

	public static void init() {
		try {
			MouseHelper mouseHelper = Minecraft.getInstance().mouseHelper;
			Method method = ObfuscationReflectionHelper.findMethod(mouseHelper.getClass(), "func_198020_a", Long.TYPE,
					Double.TYPE, Double.TYPE);
			
			GLFWScrollCallback callback = new GLFWScrollCallback() {
				@Override
				public void invoke(long win, double dx, double dy) {
					MouseScrollEvent.Post event = new MouseScrollEvent.Post(null, mouseHelper.getMouseX(),
							mouseHelper.getMouseY(), dy);
					boolean canceled = MinecraftForge.EVENT_BUS.post(event);
					if (canceled)
						return;
					
					try {
						method.invoke(mouseHelper, win, dx, dy);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			};
			
			GLFW.glfwSetScrollCallback(Minecraft.getInstance().mainWindow.getHandle(), callback);
		} catch (SecurityException e1) {
			e1.printStackTrace();
		}
	}

}
