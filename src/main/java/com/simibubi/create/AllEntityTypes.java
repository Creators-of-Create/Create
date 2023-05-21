package com.simibubi.create;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import com.simibubi.create.content.contraptions.gantry.GantryContraptionEntity;
import com.simibubi.create.content.contraptions.glue.SuperGlueEntity;
import com.simibubi.create.content.contraptions.glue.SuperGlueRenderer;
import com.simibubi.create.content.contraptions.render.ContraptionEntityRenderer;
import com.simibubi.create.content.contraptions.render.OrientedContraptionEntityRenderer;
import com.simibubi.create.content.equipment.blueprint.BlueprintEntity;
import com.simibubi.create.content.equipment.blueprint.BlueprintRenderer;
import com.simibubi.create.content.equipment.potatoCannon.PotatoProjectileEntity;
import com.simibubi.create.content.equipment.potatoCannon.PotatoProjectileRenderer;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntityRenderer;
import com.simibubi.create.content.trains.entity.CarriageContraptionInstance;
import com.simibubi.create.foundation.data.CreateEntityBuilder;
import com.simibubi.create.foundation.utility.Lang;
import com.tterrag.registrate.util.entry.EntityEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType.EntityFactory;
import net.minecraft.world.entity.MobCategory;

public class AllEntityTypes {

	public static final EntityEntry<OrientedContraptionEntity> ORIENTED_CONTRAPTION = contraption("contraption",
		OrientedContraptionEntity::new, () -> OrientedContraptionEntityRenderer::new, 5, 3, true).register();
	public static final EntityEntry<ControlledContraptionEntity> CONTROLLED_CONTRAPTION =
		contraption("stationary_contraption", ControlledContraptionEntity::new, () -> ContraptionEntityRenderer::new,
			20, 40, false).register();
	public static final EntityEntry<GantryContraptionEntity> GANTRY_CONTRAPTION = contraption("gantry_contraption",
		GantryContraptionEntity::new, () -> ContraptionEntityRenderer::new, 10, 40, false).register();
	public static final EntityEntry<CarriageContraptionEntity> CARRIAGE_CONTRAPTION =
		contraption("carriage_contraption", CarriageContraptionEntity::new,
			() -> CarriageContraptionEntityRenderer::new, 15, 3, true).instance(() -> CarriageContraptionInstance::new)
				.register();

	public static final EntityEntry<SuperGlueEntity> SUPER_GLUE =
		register("super_glue", SuperGlueEntity::new, () -> SuperGlueRenderer::new, MobCategory.MISC, 10,
			Integer.MAX_VALUE, false, true, SuperGlueEntity::build).register();

	public static final EntityEntry<BlueprintEntity> CRAFTING_BLUEPRINT =
		register("crafting_blueprint", BlueprintEntity::new, () -> BlueprintRenderer::new, MobCategory.MISC, 10,
			Integer.MAX_VALUE, false, true, BlueprintEntity::build).register();

	public static final EntityEntry<PotatoProjectileEntity> POTATO_PROJECTILE =
		register("potato_projectile", PotatoProjectileEntity::new, () -> PotatoProjectileRenderer::new,
			MobCategory.MISC, 4, 20, true, false, PotatoProjectileEntity::build).register();

	public static final EntityEntry<SeatEntity> SEAT = register("seat", SeatEntity::new, () -> SeatEntity.Render::new,
		MobCategory.MISC, 5, Integer.MAX_VALUE, false, true, SeatEntity::build).register();

	//

	private static <T extends Entity> CreateEntityBuilder<T, ?> contraption(String name, EntityFactory<T> factory,
		NonNullSupplier<NonNullFunction<EntityRendererProvider.Context, EntityRenderer<? super T>>> renderer, int range,
		int updateFrequency, boolean sendVelocity) {
		return register(name, factory, renderer, MobCategory.MISC, range, updateFrequency, sendVelocity, true,
			AbstractContraptionEntity::build);
	}

	private static <T extends Entity> CreateEntityBuilder<T, ?> register(String name, EntityFactory<T> factory,
		NonNullSupplier<NonNullFunction<EntityRendererProvider.Context, EntityRenderer<? super T>>> renderer,
		MobCategory group, int range, int updateFrequency, boolean sendVelocity, boolean immuneToFire,
		NonNullConsumer<EntityType.Builder<T>> propertyBuilder) {
		String id = Lang.asId(name);
		return (CreateEntityBuilder<T, ?>) Create.REGISTRATE
			.entity(id, factory, group)
			.properties(b -> b.setTrackingRange(range)
				.setUpdateInterval(updateFrequency)
				.setShouldReceiveVelocityUpdates(sendVelocity))
			.properties(propertyBuilder)
			.properties(b -> {
				if (immuneToFire)
					b.fireImmune();
			})
			.renderer(renderer);
	}

	public static void register() {}
}
