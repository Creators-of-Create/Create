package com.simibubi.create.foundation.data;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;
import com.jozufozu.flywheel.backend.instancing.entity.IEntityInstanceFactory;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.EntityBuilder;
import com.tterrag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@ParametersAreNonnullByDefault
public class CreateEntityBuilder<T extends Entity, P> extends EntityBuilder<T, P> {

	@Nullable
	private NonNullSupplier<IEntityInstanceFactory<? super T>> instanceFactory;

	public static <T extends Entity, P> EntityBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, EntityType.EntityFactory<T> factory, MobCategory classification) {
		return (new CreateEntityBuilder<>(owner, parent, name, callback, factory, classification)).defaultLang();
	}

	public CreateEntityBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, EntityType.EntityFactory<T> factory, MobCategory classification) {
		super(owner, parent, name, callback, factory, classification);
	}

	public CreateEntityBuilder<T, P> instance(NonNullSupplier<IEntityInstanceFactory<? super T>> instanceFactory) {
		if (this.instanceFactory == null) {
			DistExecutor.runWhenOn(Dist.CLIENT, () -> this::registerInstance);
		}

		this.instanceFactory = instanceFactory;

		return this;
	}

	protected void registerInstance() {
		OneTimeEventReceiver.addModListener(FMLClientSetupEvent.class, $ -> {
			NonNullSupplier<IEntityInstanceFactory<? super T>> instanceFactory = this.instanceFactory;
			if (instanceFactory != null) {
				InstancedRenderRegistry.getInstance().register(getEntry(), instanceFactory.get());
			}

		});
	}

}
