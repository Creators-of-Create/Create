package com.simibubi.create.foundation.data;

import static com.simibubi.create.foundation.data.BlockStateGen.axisBlock;
import static com.simibubi.create.foundation.data.CreateRegistrate.casingConnectivity;
import static com.simibubi.create.foundation.data.CreateRegistrate.connectedTextures;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.base.CasingBlock;
import com.simibubi.create.content.contraptions.components.crank.ValveHandleBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonGenerator;
import com.simibubi.create.content.contraptions.relays.encased.EncasedCTBehaviour;
import com.simibubi.create.content.contraptions.relays.encased.EncasedShaftBlock;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelBlock;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelBlock.Shape;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelItem;
import com.simibubi.create.content.logistics.block.inventories.CrateBlock;
import com.simibubi.create.foundation.block.ItemUseOverrides;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.config.StressConfigDefaults;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Rarity;
import net.minecraft.state.properties.PistonType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
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

	public static <B extends EncasedShaftBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> encasedShaft(String casing,
		CTSpriteShiftEntry casingShift) {
		return builder -> builder.initialProperties(SharedProperties::stone)
			.properties(Block.Properties::nonOpaque)
			.onRegister(CreateRegistrate.connectedTextures(new EncasedCTBehaviour(casingShift)))
			.onRegister(CreateRegistrate.casingConnectivity(
				(block, cc) -> cc.make(block, casingShift, (s, f) -> f.getAxis() != s.get(EncasedShaftBlock.AXIS))))
			.blockstate((c, p) -> axisBlock(c, p, blockState -> p.models()
				.getExistingFile(p.modLoc("block/encased_shaft/block_" + casing)), true))
			.transform(StressConfigDefaults.setNoImpact())
			.loot((p, b) -> p.registerDropping(b, AllBlocks.SHAFT.get()))
			.item()
			.model(AssetLookup.customBlockItemModel("encased_shaft", "item_" + casing))
			.build();
	}

	public static <B extends ValveHandleBlock> NonNullUnaryOperator<BlockBuilder<B, CreateRegistrate>> valveHandle(
		@Nullable DyeColor color) {
		return b -> b.initialProperties(SharedProperties::softMetal)
			.blockstate((c, p) -> {
				String variant = color == null ? "copper" : color.getString();
				p.directionalBlock(c.get(), p.models()
					.withExistingParent(variant + "_valve_handle", p.modLoc("block/valve_handle"))
					.texture("3", p.modLoc("block/valve_handle/valve_handle_" + variant)));
			})
			.tag(AllBlockTags.BRITTLE.tag, AllBlockTags.VALVE_HANDLES.tag)
			.onRegister(ItemUseOverrides::addBlock)
			.item()
			.tag(AllItemTags.VALVE_HANDLES.tag)
			.build();
	}

	public static <B extends CasingBlock> NonNullUnaryOperator<BlockBuilder<B, CreateRegistrate>> casing(
		CTSpriteShiftEntry ct) {
		return b -> b.initialProperties(SharedProperties::stone)
			.blockstate((c, p) -> p.simpleBlock(c.get()))
			.onRegister(connectedTextures(new EncasedCTBehaviour(ct)))
			.onRegister(casingConnectivity((block, cc) -> cc.makeCasing(block, ct)))
			.simpleItem();
	}

	public static <B extends BeltTunnelBlock> NonNullUnaryOperator<BlockBuilder<B, CreateRegistrate>> beltTunnel(
		String type, ResourceLocation particleTexture) {
		return b -> b.initialProperties(SharedProperties::stone)
			.addLayer(() -> RenderType::getCutoutMipped)
			.properties(Block.Properties::nonOpaque)
			.blockstate((c, p) -> p.getVariantBuilder(c.get())
				.forAllStates(state -> {
					String id = "block/" + type + "_tunnel";
					Shape shape = state.get(BeltTunnelBlock.SHAPE);
					if (shape == BeltTunnelBlock.Shape.CLOSED)
						shape = BeltTunnelBlock.Shape.STRAIGHT;
					String shapeName = shape.getString();
					return ConfiguredModel.builder()
						.modelFile(p.models()
							.withExistingParent(id + "/" + shapeName, p.modLoc("block/belt_tunnel/" + shapeName))
							.texture("1", p.modLoc(id + "_top"))
							.texture("2", p.modLoc(id))
							.texture("3", p.modLoc(id + "_top_window"))
							.texture("particle", particleTexture))
						.rotationY(state.get(BeltTunnelBlock.HORIZONTAL_AXIS) == Axis.X ? 0 : 90)
						.build();
				}))
			.item(BeltTunnelItem::new)
			.model((c, p) -> {
				String id = type + "_tunnel";
				p.withExistingParent("item/" + id, p.modLoc("block/belt_tunnel/item"))
					.texture("1", p.modLoc("block/" + id + "_top"))
					.texture("2", p.modLoc("block/" + id))
					.texture("particle", particleTexture);
			})
			.build();
	}

	public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> mechanicalPiston(PistonType type) {
		return b -> b.initialProperties(SharedProperties::stone)
			.properties(p -> p.nonOpaque())
			.blockstate(new MechanicalPistonGenerator(type)::generate)
			.addLayer(() -> RenderType::getCutoutMipped)
			.transform(StressConfigDefaults.setImpact(4.0))
			.item()
			.transform(ModelGen.customItemModel("mechanical_piston", type.getString(), "item"));
	}

	public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> bearing(String prefix,
		String backTexture, boolean woodenTop) {
		ResourceLocation baseBlockModelLocation = Create.asResource("block/bearing/block");
		ResourceLocation baseItemModelLocation = Create.asResource("block/bearing/item");
		ResourceLocation topTextureLocation = Create.asResource("block/bearing_top" + (woodenTop ? "_wooden" : ""));
		ResourceLocation nookTextureLocation =
			Create.asResource("block/" + (woodenTop ? "andesite" : "brass") + "_casing");
		ResourceLocation sideTextureLocation = Create.asResource("block/" + prefix + "_bearing_side");
		ResourceLocation backTextureLocation = Create.asResource("block/" + backTexture);
		return b -> b.initialProperties(SharedProperties::stone)
			.properties(p -> p.nonOpaque())
			.blockstate((c, p) -> p.directionalBlock(c.get(), p.models()
				.withExistingParent(c.getName(), baseBlockModelLocation)
				.texture("side", sideTextureLocation)
				.texture("nook", nookTextureLocation)
				.texture("back", backTextureLocation)))
			.item()
			.model((c, p) -> p.withExistingParent(c.getName(), baseItemModelLocation)
				.texture("top", topTextureLocation)
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
			.properties(p -> type.equals("creative") ? p.rarity(Rarity.EPIC) : p)
			.transform(ModelGen.customItemModel("crate", type, "single"));
	}

}
