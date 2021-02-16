package com.simibubi.create.foundation.ponder;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import com.google.gson.JsonElement;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.ponder.PonderScene.SceneBuilder;
import com.simibubi.create.foundation.ponder.content.PonderIndex;
import com.simibubi.create.foundation.ponder.content.SharedText;
import com.tterrag.registrate.util.entry.ItemProviderEntry;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;

public class PonderRegistry {

	static Map<ResourceLocation, List<PonderStoryBoard>> all = new HashMap<>();

	public static void addStoryBoard(ItemProviderEntry<?> component, PonderStoryBoard storyBoard) {
		ResourceLocation id = component.getId();
		all.computeIfAbsent(id, $ -> new ArrayList<>())
			.add(storyBoard);
	}

	public static List<PonderScene> compile(ResourceLocation id) {

		if (PonderIndex.EDITOR_MODE) {
			PonderLocalization.shared.clear();
			PonderLocalization.specific.clear();
			SharedText.gatherText();
		}

		List<PonderStoryBoard> list = all.get(id);
		List<PonderScene> scenes = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			
			PonderStoryBoard sb = list.get(i);
			Template activeTemplate = loadSchematic(sb.getSchematicName());
			PonderWorld world = new PonderWorld(BlockPos.ZERO, Minecraft.getInstance().world);
			activeTemplate.addBlocksToWorld(world, BlockPos.ZERO, new PlacementSettings());
			world.createBackup();
			
			PonderScene scene = new PonderScene(world, id, i);
			PonderLocalization.registerSpecific(id, i, "title", sb.getStoryTitle());
			SceneBuilder builder = scene.builder();
			sb.program(builder, builder.getSceneBuildingUtil());
			scene.begin();
			scenes.add(scene);
			
		}

		return scenes;
	}

	public static Template loadSchematic(String path) {
		Template t = new Template();
		String filepath = "ponder/" + path + ".nbt";
		InputStream resourceAsStream = Create.class.getClassLoader()
			.getResourceAsStream(filepath);
		if (resourceAsStream == null)
			throw new IllegalStateException("Could not find ponder schematic: " + filepath);
		try (DataInputStream stream =
			new DataInputStream(new BufferedInputStream(new GZIPInputStream(resourceAsStream)))) {
			CompoundNBT nbt = CompressedStreamTools.read(stream, new NBTSizeTracker(0x20000000L));
			t.read(nbt);
		} catch (IOException e) {
			Create.logger.warn("Failed to read ponder schematic", e);
		}
		return t;
	}

	public static JsonElement provideLangEntries() {
		PonderIndex.register();
		SharedText.gatherText();
		all.forEach((id, list) -> {
			for (int i = 0; i < list.size(); i++) {
				PonderStoryBoard sb = list.get(i);
				PonderScene scene = new PonderScene(null, id, i);
				PonderLocalization.registerSpecific(id, i, "title", sb.getStoryTitle());
				SceneBuilder builder = scene.builder();
				sb.program(builder, builder.getSceneBuildingUtil());
			}
		});
		return PonderLocalization.record();
	}

}
