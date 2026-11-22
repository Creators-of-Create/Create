package com.simibubi.create.content.kinetics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;

/**
 * Checks that the stress, capacity and size of a kinetic network are calculated and synced correctly.
 */
class KineticNetworkTest {

	private final FakeLevel level = FakeLevel.create();

	private FakeKineticBlockEntity place(int x, float speed, float generated, float impact, float capacity) {
		FakeKineticBlockEntity be = new FakeKineticBlockEntity(new BlockPos(x, 0, 0), speed, generated, impact, capacity);
		be.setLevel(level);
		level.putBlockEntity(be);
		return be;
	}

	// Same as the network part of KineticBlockEntity#tick
	private static void tick(KineticNetwork network, List<FakeKineticBlockEntity> all) {
		for (FakeKineticBlockEntity be : all) {
			if (be.networkDirty) {
				network.updateNetwork();
				be.networkDirty = false;
			}
		}
	}

	@Test
	void reconnectKeepsTotalsAndSyncsNewMachine() {
		KineticNetwork network = new KineticNetwork();
		List<FakeKineticBlockEntity> all = new ArrayList<>();
		all.add(place(0, 32, 32, 0, 256));
		for (int i = 1; i < 50; i++)
			all.add(place(i, 32, 0, 0, 0));
		all.forEach(network::add);
		tick(network, all);
		assertEquals(256 * 32, network.calculateCapacity());
		assertEquals(0, network.calculateStress());

		for (FakeKineticBlockEntity be : all.subList(1, all.size()))
			network.remove(be);
		for (FakeKineticBlockEntity be : all.subList(1, all.size()))
			network.add(be);
		tick(network, all);
		all.forEach(be -> assertFalse(be.networkDirty));

		FakeKineticBlockEntity press = place(100, 32, 0, 8, 0);
		all.add(press);
		network.add(press);
		tick(network, all);
		for (FakeKineticBlockEntity be : all) {
			assertEquals(256 * 32, be.receivedCapacity);
			assertEquals(8 * 32, be.receivedStress);
			assertEquals(51, be.receivedNetworkSize);
		}
	}

	@Test
	void removedBlockEntityIsDroppedOnNextUpdate() {
		KineticNetwork network = new KineticNetwork();
		FakeKineticBlockEntity motor = place(0, 32, 32, 0, 256);
		FakeKineticBlockEntity press = place(1, 32, 0, 8, 0);
		List<FakeKineticBlockEntity> all = List.of(motor, press);
		all.forEach(network::add);
		tick(network, all);
		assertEquals(8 * 32, motor.receivedStress);

		level.removeBlockEntity(press.getBlockPos());
		FakeKineticBlockEntity shaft = place(2, 32, 0, 0, 0);
		network.add(shaft);
		tick(network, List.of(motor, shaft));
		assertEquals(0, motor.receivedStress);
		assertEquals(2, network.getSize());
	}
}
