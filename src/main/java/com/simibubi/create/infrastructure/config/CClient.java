package com.simibubi.create.infrastructure.config;

import com.simibubi.create.foundation.config.ConfigBase;
import com.simibubi.create.foundation.config.ui.ConfigAnnotations;

public class CClient extends ConfigBase {

	public final ConfigGroup client = group(0, "client",
			Comments.client);

	//no group
	public final ConfigBool tooltips = b(true, "enableTooltips",
			Comments.tooltips);
	public final ConfigBool enableOverstressedTooltip = b(true, "enableOverstressedTooltip",
			Comments.enableOverstressedTooltip);
	public final ConfigBool explainRenderErrors = b(false, "explainRenderErrors",
			Comments.explainRenderErrors);
	public final ConfigFloat fanParticleDensity = f(.5f, 0, 1, "fanParticleDensity",
			Comments.fanParticleDensity);
	public final ConfigFloat filterItemRenderDistance = f(10f, 1, "filterItemRenderDistance", Comments.filterItemRenderDistance);
	public final ConfigBool rainbowDebug = b(false, "enableRainbowDebug",
			Comments.rainbowDebug);
	public final ConfigInt maxContraptionLightVolume = i(16384, 0, Integer.MAX_VALUE, "maximumContraptionLightVolume",
			Comments.maxContraptionLightVolume);
	// no group
	public final ConfigInt mainMenuConfigButtonRow = i(2, 0, 4, "mainMenuConfigButtonRow",
			Comments.mainMenuConfigButtonRow);
	public final ConfigInt mainMenuConfigButtonOffsetX = i(-4, Integer.MIN_VALUE, Integer.MAX_VALUE, "mainMenuConfigButtonOffsetX",
			Comments.mainMenuConfigButtonOffsetX);
	public final ConfigInt ingameMenuConfigButtonRow = i(3, 0, 5, "ingameMenuConfigButtonRow",
			Comments.ingameMenuConfigButtonRow);
	public final ConfigInt ingameMenuConfigButtonOffsetX = i(-4, Integer.MIN_VALUE, Integer.MAX_VALUE, "ingameMenuConfigButtonOffsetX",
			Comments.ingameMenuConfigButtonOffsetX);
	public final ConfigBool ignoreFabulousWarning = b(false, "ignoreFabulousWarning",
		Comments.ignoreFabulousWarning);
	public final ConfigBool rotateWhenSeated = b(true, "rotateWhenSeated",
		Comments.rotatewhenSeated);

	// custom fluid fog
	public final ConfigGroup fluidFogSettings = group(1, "fluidFogSettings", Comments.fluidFogSettings);
	public final ConfigFloat honeyTransparencyMultiplier =
		f(1, .125f, 256, "honey", Comments.honeyTransparencyMultiplier);
	public final ConfigFloat chocolateTransparencyMultiplier =
		f(1, .125f, 256, "chocolate", Comments.chocolateTransparencyMultiplier);

	//overlay group
	public final ConfigGroup overlay = group(1, "goggleOverlay",
			Comments.overlay);
	public final ConfigInt overlayOffsetX = i(20, Integer.MIN_VALUE, Integer.MAX_VALUE, "overlayOffsetX",
			Comments.overlayOffset);
	public final ConfigInt overlayOffsetY = i(0, Integer.MIN_VALUE, Integer.MAX_VALUE, "overlayOffsetY",
			Comments.overlayOffset);
	public final ConfigBool overlayCustomColor = b(false, "customColorsOverlay",
			Comments.overlayCustomColor);
	public final ConfigInt overlayBackgroundColor = i(0xf0_100010, Integer.MIN_VALUE, Integer.MAX_VALUE, "customBackgroundOverlay",
			Comments.overlayBackgroundColor);
	public final ConfigInt overlayBorderColorTop = i(0x50_5000ff, Integer.MIN_VALUE, Integer.MAX_VALUE, "customBorderTopOverlay",
			Comments.overlayBorderColorTop);
	public final ConfigInt overlayBorderColorBot = i(0x50_28007f, Integer.MIN_VALUE, Integer.MAX_VALUE, "customBorderBotOverlay",
			Comments.overlayBorderColorBot);

	//placement assist group
	public final ConfigGroup placementAssist = group(1, "placementAssist",
			Comments.placementAssist);
	public final ConfigEnum<PlacementIndicatorSetting> placementIndicator = e(PlacementIndicatorSetting.TEXTURE, "indicatorType",
			Comments.placementIndicator);
	public final ConfigFloat indicatorScale = f(1.0f, 0f, "indicatorScale",
			Comments.indicatorScale);

	//ponder group
	public final ConfigGroup ponder = group(1, "ponder",
			Comments.ponder);
	public final ConfigBool comfyReading = b(false, "comfyReading",
			Comments.comfyReading);
	public final ConfigBool editingMode = b(false, "editingMode",
		Comments.editingMode);

	//sound group
	public final ConfigGroup sound = group(1, "sound",
			Comments.sound);
	public final ConfigBool enableAmbientSounds = b(true, "enableAmbientSounds",
			Comments.enableAmbientSounds);
	public final ConfigFloat ambientVolumeCap = f(.1f, 0, 1, "ambientVolumeCap",
			Comments.ambientVolumeCap);

	//train group
	public final ConfigGroup trains = group(1, "trains", Comments.trains);
	public final ConfigFloat mountedZoomMultiplier = f(3, 0, "mountedZoomMultiplier", Comments.mountedZoomMultiplier);
	public final ConfigBool showTrackGraphOnF3 = b(false, "showTrackGraphOnF3", Comments.showTrackGraphOnF3);
	public final ConfigBool showExtendedTrackGraphOnF3 = b(false, "showExtendedTrackGraphOnF3", Comments.showExtendedTrackGraphOnF3);

	@Override
	public String getName() {
		return "client";
	}

	public enum PlacementIndicatorSetting {
		TEXTURE, TRIANGLE, NONE
	}

	private static class Comments {
		static String client = "Client-only settings - If you're looking for general settings, look inside your worlds serverconfig folder!";
		static String tooltips = "Show item descriptions on Shift and controls on Ctrl.";
		static String enableOverstressedTooltip = "Display a tooltip when looking at overstressed components.";
		static String explainRenderErrors = "Log a stack-trace when rendering issues happen within a moving contraption.";
		static String fanParticleDensity = "Higher density means more spawned particles.";
		static String[] filterItemRenderDistance = new String[]{
				"[in Blocks]",
				"Maximum Distance to the player at which items in Blocks' filter slots will be displayed"
		};
		static String rainbowDebug = "Show kinetic debug information on blocks while the F3-Menu is open.";
		static String maxContraptionLightVolume = "The maximum amount of blocks for which to try and calculate dynamic contraption lighting. Decrease if large contraption cause too much lag";
		static String[] mainMenuConfigButtonRow = new String[]{
				"Choose the menu row that the Create config button appears on in the main menu",
				"Set to 0 to disable the button altogether"
		};
		static String[] mainMenuConfigButtonOffsetX = new String[]{
				"Offset the Create config button in the main menu by this many pixels on the X axis",
				"The sign (-/+) of this value determines what side of the row the button appears on (left/right)"
		};
		static String[] ingameMenuConfigButtonRow = new String[]{
				"Choose the menu row that the Create config button appears on in the in-game menu",
				"Set to 0 to disable the button altogether"
		};
		static String[] ingameMenuConfigButtonOffsetX = new String[]{
				"Offset the Create config button in the in-game menu by this many pixels on the X axis",
				"The sign (-/+) of this value determines what side of the row the button appears on (left/right)"
		};
		static String ignoreFabulousWarning = "Setting this to true will prevent Create from sending you a warning when playing with Fabulous graphics enabled";
		static String rotatewhenSeated = "Disable to prevent being rotated while seated on a Moving Contraption";
		static String overlay = "Settings for the Goggle Overlay";
		static String overlayOffset = "Offset the overlay from goggle- and hover- information by this many pixels on the respective axis; Use /create overlay";
		static String overlayCustomColor = "Enable this to use your custom colors for the Goggle- and Hover- Overlay";
		static String[] overlayBackgroundColor = new String[]{
				"The custom background color to use for the Goggle- and Hover- Overlays, if enabled",
				"[in Hex: #AaRrGgBb]", ConfigAnnotations.IntDisplay.HEX.asComment()
		};
		static String[] overlayBorderColorTop = new String[]{
				"The custom top color of the border gradient to use for the Goggle- and Hover- Overlays, if enabled",
				"[in Hex: #AaRrGgBb]", ConfigAnnotations.IntDisplay.HEX.asComment()
		};
		static String[] overlayBorderColorBot = new String[]{
				"The custom bot color of the border gradient to use for the Goggle- and Hover- Overlays, if enabled",
				"[in Hex: #AaRrGgBb]", ConfigAnnotations.IntDisplay.HEX.asComment()
		};
		static String placementAssist = "Settings for the Placement Assist";
		static String[] placementIndicator = new String[]{
				"What indicator should be used when showing where the assisted placement ends up relative to your crosshair",
				"Choose 'NONE' to disable the Indicator altogether"
		};
		static String indicatorScale = "Change the size of the Indicator by this multiplier";
		static String ponder = "Ponder settings";
		static String comfyReading = "Slow down a ponder scene whenever there is text on screen.";
		static String editingMode = "Show additional info in the ponder view and reload scene scripts more frequently.";
		static String sound = "Sound settings";
		static String enableAmbientSounds = "Make cogs rumble and machines clatter.";
		static String ambientVolumeCap = "Maximum volume modifier of Ambient noise";

		static String trains = "Railway related settings";
		static String mountedZoomMultiplier = "How far away the Camera should zoom when seated on a train";
		static String showTrackGraphOnF3 = "Display nodes and edges of a Railway Network while f3 debug mode is active";
		static String showExtendedTrackGraphOnF3 = "Additionally display materials of a Rail Network while f3 debug mode is active";
		static String fluidFogSettings = "Configure your vision range when submerged in Create's custom fluids";
		static String honeyTransparencyMultiplier = "The vision range through honey will be multiplied by this factor";
		static String chocolateTransparencyMultiplier = "The vision range though chocolate will be multiplied by this factor";
	}

}
