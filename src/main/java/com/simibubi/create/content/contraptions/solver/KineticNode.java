package com.simibubi.create.content.contraptions.solver;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KineticNode {

	private final KineticSolver solver;
	private @Nullable KineticTileEntity entity;
	private boolean needsUnloading;

	private @Nullable IKineticController controller;
	private @Nullable KineticControllerSerial controllerType;

	private @Nullable KineticNode parent;
	private @Nullable BlockPos speedSource;
	private KineticNetwork network;
	private float speedRatio = 1;

	private final BlockPos pos;
	private final KineticConnections connections;
	private float generatedSpeed;
	private float stressCapacity;
	private float stressImpact;
	private final boolean constantStress;

	protected KineticNode regen() {
		return new KineticNode(solver, pos, entity, getController().get(), controllerType);
	}

	public KineticNode(KineticSolver solver, KineticTileEntity entity) {
		this(solver, entity.getBlockPos(), entity, entity, null);
		this.controller = null;
	}

	private KineticNode(KineticSolver solver, BlockPos pos, @Nullable KineticTileEntity entity,
						IKineticController controller, @Nullable KineticControllerSerial controllerType) {
		this(solver, pos, controller.getConnections(), controller.getGeneratedSpeed(), controller.getStressCapacity(),
				controller.getStressImpact(), controller.isStressConstant());
		this.entity = entity;
		this.controller = controller;
		this.controllerType = controllerType;
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
		if (controller != null && controllerType != null) {
			NBTHelper.writeEnum(tag, "ControllerType", controllerType);
			tag.put("Controller", controller.save(new CompoundTag()));
			return tag;
		}

		tag.put("Connections", connections.save(new CompoundTag()));
		if (generatedSpeed != 0)
			tag.putFloat("Generated", generatedSpeed);
		if (stressCapacity != 0)
			tag.putFloat("Capacity", stressCapacity);
		if (stressImpact != 0)
			tag.putFloat("Impact", stressImpact);
		if (constantStress)
			tag.putBoolean("ConstantStress", true);

		return tag;
	}

	public static KineticNode load(KineticSolver solver, CompoundTag tag) {
		BlockPos pos = NbtUtils.readBlockPos(tag.getCompound("Pos"));
		if (tag.contains("Controller") && tag.contains("ControllerType")) {
			KineticControllerSerial type = NBTHelper.readEnum(tag, "ControllerType", KineticControllerSerial.class);
			return new KineticNode(solver, pos, null, type.load(tag.getCompound("Controller")), type);
		}

		KineticConnections connections = KineticConnections.load(tag.getCompound("Connections"));
		float generatedSpeed = tag.getFloat("Generated");
		float stressCapacity = tag.getFloat("Capacity");
		float stressImpact = tag.getFloat("Impact");
		boolean constantStress = tag.getBoolean("ConstantStress");
		return new KineticNode(solver, pos, connections, generatedSpeed, stressCapacity, stressImpact, constantStress);
	}

	public boolean isLoaded() {
		return !(entity == null || needsUnloading);
	}

	protected void onLoaded(KineticTileEntity entity) {
		this.entity = entity;
		network.onMemberLoaded(this);
	}

	protected void onUnloaded() {
		entity = null;
		needsUnloading = true;
	}

	public Optional<IKineticController> getController() {
		if (controller != null) return Optional.of(controller);
		if (entity != null) return Optional.of(entity);
		return Optional.empty();
	}

	public boolean setController(KineticControllerSerial controller) {
		if (this.controller != null) return false;
		this.controller = controller.init(this);
		this.controllerType = controller;
		return true;
	}

	protected void removeController() {
		controller = null;
		controllerType = null;
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
	private Stream<Pair<KineticNode, Float>> getAllActiveConnections() {
		return connections.stream()
				.flatMap(from -> from.getRatios().entrySet().stream()
						.map(e -> {
							Vec3i offset = e.getKey();
							return solver.getNode(pos.offset(offset)).flatMap(node -> {
								Map<KineticConnection, Float> ratios = e.getValue();
								return node.getConnections().stream()
										.map(ratios::get)
										.filter(Objects::nonNull)
										.findFirst()
										.map(r -> Pair.of(node, r));
							});
						})
				)
				.flatMap(Optional::stream);
	}

	public Stream<Pair<KineticNode, Float>> getActiveConnections() {
		return getAllActiveConnections().filter(p -> p.getSecond() != 0);
	}

	public Stream<KineticNode> getActiveStressOnlyConnections() {
		return getAllActiveConnections().filter(p -> p.getSecond() == 0).map(Pair::getFirst);
	}

	public Optional<Float> checkConnection(KineticNode to) {
		return getActiveConnections().filter(p -> p.getFirst() == to).findAny().map(Pair::getSecond);
	}

	public boolean checkStressOnlyConnection(KineticNode to) {
		return getActiveStressOnlyConnections().anyMatch(n -> n == to);
	}

	public float getGeneratedSpeed() {
		return generatedSpeed;
	}

	public float getGeneratedSpeedAtRoot() {
		return generatedSpeed / speedRatio;
	}

	public boolean isGenerator() {
		return generatedSpeed != 0;
	}

	protected enum UpdateResult {
		UNCHANGED, CHANGED, NEEDS_REGEN, NEEDS_POP
	}
	protected UpdateResult onUpdated() {
		UpdateResult out = getController().map(ctl -> {
			if (!getConnections().equals(ctl.getConnections()) || constantStress != ctl.isStressConstant())
				return UpdateResult.NEEDS_REGEN;

			UpdateResult result = UpdateResult.UNCHANGED;

			float generatedSpeedNew = ctl.getGeneratedSpeed();
			if (this.generatedSpeed != generatedSpeedNew) {
				this.generatedSpeed = generatedSpeedNew;
				network.onMemberGeneratedSpeedUpdated(this);
				if (network.tryRecalculateSpeed().isContradiction()) {
					return UpdateResult.NEEDS_POP;
				}
				result = UpdateResult.CHANGED;
			}

			float stressImpactNew = ctl.getStressImpact();
			if (this.stressImpact != stressImpactNew) {
				this.stressImpact = stressImpactNew;
				network.onMemberStressImpactUpdated();
				result = UpdateResult.CHANGED;
			}

			float stressCapacityNew = ctl.getStressCapacity();
			if (this.stressCapacity != stressCapacityNew) {
				this.stressCapacity = stressCapacityNew;
				network.onMemberStressCapacityUpdated();
				result = UpdateResult.CHANGED;
			}

			return result;
		}).orElse(UpdateResult.UNCHANGED);

		if (needsUnloading) {
			needsUnloading = false;
			entity = null;
		}

		return out;
	}

	public boolean hasStressCapacity() {
		return stressCapacity != 0;
	}

	public boolean hasStressImpact() {
		return stressImpact != 0;
	}

	public float getSpeed() {
		return network.getRootSpeed() * speedRatio;
	}

	public float getTheoreticalSpeed() {
		return network.getRootTheoreticalSpeed() * speedRatio;
	}

	public float getStressCapacity() {
		return constantStress ? stressCapacity : stressCapacity * Math.abs(generatedSpeed);
	}

	public float getTotalStressImpact() {
		return constantStress ? stressImpact : stressImpact * Math.abs(getTheoreticalSpeed());
	}

	private SolveResult setNetwork(KineticNetwork network) {
		this.network.removeMember(this);
		this.network = network;
		network.addMember(this);
		return network.tryRecalculateSpeed();
	}

	protected @Nullable KineticNode getParent() {
		return parent;
	}

	private SolveResult setParent(KineticNode from, float ratio) {
		parent = from;
		speedRatio = from.speedRatio * ratio;
		return setNetwork(from.network);
	}

	protected void onAdded() {
		getActiveConnections()
				.findAny()
				.ifPresent(e -> {
					if (setParent(e.getFirst(), 1/e.getSecond()).isOk()) {
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
			for (Pair<KineticNode, Float> pair : cur.getActiveConnections().collect(Collectors.toList())) {
				KineticNode next = pair.getFirst();
				float ratio = pair.getSecond();

				if (next == cur.parent) continue;

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

				if (next.setParent(cur, ratio).isOk()) {
					frontier.add(next);
				} else {
					// this node will run against the network or activate a conflicting cycle
					popBlock();
					return;
				}
			}
		}
	}

	protected void onRemoved() {
		network.removeMember(this);
		getActiveConnections()
				.map(Pair::getFirst)
				.filter(n -> n.parent == this)
				.forEach(KineticNode::rerootHere);
	}

	private void rerootHere() {
		parent = null;
		speedRatio = 1;
		SolveResult recalculateSpeedResult = setNetwork(new KineticNetwork(this));
		assert(recalculateSpeedResult.isOk());
		propagateSource();
	}

	protected @Nullable BlockPos getSpeedSource() {
		if (network.getRootTheoreticalSpeed() == 0) return null;
		return speedSource;
	}

	protected void setSpeedSource(@Nullable BlockPos speedSource) {
		this.speedSource = speedSource;
	}

	protected void flushChanges() {
		if (entity != null) {
			entity.updateFromSolver(getTheoreticalSpeed(), getSpeedSource(), network.id,
					network.isOverstressed(), network.getTotalStressImpact(), network.getTotalStressCapacity());
		}
	}

	protected void popBlock() {
		if (entity != null) {
			solver.removeAndPopNow(entity);
		} else {
			solver.removeAndQueuePop(pos);
		}
	}

}
