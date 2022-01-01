package com.simibubi.create.content.contraptions.solver;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KineticNode {

	private final KineticSolver solver;
	private @Nullable KineticTileEntity entity;

	private @Nullable KineticNode source;
	private KineticNetwork network;
	private float speedRatio = 1;
	private float speedCur;
	private float speedNext;

	private final BlockPos pos;
	private final KineticConnections connections;
	private float generatedSpeed;
	private float stressCapacity;
	private float stressImpact;
	private final boolean constantStress;

	public KineticNode(KineticSolver solver, KineticTileEntity entity) {
		this.solver = solver;
		this.entity = entity;

		this.pos = entity.getBlockPos();
		this.connections = entity.getConnections();
		this.generatedSpeed = entity.getGeneratedSpeed();
		this.stressImpact = entity.getStressImpact();
		this.stressCapacity = entity.getStressCapacity();
		this.constantStress = entity.isStressConstant();

		this.network = new KineticNetwork(this);
	}

	private KineticNode(KineticSolver solver, BlockPos pos, KineticConnections connections, float generatedSpeed,
						float stressCapacity, float stressImpact, boolean constantStress) {
		this.solver = solver;

		this.pos = pos;
		this.connections = connections;
		this.generatedSpeed = generatedSpeed;
		this.stressImpact = stressImpact;
		this.stressCapacity = stressCapacity;
		this.constantStress = constantStress;

		this.network = new KineticNetwork(this);
	}

	public CompoundTag save(CompoundTag tag) {
		tag.put("Pos", NbtUtils.writeBlockPos(pos));
		tag.put("Connections", connections.save(new CompoundTag()));
		tag.putFloat("Generated", generatedSpeed);
		tag.putFloat("Capacity", stressCapacity);
		tag.putFloat("Impact", stressImpact);
		if (constantStress)
			tag.putBoolean("ConstantStress", true);
		return tag;
	}

	public static KineticNode load(KineticSolver solver, CompoundTag tag) {
		BlockPos pos = NbtUtils.readBlockPos(tag.getCompound("Pos"));
		KineticConnections connections = KineticConnections.load(tag.getCompound("Connections"));
		float generatedSpeed = tag.getFloat("Generated");
		float stressCapacity = tag.getFloat("Capacity");
		float stressImpact = tag.getFloat("Impact");
		boolean constantStress = tag.getBoolean("ConstantStress");
		return new KineticNode(solver, pos, connections, generatedSpeed, stressCapacity, stressImpact, constantStress);
	}

	public boolean isLoaded() {
		return entity != null;
	}

	public void onLoaded(KineticTileEntity entity) {
		this.entity = entity;
		network.onMemberLoaded(this);
		if (speedCur != 0) entity.setSpeed(speedCur);
	}

	public void onUnloaded() {
		this.entity = null;
	}

	public KineticConnections getConnections() {
		return connections;
	}

	public KineticNetwork getNetwork() {
		return network;
	}

	public BlockPos getPos() {
		return pos;
	}

	/**
	 * @return 	a Stream containing a pair for each compatible connection with this node, where the first value is
	 * 			the connecting node and the second value is the speed ratio of the connection
	 */
	public Stream<Pair<KineticNode, Float>> getActiveConnections() {
		return connections.getDirections().stream()
				.map(d -> solver.getNode(pos.offset(d))
						.map(n -> connections.checkConnection(n.connections, d)
								.map(r -> Pair.of(n, r))))
				.flatMap(Optional::stream)
				.flatMap(Optional::stream);
	}

	public Iterable<Pair<KineticNode, Float>> getActiveConnectionsList() {
		return getActiveConnections().collect(Collectors.toList());
	}

	public Stream<KineticNetwork> getActiveStressOnlyConnections() {
		return connections.getDirections().stream()
				.map(d -> solver.getNode(pos.offset(d))
						.filter(n -> connections.checkStressOnlyConnection(n.connections, d)))
				.flatMap(Optional::stream)
				.map(KineticNode::getNetwork);
	}

	public float getGeneratedSpeedAtRoot() {
		return generatedSpeed / speedRatio;
	}

	public boolean isGenerator() {
		return generatedSpeed != 0;
	}

	public boolean onUpdated() {
		if (entity == null) return false;

		boolean changed = false;

		float generatedSpeedNew = entity.getGeneratedSpeed();
		if (this.generatedSpeed != generatedSpeedNew) {
			this.generatedSpeed = generatedSpeedNew;
			changed = true;
			network.onMemberGeneratedSpeedUpdated(this);
			if (network.tryRecalculateSpeed().isContradiction()) {
				popBlock();
			}
		}

		float stressImpactNew = entity.getStressImpact();
		if (this.stressImpact != stressImpactNew) {
			this.stressImpact = stressImpactNew;
			changed = true;
			network.onMemberStressImpactUpdated();
		}

		float stressCapacityNew = entity.getStressCapacity();
		if (this.stressCapacity != stressCapacityNew) {
			this.stressCapacity = stressCapacityNew;
			changed = true;
			network.onMemberStressCapacityUpdated();
		}

		return changed;
	}

	public boolean hasStressCapacity() {
		return stressCapacity != 0;
	}

	public boolean hasStressImpact() {
		return stressImpact != 0;
	}

	public float getTheoreticalSpeed(float speedAtRoot) {
		return speedAtRoot * speedRatio;
	}

	public float getStressCapacity() {
		return constantStress ? stressCapacity : stressCapacity * Math.abs(generatedSpeed);
	}

	public float getTotalStressImpact(float speedAtRoot) {
		return constantStress ? stressImpact : stressImpact * Math.abs(getTheoreticalSpeed(speedAtRoot));
	}

	private SolveResult setNetwork(KineticNetwork network) {
		this.network.removeMember(this);
		this.network = network;
		network.addMember(this);
		return network.tryRecalculateSpeed();
	}

	public @Nullable KineticNode getSource() {
		return source;
	}

	private SolveResult setSource(KineticNode from, float ratio) {
		source = from;
		speedRatio = from.speedRatio * ratio;
		return setNetwork(from.network);
	}

	public void onAdded() {
		getActiveConnections()
				.findAny()
				.ifPresent(e -> {
					if (setSource(e.getFirst(), 1/e.getSecond()).isOk()) {
						propagateSource();
					} else {
						popBlock();
					}
				});
	}

	/**
	 * Propagates this node's source and network to any connected nodes that aren't yet part of the same network, then
	 * repeats this recursively with the connected nodes in a breadth-first order.
	 */
	private void propagateSource() {
		List<KineticNode> frontier = new LinkedList<>();
		frontier.add(this);

		while (!frontier.isEmpty()) {
			KineticNode cur = frontier.remove(0);
			for (Pair<KineticNode, Float> pair : cur.getActiveConnectionsList()) {
				KineticNode next = pair.getFirst();
				float ratio = pair.getSecond();

				if (next == cur.source) continue;

				if (next.network == network) {
					if (!Mth.equal(next.speedRatio, cur.speedRatio * ratio)) {
						// this node will cause a cycle with conflicting speed ratios
						if (network.isStopped()) {
							network.markConflictingCycle(cur, next);
						} else {
							popBlock();
							return;
						}
					}
					continue;
				}

				if (next.setSource(cur, ratio).isOk()) {
					frontier.add(next);
				} else {
					// this node will run against the network or activate a conflicting cycle
					popBlock();
					return;
				}
			}
		}
	}

	public void onRemoved() {
		network.removeMember(this);
		getActiveConnections()
				.map(Pair::getFirst)
				.filter(n -> n.source == this)
				.forEach(KineticNode::rerootHere);
	}

	private void rerootHere() {
		source = null;
		speedRatio = 1;
		SolveResult recalculateSpeedResult = setNetwork(new KineticNetwork(this));
		assert(recalculateSpeedResult.isOk());
		propagateSource();
	}

	/**
	 * Updates the speed of this node based on its network's root speed and its own speed ratio.
	 * @param speedAtRoot 	Current speed at the root of this node's network
	 * @return 				CONTRADICTION if the node's new speed exceeds the maximum value, and OK otherwise
	 */
	protected SolveResult tryUpdateSpeed(float speedAtRoot) {
		speedNext = getTheoreticalSpeed(speedAtRoot);
		if (Math.abs(speedNext) > AllConfigs.SERVER.kinetics.maxRotationSpeed.get())
			return SolveResult.CONTRADICTION;
		return SolveResult.OK;
	}

	protected void stop() {
		speedNext = 0;
	}

	public void flushChangedSpeed() {
		if (speedCur != speedNext) {
			speedCur = speedNext;
			if (entity != null) {
				entity.setSpeed(speedCur);
			}
		}
	}

	public void popBlock() {
		if (entity != null) {
			solver.removeAndPopNow(entity);
		} else {
			solver.removeAndQueuePop(pos);
		}
	}

}
