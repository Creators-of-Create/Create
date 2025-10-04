package com.simibubi.create.content.logistics.packagePort.postbox;

import java.lang.ref.WeakReference;
import java.util.List;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.compat.computercraft.ComputerCraftProxy;
import com.simibubi.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.simibubi.create.content.trains.station.GlobalPackagePort;
import com.simibubi.create.content.trains.station.GlobalStation;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import org.jetbrains.annotations.NotNull;

public class PostboxBlockEntity extends PackagePortBlockEntity {

	public WeakReference<GlobalStation> trackedGlobalStation;

	public LerpedFloat flag;
	public boolean forceFlag;

	private boolean sendParticles;

	public AbstractComputerBehaviour computerBehaviour;

	public PostboxBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		trackedGlobalStation = new WeakReference<>(null);
		flag = LerpedFloat.linear()
			.startWithValue(0);
	}

	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
		if (computerBehaviour.isPeripheralCap(cap))
			return computerBehaviour.getPeripheralCapability();
		return super.getCapability(cap, side);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(computerBehaviour = ComputerCraftProxy.behaviour(this));
		super.addBehaviours(behaviours);
	}

	@Override
	public void tick() {
		super.tick();
		if (!level.isClientSide && !isVirtual()) {
			if (sendParticles)
				sendData();
			return;
		}

		float currentTarget = flag.getChaseTarget();
		if (currentTarget == 0 || flag.settled()) {
			int target = (inventory.isEmpty() && !forceFlag) ? 0 : 1;
			if (target != currentTarget) {
				flag.chase(target, 0.1f, Chaser.LINEAR);
				if (target == 1)
					AllSoundEvents.CONTRAPTION_ASSEMBLE.playAt(level, worldPosition, 1, 2, true);
			}
		}
		boolean settled = flag.getValue() > .15f;
		flag.tickChaser();
		if (currentTarget == 0 && settled != flag.getValue() > .15f)
			AllSoundEvents.CONTRAPTION_DISASSEMBLE.playAt(level, worldPosition, 0.75f, 1.5f, true);

		if (sendParticles) {
			sendParticles = false;
			BoneMealItem.addGrowthParticles(level, worldPosition, 40);
		}
	}

	@Override
	protected void onOpenChange(boolean open) {
		level.setBlockAndUpdate(worldPosition, getBlockState().setValue(PostboxBlock.OPEN, open));
		level.playSound(null, worldPosition, open ? SoundEvents.BARREL_OPEN : SoundEvents.BARREL_CLOSE,
			SoundSource.BLOCKS);
	}

	public void spawnParticles() {
		sendParticles = true;
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		if (clientPacket && sendParticles)
			NBTHelper.putMarker(tag, "Particles");
		sendParticles = false;
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		sendParticles = clientPacket && tag.contains("Particles");
	}

	@Override
	public void setChanged() {
		saveOfflineBuffer();
		super.setChanged();
	}

	private void saveOfflineBuffer() {
		if (level == null || level.isClientSide)
			return;

		GlobalStation station = trackedGlobalStation.get();
		if (station == null)
			return;

		GlobalPackagePort globalPackagePort = station.connectedPorts.get(worldPosition);
		if (globalPackagePort == null)
			return;

		globalPackagePort.saveOfflineBuffer(inventory);
	}

	@Override
	public void invalidateCaps() {
		super.invalidate();
		computerBehaviour.removePeripheral();
	}
}
