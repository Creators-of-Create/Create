package com.simibubi.create.foundation.data;

import java.util.function.BiFunction;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;
import com.jozufozu.flywheel.backend.instancing.entity.EntityInstance;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.EntityBuilder;
import com.tterrag.registrate.fabric.EnvExecutor;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

@ParametersAreNonnullByDefault
public class CreateEntityBuilder<T extends Entity, B extends FabricEntityTypeBuilder<T>, P> extends EntityBuilder<T, P> {

	@Nullable
	private NonNullSupplier<BiFunction<MaterialManager, T, EntityInstance<? super T>>> instanceFactory;
	private NonNullPredicate<T> renderNormally;

	public static <T extends Entity, P> EntityBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, EntityType.EntityFactory<T> factory, MobCategory classification) {
		return (new CreateEntityBuilder<>(owner, parent, name, callback, factory, classification)).defaultLang();
	}

	public CreateEntityBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, EntityType.EntityFactory<T> factory, MobCategory classification) {
		super(owner, parent, name, callback, factory, classification/*, (mobCategory, tEntityFactory) -> FabricEntityTypeBuilder.create(mobCategory, tEntityFactory)*/);
	}

	public CreateEntityBuilder<T, P> instance(NonNullSupplier<BiFunction<MaterialManager, T, EntityInstance<? super T>>> instanceFactory) {
		return instance(instanceFactory, true);
	}

	public CreateEntityBuilder<T, P> instance(NonNullSupplier<BiFunction<MaterialManager, T, EntityInstance<? super T>>> instanceFactory, boolean renderNormally) {
		return instance(instanceFactory, be -> true);
	}

	public CreateEntityBuilder<T, P> instance(NonNullSupplier<BiFunction<MaterialManager, T, EntityInstance<? super T>>> instanceFactory, NonNullPredicate<T> renderNormally) {
		if (this.instanceFactory == null) {
			EnvExecutor.runWhenOn(EnvType.CLIENT, () -> this::registerInstance);
		}

		this.instanceFactory = instanceFactory;
		this.renderNormally = renderNormally;

		return this;
	}

	protected void registerInstance() {
		// onRegister
		OneTimeEventReceiver.addModListener(FMLClientSetupEvent.class, $ -> {
			NonNullSupplier<BiFunction<MaterialManager, T, EntityInstance<? super T>>> instanceFactory = this.instanceFactory;
			if (instanceFactory != null) {
				NonNullPredicate<T> renderNormally = this.renderNormally;
				InstancedRenderRegistry.configure(getEntry())
					.factory(instanceFactory.get())
					.skipRender(be -> !renderNormally.test(be))
					.apply();
			}

		});
	}

}
