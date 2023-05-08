package com.simibubi.create.content.logistics.block.funnel;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;

public class FunnelGenerator extends SpecialBlockStateGen {

	private String type;
	private ResourceLocation blockTexture;
	private boolean hasFilter;

	public FunnelGenerator(String type, boolean hasFilter) {
		this.type = type;
		this.hasFilter = hasFilter;
		this.blockTexture = Create.asResource("block/" + type + "_block");
	}

	@Override
	protected int getXRotation(BlockState state) {
		return state.getValue(FunnelBlock.FACING) == Direction.DOWN ? 180 : 0;
	}

	@Override
	protected int getYRotation(BlockState state) {
		return horizontalAngle(state.getValue(FunnelBlock.FACING)) + 180;
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> c, RegistrateBlockstateProvider p,
		BlockState s) {
		String prefix = "block/funnel/";
		String powered = s.getValue(FunnelBlock.POWERED) ? "_powered" : "_unpowered";
		String closed = s.getValue(FunnelBlock.POWERED) ? "_closed" : "_open";
		String extracting = s.getValue(FunnelBlock.EXTRACTING) ? "_push" : "_pull";
		Direction facing = s.getValue(FunnelBlock.FACING);
		boolean horizontal = facing.getAxis()
			.isHorizontal();
		String parent = horizontal ? "horizontal" : hasFilter ? "vertical" : "vertical_filterless";

		BlockModelBuilder model = p.models()
			.withExistingParent("block/" + type + "_funnel_" + parent + extracting + powered,
				p.modLoc(prefix + "block_" + parent))
			.texture("particle", blockTexture)
			.texture("base", p.modLoc(prefix + type + "_funnel"))
			.texture("redstone", p.modLoc(prefix + type + "_funnel" + powered))
			.texture("direction", p.modLoc(prefix + type + "_funnel" + extracting));

		if (horizontal)
			return model.texture("block", blockTexture);

		return model.texture("frame", p.modLoc(prefix + type + "_funnel_frame"))
			.texture("open", p.modLoc(prefix + "funnel" + closed));
	}

	public static NonNullBiConsumer<DataGenContext<Item, FunnelItem>, RegistrateItemModelProvider> itemModel(
		String type) {
		String prefix = "block/funnel/";
		ResourceLocation blockTexture = Create.asResource("block/" + type + "_block");
		return (c, p) -> {
			p.withExistingParent("item/" + type + "_funnel", p.modLoc("block/funnel/item"))
				.texture("particle", blockTexture)
				.texture("block", blockTexture)
				.texture("base", p.modLoc(prefix + type + "_funnel"))
				.texture("direction", p.modLoc(prefix + type + "_funnel_neutral"))
				.texture("redstone", p.modLoc(prefix + type + "_funnel_unpowered"));
		};
	}

}
