package com.simibubi.create.foundation.ponder;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.ponder.content.PonderChapter;
import com.simibubi.create.foundation.ponder.content.PonderChapterRegistry;
import com.simibubi.create.foundation.ponder.content.PonderIndex;
import com.simibubi.create.foundation.ponder.content.PonderTagRegistry;
import com.simibubi.create.foundation.ponder.content.SharedText;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;

public class PonderRegistry {

	public static final PonderTagRegistry TAGS = new PonderTagRegistry();
	public static final PonderChapterRegistry CHAPTERS = new PonderChapterRegistry();
	// Map from item IDs to storyboard entries
	public static final Map<ResourceLocation, List<PonderStoryBoardEntry>> ALL = new HashMap<>();

	public static void addStoryBoard(PonderStoryBoardEntry entry) {
		synchronized (ALL) {
			List<PonderStoryBoardEntry> list = ALL.computeIfAbsent(entry.getComponent(), $ -> new ArrayList<>());
			synchronized (list) {
				list.add(entry);
			}
		}
	}

	public static List<PonderScene> compile(ResourceLocation id) {
		List<PonderStoryBoardEntry> list = ALL.get(id);
		if (list == null) {
			return Collections.emptyList();
		}
		return compile(list);
	}

	public static List<PonderScene> compile(PonderChapter chapter) {
		List<PonderStoryBoardEntry> list = CHAPTERS.getStories(chapter);
		if (list == null) {
			return Collections.emptyList();
		}
		return compile(list);
	}

	public static List<PonderScene> compile(List<PonderStoryBoardEntry> entries) {
		if (PonderIndex.EDITOR_MODE) {
			PonderLocalization.SHARED.clear();
			SharedText.gatherText();
		}

		List<PonderScene> scenes = new ArrayList<>();

		for (int i = 0; i < entries.size(); i++) {
			PonderStoryBoardEntry sb = entries.get(i);
			Template activeTemplate = loadSchematic(sb.getSchematicLocation());
			PonderWorld world = new PonderWorld(BlockPos.ZERO, Minecraft.getInstance().level);
			activeTemplate.placeInWorld(world, BlockPos.ZERO, new PlacementSettings(), world.random);
			world.createBackup();
			PonderScene scene = compileScene(i, sb, world);
			scene.begin();
			scenes.add(scene);
		}

		return scenes;
	}

	public static PonderScene compileScene(int i, PonderStoryBoardEntry sb, PonderWorld world) {
		PonderScene scene = new PonderScene(world, sb.getNamespace(), sb.getComponent(), sb.getTags());
		SceneBuilder builder = scene.builder();
		sb.getBoard()
			.program(builder, scene.getSceneBuildingUtil());
		return scene;
	}

	public static Template loadSchematic(ResourceLocation location) {
		return loadSchematic(Minecraft.getInstance().getResourceManager(), location);
	}

	public static Template loadSchematic(IResourceManager resourceManager, ResourceLocation location) {
		String namespace = location.getNamespace();
		String path = "ponder/" + location.getPath() + ".nbt";
		ResourceLocation location1 = new ResourceLocation(namespace, path);

		try (IResource resource = resourceManager.getResource(location1)) {
			return loadSchematic(resource.getInputStream());
		} catch (FileNotFoundException e) {
			Create.LOGGER.error("Ponder schematic missing: " + location1, e);
		} catch (IOException e) {
			Create.LOGGER.error("Failed to read ponder schematic: " + location1, e);
		}
		return new Template();
	}

	public static Template loadSchematic(InputStream resourceStream) throws IOException {
		Template t = new Template();
		DataInputStream stream =
			new DataInputStream(new BufferedInputStream(new GZIPInputStream(resourceStream)));
		CompoundNBT nbt = CompressedStreamTools.read(stream, new NBTSizeTracker(0x20000000L));
		t.load(nbt);
		return t;
	}

}
