package com.simibubi.create.foundation.blockEntity.behaviour.animatedContainer;

import java.util.function.Consumer;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.gui.menu.MenuBase;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class AnimatedContainerBehaviour<M extends MenuBase<? extends SmartBlockEntity>> extends BlockEntityBehaviour {

	public static final BehaviourType<AnimatedContainerBehaviour<?>> TYPE = new BehaviourType<>();

	public int openCount;

	private Class<M> menuClass;
	private Consumer<Boolean> openChanged;

	public AnimatedContainerBehaviour(SmartBlockEntity be, Class<M> menuClass) {
		super(be);
		this.menuClass = menuClass;
		openCount = 0;
	}

	public void onOpenChanged(Consumer<Boolean> openChanged) {
		this.openChanged = openChanged;
	}

	@Override
	public void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(compound, registries, clientPacket);
		if (clientPacket)
			openCount = compound.getInt("OpenCount");
	}

	@Override
	public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(compound, registries, clientPacket);
		if (clientPacket)
			compound.putInt("OpenCount", openCount);
	}

	@Override
	public void lazyTick() {
		updateOpenCount();
		super.lazyTick();
	}

	void updateOpenCount() {
		Level level = getWorld();
		if (level.isClientSide)
			return;
		if (openCount == 0)
			return;

		int prevOpenCount = openCount;
		openCount = 0;

		for (Player playerentity : level.getEntitiesOfClass(Player.class, new AABB(getPos()).inflate(8)))
			if (menuClass.isInstance(playerentity.containerMenu)
				&& menuClass.cast(playerentity.containerMenu).contentHolder == blockEntity)
				openCount++;

		if (prevOpenCount != openCount) {
			if (openChanged != null && prevOpenCount == 0 && openCount > 0)
				openChanged.accept(true);
			if (openChanged != null && prevOpenCount > 0 && openCount == 0)
				openChanged.accept(false);
			blockEntity.sendData();
		}
	}

	public void startOpen(Player player) {
		if (player.isSpectator())
			return;
		if (getWorld().isClientSide)
			return;
		if (openCount < 0)
			openCount = 0;
		openCount++;
		if (openCount == 1 && openChanged != null)
			openChanged.accept(true);
		blockEntity.sendData();
	}

	public void stopOpen(Player player) {
		if (player.isSpectator())
			return;
		if (getWorld().isClientSide)
			return;
		openCount--;
		if (openCount == 0 && openChanged != null)
			openChanged.accept(false);
		blockEntity.sendData();
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

}
