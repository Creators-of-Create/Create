package com.simibubi.create.foundation.data;

import java.util.function.BiFunction;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockEntityBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.NonNullPredicate;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class CreateBlockEntityBuilder<T extends BlockEntity, P> extends BlockEntityBuilder<T, P> {

	@Nullable
	private NonNullSupplier<BiFunction<MaterialManager, T, BlockEntityInstance<? super T>>> instanceFactory;
	private NonNullPredicate<T> renderNormally;

	public static <T extends BlockEntity, P> BlockEntityBuilder<T, P> create(AbstractRegistrate<?> owner, P parent,
		String name, BuilderCallback callback, BlockEntityFactory<T> factory) {
		return new CreateBlockEntityBuilder<>(owner, parent, name, callback, factory);
	}

	protected CreateBlockEntityBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
		BlockEntityFactory<T> factory) {
		super(owner, parent, name, callback, factory);
	}

	public CreateBlockEntityBuilder<T, P> instance(NonNullSupplier<BiFunction<MaterialManager, T, BlockEntityInstance<? super T>>> instanceFactory) {
		return instance(instanceFactory, true);
	}

	public CreateBlockEntityBuilder<T, P> instance(NonNullSupplier<BiFunction<MaterialManager, T, BlockEntityInstance<? super T>>> instanceFactory, boolean renderNormally) {
		return instance(instanceFactory, be -> renderNormally);
	}

	public CreateBlockEntityBuilder<T, P> instance(NonNullSupplier<BiFunction<MaterialManager, T, BlockEntityInstance<? super T>>> instanceFactory, NonNullPredicate<T> renderNormally) {
		if (this.instanceFactory == null) {
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::registerInstance);
		}

		this.instanceFactory = instanceFactory;
		this.renderNormally = renderNormally;

		return this;
	}

	protected void registerInstance() {
		OneTimeEventReceiver.addModListener(FMLClientSetupEvent.class, $ -> {
			NonNullSupplier<BiFunction<MaterialManager, T, BlockEntityInstance<? super T>>> instanceFactory = this.instanceFactory;
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
