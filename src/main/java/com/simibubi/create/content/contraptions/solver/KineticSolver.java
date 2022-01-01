package com.simibubi.create.content.contraptions.solver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.WorldAttached;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.saveddata.SavedData;

import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;

public class KineticSolver extends SavedData {

	public static final String DATA_FILE_NAME = "kinetics";

	private static final WorldAttached<KineticSolver> SOLVERS = new WorldAttached<>(levelAccessor -> {
		if (levelAccessor instanceof ServerLevel level)
			return level.getDataStorage().computeIfAbsent(KineticSolver::load, KineticSolver::new, DATA_FILE_NAME);
		return new KineticSolver();
	});

	public static KineticSolver getSolver(Level level) {
		return SOLVERS.get(level);
	}

	private final Map<BlockPos, KineticNode> nodes = new HashMap<>();
	private final Set<BlockPos> popQueue = new HashSet<>();

	@Override
	public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
		ListTag popQueueTag = new ListTag();
		for (BlockPos pos : popQueue) {
			popQueueTag.add(NbtUtils.writeBlockPos(pos));
		}
		tag.put("PopQueue", popQueueTag);

		ListTag nodesTag = new ListTag();
		for (KineticNode node : nodes.values()) {
			nodesTag.add(node.save(new CompoundTag()));
		}
		tag.put("Nodes", nodesTag);

		return tag;
	}

	private static KineticSolver load(CompoundTag tag) {
		KineticSolver out = new KineticSolver();

		tag.getList("PopQueue", Tag.TAG_COMPOUND).forEach(c ->
				out.popQueue.add(NbtUtils.readBlockPos((CompoundTag) c)));

		tag.getList("Nodes", Tag.TAG_COMPOUND).forEach(c ->
				out.addUnloadedNode(KineticNode.load(out, (CompoundTag) c)));

		return out;
	}

	private void addUnloadedNode(KineticNode node) {
		KineticNode nodePrev = nodes.remove(node.getPos());
		if (nodePrev != null) nodePrev.onRemoved();

		nodes.put(node.getPos(), node);
		node.onAdded();
	}

	public void addNode(KineticTileEntity entity) {
		BlockPos pos = entity.getBlockPos();
		if (popQueue.contains(pos)) {
			popQueue.remove(pos);
			popBlock(entity.getLevel(), pos);
			setDirty();
			return;
		}

		KineticNode nodePrev = nodes.get(pos);
		if (nodePrev != null) {
			if (nodePrev.isLoaded()) {
				nodes.remove(pos);
				nodePrev.onRemoved();
			} else {
				// a node exists here but is unloaded, so just load it with this entity instead of replacing
				nodePrev.onLoaded(entity);
				return;
			}
		}

		KineticNode node = new KineticNode(this, entity);
		nodes.put(pos, node);
		node.onAdded();
		setDirty();
	}

	public void updateNode(KineticTileEntity entity) {
		KineticNode node = nodes.get(entity.getBlockPos());
		if (node == null) return;

		if (!node.getConnections().equals(entity.getConnections())) {
			// connections changed, so things could've been disconnected
			removeNode(entity);
			addNode(entity);
		} else {
			// connections are the same, so just update in case other properties changed
			if (node.onUpdated()) {
				setDirty();
			}
		}
	}

	public void unloadNode(KineticTileEntity entity) {
		KineticNode node = nodes.get(entity.getBlockPos());
		if (node != null) node.onUnloaded();
	}

	protected Optional<KineticNode> getNode(BlockPos pos) {
		return Optional.ofNullable(nodes.get(pos));
	}

	public void removeNode(KineticTileEntity entity) {
		KineticNode node = nodes.remove(entity.getBlockPos());
		if (node != null) {
			node.onRemoved();
			setDirty();
		}
	}

	protected void removeAndQueuePop(BlockPos pos) {
		KineticNode node = nodes.remove(pos);
		if (node != null) {
			popQueue.add(pos);
			node.onRemoved();
			setDirty();
		}
	}

	protected void removeAndPopNow(KineticTileEntity entity) {
		KineticNode node = nodes.remove(entity.getBlockPos());
		if (node != null) {
			popBlock(entity.getLevel(), entity.getBlockPos());
			node.onRemoved();
			setDirty();
		}
	}

	private void popBlock(Level level, BlockPos pos) {
		level.destroyBlock(pos, true);
	}

	public void tick() {
		Set<KineticNetwork> networks = nodes.values().stream().map(KineticNode::getNetwork).collect(Collectors.toSet());
		networks.forEach(KineticNetwork::untick);

		List<KineticNetwork> frontier = new LinkedList<>();

		for (KineticNetwork network : networks) {
			frontier.add(network);
			while (!frontier.isEmpty()) {
				KineticNetwork cur = frontier.remove(0);
				cur.tick(frontier);
			}
		}
	}

	public Optional<Float> isConnected(BlockPos from, BlockPos to) {
		return getNode(from).flatMap(fromNode ->
				getNode(to).flatMap(toNode ->
						fromNode.getConnections()
								.checkConnection(toNode.getConnections(), to.subtract(from))));
	}

	public boolean isStressOnlyConnected(BlockPos from, BlockPos to) {
		return getNode(from).flatMap(fromNode ->
				getNode(to).map(toNode ->
						fromNode.getConnections()
								.checkStressOnlyConnection(toNode.getConnections(), to.subtract(from)))
		).orElse(false);
	}
}
