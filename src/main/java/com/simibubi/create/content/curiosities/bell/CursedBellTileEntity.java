package com.simibubi.create.content.curiosities.bell;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.spawner.WorldEntitySpawner;

public class CursedBellTileEntity extends AbstractBellTileEntity {

	public static final int MAX_DISTANCE = 6;
	private static final List<List<BlockPos>> LAYERS = genLayers();

	public enum Mode {
		RUNNING, RECHARGING
	}

	public static final int RECHARGE_TICKS = 16;
	public static final int TICKS_PER_LAYER = 3;
	public int ticks;
	public Mode mode = Mode.RECHARGING;

	public CursedBellTileEntity(TileEntityType<?> type) {
		super(type);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) { }

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		ticks = compound.getInt("Ticks");
		mode = NBTHelper.readEnum(compound, "Mode", Mode.class);
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		compound.putInt("Ticks", ticks);
		NBTHelper.writeEnum(compound, "Mode", mode);
	}

	@Override
	public void tick() {
		super.tick();

		switch (mode) {
		case RECHARGING:
			if (ticks > 0)
				ticks--;
			break;

		case RUNNING:
			if (ticks <= 0)
				break;

			ticks--;
			if (ticks % TICKS_PER_LAYER == 0) {
				while (!trySpawnSouls(world, pos, MAX_DISTANCE - ticks / TICKS_PER_LAYER - 1)
						&& ticks > 0) {
					ticks -= TICKS_PER_LAYER;
				}

				if (ticks == 0) {
					ticks = RECHARGE_TICKS;
					mode = Mode.RECHARGING;
				}
			}

			break;
		}
	}

	public boolean tryStart() {
		if (mode != Mode.RECHARGING || ticks > 0)
			return false;

		ticks = TICKS_PER_LAYER*MAX_DISTANCE;
		mode = Mode.RUNNING;
		if (!world.isRemote)
			sendData();

		return true;
	}

	@Override
	public boolean ring(World world, BlockPos pos, Direction direction) {
		return tryStart();
	}

	public static boolean trySpawnSouls(World world, BlockPos at, int layerIdx) {
		if (world == null)
			return false;

		boolean spawnedAny = false;
		List<BlockPos> layer = LAYERS.get(layerIdx);
		for (BlockPos candidate : layer) {
			candidate = candidate.add(at);

			if (!WorldEntitySpawner.canCreatureTypeSpawnAtLocation(
					EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,
					world, candidate, EntityType.ZOMBIE))
				continue;

			if (world.getLightLevel(LightType.BLOCK, candidate) >= 8)
				continue;

			if (!world.isRemote)
				return true;
			spawnedAny = true;
			spawnParticles(world, candidate);
		}
		return spawnedAny;
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
					int dist = 1 + (int)Math.sqrt(candidate.distanceSq(0,0,0,false));
					if (dist == 0 || dist > MAX_DISTANCE)
						continue;

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

}
