package com.simibubi.create.foundation.config;

public class CSounds extends ConfigBase {

	public ConfigGroup sounds = group(0, "sounds", Comments.sounds);

	public ConfigFloat ambientVolumeCap = f(.1f, 0, 1, "ambientVolumeCap", Comments.ambientVolumeCap);
	public ConfigBool enableAmbientSounds = b(true, "enableAmbientSounds", Comments.ambientSounds);
	public ConfigEnum<TriggerType> contactTriggerType = e(TriggerType.ANY, "contactTriggerType", Comments.contactTriggerType);
	public ConfigEnum<TriggerSource> latchTriggerSource = e(TriggerSource.ANY, "latchTriggerSource", Comments.latchTriggerSource);
	public ConfigEnum<TriggerType> controllerTriggerType = e(TriggerType.ANY, "controllerTriggerType", Comments.controllerTriggerType);
	public ConfigBool enableWrenchRatchet = b(true, "enableWrenchRatchet", Comments.enableWrenchRatchet);

	@Override
	public String getName() {
		return "sounds";
	}


	public enum TriggerType {ANY, ACTIVATION, NONE}
	public enum TriggerSource {ANY, PLAYER, NONE}

	private static class Comments {
		static String sounds = "Make your factory play a symphony of thousand machines";
		static String ambientSounds = "Make cogs rumble and machines clatter.";
		static String ambientVolumeCap = "Maximum volume modifier of Ambient noise";
		static String[] contactTriggerType = {
				"What triggers Redstone Contact sound:",
				TriggerType.ANY + " - activation and deactivation",
				TriggerType.ACTIVATION + " - only activation",
				TriggerType.NONE + " - nothing"
		};
		static String[] latchTriggerSource = {
				"Play [Powered] Toggle Latch sounds whenever:",
				TriggerSource.ANY + " - block state changes",
				TriggerSource.PLAYER +" - only when player interacts with block",
				TriggerSource.NONE +" - never"
		};
		static String[] controllerTriggerType = {
				"What triggers Linked Controller sound:",
				TriggerType.ANY + " - button press or release",
				TriggerType.ACTIVATION + " - only button press",
				TriggerType.NONE + " - nothing"
		};
		static String enableWrenchRatchet = "Add ratchet sound to wrench on interaction";
	}
}
