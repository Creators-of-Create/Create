package com.simibubi.create;

import com.simibubi.create.content.contraptions.components.actors.SeatEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionEntityRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.OrientedContraptionEntityRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.gantry.GantryContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueRenderer;
import com.simibubi.create.foundation.utility.Lang;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityType.IFactory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class AllEntityTypes {

	public static final RegistryEntry<EntityType<OrientedContraptionEntity>> ORIENTED_CONTRAPTION =
		contraption("contraption", OrientedContraptionEntity::new, 5, 3, true);
	public static final RegistryEntry<EntityType<ControlledContraptionEntity>> CONTROLLED_CONTRAPTION =
		contraption("stationary_contraption", ControlledContraptionEntity::new, 20, 40, false);
	public static final RegistryEntry<EntityType<GantryContraptionEntity>> GANTRY_CONTRAPTION =
		contraption("gantry_contraption", GantryContraptionEntity::new, 10, 40, false);

	public static final RegistryEntry<EntityType<SuperGlueEntity>> SUPER_GLUE = register("super_glue",
		SuperGlueEntity::new, EntityClassification.MISC, 10, Integer.MAX_VALUE, false, true, SuperGlueEntity::build);
	public static final RegistryEntry<EntityType<SeatEntity>> SEAT = register("seat", SeatEntity::new,
		EntityClassification.MISC, 0, Integer.MAX_VALUE, false, true, SeatEntity::build);

	//

	public static void register() {}

	private static <T extends Entity> RegistryEntry<EntityType<T>> contraption(String name, IFactory<T> factory,
		int range, int updateFrequency, boolean sendVelocity) {
		return register(name, factory, EntityClassification.MISC, range, updateFrequency, sendVelocity, true,
			AbstractContraptionEntity::build);
	}

	private static <T extends Entity> RegistryEntry<EntityType<T>> register(String name, IFactory<T> factory,
		EntityClassification group, int range, int updateFrequency, boolean sendVelocity, boolean immuneToFire,
		NonNullConsumer<EntityType.Builder<T>> propertyBuilder) {
		String id = Lang.asId(name);
		return Create.registrate()
			.entity(id, factory, group)
			.properties(b -> b.setTrackingRange(range)
				.setUpdateInterval(updateFrequency)
				.setShouldReceiveVelocityUpdates(sendVelocity))
			.properties(propertyBuilder)
			.properties(b -> {
				if (immuneToFire)
					b.immuneToFire();
			})
			.register();
	}

	@OnlyIn(value = Dist.CLIENT)
	public static void registerRenderers() {
		RenderingRegistry.registerEntityRenderingHandler(CONTROLLED_CONTRAPTION.get(), ContraptionEntityRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(ORIENTED_CONTRAPTION.get(),
			OrientedContraptionEntityRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(GANTRY_CONTRAPTION.get(), ContraptionEntityRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(SUPER_GLUE.get(), SuperGlueRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(SEAT.get(), SeatEntity.Render::new);
	}
}
