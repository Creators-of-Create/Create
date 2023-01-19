package com.simibubi.create.foundation.blockEntity.behaviour.simple;

import java.util.function.Supplier;

import com.simibubi.create.foundation.blockEntity.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;

import net.minecraft.nbt.CompoundTag;

public class DeferralBehaviour extends BlockEntityBehaviour {

	public static final BehaviourType<DeferralBehaviour> TYPE = new BehaviourType<>();

	private boolean needsUpdate;
	private Supplier<Boolean> callback;

	public DeferralBehaviour(SmartBlockEntity be, Supplier<Boolean> callback) {
		super(be);
		this.callback = callback;
	}

	@Override
	public boolean isSafeNBT() { return true; }

	@Override
	public void write(CompoundTag nbt, boolean clientPacket) {
		nbt.putBoolean("NeedsUpdate", needsUpdate);
		super.write(nbt, clientPacket);
	}

	@Override
	public void read(CompoundTag nbt, boolean clientPacket) {
		needsUpdate = nbt.getBoolean("NeedsUpdate");
		super.read(nbt, clientPacket);
	}

	@Override
	public void tick() {
		super.tick();
		if (needsUpdate && callback.get())
			needsUpdate = false;
	}

	public void scheduleUpdate() {
		needsUpdate = true;
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

}
