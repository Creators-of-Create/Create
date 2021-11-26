package com.simibubi.create.foundation.sound;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import com.mojang.datafixers.util.Either;
import com.simibubi.create.Create;

import net.minecraft.core.Vec3i;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.data.SoundDefinition;


/**
 * Wrapper for polyphonic sounds. Many sounds can be added to one sfx, but event[0] becomes a representative.
 * Polyphonic sounds get 'create.polyphonic.*.[idx]' id and only `create.polyphonic.*.0` hold subtitle key (if any).
 */
public class Sfx {
	public final String id;
	public final SoundSource category;
	protected final SoundEvent[] events;

	@Override
	public String toString() { return id + (events.length > 1 ? " (" + events.length + "-polyphonic)" : "");	}

	protected Sfx(String id, SoundSource category, SoundEvent[] events) {
		this.id = id;
		this.category = category;
		this.events = events;
	}

	// play forms =============================
	// taken from previous AllSoundEvents, but abstracted the concrete play and playAt to loop

	public void playOnServer(Level world, Vec3i pos) { playOnServer(world, pos, 1, 1); }
	public void playOnServer(Level world, Vec3i pos, float volume, float pitch) { play(world, null, pos, volume, pitch); }
	public void play(Level world, Player entity, Vec3i pos) { play(world, entity, pos, 1, 1); }
	public void playFrom(Entity entity) { playFrom(entity, 1, 1); }
	public void playFrom(Entity entity, float volume, float pitch) {
		if (!entity.isSilent())
			play(entity.level, null, entity.blockPosition(), volume, pitch);
	}

	public void play(Level world, Player entity, Vec3i pos, float volume, float pitch) { play(world, entity, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, volume, pitch); }
	public void play(Level world, Player entity, Vec3 pos, float volume, float pitch) { play(world, entity, pos.x(), pos.y(), pos.z(), volume, pitch); }
	public void play(Level world, Player entity, double x, double y, double z, float volume, float pitch) {
		for (SoundEvent event : events)
			world.playSound(entity, x, y, z, event, category, volume, pitch);
	}
	public void playAt(Level world, Vec3i pos, float volume, float pitch, boolean fade) { playAt(world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, volume, pitch, fade); }
	public void playAt(Level world, Vec3 pos, float volume, float pitch, boolean fade) { playAt(world, pos.x(), pos.y(), pos.z(), volume, pitch, fade); }
	public void playAt(Level world, double x, double y, double z, float volume, float pitch, boolean fade) {
		for (SoundEvent event : events)
			world.playLocalSound(x, y, z, event, category, volume, pitch, fade);
	}
	// end of play forms =============================

	public SoundEvent[] getEvents() { return events; }

	// in the future it would be better to either:
	// - mix proper .ogg assets instead of playing several sounds at the same time
	// - mixin some way for method expecting SoundEvent to accept Sfx
	@Deprecated(forRemoval = true)
	public SoundEvent getMainEvent() { return events[0]; }

	public static class Mixer {
		public final String id;
		public final SoundSource category;
		public final Either<String, Boolean> subtitle;
		public final List<SoundDefinition> definitions = new ArrayList<>();

		/**
		 * Create builder for sound with given id, category,sound and implicit subtitles
		 */
		public Mixer(String id, SoundSource category) {
			this(id, Either.right(true), category);
		}

		/**
		 * Make a sound effect from a mix of simple sounds
		 *
		 * @param id       ID used for sound events json file and implicit subtitle keys
		 * @param subtitle use like `Either.left('sub.key')` or `Either.right(false|true)` to disable/auto generate key
		 * @param category category for the sound
		 */
		public Mixer(String id, Either<String, Boolean> subtitle, SoundSource category) {
			this.id = id;
			this.category = category;
			this.subtitle = subtitle;
		}

		/**
		 * Adds a single track to the Mixer. All the tracks will be played at the same time later
		 * @param variants single track may have several variants
		 * @return mixer for chaining.
		 */
		public Mixer track(SoundDefinition.Sound... variants) {
			SoundDefinition definition = SoundDefinition.definition();
			definition.with(variants);
			this.definitions.add(definition);
			return this;
		}

		/**
		 * Creates concrete Sfx sound from this given prototype and calls deferredProvider
		 * using events and sound definitions for all partial sounds
		 *
		 * @param registrar function that will perform registration of sound event -> sound definition
		 */
		public Sfx mix(BiConsumer<SoundEvent, SoundDefinition> registrar) {

			subtitle.ifLeft(customKey -> definitions.get(0).subtitle(Create.ID + ".subtitle." + customKey));
			subtitle.ifRight(hasSub -> definitions.get(0).subtitle(hasSub ? Create.ID + ".subtitle." + id : null));

			boolean polyphonic = definitions.size() > 1;
			List<SoundEvent> events = new ArrayList<>();
			AtomicInteger index = new AtomicInteger(0);
			for (SoundDefinition definition : definitions) {
				// create new event for partial sound with id from Sfx and `.polyphonic.i` suffix if more sounds than 1
				SoundEvent event = new SoundEvent(Create.asResource(
						polyphonic
								? id + ".track-" + index.getAndIncrement()
								: id
				));
				events.add(event);
				registrar.accept(event, definition);
			}
			return new Sfx(id, category, events.toArray(new SoundEvent[0]));
		}
	}
}
