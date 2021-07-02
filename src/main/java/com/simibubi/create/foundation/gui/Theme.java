package com.simibubi.create.foundation.gui;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.simibubi.create.foundation.utility.Couple;

public class Theme {

	private static final Theme base = new Theme();
	private static Theme custom = null;

	public static void setTheme(@Nullable Theme theme) {
		custom = theme;
	}

	public static void reload() {
		base.init();
		if (custom != null)
			custom.init();
	}

	private static ColorHolder resolve(String key) {
		ColorHolder h = null;

		if (custom != null)
			h = custom.get(key);

		if (h == null)
			h = base.get(key);

		if (h == null)
			h = ColorHolder.missing;

		return h;
	}

	@Nonnull public static Couple<Color> p(@Nonnull Key key) {return p(key.get());}
	@Nonnull public static Couple<Color> p(String key) {return resolve(key).asPair();}

	@Nonnull public static Color c(@Nonnull Key key, boolean first) {return c(key.get(), first);}
	@Nonnull public static Color c(String key, boolean first) {return p(key).get(first);}

	public static int i(@Nonnull Key key, boolean first) {return i(key.get(), first);}
	public static int i(String key, boolean first) {return p(key).get(first).getRGB();}

	@Nonnull public static Color c(@Nonnull Key key) {return c(key.get());}
	@Nonnull public static Color c(String key) {return resolve(key).get();}

	public static int i(@Nonnull Key key) {return i(key.get());}
	public static int i(String key) {return resolve(key).get().getRGB();}

	//-----------//

	protected final Map<String, ColorHolder> colors;

	protected Theme() {
		colors = new HashMap<>();
		init();
	}

	protected void init() {
		put(Key.BUTTON_IDLE, new Color(0xdd_8ab6d6, true), new Color(0x90_8ab6d6, true));
		put(Key.BUTTON_HOVER, new Color(0xff_9ABBD3, true), new Color(0xd0_9ABBD3, true));
		put(Key.BUTTON_CLICK, new Color(0xff_ffffff), new Color(0xee_ffffff));
		put(Key.BUTTON_DISABLE, new Color(0x80_909090, true), new Color(0x60_909090, true));
		put(Key.BUTTON_SUCCESS, new Color(0xcc_88f788, true), new Color(0xcc_20cc20, true));
		put(Key.BUTTON_FAIL, new Color(0xcc_f78888, true), new Color(0xcc_cc2020, true));
		put(Key.TEXT, new Color(0xff_eeeeee), new Color(0xff_a3a3a3));
		put(Key.TEXT_DARKER, new Color(0xff_a3a3a3), new Color(0xff_808080));
		put(Key.TEXT_ACCENT_STRONG, new Color(0xff_8ab6d6), new Color(0xff_8ab6d6));
		put(Key.TEXT_ACCENT_SLIGHT, new Color(0xff_ddeeff), new Color(0xff_a0b0c0));
		put(Key.STREAK, new Color(0x101010, false));

		put(Key.PONDER_BUTTON_IDLE, new Color(0x60_c0c0ff, true), new Color(0x30_c0c0ff, true));
		put(Key.PONDER_BUTTON_HOVER, new Color(0xf0_c0c0ff, true), new Color(0xa0_c0c0ff, true));
		put(Key.PONDER_BUTTON_CLICK, new Color(0xff_ffffff), new Color(0xdd_ffffff));
		put(Key.PONDER_BUTTON_DISABLE, new Color(0x80_909090, true), new Color(0x20_909090, true));
		put(Key.PONDER_BACKGROUND_TRANSPARENT, new Color(0xdd_000000, true));
		put(Key.PONDER_BACKGROUND_FLAT, new Color(0xff_000000, false));
		put(Key.PONDER_IDLE, new Color(0x40ffeedd, true), new Color(0x20ffeedd, true));
		put(Key.PONDER_HOVER, new Color(0x70ffffff, true), new Color(0x30ffffff, true));
		put(Key.PONDER_HIGHLIGHT, new Color(0xf0ffeedd, true), new Color(0x60ffeedd, true));
		put(Key.TEXT_WINDOW_BORDER, new Color(0x607a6000, true), new Color(0x207a6000, true));
		put(Key.PONDER_BACK_ARROW, new Color(0xf0aa9999, true), new Color(0x30aa9999, true));
		put(Key.PONDER_PROGRESSBAR, new Color(0x80ffeedd, true), new Color(0x50ffeedd, true));
		put(Key.PONDER_MISSING_CREATE, new Color(0x70_984500, true), new Color(0x70_692400, true));
		put(Key.PONDER_MISSING_VANILLA, new Color(0x50_5000ff, true), new Color(0x50_300077, true));
		put(Key.CONFIG_TITLE_A, new Color(0xffc69fbc, true), new Color(0xfff6b8bb, true));
		put(Key.CONFIG_TITLE_B, new Color(0xfff6b8bb, true), new Color(0xfffbf994, true));
		//put(Key., new Color(0x, true), new Color(0x, true));
	}

	protected void put(String key, Color c) {
		colors.put(key, ColorHolder.single(c));
	}

	protected void put(Key key, Color c) {
		put(key.get(), c);
	}

	protected void put(String key, Color c1, Color c2) {
		colors.put(key, ColorHolder.pair(c1, c2));
	}

	protected void put(Key key, Color c1, Color c2) {
		put(key.get(), c1 , c2);
	}

	@Nullable protected ColorHolder get(String key) {
		return colors.get(key);
	}

	public static class Key {

		public static Key BUTTON_IDLE = new Key();
		public static Key BUTTON_HOVER = new Key();
		public static Key BUTTON_CLICK = new Key();
		public static Key BUTTON_DISABLE = new Key();
		public static Key BUTTON_SUCCESS = new Key();
		public static Key BUTTON_FAIL = new Key();

		public static Key TEXT = new Key();
		public static Key TEXT_DARKER = new Key();
		public static Key TEXT_ACCENT_STRONG = new Key();
		public static Key TEXT_ACCENT_SLIGHT = new Key();

		public static Key STREAK = new Key();

		public static Key PONDER_BACKGROUND_TRANSPARENT = new Key();
		public static Key PONDER_BACKGROUND_FLAT = new Key();
		public static Key PONDER_IDLE = new Key();
		public static Key PONDER_HOVER = new Key();
		public static Key PONDER_HIGHLIGHT = new Key();
		public static Key TEXT_WINDOW_BORDER = new Key();
		public static Key PONDER_BACK_ARROW = new Key();
		public static Key PONDER_PROGRESSBAR = new Key();
		public static Key PONDER_MISSING_CREATE = new Key();
		public static Key PONDER_MISSING_VANILLA = new Key();

		public static Key PONDER_BUTTON_IDLE = new Key();
		public static Key PONDER_BUTTON_HOVER = new Key();
		public static Key PONDER_BUTTON_CLICK = new Key();
		public static Key PONDER_BUTTON_DISABLE = new Key();

		public static Key CONFIG_TITLE_A = new Key();
		public static Key CONFIG_TITLE_B = new Key();

		private static int index = 0;

		private final String s;

		protected Key() {
			this.s = "_" + index++;
		}

		protected Key(String s) {
			this.s = s;
		}

		public String get() {
			return s;
		}
	}

	private static class ColorHolder {

		private static final ColorHolder missing = ColorHolder.single(Color.BLACK);

		private Couple<Color> colors;

		private static ColorHolder single(Color c) {
			ColorHolder h = new ColorHolder();
			h.colors = Couple.create(c, c);
			return h;
		}

		private static ColorHolder pair(Color first, Color second) {
			ColorHolder h = new ColorHolder();
			h.colors = Couple.create(first, second);
			return h;
		}

		private Color get() {
			return colors.getFirst();
		}

		private Couple<Color> asPair() {
			return colors;
		}

	}
}
