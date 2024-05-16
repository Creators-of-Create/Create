package com.simibubi.create.foundation.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.lib.visual.SimpleBlockEntityVisualizer;
import com.simibubi.create.Create;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockEntityBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.NonNullPredicate;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class CreateBlockEntityBuilder<T extends BlockEntity, P> extends BlockEntityBuilder<T, P> {

	@Nullable
	private NonNullSupplier<SimpleBlockEntityVisualizer.Factory<T>> visualFactory;
	private NonNullPredicate<T> renderNormally;

	private Collection<NonNullSupplier<? extends Collection<NonNullSupplier<? extends Block>>>> deferredValidBlocks =
		new ArrayList<>();

	public static <T extends BlockEntity, P> BlockEntityBuilder<T, P> create(AbstractRegistrate<?> owner, P parent,
		String name, BuilderCallback callback, BlockEntityFactory<T> factory) {
		return new CreateBlockEntityBuilder<>(owner, parent, name, callback, factory);
	}

	protected CreateBlockEntityBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
		BlockEntityFactory<T> factory) {
		super(owner, parent, name, callback, factory);
	}

	public CreateBlockEntityBuilder<T, P> validBlocksDeferred(
		NonNullSupplier<? extends Collection<NonNullSupplier<? extends Block>>> blocks) {
		deferredValidBlocks.add(blocks);
		return this;
	}

	@Override
	protected BlockEntityType<T> createEntry() {
		deferredValidBlocks.stream()
			.map(Supplier::get)
			.flatMap(Collection::stream)
			.forEach(this::validBlock);
		return super.createEntry();
	}

	public CreateBlockEntityBuilder<T, P> visual(
		NonNullSupplier<SimpleBlockEntityVisualizer.Factory<T>> visualFactory) {
		return visual(visualFactory, true);
	}

	public CreateBlockEntityBuilder<T, P> visual(
		NonNullSupplier<SimpleBlockEntityVisualizer.Factory<T>> visualFactory,
		boolean renderNormally) {
		return visual(visualFactory, be -> renderNormally);
	}

	public CreateBlockEntityBuilder<T, P> visual(
		NonNullSupplier<SimpleBlockEntityVisualizer.Factory<T>> visualFactory,
		NonNullPredicate<T> renderNormally) {
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
				SimpleBlockEntityVisualizer.builder(getEntry())
					.factory(visualFactory.get())
					.skipVanillaRender(be -> !renderNormally.test(be))
					.apply();
			}
		});
	}
}
