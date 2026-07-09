package com.simibubi.create.foundation.data;

import java.util.function.Function;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;
import com.tterrag.registrate.providers.generators.RegistrateItemModelGenerator;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import com.tterrag.registrate.providers.generators.ItemModelBuilder;
import com.tterrag.registrate.providers.generators.ModelFile;

public class AssetLookup {

	/**
	 * Custom block models packaged with other partials. Example:
	 * models/block/schematicannon/block.json <br>
	 * <br>
	 * Adding "powered", "vertical" will look for /block_powered_vertical.json
	 */
	public static ModelFile partialBaseModel(DataGenContext<?, ?> ctx, RegistrateBlockModelGenerator prov,
		String... suffix) {
		String string = "/block";
		for (String suf : suffix)
			if (!suf.isEmpty())
				string += "_" + suf;
		final String location = "block/" + ctx.getName() + string;
		return prov.models()
			.getExistingFile(prov.modLoc(location));
	}

	/**
	 * Custom block model from models/block/x.json
	 */
	public static ModelFile standardModel(DataGenContext<?, ?> ctx, RegistrateBlockModelGenerator prov) {
		return prov.models()
			.getExistingFile(prov.modLoc("block/" + ctx.getName()));
	}

	/**
	 * Generate item model inheriting from a seperate model in
	 * models/block/x/item.json
	 */
	public static <I extends BlockItem> ItemModelBuilder customItemModel(DataGenContext<Item, I> ctx,
		RegistrateItemModelGenerator prov) {
		return prov.blockItem(() -> ctx.getEntry()
			.getBlock(), "/item");
	}

	/**
	 * Generate item model inheriting from a seperate model in
	 * models/block/folders[0]/folders[1]/.../item.json "_" will be replaced by the
	 * item name
	 */
	public static <I extends BlockItem> NonNullBiConsumer<DataGenContext<Item, I>, RegistrateItemModelGenerator> customBlockItemModel(
		String... folders) {
		return (c, p) -> {
			String path = "block";
			for (String string : folders)
				path += "/" + ("_".equals(string) ? c.getName() : string);
			ItemModelBuilder model = p.withExistingParent(c.getName(), p.modLoc(path));
			p.createWithExistingModel(c.get(), model.getLocation());
		};
	}

	public static <I extends Item> NonNullBiConsumer<DataGenContext<Item, I>, RegistrateItemModelGenerator> customGenericItemModel(
		String... folders) {
		return (c, p) -> {
			String path = "block";
			for (String string : folders)
				path += "/" + ("_".equals(string) ? c.getName() : string);
			ItemModelBuilder model = p.withExistingParent(c.getName(), p.modLoc(path));
			p.createWithExistingModel(c.get(), model.getLocation());
		};
	}

	public static Function<BlockState, ModelFile> forPowered(DataGenContext<?, ?> ctx,
		RegistrateBlockModelGenerator prov) {
		return state -> state.getValue(BlockStateProperties.POWERED) ? partialBaseModel(ctx, prov, "powered")
			: partialBaseModel(ctx, prov);
	}

	public static Function<BlockState, ModelFile> forPowered(DataGenContext<?, ?> ctx,
		RegistrateBlockModelGenerator prov, String path) {
		return state -> prov.models()
			.getExistingFile(
				prov.modLoc("block/" + path + (state.getValue(BlockStateProperties.POWERED) ? "_powered" : "")));
	}

	public static Function<BlockState, ModelFile> withIndicator(DataGenContext<?, ?> ctx,
		RegistrateBlockModelGenerator prov, Function<BlockState, ModelFile> baseModelFunc, IntegerProperty property) {
		return state -> {
			Identifier baseModel = baseModelFunc.apply(state)
				.getLocation();
			Integer integer = state.getValue(property);
			return prov.models()
				.withExistingParent(ctx.getName() + "_" + integer, baseModel)
				.texture("indicator", "block/indicator/" + integer);
		};
	}

	public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelGenerator> existingItemModel() {
		return (c, p) -> p.createWithExistingModel(c.get(), p.modLoc("item/" + c.getName()));
	}

	public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelGenerator> itemModel(String name) {
		return (c, p) -> p.createWithExistingModel(c.get(), p.modLoc("item/" + name));
	}

	public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelGenerator> itemModelWithPartials() {
		return (c, p) -> {
			ItemModelBuilder model = p.withExistingParent("item/" + c.getName(), p.modLoc("item/" + c.getName() + "/item"));
			p.createWithExistingModel(c.get(), model.getLocation());
		};
	}

}
