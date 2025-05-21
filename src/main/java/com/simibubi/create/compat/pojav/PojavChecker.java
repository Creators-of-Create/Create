package com.simibubi.create.compat.pojav;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;

import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Mobile devices have low quality graphics drivers that cause visual issues.
 * This class checks if Pojav is present and shows a warning screen if so.
 * <p>
 * Based on Sodium's impl
 * <a href="https://github.com/CaffeineMC/sodium/blob/d8fe39c3d2a119d9638c3a5e338a9fbaf4de67fe/common/src/boot/java/net/caffeinemc/mods/sodium/client/compatibility/checks/PostLaunchChecks.java">here</a>.
 */
public class PojavChecker {
	private static final Logger LOGGER = LoggerFactory.getLogger(PojavChecker.class);

	private static final Pattern KNOWN_ANDROID_PATH = Pattern.compile("/data/user/[0-9]+/net\\.kdt\\.pojavlaunch");

	public static final boolean IS_PRESENT = Util.make(() -> {
		if (System.getenv("POJAV_RENDERER") != null) {
			LOGGER.warn("[Create]: Detected presence of environment variable POJAV_LAUNCHER, which seems to indicate we are running on Android");
			return true;
		}

		String librarySearchPaths = System.getProperty("java.library.path", null);

		if (librarySearchPaths != null) {
			for (String path : librarySearchPaths.split(":")) {
				if (isKnownAndroidPathFragment(path)) {
					LOGGER.warn("[Create]: Found a library search path which seems to be hosted in an Android filesystem: {}", path);
					return true;
				}
			}
		}

		String workingDirectory = System.getProperty("user.home", null);

		if (workingDirectory != null) {
			if (isKnownAndroidPathFragment(workingDirectory)) {
				LOGGER.warn("[Create]: Working directory seems to be hosted in an Android filesystem: {}", workingDirectory);
				// note: Sodium doesn't return here. Mistake?
				return true;
			}
		}

		return false;
	});

	private static boolean screenShown = false;

	public static void init() {
		if (!IS_PRESENT)
			return;

		NeoForge.EVENT_BUS.addListener(PojavChecker::onScreenInit);
	}

	public static void onScreenInit(ScreenEvent.Init.Post event) {
		if (!screenShown && event.getScreen() instanceof TitleScreen titleScreen) {
			Minecraft.getInstance().setScreen(new PojavWarningScreen(titleScreen));
			screenShown = true;
		}
	}

	private static boolean isKnownAndroidPathFragment(String path) {
		return KNOWN_ANDROID_PATH.matcher(path).matches();
	}
}
