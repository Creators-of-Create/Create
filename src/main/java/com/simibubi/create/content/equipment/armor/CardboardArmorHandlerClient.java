package com.simibubi.create.content.equipment.armor;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.logistics.box.PackageRenderer;
import com.simibubi.create.foundation.utility.TickBasedCache;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import net.minecraft.world.phys.Vec3;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class CardboardArmorHandlerClient {

	private static final Cache<UUID, Integer> BOXES_PLAYERS_ARE_HIDING_AS = new TickBasedCache<>(20, true);

	@SubscribeEvent
	public static void keepCacheAliveDesignDespiteNotRendering(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		if (!CardboardArmorHandler.testForStealth(player))
			return;
		try {
			getCurrentBoxIndex(player);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void playerRendersAsBoxWhenSneaking(RenderPlayerEvent.Pre event) {
		// TODO 26.2: Rebuild cardboard player replacement against AvatarRenderState and SubmitNodeCollector.
	}

	private static Integer getCurrentBoxIndex(Player player) throws ExecutionException {
		return BOXES_PLAYERS_ARE_HIDING_AS.get(player.getUUID(),
			() -> player.level().getRandom().nextInt(AllPartialModels.PACKAGES_TO_HIDE_AS.size()));
	}

}
