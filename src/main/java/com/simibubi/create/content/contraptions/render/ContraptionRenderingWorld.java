package com.simibubi.create.content.contraptions.render;

import java.lang.ref.Reference;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.ContraptionHandler;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public abstract class ContraptionRenderingWorld<C extends ContraptionRenderInfo> {
	protected final Level world;

	private int removalTimer;

	protected final Int2ObjectMap<C> renderInfos = new Int2ObjectOpenHashMap<>();
	protected final List<C> visible = new ObjectArrayList<>();

	public ContraptionRenderingWorld(LevelAccessor world) {
		this.world = (Level) world;
	}

	public boolean invalidate(Contraption contraption) {
		int entityId = contraption.entity.getId();

		C removed = renderInfos.remove(entityId);

		if (removed != null) {
			removed.invalidate();
			visible.remove(removed);
			return true;
		}

		return false;
	}

	public void renderLayer(RenderLayerEvent event) {
		for (C c : visible) {
			c.setupMatrices(event.stack, event.camX, event.camY, event.camZ);
		}
	}

	protected abstract C create(Contraption c);

	public void tick() {
		removalTimer++;
		if (removalTimer >= 20) {
			removeDeadRenderers();
			removalTimer = 0;
		}

		ContraptionHandler.loadedContraptions.get(world)
				.values()
				.stream()
				.map(Reference::get)
				.filter(Objects::nonNull)
				.map(AbstractContraptionEntity::getContraption)
				.filter(Objects::nonNull) // contraptions that are too large will not be synced, and un-synced contraptions will be null
				.forEach(this::getRenderInfo);
	}

	public void beginFrame(BeginFrameEvent event) {

		renderInfos.int2ObjectEntrySet()
				.stream()
				.map(Map.Entry::getValue)
				.forEach(renderInfo -> renderInfo.beginFrame(event));

		collectVisible();
	}

	protected void collectVisible() {
		visible.clear();
		renderInfos.int2ObjectEntrySet()
				.stream()
				.map(Map.Entry::getValue)
				.filter(ContraptionRenderInfo::isVisible)
				.forEach(visible::add);
	}

	public C getRenderInfo(Contraption c) {
		int entityId = c.entity.getId();
		C renderInfo = renderInfos.get(entityId);

		if (renderInfo == null) {
			renderInfo = create(c);
			renderInfos.put(entityId, renderInfo);
		}

		return renderInfo;
	}

	public void delete() {
		for (C renderer : renderInfos.values()) {
			renderer.invalidate();
		}
		renderInfos.clear();
	}

	/**
	 * Remove all render infos associated with dead/removed contraptions.
	 */
	public void removeDeadRenderers() {
		renderInfos.values().removeIf(ContraptionRenderInfo::isDead);
	}

}
