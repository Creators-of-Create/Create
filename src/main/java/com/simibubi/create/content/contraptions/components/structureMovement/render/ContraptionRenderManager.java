package com.simibubi.create.content.contraptions.components.structureMovement.render;

import java.lang.ref.Reference;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionHandler;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public abstract class ContraptionRenderManager<C extends ContraptionRenderInfo> {
	protected final World world;

	private int removalTimer;

	protected final Int2ObjectMap<C> renderInfos = new Int2ObjectOpenHashMap<>();
	protected final List<C> visible = new ObjectArrayList<>();

	public ContraptionRenderManager(IWorld world) {
		this.world = (World) world;
	}

	public abstract void renderLayer(RenderLayerEvent event);

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
				.forEach(this::getRenderInfo);
	}

	public void beginFrame(BeginFrameEvent event) {
		visible.clear();

		renderInfos.int2ObjectEntrySet()
				.stream()
				.map(Map.Entry::getValue)
				.forEach(renderInfo -> renderInfo.beginFrame(event));

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
		renderInfos.clear();
	}

	public void removeDeadRenderers() {
		renderInfos.values().removeIf(ContraptionRenderInfo::isDead);
	}

}
