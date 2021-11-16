package com.simibubi.create.foundation.data;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;
import com.jozufozu.flywheel.backend.instancing.entity.IEntityInstanceFactory;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.EntityBuilder;
import com.tterrag.registrate.fabric.EnvExecutor;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;

@ParametersAreNonnullByDefault
public class CreateEntityBuilder<T extends Entity, B extends FabricEntityTypeBuilder<T>, P> extends EntityBuilder<T, B, P> {

	@Nullable
	private NonNullSupplier<IEntityInstanceFactory<? super T>> instanceFactory;

	public static <T extends Entity, P> EntityBuilder<T, FabricEntityTypeBuilder<T>, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, EntityType.EntityFactory<T> factory, MobCategory classification) {
		return (new CreateEntityBuilder<>(owner, parent, name, callback, factory, classification)).defaultLang();
	}

	public CreateEntityBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, EntityType.EntityFactory<T> factory, MobCategory classification) {
		super(owner, parent, name, callback, factory, classification, (mobCategory, tEntityFactory) -> (B) FabricEntityTypeBuilder.create(mobCategory));
	}

	public CreateEntityBuilder<T, B, P> instance(NonNullSupplier<IEntityInstanceFactory<? super T>> instanceFactory) {
		if (this.instanceFactory == null) {
			EnvExecutor.runWhenOn(EnvType.CLIENT, () -> this::registerInstance);
		}

		this.instanceFactory = instanceFactory;

		return this;
	}

	protected void registerInstance() {
//		OneTimeEventReceiver.addModListener(FMLClientSetupEvent.class, $ -> {
			NonNullSupplier<IEntityInstanceFactory<? super T>> instanceFactory = this.instanceFactory;
			if (instanceFactory != null) {
				InstancedRenderRegistry.getInstance()
					.entity(getEntry())
					.factory(instanceFactory.get());
			}

//		});
	}

}
