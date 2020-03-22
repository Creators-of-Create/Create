package com.simibubi.create.foundation.behaviour.simple;

import java.util.function.Supplier;

import com.simibubi.create.foundation.behaviour.base.IBehaviourType;
import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;

import net.minecraft.nbt.CompoundNBT;

public class DeferralBehaviour extends TileEntityBehaviour {

	public static IBehaviourType<DeferralBehaviour> TYPE = new IBehaviourType<DeferralBehaviour>() {
	};

	private boolean needsUpdate;
	private Supplier<Boolean> callback;

	public DeferralBehaviour(SmartTileEntity te, Supplier<Boolean> callback) {
		super(te);
		this.callback = callback;
	}

	@Override
	public void writeNBT(CompoundNBT nbt) {
		nbt.putBoolean("NeedsUpdate", needsUpdate);
		super.writeNBT(nbt);
	}

	@Override
	public void readNBT(CompoundNBT nbt) {
		needsUpdate = nbt.getBoolean("NeedsUpdate");
		super.readNBT(nbt);
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
	public IBehaviourType<?> getType() {
		return TYPE;
	}

}
