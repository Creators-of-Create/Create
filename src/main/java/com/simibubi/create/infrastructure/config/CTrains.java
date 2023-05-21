package com.simibubi.create.infrastructure.config;

import com.simibubi.create.foundation.config.ConfigBase;

public class CTrains extends ConfigBase {

	public final ConfigBool trainsCauseDamage = b(true, "trainsCauseDamage", Comments.trainsCauseDamage);
	public final ConfigInt maxTrackPlacementLength = i(32, 16, 128, "maxTrackPlacementLength", Comments.maxTrackPlacementLength);
	public final ConfigInt maxAssemblyLength = i(128, 5, "maxAssemblyLength", Comments.maxAssemblyLength);
	public final ConfigInt maxBogeyCount = i(20, 1, "maxBogeyCount", Comments.maxBogeyCount);
	public final ConfigFloat manualTrainSpeedModifier = f(.75f, 0, "manualTrainSpeedModifier", Comments.manualTrainSpeedModifier);
	
	public final ConfigGroup trainStats = group(1, "trainStats", "Standard Trains");
	public final ConfigFloat trainTopSpeed = f(28, 0, "trainTopSpeed", Comments.mps, Comments.trainTopSpeed);
	public final ConfigFloat trainTurningTopSpeed = f(14, 0, "trainTurningTopSpeed", Comments.mps, Comments.trainTurningTopSpeed);
	public final ConfigFloat trainAcceleration = f(3, 0, "trainAcceleration", Comments.acc, Comments.trainAcceleration);
	
	public final ConfigGroup poweredTrainStats = group(1, "poweredTrainStats", "Powered Trains");
	public final ConfigFloat poweredTrainTopSpeed = f(40, 0, "poweredTrainTopSpeed", Comments.mps, Comments.poweredTrainTopSpeed);
	public final ConfigFloat poweredTrainTurningTopSpeed = f(20, 0, "poweredTrainTurningTopSpeed", Comments.mps, Comments.poweredTrainTurningTopSpeed);
	public final ConfigFloat poweredTrainAcceleration = f(3, 0, "poweredTrainAcceleration", Comments.acc, Comments.poweredTrainAcceleration);
	

	@Override
	public String getName() {
		return "trains";
	}

	private static class Comments {
		static String mps = "[in Blocks/Second]";
		static String acc = "[in Blocks/Second²]";
		static String trainTopSpeed = "The top speed of any assembled Train.";
		static String trainTurningTopSpeed = "The top speed of Trains during a turn.";
		static String trainAcceleration = "The acceleration of any assembled Train.";
		static String poweredTrainTopSpeed = "The top speed of powered Trains.";
		static String poweredTrainTurningTopSpeed = "The top speed of powered Trains during a turn.";
		static String poweredTrainAcceleration = "The acceleration of powered Trains.";
		static String trainsCauseDamage = "Whether moving Trains can hurt colliding mobs and players.";
		static String maxTrackPlacementLength = "Maximum length of track that can be placed as one batch or turn.";
		static String maxAssemblyLength = "Maximum length of a Train Stations' assembly track.";
		static String maxBogeyCount = "Maximum amount of bogeys assembled as a single Train.";
		static String manualTrainSpeedModifier = "Relative speed of a manually controlled Train compared to a Scheduled one.";
	}

}
