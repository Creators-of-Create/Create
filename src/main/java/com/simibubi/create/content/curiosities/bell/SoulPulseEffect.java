package com.simibubi.create.content.curiosities.bell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.spawner.WorldEntitySpawner;

public class SoulPulseEffect {

	public static final int MAX_DISTANCE = 5;
	private static final List<List<BlockPos>> LAYERS = genLayers();

	private static final int WAITING_TICKS = 120;
	public static final int TICKS_PER_LAYER = 4;
	private int ticks;
	public final BlockPos pos;
	public final int distance;
	private List<BlockPos> added;

	public SoulPulseEffect(BlockPos pos, int distance, boolean overlaps) {
		this.ticks = TICKS_PER_LAYER * distance;
		this.pos = pos;
		this.distance = distance;
		this.added = overlaps ? null : new ArrayList<>();
	}

	public boolean finished() {
		return ticks <= -WAITING_TICKS;
	}

	public boolean overlaps() { return added == null; }

	public void removeAdded(Collection<BlockPos> positions) {
		if (!overlaps())
			positions.removeAll(added);
	}

	public List<BlockPos> tick(World world) {
		if (finished())
			return null;

		ticks--;
		if (ticks < 0 || ticks % TICKS_PER_LAYER != 0)
			return null;

		List<BlockPos> spawns = getSoulSpawns(world);
		while (spawns.isEmpty() && ticks > 0) {
			ticks -= TICKS_PER_LAYER;
			spawns.addAll(getSoulSpawns(world));
		}

		if (!overlaps())
			added.addAll(spawns);

		return spawns;
	}

	public int currentLayerIdx() {
		return distance - ticks / TICKS_PER_LAYER - 1;
	}

	public List<BlockPos> getSoulSpawns(World world) {
		if (world == null)
			return new ArrayList<>();

		return getLayer(currentLayerIdx()).map(p -> p.add(pos))
				.filter(p -> canSpawnSoulAt(world, p))
				.collect(Collectors.toList());
	}

	public static boolean canSpawnSoulAt(World world, BlockPos at) {
		return world != null
				&& WorldEntitySpawner.canCreatureTypeSpawnAtLocation(
					EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,
					world, at, EntityType.ZOMBIE)
				&& world.getLightLevel(LightType.BLOCK, at) < 8;
	}

	public static void spawnParticles(World world, BlockPos at) {
		if (world == null || !world.isRemote)
			return;

		Vector3d p = Vector3d.of(at);
		world.addParticle(new SoulParticle.Data(), p.x + 0.5, p.y + 0.5, p.z + 0.5, 0, 0, 0);
		world.addParticle(new SoulBaseParticle.Data(), p.x + 0.5, p.y + 0.01, p.z + 0.5, 0, 0, 0);
	}

	private static List<List<BlockPos>> genLayers() {
		List<List<BlockPos>> layers = new ArrayList<>();
		for (int i = 0; i < MAX_DISTANCE; i++)
			layers.add(new ArrayList<>());

		for (int x = 0; x < MAX_DISTANCE; x++) {
			for (int y = 0; y < MAX_DISTANCE; y++) {
				for (int z = 0; z < MAX_DISTANCE; z++) {
					BlockPos candidate = new BlockPos(x,y,z);

					int dist = (int) Math.round(Math.sqrt(
							candidate.distanceSq(0,0,0,false)));
					if (dist > MAX_DISTANCE)
						continue;
					if (dist <= 0)
						dist = 1;

					List<BlockPos> layer = layers.get(dist - 1);
					int start = layer.size(), end = start + 1;
					layer.add(candidate);

					if (candidate.getX() != 0) {
						layer.add(new BlockPos(-candidate.getX(), candidate.getY(), candidate.getZ()));
						end += 1;
					}
					if (candidate.getY() != 0) {
						for (int i = start; i < end; i++) {
							BlockPos prev = layer.get(i);
							layer.add(new BlockPos(prev.getX(), -prev.getY(), prev.getZ()));
						}
						end += end - start;
					}
					if (candidate.getZ() != 0) {
						for (int i = start; i < end; i++) {
							BlockPos prev = layer.get(i);
							layer.add(new BlockPos(prev.getX(), prev.getY(), -prev.getZ()));
						}
					}
				}
			}
		}

		return layers;
	}

	public static Stream<BlockPos> getLayer(int idx) {
		if (idx < 0 || idx >= MAX_DISTANCE)
			return Stream.empty();
		return LAYERS.get(idx).stream();
	}

}
