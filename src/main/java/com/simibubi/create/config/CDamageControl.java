package com.simibubi.create.config;

public class CDamageControl extends ConfigBase {

	public ConfigBool freezeCrushing = b(false, "freezeCrushing", Comments.freezeCrushing);
	public ConfigBool freezeExtractors = b(false, "freezeExtractors", Comments.freezeExtractors);
	public ConfigBool freezeInWorldProcessing = b(false, "freezeInWorldProcessing", Comments.freezeInWorldProcessing);
	public ConfigBool freezeRotationPropagator =
		b(false, "freezeRotationPropagator", Comments.freezeRotationPropagator);
	public ConfigBool freezeContraptions = b(false, "freezeContraptions", Comments.freezeContraptions);

	@Override
	public String getName() {
		return "damageControl";
	}

	private static class Comments {
		static String freezeCrushing = "In case Crushing Wheels crushed your server.";
		static String freezeExtractors = "In case Extractors pulled the plug.";
		static String freezeInWorldProcessing = "In case Encased Fans tried smelting your hardware.";
		static String freezeRotationPropagator =
			"Pauses rotation logic altogether - Use if crash mentions RotationPropagators.";
		static String freezeContraptions = "In case Moving contraptions pushed it too far.";
	}

}
