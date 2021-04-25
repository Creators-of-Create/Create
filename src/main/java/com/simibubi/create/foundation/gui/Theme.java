package com.simibubi.create.foundation.gui;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Theme {

	private static final Theme base = new Theme();
	private static Theme custom = null;

	public static void setTheme(@Nullable Theme theme) {
		custom = theme;
	}

	@Nonnull public static Color c(String key) {
		Color r = null;

		if (custom != null)
			r = custom.get(key);

		if (r == null)
			r = base.get(key);

		if (r == null)
			r = Color.BLACK;

		return r;
	}

	@Nonnull public static Color c(Key key) {
		return c(key.get());
	}

	public static int i(String key) {
		return c(key).getRGB();
	}

	public static int i(Key key) {
		return i(key.get());
	}

	//-----------//

	protected final Map<String, Color> colors;

	protected Theme() {
		colors = new HashMap<>();
		init();
	}

	protected void init() {
		put(Key.BUTTON_IDLE_1, new Color(0x60_c0c0ff, true));
		put(Key.BUTTON_IDLE_2, new Color(0x30_c0c0ff, true));
		put(Key.BUTTON_HOVER_1, new Color(0xa0_c0c0ff, true));
		put(Key.BUTTON_HOVER_2, new Color(0x50_c0c0ff, true));
		put(Key.BUTTON_CLICK_1, new Color(0xff_4b4bff));
		put(Key.BUTTON_CLICK_2, new Color(0xff_3b3bdd));
		put(Key.BUTTON_DISABLE_1, new Color(0x80_909090, true));
		put(Key.BUTTON_DISABLE_2, new Color(0x20_909090, true));
		put("button_success_1", new Color(0xcc_88f788, true));
		put("button_success_2", new Color(0xcc_20cc20, true));
		put("button_fail_1", new Color(0xcc_f78888, true));
		put("button_fail_2", new Color(0xcc_cc2020, true));
		put(Key.TEXT_1, new Color(0xff_eeeeee));
		put(Key.TEXT_2, new Color(0xff_a3a3a3));
		put(Key.TEXT_ACCENT_1, new Color(0xff_7b7ba3));
		put(Key.TEXT_ACCENT_2, new Color(0xff_616192));
		//values from PonderUI & PonderButton
		put(Key.PONDER_BACKGROUND, new Color(0xdd_000000, true));
		put(Key.PONDER_IDLE_1, new Color(0x40ffeedd, true));
		put(Key.PONDER_IDLE_2, new Color(0x20ffeedd, true));
		put(Key.PONDER_HOVER_1, new Color(0x70ffffff, true));
		put(Key.PONDER_HOVER_2, new Color(0x30ffffff, true));
		put(Key.PONDER_HIGHLIGHT_1, new Color(0xf0ffeedd, true));
		put(Key.PONDER_HIGHLIGHT_2, new Color(0x60ffeedd, true));
	}

	protected void put(String key, Color c) {
		colors.put(key, c);
	}

	protected void put(Key key, Color c) {
		put(key.get(), c);
	}

	@Nullable public Color get(String key) {
		return colors.get(key);
	}

	public enum Key {
		BUTTON_IDLE_1("button_idle_1"),
		BUTTON_IDLE_2("button_idle_2"),
		BUTTON_HOVER_1("button_hover_1"),
		BUTTON_HOVER_2("button_hover_2"),
		BUTTON_CLICK_1("button_click_1"),
		BUTTON_CLICK_2("button_click_2"),
		BUTTON_DISABLE_1("button_disable_1"),
		BUTTON_DISABLE_2("button_disable_2"),

		TEXT_1("text_1"),
		TEXT_2("text_2"),
		TEXT_ACCENT_1("text_accent_1"),
		TEXT_ACCENT_2("text_accent_2"),

		PONDER_BACKGROUND("ponder_background"),
		PONDER_IDLE_1("ponder_idle_1"),
		PONDER_IDLE_2("ponder_idle_2"),
		PONDER_HOVER_1("ponder_hover_1"),
		PONDER_HOVER_2("ponder_hover_2"),
		PONDER_HIGHLIGHT_1("ponder_highlight_1"),
		PONDER_HIGHLIGHT_2("ponder_highlight_2"),

		;

		String s;

		Key(String s) {
			this.s = s;
		}

		String get() {
			return s;
		}
	}
}
