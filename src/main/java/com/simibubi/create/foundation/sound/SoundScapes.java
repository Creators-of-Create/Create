package com.simibubi.create.foundation.sound;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;

public class SoundScapes {

	static final int MAX_AMBIENT_SOURCE_DISTANCE = 16;
	static final int UPDATE_INTERVAL = 5;
	static final int SOUND_VOLUME_ARG_MAX = 15;

	public enum AmbienceGroup {

		KINETIC(SoundScapes::kinetic),
		COG(SoundScapes::cogwheel),
		CRUSHING(SoundScapes::crushing),
		MILLING(SoundScapes::milling),

		;

		private BiFunction<Float, AmbienceGroup, SoundScape> factory;

		private AmbienceGroup(BiFunction<Float, AmbienceGroup, SoundScape> factory) {
			this.factory = factory;
		}

		public SoundScape instantiate(float pitch) {
			return factory.apply(pitch, this);
		}

	}

	private static SoundScape kinetic(float pitch, AmbienceGroup group) {
		return new SoundScape(pitch, group).continuous(SoundEvents.MINECART_INSIDE, .25f, 1);
	}

	private static SoundScape cogwheel(float pitch, AmbienceGroup group) {
		return new SoundScape(pitch, group).continuous(AllSoundEvents.COGS.getMainEvent(), 1.5f, 1);
	}

	private static SoundScape crushing(float pitch, AmbienceGroup group) {
		return new SoundScape(pitch, group).repeating(AllSoundEvents.CRUSHING_1.getMainEvent(), 1.545f, .75f, 1)
			.repeating(AllSoundEvents.CRUSHING_2.getMainEvent(), 0.425f, .75f, 2)
			.repeating(AllSoundEvents.CRUSHING_3.getMainEvent(), 2f, 1.75f, 2);
	}
	
	private static SoundScape milling(float pitch, AmbienceGroup group) {
		return new SoundScape(pitch, group).repeating(AllSoundEvents.CRUSHING_1.getMainEvent(), 1.545f, .75f, 1)
			.repeating(AllSoundEvents.CRUSHING_2.getMainEvent(), 0.425f, .75f, 2);
	}

	enum PitchGroup {
		VERY_LOW, LOW, NORMAL, HIGH, VERY_HIGH
	}

	private static Map<AmbienceGroup, Map<PitchGroup, Set<BlockPos>>> counter = new IdentityHashMap<>();
	private static Map<Pair<AmbienceGroup, PitchGroup>, SoundScape> activeSounds = new HashMap<>();

	public static void play(AmbienceGroup group, BlockPos pos, float pitch) {
		if (!AllConfigs.client().enableAmbientSounds.get())
			return;
		if (!outOfRange(pos))
			addSound(group, pos, pitch);
	}

	public static void tick() {
		activeSounds.values()
			.forEach(SoundScape::tick);

		if (AnimationTickHolder.getTicks() % UPDATE_INTERVAL != 0)
			return;

		boolean disable = !AllConfigs.client().enableAmbientSounds.get();
		for (Iterator<Entry<Pair<AmbienceGroup, PitchGroup>, SoundScape>> iterator = activeSounds.entrySet()
			.iterator(); iterator.hasNext();) {

			Entry<Pair<AmbienceGroup, PitchGroup>, SoundScape> entry = iterator.next();
			Pair<AmbienceGroup, PitchGroup> key = entry.getKey();
			SoundScape value = entry.getValue();

			if (disable || getSoundCount(key.getFirst(), key.getSecond()) == 0) {
				value.remove();
				iterator.remove();
			}
		}

		counter.values()
			.forEach(m -> m.values()
				.forEach(Set::clear));
	}

	private static void addSound(AmbienceGroup group, BlockPos pos, float pitch) {
		PitchGroup groupFromPitch = getGroupFromPitch(pitch);
		Set<BlockPos> set = counter.computeIfAbsent(group, ag -> new IdentityHashMap<>())
			.computeIfAbsent(groupFromPitch, pg -> new HashSet<>());
		set.add(pos);

		Pair<AmbienceGroup, PitchGroup> pair = Pair.of(group, groupFromPitch);
		activeSounds.computeIfAbsent(pair, $ -> {
			SoundScape soundScape = group.instantiate(pitch);
			soundScape.play();
			return soundScape;
		});
	}

	public static void invalidateAll() {
		counter.clear();
		activeSounds.forEach(($, sound) -> sound.remove());
		activeSounds.clear();
	}

	protected static boolean outOfRange(BlockPos pos) {
		return !getCameraPos().closerThan(pos, MAX_AMBIENT_SOURCE_DISTANCE);
	}

	protected static BlockPos getCameraPos() {
		Entity renderViewEntity = Minecraft.getInstance().cameraEntity;
		if (renderViewEntity == null)
			return BlockPos.ZERO;
		BlockPos playerLocation = renderViewEntity.blockPosition();
		return playerLocation;
	}

	public static int getSoundCount(AmbienceGroup group, PitchGroup pitchGroup) {
		return getAllLocations(group, pitchGroup).size();
	}

	public static Set<BlockPos> getAllLocations(AmbienceGroup group, PitchGroup pitchGroup) {
		return counter.getOrDefault(group, Collections.emptyMap())
			.getOrDefault(pitchGroup, Collections.emptySet());
	}

	public static PitchGroup getGroupFromPitch(float pitch) {
		if (pitch < .70)
			return PitchGroup.VERY_LOW;
		if (pitch < .90)
			return PitchGroup.LOW;
		if (pitch < 1.10)
			return PitchGroup.NORMAL;
		if (pitch < 1.30)
			return PitchGroup.HIGH;
		return PitchGroup.VERY_HIGH;
	}

}
