package com.simibubi.create.content.contraptions.actors.trainControls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.foundation.utility.IntAttached;
import com.simibubi.create.foundation.utility.WorldAttached;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;

public class ControlsServerHandler {

	public static WorldAttached<Map<UUID, ControlsContext>> receivedInputs = new WorldAttached<>($ -> new HashMap<>());
	static final int TIMEOUT = 30;

	public static void tick(LevelAccessor world) {
		Map<UUID, ControlsContext> map = receivedInputs.get(world);
		for (Iterator<Entry<UUID, ControlsContext>> iterator = map.entrySet()
			.iterator(); iterator.hasNext();) {

			Entry<UUID, ControlsContext> entry = iterator.next();
			ControlsContext ctx = entry.getValue();
			Collection<ManuallyPressedKey> list = ctx.keys;

			if (ctx.entity.isRemoved()) {
				iterator.remove();
				continue;
			}

			for (Iterator<ManuallyPressedKey> entryIterator = list.iterator(); entryIterator.hasNext();) {
				ManuallyPressedKey pressedKey = entryIterator.next();
				pressedKey.decrement();
				if (!pressedKey.isAlive())
					entryIterator.remove(); // key released
			}

			Player player = world.getPlayerByUUID(entry.getKey());
			if (player == null) {
				ctx.entity.stopControlling(ctx.controlsLocalPos);
				iterator.remove();
				continue;
			}

			if (!ctx.entity.control(ctx.controlsLocalPos, list.stream()
				.map(ManuallyPressedKey::getSecond)
				.toList(), player)) {
				ctx.entity.stopControlling(ctx.controlsLocalPos);
			}

			if (list.isEmpty())
				iterator.remove();
		}
	}

	public static void receivePressed(LevelAccessor world, AbstractContraptionEntity entity, BlockPos controlsPos,
		UUID uniqueID, Collection<Integer> collect, boolean pressed) {
		Map<UUID, ControlsContext> map = receivedInputs.get(world);

		if (map.containsKey(uniqueID) && map.get(uniqueID).entity != entity)
			map.remove(uniqueID);

		ControlsContext ctx = map.computeIfAbsent(uniqueID, $ -> new ControlsContext(entity, controlsPos));
		Collection<ManuallyPressedKey> list = ctx.keys;

		WithNext: for (Integer activated : collect) {
			for (Iterator<ManuallyPressedKey> iterator = list.iterator(); iterator.hasNext();) {
				ManuallyPressedKey entry = iterator.next();
				Integer inputType = entry.getSecond();
				if (inputType.equals(activated)) {
					if (!pressed)
						entry.setFirst(0);
					else
						entry.keepAlive();
					continue WithNext;
				}
			}

			if (!pressed)
				continue;

			list.add(new ManuallyPressedKey(activated)); // key newly pressed
		}
	}

	static class ControlsContext {

		Collection<ManuallyPressedKey> keys;
		AbstractContraptionEntity entity;
		BlockPos controlsLocalPos;

		public ControlsContext(AbstractContraptionEntity entity, BlockPos controlsPos) {
			this.entity = entity;
			controlsLocalPos = controlsPos;
			keys = new ArrayList<>();
		}

	}

	static class ManuallyPressedKey extends IntAttached<Integer> {

		public ManuallyPressedKey(Integer second) {
			super(TIMEOUT, second);
		}

		public void keepAlive() {
			setFirst(TIMEOUT);
		}

		public boolean isAlive() {
			return getFirst() > 0;
		}

	}

}
