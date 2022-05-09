package com.simibubi.create.foundation.config;

public class CTrains extends ConfigBase {

	public final ConfigBool trainsCauseDamage = b(true, "trainsCauseDamage", Comments.trainsCauseDamage);
	public final ConfigFloat trainTopSpeed = f(36, 0, "trainTopSpeed", Comments.mps, Comments.trainTopSpeed);
	public final ConfigFloat trainTurningTopSpeed =
		f(18, 0, "trainTurningTopSpeed", Comments.mps, Comments.trainTurningTopSpeed);
	public final ConfigFloat trainAcceleration = f(4, 0, "trainAcceleration", Comments.acc, Comments.trainAcceleration);
	public final ConfigInt maxAssemblyLength = i(128, 5, "maxAssemblyLength", Comments.maxAssemblyLength);
	public final ConfigInt maxBogeyCount = i(20, 1, "maxBogeyCount", Comments.maxBogeyCount);

	@Override
	public String getName() {
		return "trains";
	}

	public double getTopSpeedMPT() {
		return trainTopSpeed.getF() / 20;
	}

	public double getTurningTopSpeedMPT() {
		return trainTurningTopSpeed.getF() / 20;
	}

	public double getAccelerationMPTT() {
		return trainAcceleration.getF() / 400;
	}

	private static class Comments {
		static String mps = "[in Blocks/Second]";
		static String acc = "[in Blocks/Second²]";
		static String trainTopSpeed = "The top speed of any assembled Train.";
		static String trainTurningTopSpeed = "The top speed of Trains during a turn.";
		static String trainAcceleration = "The acceleration of any assembled Train.";
		static String trainsCauseDamage = "Whether moving Trains can hurt colliding mobs and players.";
		static String maxAssemblyLength = "Maximum length of a Train Stations' assembly track.";
		static String maxBogeyCount = "Maximum amount of bogeys assembled as a single Train.";
	}

}
