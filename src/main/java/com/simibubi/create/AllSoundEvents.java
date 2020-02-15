package com.simibubi.create;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.simibubi.create.foundation.utility.data.ICanGenerateJson;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.io.IOException;
import java.nio.file.Path;


public enum AllSoundEvents implements ICanGenerateJson {

	CUCKOO_PIG("creeperclock"),
	CUCKOO_CREEPER("pigclock"),

	SCHEMATICANNON_LAUNCH_BLOCK(SoundEvents.ENTITY_GENERIC_EXPLODE),
	SCHEMATICANNON_FINISH(SoundEvents.BLOCK_NOTE_BLOCK_BELL),
	SLIME_ADDED(SoundEvents.BLOCK_SLIME_BLOCK_PLACE),
	MECHANICAL_PRESS_ACTIVATION(SoundEvents.BLOCK_ANVIL_LAND),
	MECHANICAL_PRESS_ITEM_BREAK(SoundEvents.ENTITY_ITEM_BREAK),
	BLOCKZAPPER_PLACE(SoundEvents.BLOCK_NOTE_BLOCK_BASEDRUM),
	BLOCKZAPPER_CONFIRM(SoundEvents.BLOCK_NOTE_BLOCK_BELL),
	BLOCKZAPPER_DENY(SoundEvents.BLOCK_NOTE_BLOCK_BASS),
	BLOCK_FUNNEL_EAT(SoundEvents.ENTITY_GENERIC_EAT),


	;

	String id;
	SoundEvent event, child;

	//For adding our own sounds at assets/create/sounds/name.ogg
	AllSoundEvents(){
		id = name().toLowerCase();
	}

	AllSoundEvents(String name){
		id = name;
	}

	//For wrapping a existing sound with new subtitle
	AllSoundEvents(SoundEvent child){
		id = name().toLowerCase();
		this.child = child;
	}

	//subtitles are taken from the lang file (create.subtitle.sound_event_name)

	public SoundEvent get(){
		return event;
	}

	private String getName(){
		return id;
	}

	public static void register(RegistryEvent.Register<SoundEvent> event) {
		IForgeRegistry<SoundEvent> registry = event.getRegistry();

		for (AllSoundEvents entry :
				values()) {

			ResourceLocation rec = new ResourceLocation(Create.ID, entry.getName());
			SoundEvent sound = new SoundEvent(rec).setRegistryName(rec);
			registry.register(sound);
			entry.event = sound;
		}
	}

	public void generate(Path path, DirectoryCache cache){
		Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
		path = path.resolve("assets/create");

		try {
			JsonObject json = new JsonObject();
			for (AllSoundEvents soundEvent :
					values()) {
				JsonObject entry = new JsonObject();
				JsonArray arr = new JsonArray();
				if (soundEvent.child != null){
					//wrapper
					JsonObject s = new JsonObject();
					s.addProperty("name", soundEvent.child.getName().toString());
					s.addProperty("type", "event");
					arr.add(s);
				} else{
					//own sound
					arr.add(Create.ID + ":" + soundEvent.getName());
				}
				entry.add("sounds", arr);
				entry.addProperty("subtitle", Create.ID + ".subtitle." + soundEvent.getName());
				json.add(soundEvent.getName(), entry);
			}
			IDataProvider.save(GSON, cache, json, path.resolve("sounds.json"));

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
