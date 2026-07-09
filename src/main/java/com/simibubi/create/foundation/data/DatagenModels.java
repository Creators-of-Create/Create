package com.simibubi.create.foundation.data;

import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;

import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.SlabBlock;

public class DatagenModels {
	private DatagenModels() {
	}

	public static void generateSlabBlock(RegistrateBlockModelGenerator prov, SlabBlock block, Identifier doubleSlab,
										 Material side, Material bottom, Material top) {
		try {
			Class<?> multiVariantClass = Class.forName(clientDataModelClass("MultiVariant"));
			Object plainVariant = Class.forName(clientDataModelClass("BlockModelGenerators"))
				.getMethod("plainVariant", Identifier.class)
				.invoke(null, doubleSlab);
			RegistrateBlockModelGenerator.class
				.getMethod("generateSlabBlock", SlabBlock.class, multiVariantClass, Material.class, Material.class,
					Material.class)
				.invoke(prov, block, plainVariant, side, bottom, top);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Could not generate slab block model variants", e);
		}
	}

	private static String clientDataModelClass(String simpleName) {
		return String.join(".", "net", "minecraft", "client", "data", "models", simpleName);
	}
}
