package com.simibubi.create.foundation.utility.data;

import static com.simibubi.create.foundation.registrate.CreateRegistrate.connectedTextures;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.Create;
import com.simibubi.create.SharedProperties;
import com.simibubi.create.config.StressConfigDefaults;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.StandardCTBehaviour;
import com.simibubi.create.foundation.registrate.CreateRegistrate;
import com.simibubi.create.modules.contraptions.CasingBlock;
import com.simibubi.create.modules.contraptions.components.contraptions.piston.MechanicalPistonGenerator;
import com.simibubi.create.modules.logistics.block.inventories.CrateBlock;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.state.properties.PistonType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Direction.Axis;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;

public class BuilderTransformers {

	public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> cuckooClock() {
		return b -> b.initialProperties(SharedProperties::wooden)
			.blockstate((c, p) -> p.horizontalBlock(c.get(), p.models()
				.getExistingFile(p.modLoc("block/cuckoo_clock/block"))))
			.addLayer(() -> RenderType::getCutoutMipped)
			.transform(StressConfigDefaults.setImpact(1.0))
			.item()
			.transform(ModelGen.customItemModel("cuckoo_clock", "item"));
	}

	public static <B extends CasingBlock> NonNullUnaryOperator<BlockBuilder<B, CreateRegistrate>> casing(
		CTSpriteShiftEntry ct) {
		return b -> b.transform(connectedTextures(new StandardCTBehaviour(ct)))
			.initialProperties(SharedProperties::stone)
			.blockstate((c, p) -> p.simpleBlock(c.get()))
			.simpleItem();
	}

	public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> mechanicalPiston(PistonType type) {
		return b -> b.initialProperties(SharedProperties::stone)
			.properties(p -> p.nonOpaque())
			.blockstate(new MechanicalPistonGenerator(type)::generate)
			.addLayer(() -> RenderType::getCutoutMipped)
			.transform(StressConfigDefaults.setImpact(4.0))
			.item()
			.transform(ModelGen.customItemModel("mechanical_piston", type.getName(), "item"));
	}

	public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> bearing(String prefix,
		String backTexture) {
		ResourceLocation baseBlockModelLocation = Create.asResource("block/bearing/block");
		ResourceLocation baseItemModelLocation = Create.asResource("block/bearing/item");
		ResourceLocation sideTextureLocation = Create.asResource("block/" + prefix + "_bearing_side");
		ResourceLocation backTextureLocation = Create.asResource("block/" + backTexture);
		return b -> b.initialProperties(SharedProperties::stone)
			.properties(p -> p.nonOpaque())
			.blockstate((c, p) -> p.directionalBlock(c.get(), p.models()
				.withExistingParent(c.getName(), baseBlockModelLocation)
				.texture("side", sideTextureLocation)
				.texture("back", backTextureLocation)))
			.item()
			.model((c, p) -> p.withExistingParent(c.getName(), baseItemModelLocation)
				.texture("side", sideTextureLocation)
				.texture("back", backTextureLocation))
			.build();
	}

	public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> crate(String type) {
		return b -> b.initialProperties(SharedProperties::stone)
			.blockstate((c, p) -> {
				String[] variants = { "single", "top", "bottom", "left", "right" };
				Map<String, ModelFile> models = new HashMap<>();

				ResourceLocation crate = p.modLoc("block/crate_" + type);
				ResourceLocation side = p.modLoc("block/crate_" + type + "_side");
				ResourceLocation casing = p.modLoc("block/" + type + "_casing");

				for (String variant : variants)
					models.put(variant, p.models()
						.withExistingParent("block/crate/" + type + "/" + variant, p.modLoc("block/crate/" + variant))
						.texture("crate", crate)
						.texture("side", side)
						.texture("casing", casing));

				p.getVariantBuilder(c.get())
					.forAllStates(state -> {
						String variant = "single";
						int yRot = 0;

						if (state.get(CrateBlock.DOUBLE)) {
							Direction direction = state.get(CrateBlock.FACING);
							if (direction.getAxis() == Axis.X)
								yRot = 90;

							switch (direction) {
							case DOWN:
								variant = "top";
								break;
							case NORTH:
							case EAST:
								variant = "right";
								break;
							case UP:
								variant = "bottom";
								break;
							case SOUTH:
							case WEST:
							default:
								variant = "left";

							}
						}

						return ConfiguredModel.builder()
							.modelFile(models.get(variant))
							.rotationY(yRot)
							.build();
					});
			})
			.item()
			.transform(ModelGen.customItemModel("crate", type, "single"));
	}

}
