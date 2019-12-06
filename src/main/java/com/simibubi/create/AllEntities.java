package com.simibubi.create;

import java.util.function.Function;

import com.simibubi.create.modules.contraptions.receivers.contraptions.ContraptionEntity;
import com.simibubi.create.modules.contraptions.receivers.contraptions.ContraptionEntityRenderer;
import com.simibubi.create.modules.logistics.transport.CardboardBoxEntity;
import com.simibubi.create.modules.logistics.transport.CardboardBoxEntityRenderer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityType.Builder;
import net.minecraft.entity.EntityType.IFactory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public enum AllEntities {

	CARDBOARD_BOX(CardboardBoxEntity::new, 30, 3, CardboardBoxEntity::build),
	CONTRAPTION(ContraptionEntity::new, 30, 3, ContraptionEntity::build),

	;

	private IFactory<?> factory;
	private int range;
	private int updateFrequency;
	private Function<EntityType.Builder<? extends Entity>, EntityType.Builder<? extends Entity>> propertyBuilder;
	private EntityClassification group;

	public EntityType<? extends Entity> type;

	private AllEntities(IFactory<?> factory, int range, int updateFrequency,
			Function<EntityType.Builder<? extends Entity>, EntityType.Builder<? extends Entity>> propertyBuilder) {
		this.factory = factory;
		this.range = range;
		this.updateFrequency = updateFrequency;
		this.propertyBuilder = propertyBuilder;
	}

	public static void register(final RegistryEvent.Register<EntityType<?>> event) {
		for (AllEntities entity : values()) {
			String id = entity.name().toLowerCase();
			ResourceLocation resourceLocation = new ResourceLocation(Create.ID, id);
			Builder<? extends Entity> builder = EntityType.Builder.create(entity.factory, entity.group)
					.setTrackingRange(entity.range).setUpdateInterval(entity.updateFrequency)
					.setShouldReceiveVelocityUpdates(true);
			if (entity.propertyBuilder != null)
				builder = entity.propertyBuilder.apply(builder);
			entity.type = builder.build(id).setRegistryName(resourceLocation);
			event.getRegistry().register(entity.type);
		}

	}

	@OnlyIn(value = Dist.CLIENT)
	public static void registerRenderers() {
		RenderingRegistry.registerEntityRenderingHandler(CardboardBoxEntity.class, CardboardBoxEntityRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(ContraptionEntity.class, ContraptionEntityRenderer::new);
	}

}
