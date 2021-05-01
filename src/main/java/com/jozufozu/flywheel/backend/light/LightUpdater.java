package com.jozufozu.flywheel.backend.light;

import java.util.WeakHashMap;
import java.util.function.LongConsumer;

import com.simibubi.create.foundation.utility.WeakHashSet;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongRBTreeSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.LightType;

/**
 * By using WeakReferences we can automatically remove listeners when they are garbage collected.
 * This allows us to easily be more clever about how we store the listeners. Each listener is associated
 * with 2 sets of longs indicating what chunks and sections each listener is in. Additionally, a reverse
 * mapping is created to allow for fast lookups when light updates. The reverse mapping is more interesting,
 * but {@link #listenersToSections}, and {@link #listenersToChunks} are used to know what sections and
 * chunks we need to remove the listeners from if they re-subscribe. Otherwise, listeners could get updates
 * they no longer care about. This is done in {@link #clearSections} and {@link #clearChunks}
 */
public class LightUpdater {

	private static LightUpdater instance;

	public static LightUpdater getInstance() {
		if (instance == null)
			instance = new LightUpdater();

		return instance;
	}

	private final Long2ObjectMap<WeakHashSet<LightUpdateListener>> sections;
	private final WeakHashMap<LightUpdateListener, LongRBTreeSet> listenersToSections;

	private final Long2ObjectMap<WeakHashSet<LightUpdateListener>> chunks;
	private final WeakHashMap<LightUpdateListener, LongRBTreeSet> listenersToChunks;

	public LightUpdater() {
		sections = new Long2ObjectOpenHashMap<>();
		listenersToSections = new WeakHashMap<>();

		chunks = new Long2ObjectOpenHashMap<>();
		listenersToChunks = new WeakHashMap<>();
	}

	/**
	 * Add a listener associated with the given {@link BlockPos}.
	 * <p>
	 * When a light update occurs in the chunk the position is contained in,
	 * {@link LightUpdateListener#onLightUpdate} will be called.
	 *
	 * @param pos      The position in the world that the listener cares about.
	 * @param listener The object that wants to receive light update notifications.
	 */
	public void startListening(BlockPos pos, LightUpdateListener listener) {
		LongRBTreeSet sections = clearSections(listener);
		LongRBTreeSet chunks = clearChunks(listener);

		long sectionPos = worldToSection(pos);
		addToSection(sectionPos, listener);
		sections.add(sectionPos);

		long chunkPos = sectionToChunk(sectionPos);
		addToChunk(chunkPos, listener);
		chunks.add(chunkPos);
	}

	/**
	 * Add a listener associated with the given {@link GridAlignedBB}.
	 * <p>
	 * When a light update occurs in any chunk spanning the given volume,
	 * {@link LightUpdateListener#onLightUpdate} will be called.
	 *
	 * @param volume   The volume in the world that the listener cares about.
	 * @param listener The object that wants to receive light update notifications.
	 */
	public void startListening(GridAlignedBB volume, LightUpdateListener listener) {
		LongRBTreeSet sections = clearSections(listener);
		LongRBTreeSet chunks = clearSections(listener);

		int minX = SectionPos.toChunk(volume.minX);
		int minY = SectionPos.toChunk(volume.minY);
		int minZ = SectionPos.toChunk(volume.minZ);
		int maxX = SectionPos.toChunk(volume.maxX);
		int maxY = SectionPos.toChunk(volume.maxY);
		int maxZ = SectionPos.toChunk(volume.maxZ);

		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				for (int y = minY; y <= maxY; y++) {
					long sectionPos = SectionPos.asLong(x, y, z);
					addToSection(sectionPos, listener);
					sections.add(sectionPos);
				}
				long chunkPos = SectionPos.asLong(x, 0, z);
				addToChunk(chunkPos, listener);
				chunks.add(chunkPos);
			}
		}
	}

	/**
	 * Dispatch light updates to all registered {@link LightUpdateListener}s.
	 *
	 * @param world      The world in which light was updated.
	 * @param type       The type of light that changed.
	 * @param sectionPos A long representing the section position where light changed.
	 */
	public void onLightUpdate(IBlockDisplayReader world, LightType type, long sectionPos) {
		WeakHashSet<LightUpdateListener> set = sections.get(sectionPos);

		if (set == null || set.isEmpty()) return;

		GridAlignedBB chunkBox = GridAlignedBB.from(SectionPos.from(sectionPos));

		set.removeIf(listener -> listener.onLightUpdate(world, type, chunkBox.copy()));
	}

	/**
	 * Dispatch light updates to all registered {@link LightUpdateListener}s
	 * when the server sends lighting data for an entire chunk.
	 *
	 * @param world The world in which light was updated.
	 */
	public void onLightPacket(IBlockDisplayReader world, int chunkX, int chunkZ) {

		long chunkPos = SectionPos.asLong(chunkX, 0, chunkZ);

		WeakHashSet<LightUpdateListener> set = chunks.get(chunkPos);

		if (set == null || set.isEmpty()) return;

		set.removeIf(listener -> listener.onLightPacket(world, chunkX, chunkZ));
	}

	private LongRBTreeSet clearChunks(LightUpdateListener listener) {
		return clear(listener, listenersToChunks, chunks);
	}

	private LongRBTreeSet clearSections(LightUpdateListener listener) {
		return clear(listener, listenersToSections, sections);
	}

	private LongRBTreeSet clear(LightUpdateListener listener, WeakHashMap<LightUpdateListener, LongRBTreeSet> listeners, Long2ObjectMap<WeakHashSet<LightUpdateListener>> lookup) {
		LongRBTreeSet set = listeners.get(listener);

		if (set == null) {
			set = new LongRBTreeSet();
			listeners.put(listener, set);
		} else {
			set.forEach((LongConsumer) l -> {
				WeakHashSet<LightUpdateListener> listeningSections = lookup.get(l);

				if (listeningSections != null) listeningSections.remove(listener);
			});

			set.clear();
		}

		return set;
	}

	private void addToSection(long sectionPos, LightUpdateListener listener) {
		getOrCreate(sections, sectionPos).add(listener);
	}

	private void addToChunk(long chunkPos, LightUpdateListener listener) {
		getOrCreate(chunks, chunkPos).add(listener);
	}

	private WeakHashSet<LightUpdateListener> getOrCreate(Long2ObjectMap<WeakHashSet<LightUpdateListener>> sections, long chunkPos) {
		WeakHashSet<LightUpdateListener> set = sections.get(chunkPos);

		if (set == null) {
			set = new WeakHashSet<>();
			sections.put(chunkPos, set);
		}

		return set;
	}

	public static long worldToSection(BlockPos pos) {
		return SectionPos.asLong(pos.getX(), pos.getY(), pos.getZ());
	}

	public static long sectionToChunk(long sectionPos) {
		return sectionPos & 0xFFFFFFFFFFF_00000L;
	}
}
