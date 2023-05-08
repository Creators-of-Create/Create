package com.simibubi.create.api.event;

import java.lang.reflect.Type;
import java.util.Map;

import com.simibubi.create.foundation.blockEntity.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.GenericEvent;

/**
 * Event that is fired just before a SmartBlockEntity is being deserialized<br>
 * Also if a new one is placed<br>
 * Use it to attach a new {@link BlockEntityBehaviour} or replace existing ones
 * (with caution)<br>
 * <br>
 * Actual setup of the behaviours internal workings and data should be done in
 * BlockEntityBehaviour#read() and BlockEntityBehaviour#initialize()
 * respectively.<br>
 * <br>
 * Because of the earliness of this event, the added behaviours will have access
 * to the initial NBT read (unless the BE was placed, not loaded), thereby
 * allowing block entities to store and retrieve data for injected behaviours.
 */
public class BlockEntityBehaviourEvent<T extends SmartBlockEntity> extends GenericEvent<T> {

	private T smartBlockEntity;
	private Map<BehaviourType<?>, BlockEntityBehaviour> behaviours;

	public BlockEntityBehaviourEvent(T blockEntity, Map<BehaviourType<?>, BlockEntityBehaviour> behaviours) {
		smartBlockEntity = blockEntity;
		this.behaviours = behaviours;
	}

	@Override
	public Type getGenericType() {
		return smartBlockEntity.getClass();
	}

	public void attach(BlockEntityBehaviour behaviour) {
		behaviours.put(behaviour.getType(), behaviour);
	}

	public BlockEntityBehaviour remove(BehaviourType<?> type) {
		return behaviours.remove(type);
	}

	public T getBlockEntity() {
		return smartBlockEntity;
	}

	public BlockState getBlockState() {
		return smartBlockEntity.getBlockState();
	}

}
