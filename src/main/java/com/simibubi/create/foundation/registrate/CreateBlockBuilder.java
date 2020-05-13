package com.simibubi.create.foundation.registrate;

import java.util.LinkedList;
import java.util.List;

import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.block.Block;
import net.minecraft.block.Block.Properties;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class CreateBlockBuilder<T extends Block, P> extends BlockBuilder<T, P> {

	private List<NonNullConsumer<BlockEntry<T>>> registerCallbacks;

	protected CreateBlockBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
		NonNullFunction<Properties, T> factory, NonNullSupplier<Properties> initialProperties) {
		super(owner, parent, name, callback, factory, initialProperties);
		registerCallbacks = new LinkedList<>();
	}

	public static <T extends Block, P> CreateBlockBuilder<T, P> create(AbstractRegistrate<?> owner, P parent,
		String name, BuilderCallback callback, NonNullFunction<Block.Properties, T> factory, Material material) {
		return (CreateBlockBuilder<T, P>) new CreateBlockBuilder<>(owner, parent, name, callback, factory,
			() -> Block.Properties.create(material)).defaultBlockstate()
				.defaultLoot()
				.defaultLang();
	}

	public CreateBlockBuilder<T, P> connectedTextures(ConnectedTextureBehaviour behaviour) {
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> registerConnectedTexture(behaviour));
		return this;
	}

	public CreateBlockBuilder<T, P> onRegister(NonNullConsumer<BlockEntry<T>> callback) {
		registerCallbacks.add(callback);
		return this;
	}

	@Override
	public BlockEntry<T> register() {
		BlockEntry<T> register = super.register();
		registerCallbacks.forEach(func -> func.accept(register));
		return register;
	}

	@OnlyIn(Dist.CLIENT)
	private void registerConnectedTexture(ConnectedTextureBehaviour behaviour) {
		registerModelSwap(model -> new CTModel(model, behaviour));
	}

	@OnlyIn(Dist.CLIENT)
	private void registerModelSwap(NonNullFunction<IBakedModel, ? extends IBakedModel> modelFunc) {
		onRegister(entry -> CreateClient.getCustomBlockModels().register(entry, modelFunc));
	}

}
