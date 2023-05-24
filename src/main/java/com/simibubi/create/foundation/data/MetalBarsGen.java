package com.simibubi.create.foundation.data;

import static com.simibubi.create.Create.REGISTRATE;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.EAST;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.NORTH;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.SOUTH;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WEST;

import java.util.function.Supplier;

import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.Create;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.client.model.generators.ModelFile;

public class MetalBarsGen {

	public static <P extends IronBarsBlock> NonNullBiConsumer<DataGenContext<Block, P>, RegistrateBlockstateProvider> barsBlockState(
		String name, boolean specialEdge) {
		return (c, p) -> {

			ModelFile post_ends = barsSubModel(p, name, "post_ends", specialEdge);
			ModelFile post = barsSubModel(p, name, "post", specialEdge);
			ModelFile cap = barsSubModel(p, name, "cap", specialEdge);
			ModelFile cap_alt = barsSubModel(p, name, "cap_alt", specialEdge);
			ModelFile side = barsSubModel(p, name, "side", specialEdge);
			ModelFile side_alt = barsSubModel(p, name, "side_alt", specialEdge);

			p.getMultipartBuilder(c.get())
				.part()
				.modelFile(post_ends)
				.addModel()
				.end()
				.part()
				.modelFile(post)
				.addModel()
				.condition(NORTH, false)
				.condition(EAST, false)
				.condition(SOUTH, false)
				.condition(WEST, false)
				.end()
				.part()
				.modelFile(cap)
				.addModel()
				.condition(NORTH, true)
				.condition(EAST, false)
				.condition(SOUTH, false)
				.condition(WEST, false)
				.end()
				.part()
				.modelFile(cap)
				.rotationY(90)
				.addModel()
				.condition(NORTH, false)
				.condition(EAST, true)
				.condition(SOUTH, false)
				.condition(WEST, false)
				.end()
				.part()
				.modelFile(cap_alt)
				.addModel()
				.condition(NORTH, false)
				.condition(EAST, false)
				.condition(SOUTH, true)
				.condition(WEST, false)
				.end()
				.part()
				.modelFile(cap_alt)
				.rotationY(90)
				.addModel()
				.condition(NORTH, false)
				.condition(EAST, false)
				.condition(SOUTH, false)
				.condition(WEST, true)
				.end()
				.part()
				.modelFile(side)
				.addModel()
				.condition(NORTH, true)
				.end()
				.part()
				.modelFile(side)
				.rotationY(90)
				.addModel()
				.condition(EAST, true)
				.end()
				.part()
				.modelFile(side_alt)
				.addModel()
				.condition(SOUTH, true)
				.end()
				.part()
				.modelFile(side_alt)
				.rotationY(90)
				.addModel()
				.condition(WEST, true)
				.end();
		};
	}

	private static ModelFile barsSubModel(RegistrateBlockstateProvider p, String name, String suffix,
		boolean specialEdge) {
		ResourceLocation barsTexture = p.modLoc("block/bars/" + name + "_bars");
		ResourceLocation edgeTexture = specialEdge ? p.modLoc("block/bars/" + name + "_bars_edge") : barsTexture;
		return p.models()
			.withExistingParent(name + "_" + suffix, p.modLoc("block/bars/" + suffix))
			.texture("bars", barsTexture)
			.texture("particle", barsTexture)
			.texture("edge", edgeTexture);
	}

	public static BlockEntry<IronBarsBlock> createBars(String name, boolean specialEdge,
		Supplier<DataIngredient> ingredient, MaterialColor color) {
		return REGISTRATE.block(name + "_bars", IronBarsBlock::new)
			.addLayer(() -> RenderType::cutoutMipped)
			.initialProperties(() -> Blocks.IRON_BARS)
			.properties(p -> p.sound(SoundType.COPPER)
				.color(color))
			.tag(AllBlockTags.WRENCH_PICKUP.tag)
			.tag(AllBlockTags.FAN_TRANSPARENT.tag)
			.transform(TagGen.pickaxeOnly())
			.blockstate(barsBlockState(name, specialEdge))
			.item()
			.model((c, p) -> {
				ResourceLocation barsTexture = p.modLoc("block/bars/" + name + "_bars");
				p.withExistingParent(c.getName(), Create.asResource("item/bars"))
					.texture("bars", barsTexture)
					.texture("edge", specialEdge ? p.modLoc("block/bars/" + name + "_bars_edge") : barsTexture);
			})
			.recipe((c, p) -> p.stonecutting(ingredient.get(), c::get, 4))
			.build()
			.register();
	}

}
