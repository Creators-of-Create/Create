package com.simibubi.create.content.logistics.block.diodes;

import java.util.Vector;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelFile.ExistingModelFile;

public abstract class AbstractDiodeGenerator extends SpecialBlockStateGen {

	private Vector<ModelFile> models;

	public static <I extends BlockItem> NonNullBiConsumer<DataGenContext<Item, I>, RegistrateItemModelProvider> diodeItemModel(
		boolean needsItemTexture) {
		return (c, p) -> {
			String name = c.getName();
			String path = "block/diodes/";
			ItemModelBuilder builder = p.withExistingParent(name, p.modLoc(path + name));
			if (!needsItemTexture)
				return;
			builder.texture("top", path + name + "/item");
		};
	}

	@Override
	protected final int getXRotation(BlockState state) {
		return 0;
	}

	@Override
	protected final int getYRotation(BlockState state) {
		return horizontalAngle(state.get(AbstractDiodeBlock.HORIZONTAL_FACING));
	}

	abstract <T extends Block> Vector<ModelFile> createModels(DataGenContext<Block, T> ctx, BlockModelProvider prov);

	abstract int getModelIndex(BlockState state);

	@Override
	public final <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		if (models == null)
			models = createModels(ctx, prov.models());
		return models.get(getModelIndex(state));
	}

	protected Vector<ModelFile> makeVector(int size) {
		return new Vector<>(size);
	}

	protected ExistingModelFile existingModel(BlockModelProvider prov, String name) {
		return prov.getExistingFile(existing(name));
	}

	protected ResourceLocation existing(String name) {
		return Create.asResource("block/diodes/" + name);
	}

	protected <T extends Block> ResourceLocation texture(DataGenContext<Block, T> ctx, String name) {
		return Create.asResource("block/diodes/" + ctx.getName() + "/" + name);
	}

	protected ResourceLocation poweredTorch() {
		return new ResourceLocation("block/redstone_torch");
	}

}
