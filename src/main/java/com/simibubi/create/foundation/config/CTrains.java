package com.simibubi.create.foundation.config;

public class CTrains extends ConfigBase {

	public final ConfigFloat trainTopSpeed = f(40, 0, "trainTopSpeed", Comments.mps, Comments.trainTopSpeed);
	public final ConfigFloat trainAcceleration =
		f(6, 0, "trainAcceleration", Comments.acc, Comments.trainAcceleration);

	@Override
	public String getName() {
		return "trains";
	}
	
	public double getTopSpeedMPT() {
		return trainTopSpeed.getF() / 20;
	}
	
	public double getAccelerationMPTT() {
		return trainAcceleration.getF() / 400;
	}

	private static class Comments {
		static String mps = "[in Blocks/Second]";
		static String acc = "[in Blocks/Second²]";
		static String trainTopSpeed = "The top speed of any assembled Train.";
		static String trainAcceleration = "The acceleration of any assembled Train.";
	}

}
