package com.simibubi.create.foundation.config;

public class CFluids extends ConfigBase {

	public ConfigInt fluidTankCapacity = i(8, 1, "fluidTankCapacity", Comments.buckets, Comments.fluidTankCapacity);
	public ConfigInt fluidTankMaxHeight = i(32, 1, "fluidTankMaxHeight", Comments.blocks, Comments.fluidTankMaxHeight);
	
	@Override
	public String getName() {
		return "fluids";
	}

	private static class Comments {
		static String blocks = "[in Blocks]";
		static String buckets = "[in Buckets]";
		static String fluidTankCapacity = "The amount of liquid a tank can hold per block.";
		static String fluidTankMaxHeight = "The maximum height a fluid tank can reach.";
	}

}
