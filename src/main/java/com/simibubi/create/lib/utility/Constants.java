package com.simibubi.create.lib.utility;

public class Constants {
	public static class NBT {
		public static final int TAG_INT         = 3;
		public static final int TAG_FLOAT       = 5;
		public static final int TAG_DOUBLE      = 6;
		public static final int TAG_STRING      = 8;
		public static final int TAG_LIST        = 9;
		public static final int TAG_COMPOUND    = 10;
	}

	public static class BlockFlags {
		public static final int NOTIFY_NEIGHBORS = 1; // 0b1
		public static final int BLOCK_UPDATE = 2; // 0b10
		public static final int NO_RERENDER = 4; // 0b100
		public static final int RERENDER_MAIN_THREAD = 8; // 0b1000
		public static final int UPDATE_NEIGHBORS = 16; // 0b10000
		public static final int NO_NEIGHBOR_DROPS = 32; // 0b100000
		public static final int IS_MOVING = 64; // 0b1000000
		public static final int DEFAULT = NOTIFY_NEIGHBORS | BLOCK_UPDATE; // 3
		public static final int DEFAULT_AND_RERENDER = DEFAULT | RERENDER_MAIN_THREAD; // 11
	}

	public static class Crafting {
		public static int HEIGHT = 3;
		public static int WIDTH = 3;
	}
}
