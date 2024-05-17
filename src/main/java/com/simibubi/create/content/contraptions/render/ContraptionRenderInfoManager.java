package com.simibubi.create.content.contraptions.render;

import com.jozufozu.flywheel.api.event.ReloadLevelRendererEvent;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.foundation.utility.WorldAttached;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(Dist.CLIENT)
public class ContraptionRenderInfoManager {
	static final WorldAttached<ContraptionRenderInfoManager> MANAGERS = new WorldAttached<>(ContraptionRenderInfoManager::new);

	private final Level level;
	private final Int2ObjectMap<ContraptionRenderInfo> renderInfos = new Int2ObjectOpenHashMap<>();
	private int removalTimer;

	private ContraptionRenderInfoManager(LevelAccessor level) {
		this.level = (Level) level;
	}

	public static void tickFor(Level level) {
		if (Minecraft.getInstance()
			.isPaused())
			return;

		MANAGERS.get(level)
			.tick();
	}

	public static void resetAll() {
		MANAGERS.empty(ContraptionRenderInfoManager::delete);
	}

	@SubscribeEvent
	public static void onReloadLevelRenderer(ReloadLevelRendererEvent event) {
		resetAll();
	}

	ContraptionRenderInfo getRenderInfo(Contraption contraption) {
		int entityId = contraption.entity.getId();
		ContraptionRenderInfo renderInfo = renderInfos.get(entityId);

		if (renderInfo == null) {
			renderInfo = new ContraptionRenderInfo(level, contraption);
			renderInfos.put(entityId, renderInfo);
		}

		return renderInfo;
	}

	boolean invalidate(Contraption contraption) {
		int entityId = contraption.entity.getId();
		ContraptionRenderInfo renderInfo = renderInfos.remove(entityId);

		if (renderInfo != null) {
			renderInfo.invalidate();
			return true;
		}

		return false;
	}

	private void tick() {
		if (removalTimer >= 20) {
			renderInfos.values().removeIf(ContraptionRenderInfo::isDead);
			removalTimer = 0;
		}
		removalTimer++;
	}

	private void delete() {
		for (ContraptionRenderInfo renderer : renderInfos.values()) {
			renderer.invalidate();
		}
		renderInfos.clear();
	}
}
