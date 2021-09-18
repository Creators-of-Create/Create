package com.simibubi.create;

import com.simibubi.create.content.contraptions.components.actors.SeatEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionEntityRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.OrientedContraptionEntityRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.gantry.GantryContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.GlueInstance;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueRenderer;
import com.simibubi.create.content.curiosities.tools.BlueprintEntity;
import com.simibubi.create.content.curiosities.tools.BlueprintRenderer;
import com.simibubi.create.content.curiosities.weapons.PotatoProjectileEntity;
import com.simibubi.create.content.curiosities.weapons.PotatoProjectileRenderer;
import com.simibubi.create.foundation.data.CreateEntityBuilder;
import com.simibubi.create.foundation.utility.Lang;
import com.tterrag.registrate.util.entry.EntityEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType.EntityFactory;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class AllEntityTypes {

	public static final EntityEntry<OrientedContraptionEntity> ORIENTED_CONTRAPTION = contraption("contraption",
		OrientedContraptionEntity::new, 5, 3, true);

	public static final EntityEntry<ControlledContraptionEntity> CONTROLLED_CONTRAPTION = contraption("stationary_contraption", ControlledContraptionEntity::new,
			20, 40, false);

	public static final EntityEntry<GantryContraptionEntity> GANTRY_CONTRAPTION = contraption("gantry_contraption",
		GantryContraptionEntity::new, 10, 40, false);

	public static final EntityEntry<SuperGlueEntity> SUPER_GLUE = AllEntityTypes.<SuperGlueEntity>register("super_glue", SuperGlueEntity::new, MobCategory.MISC, 10,
			Integer.MAX_VALUE, false, true, SuperGlueEntity::build).instance(() -> GlueInstance::new).register();

	public static final EntityEntry<BlueprintEntity> CRAFTING_BLUEPRINT = AllEntityTypes.<BlueprintEntity>register("crafting_blueprint", BlueprintEntity::new, MobCategory.MISC,
			10, Integer.MAX_VALUE, false, true, BlueprintEntity::build).register();

	public static final EntityEntry<PotatoProjectileEntity> POTATO_PROJECTILE = register("potato_projectile", PotatoProjectileEntity::new,
			MobCategory.MISC, 4, 20, true, false, PotatoProjectileEntity::build).register();

	public static final EntityEntry<SeatEntity> SEAT = AllEntityTypes.<SeatEntity>register("seat", SeatEntity::new, MobCategory.MISC,
			0, Integer.MAX_VALUE, false, true, SeatEntity::build).register();

	private static <T extends Entity> EntityEntry<T> contraption(String name, EntityFactory<T> factory,
		int range, int updateFrequency, boolean sendVelocity) {
		return register(name, factory, MobCategory.MISC, range, updateFrequency, sendVelocity, true,
			AbstractContraptionEntity::build).register();
	}

	private static <T extends Entity> CreateEntityBuilder<T, ?> register(String name, EntityFactory<T> factory,
		MobCategory group, int range, int updateFrequency,
		boolean sendVelocity, boolean immuneToFire, NonNullConsumer<EntityType.Builder<T>> propertyBuilder) {
		String id = Lang.asId(name);
		return (CreateEntityBuilder<T, ?>) Create.registrate()
			.entity(id, factory, group)
			.properties(b -> b.setTrackingRange(range)
				.setUpdateInterval(updateFrequency)
				.setShouldReceiveVelocityUpdates(sendVelocity))
			.properties(propertyBuilder)
			.properties(b -> {
				if (immuneToFire)
					b.fireImmune();
			});
	}

	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
	{
		event.registerEntityRenderer(ORIENTED_CONTRAPTION.get(), OrientedContraptionEntityRenderer::new);
		event.registerEntityRenderer(CONTROLLED_CONTRAPTION.get(), ContraptionEntityRenderer::new);
		event.registerEntityRenderer(GANTRY_CONTRAPTION.get(), ContraptionEntityRenderer::new);

		event.registerEntityRenderer(SUPER_GLUE.get(), SuperGlueRenderer::new);
		event.registerEntityRenderer(SEAT.get(), SeatEntity.Render::new);
		event.registerEntityRenderer(POTATO_PROJECTILE.get(), PotatoProjectileRenderer::new);
		event.registerEntityRenderer(CRAFTING_BLUEPRINT.get(), BlueprintRenderer::new);
	}


	public static void register() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get()
				.getModEventBus();
		modEventBus.addListener(AllEntityTypes::registerRenderers);
	}
}
