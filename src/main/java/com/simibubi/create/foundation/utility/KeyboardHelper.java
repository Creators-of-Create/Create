package com.simibubi.create.foundation.utility;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KeyboardHelper {
	
	public static final int PRESS = 1;
	public static final int HOLD = 2;
	public static final int RELEASE = 0;
	
	public static final int LSHIFT = 340;
	public static final int LALT = 342;
	public static final int RETURN = 257;
	
	public static final int DOWN = 264;
	public static final int LEFT = 263;
	public static final int RIGHT = 262;
	public static final int UP = 265;
	
	public static final int G = 71;

	public static boolean isKeyDown(int key) {
		return GLFW.glfwGetKey(Minecraft.getInstance().mainWindow.getHandle(), key) != 0;
	}
	
}
