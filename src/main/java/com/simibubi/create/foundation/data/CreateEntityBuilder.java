package com.simibubi.create.foundation.data;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.Create;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.EntityBuilder;
import com.tterrag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import dev.engine_room.flywheel.lib.visual.SimpleEntityVisualizer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.NonNullPredicate;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@ParametersAreNonnullByDefault
public class CreateEntityBuilder<T extends Entity, P> extends EntityBuilder<T, P> {

	@Nullable
	private NonNullSupplier<SimpleEntityVisualizer.Factory<T>> visualFactory;
	private NonNullPredicate<T> renderNormally;

	public static <T extends Entity, P> EntityBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, EntityType.EntityFactory<T> factory, MobCategory classification) {
		return (new CreateEntityBuilder<>(owner, parent, name, callback, factory, classification)).defaultLang();
	}

	public CreateEntityBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, EntityType.EntityFactory<T> factory, MobCategory classification) {
		super(owner, parent, name, callback, factory, classification);
	}

	public CreateEntityBuilder<T, P> visual(NonNullSupplier<SimpleEntityVisualizer.Factory<T>> visualFactory) {
		return visual(visualFactory, true);
	}

	public CreateEntityBuilder<T, P> visual(NonNullSupplier<SimpleEntityVisualizer.Factory<T>> visualFactory, boolean renderNormally) {
		return visual(visualFactory, entity -> renderNormally);
	}

	public CreateEntityBuilder<T, P> visual(NonNullSupplier<SimpleEntityVisualizer.Factory<T>> visualFactory, NonNullPredicate<T> renderNormally) {
		if (this.visualFactory == null) {
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::registerVisualizer);
		}

		this.visualFactory = visualFactory;
		this.renderNormally = renderNormally;

		return this;
	}

	protected void registerVisualizer() {
		OneTimeEventReceiver.addModListener(Create.REGISTRATE, FMLClientSetupEvent.class, $ -> {
			var visualFactory = this.visualFactory;
			if (visualFactory != null) {
				NonNullPredicate<T> renderNormally = this.renderNormally;
				SimpleEntityVisualizer.builder(getEntry())
					.factory(visualFactory.get())
					.skipVanillaRender(entity -> !renderNormally.test(entity))
					.apply();
			}
		});
	}
}
