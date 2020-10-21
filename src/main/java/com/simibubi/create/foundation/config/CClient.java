package com.simibubi.create.foundation.config;

public class CClient extends ConfigBase {

	public ConfigGroup client = group(0, "client",
			"Client-only settings - If you're looking for general settings, look inside your worlds serverconfig folder!");
	public ConfigBool tooltips = b(true, "enableTooltips", "Show item descriptions on Shift and controls on Ctrl.");
	public ConfigBool enableOverstressedTooltip =
		b(true, "enableOverstressedTooltip", "Display a tooltip when looking at overstressed components.");
	public ConfigBool explainRenderErrors =
		b(false, "explainRenderErrors", "Log a stack-trace when rendering issues happen within a moving contraption.");
	public ConfigFloat fanParticleDensity = f(.5f, 0, 1, "fanParticleDensity");
	public ConfigBool rainbowDebug =
		b(true, "enableRainbowDebug", "Show colourful debug information while the F3-Menu is open.");

	public ConfigInt overlayOffsetX = i(20, Integer.MIN_VALUE, Integer.MAX_VALUE, "overlayOffsetX", "Offset the overlay from goggle- and hover- information by this many pixels on the X axis; Use /create overlay");
	public ConfigInt overlayOffsetY = i(0, Integer.MIN_VALUE, Integer.MAX_VALUE, "overlayOffsetY", "Offset the overlay from goggle- and hover- information by this many pixels on the Y axis; Use /create overlay");

	@Override
	public String getName() {
		return "client";
	}

}
