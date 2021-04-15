package com.simibubi.create;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

//@EventBusSubscriber(bus = Bus.FORGE)
public class AllSoundEvents {

	public static Map<ResourceLocation, SoundEntry> entries = Maps.newHashMap();
	public static final SoundEntry

	SCHEMATICANNON_LAUNCH_BLOCK = create("schematicannon_launch_block").subtitle("Schematicannon fires")
		.playExisting(SoundEvents.ENTITY_GENERIC_EXPLODE, .1f, 1.1f)
		.category(SoundCategory.BLOCKS)
		.build(),

		SCHEMATICANNON_FINISH = create("schematicannon_finish").subtitle("Schematicannon dings")
			.playExisting(SoundEvents.BLOCK_NOTE_BLOCK_BELL, 1, .7f)
			.category(SoundCategory.BLOCKS)
			.build(),

		SLIME_ADDED = create("slime_added").subtitle("Slime squishes")
			.playExisting(SoundEvents.BLOCK_SLIME_BLOCK_PLACE)
			.category(SoundCategory.BLOCKS)
			.build(),

		MECHANICAL_PRESS_ACTIVATION = create("mechanical_press_activation").subtitle("Mechanical Press clangs")
			.playExisting(SoundEvents.BLOCK_ANVIL_LAND, .125f, 1f)
			.playExisting(SoundEvents.ENTITY_ITEM_BREAK, .5f, 1f)
			.category(SoundCategory.BLOCKS)
			.build(),

		MECHANICAL_PRESS_ACTIVATION_ON_BELT =
			create("mechanical_press_activation_belt").subtitle("Mechanical Press bonks")
				.playExisting(SoundEvents.BLOCK_WOOL_HIT, .75f, 1f)
				.playExisting(SoundEvents.ENTITY_ITEM_BREAK, .15f, .75f)
				.category(SoundCategory.BLOCKS)
				.build(),

		BLOCKZAPPER_PLACE = create("blockzapper_place").subtitle("Blockzapper zaps")
			.playExisting(SoundEvents.BLOCK_NOTE_BLOCK_BASEDRUM)
			.category(SoundCategory.PLAYERS)
			.build(),

		BLOCKZAPPER_CONFIRM = create("blockzapper_confirm").subtitle("Affirmative ding")
			.playExisting(SoundEvents.BLOCK_NOTE_BLOCK_BELL, 0.5f, 0.8f)
			.category(SoundCategory.PLAYERS)
			.build(),

		BLOCKZAPPER_DENY = create("blockzapper_deny").subtitle("Declining boop")
			.playExisting(SoundEvents.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f)
			.category(SoundCategory.PLAYERS)
			.build(),

		BLAZE_MUNCH = create("blaze_munch").subtitle("Blaze Burner munches")
			.playExisting(SoundEvents.ENTITY_GENERIC_EAT, .5f, 1f)
			.category(SoundCategory.BLOCKS)
			.build();

	public static SoundEntryBuilder create(String id) {
		return new SoundEntryBuilder(id);
	}

	public static void register(RegistryEvent.Register<SoundEvent> event) {
		IForgeRegistry<SoundEvent> registry = event.getRegistry();
		for (SoundEntry entry : entries.values())
			entry.register(registry);
	}

	public static JsonElement provideLangEntries() {
		JsonObject object = new JsonObject();
		for (SoundEntry entry : entries.values())
			object.addProperty(entry.getSubtitleKey(), entry.getSubtitle());
		return object;
	}

	public static SoundEntryProvider provider(DataGenerator generator) {
		return new SoundEntryProvider(generator);
	}

//	@SubscribeEvent
//	public static void cancelSubtitlesOfCompoundedSounds(PlaySoundEvent event) {
//		ResourceLocation soundLocation = event.getSound().getSoundLocation();
//		if (!soundLocation.getNamespace().equals(Create.ID))
//			return;
//		if (soundLocation.getPath().contains("_compounded_")
//			event.setResultSound();
//		
//	}

	private static class SoundEntryProvider implements IDataProvider {

		private DataGenerator generator;

		public SoundEntryProvider(DataGenerator generator) {
			this.generator = generator;
		}

		@Override
		public void act(DirectoryCache cache) throws IOException {
			generate(generator.getOutputFolder(), cache);
		}

		@Override
		public String getName() {
			return "Create's Custom Sounds";
		}

		public void generate(Path path, DirectoryCache cache) {
			Gson GSON = (new GsonBuilder()).setPrettyPrinting()
				.disableHtmlEscaping()
				.create();
			path = path.resolve("assets/create");

			try {
				JsonObject json = new JsonObject();
				entries.entrySet()
					.stream()
					.sorted(Map.Entry.comparingByKey())
					.forEach(entry -> {
						entry.getValue()
							.write(json);
					});
				IDataProvider.save(GSON, cache, json, path.resolve("sounds.json"));

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	static class SoundEntryBuilder {

		protected String id;
		protected String subtitle = "unregistered";
		protected SoundCategory category = SoundCategory.BLOCKS;
		List<Pair<SoundEvent, Couple<Float>>> wrappedEvents;

		public SoundEntryBuilder(String id) {
			wrappedEvents = Lists.newArrayList();
			this.id = id;
		}

		public SoundEntryBuilder subtitle(String subtitle) {
			this.subtitle = subtitle;
			return this;
		}

		public SoundEntryBuilder category(SoundCategory category) {
			this.category = category;
			return this;
		}

		public SoundEntryBuilder playExisting(SoundEvent event, float volume, float pitch) {
			wrappedEvents.add(Pair.of(event, Couple.create(volume, pitch)));
			return this;
		}

		public SoundEntryBuilder playExisting(SoundEvent event) {
			return playExisting(event, 1, 1);
		}

		public SoundEntry build() {
			SoundEntry entry = wrappedEvents.isEmpty() ? new CustomSoundEntry(id, subtitle, category)
				: new WrappedSoundEntry(id, subtitle, wrappedEvents, category);
			entries.put(entry.getLocation(), entry);
			return entry;
		}

	}

	public static abstract class SoundEntry {

		protected String id;
		protected String subtitle;
		protected SoundCategory category;

		public SoundEntry(String id, String subtitle, SoundCategory category) {
			this.id = id;
			this.subtitle = subtitle;
			this.category = category;
		}

		public abstract void register(IForgeRegistry<SoundEvent> registry);

		public abstract void write(JsonObject json);

		public String getSubtitleKey() {
			return Create.ID + ".subtitle." + id;
		}

		public String getId() {
			return id;
		}

		public ResourceLocation getLocation() {
			return Create.asResource(id);
		}

		public String getSubtitle() {
			return subtitle;
		}

		public void playOnServer(World world, BlockPos pos) {
			playOnServer(world, pos, 1, 1);
		}

		public void playOnServer(World world, BlockPos pos, float volume, float pitch) {
			play(world, null, pos, volume, pitch);
		}

		public void play(World world, PlayerEntity entity, BlockPos pos) {
			play(world, entity, pos, 1, 1);
		}

		public void playFrom(Entity entity) {
			playFrom(entity, 1, 1);
		}

		public void playFrom(Entity entity, float volume, float pitch) {
			if (!entity.isSilent())
				play(entity.world, null, entity.getBlockPos(), volume, pitch);
		}

		public void play(World world, PlayerEntity entity, BlockPos pos, float volume, float pitch) {
			play(world, entity, pos.getX(), pos.getY(), pos.getZ(), volume, pitch);
		}

		abstract void play(World world, PlayerEntity entity, double x, double y, double z, float volume, float pitch);

	}

	static class WrappedSoundEntry extends SoundEntry {

		private List<Pair<SoundEvent, Couple<Float>>> wrappedEvents;
		private List<Pair<SoundEvent, Couple<Float>>> compiledEvents;

		public WrappedSoundEntry(String id, String subtitle, List<Pair<SoundEvent, Couple<Float>>> wrappedEvents,
			SoundCategory category) {
			super(id, subtitle, category);
			this.wrappedEvents = wrappedEvents;
			compiledEvents = Lists.newArrayList();
		}

		@Override
		public void register(IForgeRegistry<SoundEvent> registry) {
			for (int i = 0; i < wrappedEvents.size(); i++) {
				ResourceLocation location = Create.asResource(getIdOf(i));
				SoundEvent sound = new SoundEvent(location).setRegistryName(location);
				registry.register(sound);
				compiledEvents.add(Pair.of(sound, wrappedEvents.get(i)
					.getSecond()));
			}
		}

		protected String getIdOf(int i) {
			return i == 0 ? id : id + "_compounded_" + i;
		}

		@Override
		public void write(JsonObject json) {
			for (int i = 0; i < wrappedEvents.size(); i++) {
				Pair<SoundEvent, Couple<Float>> pair = wrappedEvents.get(i);
				JsonObject entry = new JsonObject();
				JsonArray list = new JsonArray();
				JsonObject s = new JsonObject();
				s.addProperty("name", pair.getFirst()
					.getName()
					.toString());
				s.addProperty("type", "event");
				list.add(s);
				entry.add("sounds", list);
				if (i == 0)
					entry.addProperty("subtitle", getSubtitleKey());
				json.add(getIdOf(i), entry);
			}
		}

		@Override
		void play(World world, PlayerEntity entity, double x, double y, double z, float volume, float pitch) {
			for (Pair<SoundEvent, Couple<Float>> pair : compiledEvents) {
				Couple<Float> volPitch = pair.getSecond();
				world.playSound(entity, x, y, z, pair.getFirst(), category, volPitch.getFirst() * volume,
					volPitch.getSecond() * pitch);
			}
		}
	}

	static class CustomSoundEntry extends SoundEntry {

		protected SoundEvent event;

		public CustomSoundEntry(String id, String subtitle, SoundCategory category) {
			super(id, subtitle, category);
		}

		@Override
		public void register(IForgeRegistry<SoundEvent> registry) {
			ResourceLocation location = getLocation();
			SoundEvent sound = new SoundEvent(location).setRegistryName(location);
			registry.register(sound);
		}

		@Override
		public void write(JsonObject json) {
			JsonObject entry = new JsonObject();
			JsonArray list = new JsonArray();
			list.add(getLocation().toString());
			entry.add("sounds", list);
			entry.addProperty("subtitle", getSubtitleKey());
			json.add(id, entry);
		}

		@Override
		void play(World world, PlayerEntity entity, double x, double y, double z, float volume, float pitch) {
			world.playSound(entity, x, y, z, event, category, volume, pitch);
		}

	}

}
