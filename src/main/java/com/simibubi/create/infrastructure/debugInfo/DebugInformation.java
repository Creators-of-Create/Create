package com.simibubi.create.infrastructure.debugInfo;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.Backend;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.mixin.accessor.SystemReportAccessor;
import com.simibubi.create.infrastructure.debugInfo.element.DebugInfoSection;

import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Allows for providing easily accessible debugging information.
 * This info can be retrieved with the "/create debuginfo" command.
 * This command copies all information to the clipboard, formatted for a GitHub issue.
 */
public class DebugInformation {
	private static DebugInfoSection client = DebugInfoSection.builder("Client Info").build();
	private static DebugInfoSection server = DebugInfoSection.builder("Server Info").build();

	public static void registerClientInfo(DebugInfoSection section) {
		client = client.builder().put(section).build();
	}

	public static void registerServerInfo(DebugInfoSection section) {
		server = server.builder().put(section).build();
	}

	public static void registerBothInfo(DebugInfoSection section) {
		registerClientInfo(section);
		registerServerInfo(section);
	}

	public static DebugInfoSection getClientInfo() {
		return client;
	}

	public static DebugInfoSection getServerInfo() {
		return server;
	}

	static {
		DebugInfoSection.builder(Create.NAME)
				.put("Mod Version", Create.VERSION)
				.put("Forge Version", getVersionOfMod("forge"))
				.put("Minecraft Version", SharedConstants.getCurrentVersion().getName())
				.put("Other Mods", listAllOtherMods())
				.put("Operating System", SystemReportAccessor.getOPERATING_SYSTEM())
				.put("Java Version", SystemReportAccessor.getJAVA_VERSION())
				.put("Memory", () -> getMcSystemInfo().get("Memory"))
				.buildTo(DebugInformation::registerBothInfo);

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			DebugInfoSection.builder(Create.NAME)
					.put("Flywheel Version", Flywheel.getVersion().toString())
					.put("Flywheel Backend", () -> Backend.getBackendType().toString())
					.put("Graphics Cards", DebugInformation.getGraphicsCardsInfo())
					.buildTo(DebugInformation::registerClientInfo);
		});
	}

	public static String getVersionOfMod(String id) {
		return ModList.get().getModContainerById(id)
				.map(mod -> mod.getModInfo().getVersion().toString())
				.orElse("None");
	}

	public static String listAllOtherMods() {
		StringBuilder mods = new StringBuilder();
		ModList.get().forEachModContainer((id, mod) -> {
			if (!id.equals(Create.ID) && !id.equals("forge") && !id.equals("minecraft")) {
				IModInfo info = mod.getModInfo();
				String name = info.getDisplayName();
				String version = info.getVersion().toString();
				if (!mods.isEmpty())
					mods.append('\n');
				mods.append(name).append(": ").append(version);
			}
		});
		return mods.toString();
	}

	public static Map<String, String> getMcSystemInfo() {
		return ((SystemReportAccessor) new SystemReport()).getEntries();
	}

	public static String getGraphicsCardsInfo() {
		StringBuilder builder = new StringBuilder();
		Map<String, String> info = getMcSystemInfo();
		String[] types = { "name", "vendor", "VRAM (MB)" };
		cards: for (int i = 0; i < 10; i++) {
			for (String type : types) {
				String key = "Graphics card #" + i + " " + type;
				if (!info.containsKey(key))
					break cards;
				if (!builder.isEmpty())
					builder.append('\n');
				String value = String.format("%s #%s: %s", type, i, info.get(key));
				builder.append(value);
			}
		}
		if (builder.isEmpty())
			return "No GPU found?";
		return builder.toString();
	}
}
