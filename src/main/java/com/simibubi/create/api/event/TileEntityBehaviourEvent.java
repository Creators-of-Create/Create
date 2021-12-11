package com.simibubi.create.api.event;

import java.lang.reflect.Type;
import java.util.Map;

import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.GenericEvent;

/**
 * Event that is fired just before a SmartTileEntity is being deserialized<br>
 * Also if a new one is placed<br>
 * Use it to attach a new {@link TileEntityBehaviour} or replace existing ones
 * (with caution)<br>
 * <br>
 * Actual setup of the behaviours internal workings and data should be done in
 * TileEntityBehaviour#read() and TileEntityBehaviour#initialize()
 * respectively.<br>
 * <br>
 * Because of the earliness of this event, the added behaviours will have access
 * to the initial NBT read (unless the TE was placed, not loaded), thereby
 * allowing tiles to store and retrieve data for injected behaviours.
 */
public class TileEntityBehaviourEvent<T extends SmartTileEntity> extends GenericEvent<T> {

	private T smartTileEntity;
	private Map<BehaviourType<?>, TileEntityBehaviour> behaviours;

	public TileEntityBehaviourEvent(T tileEntity, Map<BehaviourType<?>, TileEntityBehaviour> behaviours) {
		smartTileEntity = tileEntity;
		this.behaviours = behaviours;
	}

	@Override
	public Type getGenericType() {
		return smartTileEntity.getClass();
	}

	public void attach(TileEntityBehaviour behaviour) {
		behaviours.put(behaviour.getType(), behaviour);
	}

	public TileEntityBehaviour remove(BehaviourType<?> type) {
		return behaviours.remove(type);
	}

	public T getTileEntity() {
		return smartTileEntity;
	}

	public BlockState getBlockState() {
		return smartTileEntity.getBlockState();
	}

}
