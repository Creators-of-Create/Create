package com.simibubi.create.api.event;

import java.util.Map;
import java.util.function.Consumer;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.world.level.block.entity.BlockEntityType;

import net.neoforged.bus.api.Event;

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
 * allowing block entities to store and retrieve data for injected behaviours.<br>
 * <br>
 * Example: <pre> {@code
 * 		neoForgeEventBus.addListener((BlockEntityBehaviourEvent event) -> {
 * 			event.forType(AllBlockEntityTypes.FUNNEL.get(), be -> {
 * 				event.attach(new FunFunnelBehaviour(be));
 * 			});
 * 		});
 * } <pre>
 */
public class BlockEntityBehaviourEvent extends Event {
	private final SmartBlockEntity smartBlockEntity;
	private final Map<BehaviourType<?>, BlockEntityBehaviour> behaviours;

	public BlockEntityBehaviourEvent(SmartBlockEntity blockEntity, Map<BehaviourType<?>, BlockEntityBehaviour> behaviours) {
		smartBlockEntity = blockEntity;
		this.behaviours = behaviours;
	}

	public <T extends SmartBlockEntity> void forType(BlockEntityType<T> type, Consumer<T> action) {
		if (smartBlockEntity.getType() == type) {
			//noinspection unchecked
			action.accept((T) smartBlockEntity);
		}
	}

	public void attach(BlockEntityBehaviour behaviour) {
		behaviours.put(behaviour.getType(), behaviour);
	}

	public BlockEntityBehaviour remove(BehaviourType<?> type) {
		return behaviours.remove(type);
	}

}
