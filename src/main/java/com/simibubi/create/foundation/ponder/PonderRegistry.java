package com.simibubi.create.foundation.ponder;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

import com.google.gson.JsonElement;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.ponder.PonderStoryBoardEntry.PonderStoryBoard;
import com.simibubi.create.foundation.ponder.content.PonderChapter;
import com.simibubi.create.foundation.ponder.content.PonderChapterRegistry;
import com.simibubi.create.foundation.ponder.content.PonderIndex;
import com.simibubi.create.foundation.ponder.content.PonderTag;
import com.simibubi.create.foundation.ponder.content.PonderTagRegistry;
import com.simibubi.create.foundation.ponder.content.SharedText;
import com.tterrag.registrate.util.entry.ItemProviderEntry;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;

public class PonderRegistry {

	public static final PonderTagRegistry tags = new PonderTagRegistry();
	public static final PonderChapterRegistry chapters = new PonderChapterRegistry();
	public static Map<ResourceLocation, List<PonderStoryBoardEntry>> all = new HashMap<>();

	public static PonderSceneBuilder addStoryBoard(ItemProviderEntry<?> component, String schematic,
		PonderStoryBoard storyBoard, PonderTag... tags) {
		ResourceLocation id = component.getId();
		PonderStoryBoardEntry entry = new PonderStoryBoardEntry(storyBoard, schematic, id);
		PonderSceneBuilder builder = new PonderSceneBuilder(entry);
		if (tags.length > 0)
			builder.highlightTags(tags);
		all.computeIfAbsent(id, _$ -> new ArrayList<>())
			.add(entry);
		return builder;
	}

	public static PonderSceneBuilder addStoryBoard(PonderChapter chapter, ResourceLocation component, String schematic,
		PonderStoryBoard storyBoard) {
		if (component == null)
			component = new ResourceLocation("minecraft", "stick");

		PonderStoryBoardEntry entry = new PonderStoryBoardEntry(storyBoard, schematic, component);
		PonderSceneBuilder builder = new PonderSceneBuilder(entry);
		chapters.addStoriesToChapter(chapter, entry);
		return builder;
	}

	public static MultiSceneBuilder forComponents(ItemProviderEntry<?>... components) {
		return new MultiSceneBuilder(Arrays.asList(components));
	}
	
	public static MultiSceneBuilder forComponents(Iterable<? extends ItemProviderEntry<?>> components) {
		return new MultiSceneBuilder(components);
	}

	public static List<PonderScene> compile(ResourceLocation id) {
		return compile(all.get(id));
	}

	public static List<PonderScene> compile(PonderChapter chapter) {
		return compile(chapters.getStories(chapter));
	}

	public static List<PonderScene> compile(List<PonderStoryBoardEntry> entries) {
		if (PonderIndex.EDITOR_MODE) {
			PonderLocalization.shared.clear();
			SharedText.gatherText();
		}

		List<PonderScene> scenes = new ArrayList<>();

		for (int i = 0; i < entries.size(); i++) {
			PonderStoryBoardEntry sb = entries.get(i);
			Template activeTemplate = loadSchematic(sb.getSchematicName());
			PonderWorld world = new PonderWorld(BlockPos.ZERO, Minecraft.getInstance().world);
			activeTemplate.placeAndNotifyListeners(world, BlockPos.ZERO, new PlacementSettings(), world.rand);
			world.createBackup();
			PonderScene scene = compileScene(i, sb, world);
			scene.begin();
			scenes.add(scene);
		}

		return scenes;
	}

	public static PonderScene compileScene(int i, PonderStoryBoardEntry sb, PonderWorld world) {
		PonderScene scene = new PonderScene(world, sb.getComponent(), sb.getTags());
		SceneBuilder builder = scene.builder();
		sb.getBoard()
			.program(builder, scene.getSceneBuildingUtil());
		return scene;
	}

	public static Template loadSchematic(String path) {
		Template t = new Template();
		String filepath = "ponder/" + path + ".nbt";
		InputStream resourceAsStream = Create.class.getClassLoader()
			.getResourceAsStream(filepath);
		if (resourceAsStream == null) {
			Create.LOGGER.error("Ponder schematic missing: " + path);
			return t;
		}
		try (DataInputStream stream =
			new DataInputStream(new BufferedInputStream(new GZIPInputStream(resourceAsStream)))) {
			CompoundNBT nbt = CompressedStreamTools.read(stream, new NBTSizeTracker(0x20000000L));
			t.read(nbt);
		} catch (IOException e) {
			Create.LOGGER.warn("Failed to read ponder schematic", e);
		}
		return t;
	}

	public static JsonElement provideLangEntries() {
		PonderIndex.register();
		PonderTag.register();
		SharedText.gatherText();
		all.forEach((id, list) -> {
			for (int i = 0; i < list.size(); i++)
				compileScene(i, list.get(i), null);
		});
		return PonderLocalization.record();
	}

	public static class MultiSceneBuilder {

		private final Iterable<? extends ItemProviderEntry<?>> components;

		MultiSceneBuilder(Iterable<? extends ItemProviderEntry<?>> components) {
			this.components = components;
		}

		public MultiSceneBuilder addStoryBoard(String schematicPath, PonderStoryBoard storyBoard) {
			return addStoryBoard(schematicPath, storyBoard, $ -> {
			});
		}

		public MultiSceneBuilder addStoryBoard(String schematicPath, PonderStoryBoard storyBoard, PonderTag... tags) {
			return addStoryBoard(schematicPath, storyBoard, sb -> sb.highlightTags(tags));
		}

		public MultiSceneBuilder addStoryBoard(String schematicPath, PonderStoryBoard storyBoard,
			Consumer<PonderSceneBuilder> extras) {
			components.forEach(c -> extras.accept(PonderRegistry.addStoryBoard(c, schematicPath, storyBoard)));
			return this;
		}

	}

	public static class PonderSceneBuilder {

		private final PonderStoryBoardEntry entry;

		PonderSceneBuilder(PonderStoryBoardEntry entry) {
			this.entry = entry;
		}

		public PonderSceneBuilder highlightAllTags() {
			entry.getTags()
				.add(PonderTag.Highlight.ALL);
			return this;
		}

		public PonderSceneBuilder highlightTags(PonderTag... tags) {
			entry.getTags()
				.addAll(Arrays.asList(tags));
			return this;
		}

		public PonderSceneBuilder chapter(PonderChapter chapter) {
			PonderRegistry.chapters.addStoriesToChapter(chapter, entry);
			return this;
		}

		public PonderSceneBuilder chapters(PonderChapter... chapters) {
			for (PonderChapter c : chapters)
				chapter(c);
			return this;
		}
	}

}
