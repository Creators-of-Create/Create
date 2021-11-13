package com.simibubi.create.lib.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.LivingEntity;

public interface MobEntitySetTargetCallback {
	public static final Event<MobEntitySetTargetCallback> EVENT = EventFactory.createArrayBacked(MobEntitySetTargetCallback.class, callbacks -> (entity, target) -> {
		for (MobEntitySetTargetCallback callback : callbacks) {
			callback.onMobEntitySetTarget(entity, target);
		}
	});

	void onMobEntitySetTarget(LivingEntity entity, LivingEntity target);
}
