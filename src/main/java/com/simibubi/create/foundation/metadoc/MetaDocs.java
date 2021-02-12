package com.simibubi.create.foundation.metadoc;

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
import com.simibubi.create.foundation.metadoc.content.MetaDocIndex;
import com.simibubi.create.foundation.metadoc.content.SharedText;
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

public class MetaDocs {

	static Map<ResourceLocation, List<MetaDocStoryBoard>> all = new HashMap<>();

	public static void addStoryBoard(ItemProviderEntry<?> component, MetaDocStoryBoard storyBoard) {
		ResourceLocation id = component.getId();
		all.computeIfAbsent(id, $ -> new ArrayList<>())
			.add(storyBoard);
	}

	public static List<MetaDocScene> compile(ResourceLocation id) {

		if (MetaDocIndex.EDITOR_MODE) {
			MetaDocLocalization.shared.clear();
			MetaDocLocalization.specific.clear();
			SharedText.gatherText();
		}

		List<MetaDocStoryBoard> list = all.get(id);
		List<MetaDocScene> scenes = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			
			MetaDocStoryBoard sb = list.get(i);
			Template activeTemplate = loadSchematic(sb.getSchematicName());
			MetaDocWorld world = new MetaDocWorld(BlockPos.ZERO, Minecraft.getInstance().world);
			activeTemplate.addBlocksToWorld(world, BlockPos.ZERO, new PlacementSettings());
			world.createBackup();
			
			MetaDocScene scene = new MetaDocScene(world, id, i);
			MetaDocLocalization.registerSpecific(id, i, "title", sb.getStoryTitle());
			sb.program(scene.builder(), world.getBounds()
				.getLength());
			scene.begin();
			scenes.add(scene);
			
		}

		return scenes;
	}

	public static Template loadSchematic(String path) {
		Template t = new Template();
		String filepath = "doc/" + path + ".nbt";
		InputStream resourceAsStream = Create.class.getClassLoader()
			.getResourceAsStream(filepath);
		if (resourceAsStream == null)
			throw new IllegalStateException("Could not find metadoc schematic: " + filepath);
		try (DataInputStream stream =
			new DataInputStream(new BufferedInputStream(new GZIPInputStream(resourceAsStream)))) {
			CompoundNBT nbt = CompressedStreamTools.read(stream, new NBTSizeTracker(0x20000000L));
			t.read(nbt);
		} catch (IOException e) {
			Create.logger.warn("Failed to read metadoc schematic", e);
		}
		return t;
	}

	public static JsonElement provideLangEntries() {
		MetaDocIndex.register();
		SharedText.gatherText();
		all.forEach((id, list) -> {
			for (int i = 0; i < list.size(); i++) {
				MetaDocStoryBoard sb = list.get(i);
				MetaDocScene scene = new MetaDocScene(null, id, i);
				MetaDocLocalization.registerSpecific(id, i, "title", sb.getStoryTitle());
				sb.program(scene.builder(), Vec3i.NULL_VECTOR);
			}
		});
		return MetaDocLocalization.record();
	}

}
