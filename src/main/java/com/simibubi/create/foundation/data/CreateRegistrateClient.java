package com.simibubi.create.foundation.data;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.decoration.encasing.CasingConnectivity;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.model.BakedModel;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.createmod.catnip.api.registry.RegisteredObjectsHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

final class CreateRegistrateClient {
	private CreateRegistrateClient() {
	}

	static <T extends Block> void registerCasingConnectivity(T entry,
															 BiConsumer<T, CasingConnectivity> consumer) {
		consumer.accept(entry, CreateClient.CASING_CONNECTIVITY);
	}

	@SuppressWarnings("unchecked")
	static <M> void registerBlockModel(Block entry, Supplier<NonNullFunction<M, ? extends M>> func) {
		CreateClient.MODEL_SWAPPER.getCustomBlockModels()
			.register(RegisteredObjectsHelper.getKeyOrThrow(entry),
				(NonNullFunction<BakedModel, ? extends BakedModel>) func.get());
	}

	@SuppressWarnings("unchecked")
	static <M> void registerItemModel(Item entry, Supplier<NonNullFunction<M, ? extends M>> func) {
		CreateClient.MODEL_SWAPPER.getCustomItemModels()
			.register(RegisteredObjectsHelper.getKeyOrThrow(entry),
				(NonNullFunction<BakedModel, ? extends BakedModel>) func.get());
	}

	static void registerCTBehviour(Block entry, Supplier<ConnectedTextureBehaviour> behaviorSupplier) {
		ConnectedTextureBehaviour behavior = behaviorSupplier.get();
		CreateClient.MODEL_SWAPPER.getCustomBlockModels()
			.register(RegisteredObjectsHelper.getKeyOrThrow(entry), model -> new CTModel(model, behavior));
	}
}
