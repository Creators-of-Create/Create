package com.simibubi.create.foundation.config;

public class CSounds extends ConfigBase {

	public ConfigGroup sounds = group(0, "sounds", Comments.sounds);

	public ConfigFloat ambientVolumeCap = f(.1f, 0, 1, "ambientVolumeCap", Comments.ambientVolumeCap);
	public ConfigEnum<ContactSoundSetting> contactClickSound = e(ContactSoundSetting.ALWAYS, "contactClickSound", Comments.contactClick);
	public ConfigBool enableAmbientSounds = b(true, "enableAmbientSounds", Comments.ambientSounds);
	public ConfigEnum<EventSourceSetting> latchToggleSourceSound = e(EventSourceSetting.ANY, "latchToggleSourceSound", Comments.latchToggleSourceSound);

	@Override
	public String getName() {
		return "sounds";
	}


	public enum ContactSoundSetting {ALWAYS, EDGE_RISE, NONE}
	public enum EventSourceSetting {ANY, PLAYER, NONE}

	private static class Comments {
		static String sounds = "Make your factory play a symphony of thousand machines";
		static String ambientSounds = "Make cogs rumble and machines clatter.";
		static String ambientVolumeCap = "Maximum volume modifier of Ambient noise";
		static String[] contactClick = {
				"When to play Redstone Contact sound:",
				ContactSoundSetting.ALWAYS + " - on activation and deactivation",
				ContactSoundSetting.EDGE_RISE + " - only on activation",
				ContactSoundSetting.NONE + " - contact will be silent"
		};
		public static String[] latchToggleSourceSound = {
				"Which sources trigger [Powered] Toggle Latch sounds:",
				EventSourceSetting.ANY + " - circuits and player interactions",
				EventSourceSetting.PLAYER +" - only player interactions (silent when in circuits)",
				EventSourceSetting.NONE +" - latches will be silent"
		};
	}
}
