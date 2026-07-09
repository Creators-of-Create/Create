package com.simibubi.create.content.fluids.tank;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;

/**
 * One person walking sounds like one person walking, and you can easily distinguish where they are.
 *
 * <br>With two people walking, you can still pick out which footsteps belong to which person.
 *
 * <br>Try and listen to three people walking in a group, however, and you'll find that you can't distinguish
 * individual footsteps anymore. You now just hear the sound of a group of people walking.
 *
 * <p>You'll likely find that you perceive any number of people walking in a group as a single distinguishable sound.
 * This class is a helper to take advantage of that for sound effects in Create to avoid saturating the sound engine
 * without a perceptible loss in quality.
 *
 * <p>NOTE: It's up to the user of this class to decide how to group sounds such that they are perceived as a single
 * sound. There are no spatial calculations made here.
 */
public class SoundPool {
	/**
	 * The maximum number of sounds that can be played at once.
	 */
	private final int maxConcurrent;
	/**
	 * The number of ticks to wait before playing sounds. Useful if sounds are queued across many block entities,
	 * and you don't have control over the tick order.
	 */
	private final int mergeTicks;

	private final Sound sound;

	private final LongList queuedPositions = new LongArrayList();

	private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

	private int ticks = 0;

	public SoundPool(int maxConcurrent, int mergeTicks, Sound sound) {
		this.maxConcurrent = maxConcurrent;
		this.sound = sound;
		this.mergeTicks = mergeTicks;
	}

	public void queueAt(BlockPos pos) {
		queueAt(pos.asLong());
	}

	public void queueAt(long pos) {
		queuedPositions.add(pos);
	}

	public void play(Level level) {
		if (queuedPositions.isEmpty()) {
			return;
		}

		ticks++;

		if (ticks < mergeTicks) {
			// Wait for more sounds to be queued in further ticks.
			return;
		}

		ticks = 0;

		var numberOfPositions = queuedPositions.size();

		if (numberOfPositions <= maxConcurrent) {
			// Fewer sound positions than maxConcurrent, play them all.
			for (long pos : queuedPositions) {
				playAt(level, pos);
			}
		} else {
			// Roll for n random positions and play there.
			while (!queuedPositions.isEmpty() && queuedPositions.size() > numberOfPositions - maxConcurrent) {
				rollNextPosition(level);
			}
		}

		queuedPositions.clear();
	}

	private void rollNextPosition(Level level) {
		int index = level.getRandom().nextInt(queuedPositions.size());
		long pos = queuedPositions.removeLong(index);
		playAt(level, pos);
	}

	private void playAt(Level level, long pos) {
		sound.playAt(level, this.pos.set(pos));
	}

	public interface Sound {
		void playAt(Level level, Vec3i pos);
	}
}
