package com.simibubi.create.foundation.config;

public class CSounds extends ConfigBase {

	public ConfigGroup sounds = group(0, "sounds", Comments.sounds);

	public ConfigFloat ambientVolumeCap = f(.1f, 0, 1, "ambientVolumeCap", Comments.ambientVolumeCap);
	public ConfigBool enableAmbientSounds = b(true, "enableAmbientSounds", Comments.ambientSounds);

	@Override
	public String getName() {
		return "sounds";
	}

	private static class Comments {
		static String sounds = "Make your factory play a symphony of thousand machines";
		static String ambientSounds = "Make cogs rumble and machines clatter.";
		static String ambientVolumeCap = "Maximum volume modifier of Ambient noise";
	}
}
