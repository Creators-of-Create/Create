package com.simibubi.create.infrastructure.debugInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.platform.GlUtil;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.mixin.accessor.SystemReportAccessor;
import com.simibubi.create.infrastructure.debugInfo.element.DebugInfoSection;
import com.simibubi.create.infrastructure.debugInfo.element.InfoElement;
import com.simibubi.create.infrastructure.debugInfo.element.InfoEntry;

import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import oshi.SystemInfo;

/**
 * Allows for providing easily accessible debugging information.
 * This info can be retrieved with the "/create debuginfo" command.
 * This command copies all information to the clipboard, formatted for a GitHub issue.
 * Addons are welcome to add their own sections. Registration must occur synchronously.
 */
public class DebugInformation {
	private static final List<DebugInfoSection> client = new ArrayList<>();
	private static final List<DebugInfoSection> server = new ArrayList<>();

	private static final ImmutableMap<String, String> mcSystemInfo = Util.make(() -> {
		SystemReport systemReport = new SystemReport();
		SystemReportAccessor access = (SystemReportAccessor) systemReport;
		return ImmutableMap.copyOf(access.getEntries());
	});

	public static void registerClientInfo(DebugInfoSection section) {
		client.add(section);
	}

	public static void registerServerInfo(DebugInfoSection section) {
		server.add(section);
	}

	public static void registerBothInfo(DebugInfoSection section) {
		registerClientInfo(section);
		registerServerInfo(section);
	}

	public static List<DebugInfoSection> getClientInfo() {
		return client;
	}

	public static List<DebugInfoSection> getServerInfo() {
		return server;
	}

	static {
		DebugInfoSection.builder(Create.NAME)
				.put("Mod Version", Create.VERSION)
				.put("Forge Version", getVersionOfMod("forge"))
				.put("Minecraft Version", SharedConstants.getCurrentVersion().getName())
				.buildTo(DebugInformation::registerBothInfo);

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			DebugInfoSection.builder("Graphics")
					.put("Flywheel Version", Flywheel.getVersion().toString())
					.put("Flywheel Backend", () -> Backend.getBackendType().toString())
					.put("OpenGL Renderer", GlUtil::getRenderer)
					.put("OpenGL Version", GlUtil::getOpenGLVersion)
					.put("Graphics Mode", () -> Minecraft.getInstance().options.graphicsMode().get().getKey())
					.buildTo(DebugInformation::registerClientInfo);
		});

		DebugInfoSection.builder("System Information")
				.put("Operating System", SystemReportAccessor.getOPERATING_SYSTEM())
				.put("Java Version", SystemReportAccessor.getJAVA_VERSION())
				.put("JVM Flags", getMcSystemInfo("JVM Flags"))
				.put("Memory", () -> getMcSystemInfo("Memory"))
				.put("Total Memory", getTotalRam())
				.put("CPU", getCpuInfo())
				.putAll(listAllGraphicsCards())
				.buildTo(DebugInformation::registerBothInfo);

		DebugInfoSection.builder("Other Mods")
				.putAll(listAllOtherMods())
				.buildTo(DebugInformation::registerBothInfo);
	}

	public static String getVersionOfMod(String id) {
		return ModList.get().getModContainerById(id)
				.map(mod -> mod.getModInfo().getVersion().toString())
				.orElse("None");
	}

	public static Collection<InfoElement> listAllOtherMods() {
		List<InfoElement> mods = new ArrayList<>();
		ModList.get().forEachModContainer((id, mod) -> {
			if (!id.equals(Create.ID) && !id.equals("forge") && !id.equals("minecraft") && !id.equals("flywheel")) {
				IModInfo info = mod.getModInfo();
				String name = info.getDisplayName();
				String version = info.getVersion().toString();
				mods.add(new InfoEntry(name, version));
			}
		});
		return mods;
	}

	public static Collection<InfoElement> listAllGraphicsCards() {
		List<InfoElement> cards = new ArrayList<>();
		for (int i = 0; i < 10; i++) { // there won't be more than 10, right? right??
			String name = getMcSystemInfo("Graphics card #" + i + " name");
			String vendor = getMcSystemInfo("Graphics card #" + i + " vendor");
			String vram = getMcSystemInfo("Graphics card #" + i + " VRAM (MB)");
			if (name == null || vendor == null || vram == null)
				break;
			String key = "Graphics card #" + i;
			String value = String.format("%s (%s); %s MB of VRAM", name, vendor, vram);
			cards.add(new InfoEntry(key, value));
		}
		return cards.isEmpty() ? List.of(new InfoEntry("Graphics cards", "none")) : cards;
	}

	public static String getTotalRam() {
		long availableMemory = new SystemInfo().getHardware().getMemory().getAvailable();
		long totalMemory = new SystemInfo().getHardware().getMemory().getTotal();
		long usedMemory = totalMemory - availableMemory;
		return String.format("%s bytes (%s MiB) / %s bytes (%s MiB)", usedMemory, usedMemory / 1049000, totalMemory, totalMemory / 1049000);
	}

	public static String getCpuInfo() {
		String name = tryTrim(getMcSystemInfo("Processor Name"));
		String freq = getMcSystemInfo("Frequency (GHz)");
		String sockets = getMcSystemInfo("Number of physical packages");
		String cores = getMcSystemInfo("Number of physical CPUs");
		String threads = getMcSystemInfo("Number of logical CPUs");
		return String.format("%s @ %s GHz; %s cores / %s threads on %s socket(s)", name, freq, cores, threads, sockets);
	}

	/**
	 * Get a system attribute provided by Minecraft.
	 * They can be found in the constructor of {@link SystemReport}.
	 */
	@Nullable
	public static String getMcSystemInfo(String key) {
		return mcSystemInfo.get(key);
	}

	public static String getIndent(int depth) {
		return Stream.generate(() -> "\t").limit(depth).collect(Collectors.joining());
	}

	@Nullable
	public static String tryTrim(@Nullable String s) {
		return s == null ? null : s.trim();
	}
}
