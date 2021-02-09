package com.simibubi.create.foundation.metadoc;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.metadoc.stories.CogwheelStory;
import com.tterrag.registrate.util.entry.ItemProviderEntry;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;

public class MetaDocs {

	static Map<ResourceLocation, List<MetaDocStoryBoard>> all = new HashMap<>();

	public static void register() {

		addStoryBoard(AllBlocks.COGWHEEL, new CogwheelStory());

	}

	private static void addStoryBoard(ItemProviderEntry<?> component, MetaDocStoryBoard storyBoard) {
		ResourceLocation id = component.getId();
		all.computeIfAbsent(id, $ -> new ArrayList<>())
			.add(storyBoard);
	}

	public static List<MetaDocScene> compile(ResourceLocation id) {
		return all.get(id)
			.stream()
			.map(sb -> {
				Template activeTemplate = loadSchematic(sb.getSchematicName());
				MetaDocWorld world = new MetaDocWorld(BlockPos.ZERO, Minecraft.getInstance().world);
				activeTemplate.addBlocksToWorld(world, BlockPos.ZERO, new PlacementSettings());
				MetaDocScene scene = new MetaDocScene(world);
				sb.program(scene.builder(), world.getBounds()
					.getLength());
				scene.begin();
				return scene;
			})
			.collect(Collectors.toList());
	}

	public static Template loadSchematic(String path) {
		Template t = new Template();
		String filepath = "doc/" + path + ".nbt";
		try (DataInputStream stream =
			new DataInputStream(new BufferedInputStream(new GZIPInputStream(Create.class.getClassLoader()
				.getResourceAsStream(filepath))))) {
			CompoundNBT nbt = CompressedStreamTools.read(stream, new NBTSizeTracker(0x20000000L));
			t.read(nbt);
		} catch (IOException e) {
			Create.logger.warn("Failed to read metadoc schematic", e);
		}
		return t;
	}

}
