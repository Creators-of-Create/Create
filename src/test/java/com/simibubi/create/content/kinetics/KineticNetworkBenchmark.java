package com.simibubi.create.content.kinetics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import net.minecraft.core.BlockPos;

/**
 * Simulates the server tick right after a long line of shafts has been reconnected to a running motor (e.g. a
 * broken shaft was replaced): every shaft is removed from and re-added to the existing network and is marked
 * dirty, then every member ticks once. The network totals do not change, since shafts apply no stress. The tick
 * logic is copied from {@code KineticBlockEntity#tick}.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(1)
public class KineticNetworkBenchmark {

	@Param({"100", "1000", "5000"})
	public int members;

	private List<FakeKineticBlockEntity> blockEntities;
	private List<FakeKineticBlockEntity> shafts;
	private KineticNetwork network;

	@Setup(org.openjdk.jmh.annotations.Level.Trial)
	public void createWorld() {
		FakeLevel level = FakeLevel.create();

		blockEntities = new ArrayList<>(members);
		// One motor driving a long line of shafts (shafts apply no stress)
		for (int i = 0; i < members; i++) {
			BlockPos pos = new BlockPos(i, 64, 0);
			FakeKineticBlockEntity be = i == 0
				? new FakeKineticBlockEntity(pos, 32, 32, 0, 256)
				: new FakeKineticBlockEntity(pos, 32, 0, 0, 0);
			be.setLevel(level);
			level.putBlockEntity(be);
			blockEntities.add(be);
		}
		shafts = blockEntities.subList(1, members);

		network = new KineticNetwork();
		network.initialized = true;
		for (FakeKineticBlockEntity be : blockEntities)
			network.add(be);
		network.updateNetwork();
	}

	@Setup(org.openjdk.jmh.annotations.Level.Invocation)
	public void reconnectShafts() {
		for (FakeKineticBlockEntity be : shafts)
			network.remove(be);
		for (FakeKineticBlockEntity be : shafts)
			network.add(be);
	}

	@Benchmark
	public KineticNetwork tickAfterReconnect() {
		for (FakeKineticBlockEntity be : blockEntities) {
			if (be.networkDirty) {
				network.updateNetwork();
				be.networkDirty = false;
			}
		}
		return network;
	}
}
