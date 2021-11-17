package com.simibubi.create.lib.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.LivingEntity;

public interface MobEntitySetTargetCallback {
	Event<MobEntitySetTargetCallback> EVENT = EventFactory.createArrayBacked(MobEntitySetTargetCallback.class, callbacks -> (targeting, target) -> {
		for (MobEntitySetTargetCallback callback : callbacks) {
			callback.onMobEntitySetTarget(targeting, target);
		}
	});

	void onMobEntitySetTarget(LivingEntity targeting, LivingEntity target);
}
