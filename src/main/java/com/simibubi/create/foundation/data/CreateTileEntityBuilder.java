package com.simibubi.create.foundation.data;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.logistics.block.redstone.AnalogLeverInstance;
import com.simibubi.create.foundation.render.backend.instancing.IRendererFactory;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderRegistry;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.TileEntityBuilder;
import com.tterrag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import javax.annotation.Nullable;
import java.util.function.Function;

public class CreateTileEntityBuilder<T extends TileEntity, P> extends TileEntityBuilder<T, P> {

    @Nullable
    private NonNullSupplier<IRendererFactory<? super T>> instanceFactory;

    public static <T extends TileEntity, P> TileEntityBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, NonNullFunction<TileEntityType<T>, ? extends T> factory) {
        return new CreateTileEntityBuilder<>(owner, parent, name, callback, factory);
    }

    protected CreateTileEntityBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, NonNullFunction<TileEntityType<T>, ? extends T> factory) {
        super(owner, parent, name, callback, factory);
    }

    public CreateTileEntityBuilder<T, P> instance(NonNullSupplier<IRendererFactory<? super T>> instanceFactory) {
        if (this.instanceFactory == null) {
            DistExecutor.runWhenOn(Dist.CLIENT, () -> this::registerInstance);
        }

        this.instanceFactory = instanceFactory;

        return this;
    }

    protected void registerInstance() {
        OneTimeEventReceiver.addModListener(FMLClientSetupEvent.class, ($) -> {
            NonNullSupplier<IRendererFactory<? super T>> instanceFactory = this.instanceFactory;
            if (instanceFactory != null) {
                InstancedTileRenderRegistry.instance.register(getEntry(), instanceFactory.get());
            }

        });
    }
}
