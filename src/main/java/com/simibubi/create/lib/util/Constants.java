package com.simibubi.create.lib.util;

public class Constants {
	public static class BlockFlags {
		public static final int NOTIFY_NEIGHBORS = 1; // 0b1
		public static final int BLOCK_UPDATE = 2; // 0b10
		public static final int RERENDER_MAIN_THREAD = 8; // 0b1000
		public static final int DEFAULT = NOTIFY_NEIGHBORS | BLOCK_UPDATE; // 3
		public static final int DEFAULT_AND_RERENDER = DEFAULT | RERENDER_MAIN_THREAD; // 11
	}

	public static class Crafting {
		public static int HEIGHT = 3;
		public static int WIDTH = 3;
	}
}
