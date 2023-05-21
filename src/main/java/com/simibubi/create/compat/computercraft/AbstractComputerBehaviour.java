package com.simibubi.create.compat.computercraft;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class AbstractComputerBehaviour extends BlockEntityBehaviour {

	public static final BehaviourType<AbstractComputerBehaviour> TYPE = new BehaviourType<>();

	boolean hasAttachedComputer;

	public AbstractComputerBehaviour(SmartBlockEntity te) {
		super(te);
		this.hasAttachedComputer = false;
	}

	@Override
	public void read(CompoundTag nbt, boolean clientPacket) {
		hasAttachedComputer = nbt.getBoolean("HasAttachedComputer");
		super.read(nbt, clientPacket);
	}

	@Override
	public void write(CompoundTag nbt, boolean clientPacket) {
		nbt.putBoolean("HasAttachedComputer", hasAttachedComputer);
		super.write(nbt, clientPacket);
	}

	public <T> boolean isPeripheralCap(Capability<T> cap) {
		return false;
	}

	public <T> LazyOptional<T> getPeripheralCapability() {
		return LazyOptional.empty();
	}

	public void removePeripheral() {}

	public void setHasAttachedComputer(boolean hasAttachedComputer) {
		this.hasAttachedComputer = hasAttachedComputer;
	}

	public boolean hasAttachedComputer() {
		return hasAttachedComputer;
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

}
