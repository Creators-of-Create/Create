package com.simibubi.create;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public enum AllKeys {

	TOOL_MENU("toolmenu", GLFW.GLFW_KEY_LEFT_ALT),
	ACTIVATE_TOOL("", GLFW.GLFW_KEY_RIGHT_CONTROL),
	TOOLBELT("toolbelt", GLFW.GLFW_KEY_LEFT_ALT),

	;

	private KeyMapping keybind;
	private String description;
	private int key;
	private boolean modifiable;

	private AllKeys(String description, int defaultKey) {
		this.description = Create.ID + ".keyinfo." + description;
		this.key = defaultKey;
		this.modifiable = !description.isEmpty();
	}

	public static void register() {
		for (AllKeys key : values()) {
			key.keybind = new KeyMapping(key.description, key.key, Create.NAME);
			if (!key.modifiable)
				continue;

			KeyBindingHelper.registerKeyBinding(key.keybind);
		}
	}

	public KeyMapping getKeybind() {
		return keybind;
	}

	public boolean isPressed() {
		if (!modifiable)
			return isKeyDown(key);
		return keybind.isDown();
	}

	public String getBoundKey() {
		return keybind.getTranslatedKeyMessage()
			.getString()
			.toUpperCase();
	}

	public int getBoundCode() {
		return com.simibubi.create.lib.util.KeyBindingHelper.getKeyCode(keybind)
				.getValue();
	}

	public static boolean isKeyDown(int key) {
		return GLFW.glfwGetKey(Minecraft.getInstance()
			.getWindow()
			.getWindow(), key) != 0;
	}

	public static boolean ctrlDown() {
		return Screen.hasControlDown();
	}

	public static boolean shiftDown() {
		return Screen.hasShiftDown();
	}

	public static boolean altDown() {
		return Screen.hasAltDown();
	}

}
