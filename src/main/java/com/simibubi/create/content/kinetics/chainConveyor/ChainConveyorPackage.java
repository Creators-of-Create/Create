package com.simibubi.create.content.kinetics.chainConveyor;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.cache.Cache;
import com.simibubi.create.foundation.utility.TickBasedCache;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.data.WorldAttached;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

public class ChainConveyorPackage {

	// Server creates unique ids for chain boxes
	public static final AtomicInteger netIdGenerator = new AtomicInteger();

	// Client tracks physics data by id so it can travel between BEs
	private static final int ticksUntilExpired = 30;
	public static final WorldAttached<Cache<Integer, ChainConveyorPackagePhysicsData>> physicsDataCache =
		new WorldAttached<>($ -> new TickBasedCache<>(ticksUntilExpired, true));

	public class ChainConveyorPackagePhysicsData {
		public Vec3 targetPos;
		public Vec3 prevTargetPos;
		public Vec3 prevPos;
		public Vec3 pos;

		public Vec3 motion;
		public int lastTick;
		public float yaw;
		public float prevYaw;
		public boolean flipped;
		public ResourceLocation modelKey;

		public WeakReference<ChainConveyorBlockEntity> beReference;

		public ChainConveyorPackagePhysicsData(Vec3 serverPosition) {
			this.targetPos = null;
			this.prevTargetPos = null;
			this.pos = null;
			this.prevPos = null;

			this.motion = Vec3.ZERO;
			this.lastTick = AnimationTickHolder.getTicks();
		}

		public boolean shouldTick() {
			if (lastTick == AnimationTickHolder.getTicks())
				return false;
			lastTick = AnimationTickHolder.getTicks();
			return true;
		}

		public void setBE(ChainConveyorBlockEntity ccbe) {
			if (beReference == null || beReference.get() != ccbe)
				beReference = new WeakReference<>(ccbe);
		}

	}

	public float chainPosition;
	public ItemStack item;
	public int netId;
	public boolean justFlipped;

	public Vec3 worldPosition;
	public float yaw;

	private ChainConveyorPackagePhysicsData physicsData;

	public ChainConveyorPackage(float chainPosition, ItemStack item) {
		this(chainPosition, item, netIdGenerator.incrementAndGet());
	}

	public ChainConveyorPackage(float chainPosition, ItemStack item, int netId) {
		this.chainPosition = chainPosition;
		this.item = item;
		this.netId = netId;
		this.physicsData = null;
	}

	public CompoundTag writeToClient(HolderLookup.Provider registries) {
		CompoundTag tag = write(registries);
		tag.putInt("NetID", netId);
		return tag;
	}

	public CompoundTag write(HolderLookup.Provider registries) {
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.putFloat("Position", chainPosition);
		compoundTag.put("Item", item.saveOptional(registries));
		return compoundTag;
	}

	public static ChainConveyorPackage read(CompoundTag compoundTag, HolderLookup.Provider registries) {
		float pos = compoundTag.getFloat("Position");
		ItemStack item = ItemStack.parseOptional(registries, compoundTag.getCompound("Item"));
		if (compoundTag.contains("NetID"))
			return new ChainConveyorPackage(pos, item, compoundTag.getInt("NetID"));
		return new ChainConveyorPackage(pos, item);
	}

	public ChainConveyorPackagePhysicsData physicsData(LevelAccessor level) {
		if (physicsData == null) {
			try {
				return physicsData = physicsDataCache.get(level)
					.get(netId, () -> new ChainConveyorPackagePhysicsData(worldPosition));
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		physicsDataCache.get(level)
			.getIfPresent(netId);
		return physicsData;
	}

}
