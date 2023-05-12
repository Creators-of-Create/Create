package com.simibubi.create.foundation.data;

import static com.simibubi.create.AllInteractionBehaviours.interactionBehaviour;
import static com.simibubi.create.AllMovementBehaviours.movementBehaviour;
import static com.simibubi.create.foundation.data.BlockStateGen.axisBlock;
import static com.simibubi.create.foundation.data.CreateRegistrate.casingConnectivity;
import static com.simibubi.create.foundation.data.CreateRegistrate.connectedTextures;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.base.CasingBlock;
import com.simibubi.create.content.contraptions.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.contraptions.components.crank.ValveHandleBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.interaction.DoorMovingInteraction;
import com.simibubi.create.content.contraptions.components.structureMovement.interaction.TrapdoorMovingInteraction;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonGenerator;
import com.simibubi.create.content.contraptions.relays.encased.EncasedCTBehaviour;
import com.simibubi.create.content.contraptions.relays.encased.EncasedCogCTBehaviour;
import com.simibubi.create.content.contraptions.relays.encased.EncasedCogwheelBlock;
import com.simibubi.create.content.contraptions.relays.encased.EncasedShaftBlock;
import com.simibubi.create.content.curiosities.deco.MetalScaffoldingBlock;
import com.simibubi.create.content.curiosities.deco.MetalScaffoldingBlockItem;
import com.simibubi.create.content.curiosities.deco.MetalScaffoldingCTBehaviour;
import com.simibubi.create.content.curiosities.deco.SlidingDoorBlock;
import com.simibubi.create.content.curiosities.deco.SlidingDoorMovementBehaviour;
import com.simibubi.create.content.curiosities.frames.CopycatBlock;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelBlock;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelBlock.Shape;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelItem;
import com.simibubi.create.content.logistics.trains.AbstractBogeyBlock;
import com.simibubi.create.content.logistics.trains.track.StandardBogeyBlock;
import com.simibubi.create.foundation.block.BlockStressDefaults;
import com.simibubi.create.foundation.block.ItemUseOverrides;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.HorizontalCTBehaviour;
import com.simibubi.create.foundation.utility.RegisteredObjects;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;

public class BuilderTransformers {

	public static <B extends EncasedShaftBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> encasedShaft(String casing,
		Supplier<CTSpriteShiftEntry> casingShift) {
		return builder -> encasedBase(builder, () -> AllBlocks.SHAFT.get())
			.onRegister(CreateRegistrate.connectedTextures(() -> new EncasedCTBehaviour(casingShift.get())))
			.onRegister(CreateRegistrate.casingConnectivity((block, cc) -> cc.make(block, casingShift.get(),
				(s, f) -> f.getAxis() != s.getValue(EncasedShaftBlock.AXIS))))
			.blockstate((c, p) -> axisBlock(c, p, blockState -> p.models()
				.getExistingFile(p.modLoc("block/encased_shaft/block_" + casing)), true))
			.item()
			.model(AssetLookup.customBlockItemModel("encased_shaft", "item_" + casing))
			.build();
	}

	@SuppressWarnings("deprecation")
	public static <B extends StandardBogeyBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> bogey() {
		return b -> b.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
			.properties(p -> p.noOcclusion())
			.transform(pickaxeOnly())
			.blockstate((c, p) -> BlockStateGen.horizontalAxisBlock(c, p, s -> p.models()
				.getExistingFile(p.modLoc("block/track/bogey/top"))))
			.loot((p, l) -> p.dropOther(l, AllBlocks.RAILWAY_CASING.get()))
			.onRegister(block -> AbstractBogeyBlock.registerStandardBogey(RegisteredObjects.getKeyOrThrow(block)));
	}

	public static <B extends CopycatBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> copycat() {
		return b -> b.initialProperties(SharedProperties::softMetal)
			.blockstate((c, p) -> p.simpleBlock(c.get(), p.models()
				.getExistingFile(p.mcLoc("air"))))
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.noOcclusion()
				.color(MaterialColor.NONE))
			.addLayer(() -> RenderType::solid)
			.addLayer(() -> RenderType::cutout)
			.addLayer(() -> RenderType::cutoutMipped)
			.addLayer(() -> RenderType::translucent)
			.color(() -> CopycatBlock::wrappedColor)
			.tag(AllBlockTags.SAFE_NBT.tag)
			.transform(TagGen.axeOrPickaxe());
	}

	public static <B extends TrapDoorBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> trapdoor(boolean orientable) {
		return b -> b.blockstate((c, p) -> {
			ModelFile bottom = AssetLookup.partialBaseModel(c, p, "bottom");
			ModelFile top = AssetLookup.partialBaseModel(c, p, "top");
			ModelFile open = AssetLookup.partialBaseModel(c, p, "open");
			if (orientable)
				p.trapdoorBlock(c.get(), bottom, top, open, orientable);
			else
				BlockStateGen.uvLockedTrapdoorBlock(c.get(), bottom, top, open)
					.accept(c, p);
		})
			.transform(pickaxeOnly())
			.tag(BlockTags.TRAPDOORS)
			.onRegister(interactionBehaviour(new TrapdoorMovingInteraction()))
			.item()
			.tag(ItemTags.TRAPDOORS)
			.build();
	}

	public static <B extends SlidingDoorBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> slidingDoor(String type) {
		return b -> b.initialProperties(Material.NETHER_WOOD) // for villager AI..
			.properties(p -> p.requiresCorrectToolForDrops()
				.strength(3.0F, 6.0F))
			.blockstate((c, p) -> {
				ModelFile bottom = AssetLookup.partialBaseModel(c, p, "bottom");
				ModelFile top = AssetLookup.partialBaseModel(c, p, "top");
				p.doorBlock(c.get(), bottom, bottom, bottom, bottom, top, top, top, top);
			})
			.addLayer(() -> RenderType::cutoutMipped)
			.transform(pickaxeOnly())
			.onRegister(interactionBehaviour(new DoorMovingInteraction()))
			.onRegister(movementBehaviour(new SlidingDoorMovementBehaviour()))
			.tag(BlockTags.DOORS)
			.tag(BlockTags.WOODEN_DOORS) // for villager AI
			.tag(AllBlockTags.NON_DOUBLE_DOOR.tag)
			.loot((lr, block) -> lr.add(block, BlockLoot.createDoorTable(block)))
			.item()
			.tag(ItemTags.DOORS)
			.tag(AllItemTags.CONTRAPTION_CONTROLLED.tag)
			.model((c, p) -> p.blockSprite(c, p.modLoc("item/" + type + "_door")))
			.build();
	}

	public static <B extends EncasedCogwheelBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> encasedCogwheel(
		String casing, Supplier<CTSpriteShiftEntry> casingShift) {
		return b -> encasedCogwheelBase(b, casing, casingShift, () -> AllBlocks.COGWHEEL.get(), false);
	}

	public static <B extends EncasedCogwheelBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> encasedLargeCogwheel(
		String casing, Supplier<CTSpriteShiftEntry> casingShift) {
		return b -> encasedCogwheelBase(b, casing, casingShift, () -> AllBlocks.LARGE_COGWHEEL.get(), true)
			.onRegister(CreateRegistrate.connectedTextures(() -> new EncasedCogCTBehaviour(casingShift.get())));
	}

	private static <B extends EncasedCogwheelBlock, P> BlockBuilder<B, P> encasedCogwheelBase(BlockBuilder<B, P> b,
		String casing, Supplier<CTSpriteShiftEntry> casingShift, Supplier<ItemLike> drop, boolean large) {
		String encasedSuffix = "_encased_cogwheel_side" + (large ? "_connected" : "");
		String blockFolder = large ? "encased_large_cogwheel" : "encased_cogwheel";
		String wood = casing.equals("brass") ? "dark_oak" : "spruce";
		String gearbox = casing.equals("brass") ? "brass_gearbox" : "gearbox";
		return encasedBase(b, drop).addLayer(() -> RenderType::cutoutMipped)
			.onRegister(CreateRegistrate.casingConnectivity((block, cc) -> cc.make(block, casingShift.get(),
				(s, f) -> f.getAxis() == s.getValue(EncasedCogwheelBlock.AXIS)
					&& !s.getValue(f.getAxisDirection() == AxisDirection.POSITIVE ? EncasedCogwheelBlock.TOP_SHAFT
						: EncasedCogwheelBlock.BOTTOM_SHAFT))))
			.blockstate((c, p) -> axisBlock(c, p, blockState -> {
				String suffix = (blockState.getValue(EncasedCogwheelBlock.TOP_SHAFT) ? "_top" : "")
					+ (blockState.getValue(EncasedCogwheelBlock.BOTTOM_SHAFT) ? "_bottom" : "");
				String modelName = c.getName() + suffix;
				return p.models()
					.withExistingParent(modelName, p.modLoc("block/" + blockFolder + "/block" + suffix))
					.texture("casing", Create.asResource("block/" + casing + "_casing"))
					.texture("particle", Create.asResource("block/" + casing + "_casing"))
					.texture("4", Create.asResource("block/" + gearbox))
					.texture("1", new ResourceLocation("block/stripped_" + wood + "_log_top"))
					.texture("side", Create.asResource("block/" + casing + encasedSuffix));
			}, false))
			.item()
			.model((c, p) -> p.withExistingParent(c.getName(), p.modLoc("block/" + blockFolder + "/item"))
				.texture("casing", Create.asResource("block/" + casing + "_casing"))
				.texture("particle", Create.asResource("block/" + casing + "_casing"))
				.texture("1", new ResourceLocation("block/stripped_" + wood + "_log_top"))
				.texture("side", Create.asResource("block/" + casing + encasedSuffix)))
			.build();
	}

	private static <B extends RotatedPillarKineticBlock, P> BlockBuilder<B, P> encasedBase(BlockBuilder<B, P> b,
		Supplier<ItemLike> drop) {
		return b.initialProperties(SharedProperties::stone)
			.properties(BlockBehaviour.Properties::noOcclusion)
			.transform(BlockStressDefaults.setNoImpact())
			.loot((p, lb) -> p.dropOther(lb, drop.get()));
	}

	public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> cuckooClock() {
		return b -> b.initialProperties(SharedProperties::wooden)
			.blockstate((c, p) -> p.horizontalBlock(c.get(), p.models()
				.getExistingFile(p.modLoc("block/cuckoo_clock/block"))))
			.addLayer(() -> RenderType::cutoutMipped)
			.transform(BlockStressDefaults.setImpact(1.0))
			.item()
			.transform(ModelGen.customItemModel("cuckoo_clock", "item"));
	}

	public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> ladder(String name,
		Supplier<DataIngredient> ingredient, MaterialColor color) {
		return b -> b.initialProperties(() -> Blocks.LADDER)
			.properties(p -> p.color(color))
			.addLayer(() -> RenderType::cutout)
			.blockstate((c, p) -> p.horizontalBlock(c.get(), p.models()
				.withExistingParent(c.getName(), p.modLoc("block/ladder"))
				.texture("0", p.modLoc("block/ladder_" + name + "_hoop"))
				.texture("1", p.modLoc("block/ladder_" + name))
				.texture("particle", p.modLoc("block/ladder_" + name))))
			.properties(p -> p.sound(SoundType.COPPER))
			.transform(pickaxeOnly())
			.tag(BlockTags.CLIMBABLE)
			.item()
			.recipe((c, p) -> p.stonecutting(ingredient.get(), c::get, 2))
			.model((c, p) -> p.blockSprite(c::get, p.modLoc("block/ladder_" + name)))
			.build();
	}

	public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> scaffold(String name,
		Supplier<DataIngredient> ingredient, MaterialColor color, CTSpriteShiftEntry scaffoldShift,
		CTSpriteShiftEntry scaffoldInsideShift, CTSpriteShiftEntry casingShift) {
		return b -> b.initialProperties(() -> Blocks.SCAFFOLDING)
			.properties(p -> p.sound(SoundType.COPPER)
				.color(color))
			.addLayer(() -> RenderType::cutout)
			.blockstate((c, p) -> p.getVariantBuilder(c.get())
				.forAllStatesExcept(s -> {
					String suffix = s.getValue(MetalScaffoldingBlock.BOTTOM) ? "_horizontal" : "";
					return ConfiguredModel.builder()
						.modelFile(p.models()
							.withExistingParent(c.getName() + suffix, p.modLoc("block/scaffold/block" + suffix))
							.texture("top", p.modLoc("block/funnel/" + name + "_funnel_frame"))
							.texture("inside", p.modLoc("block/scaffold/" + name + "_scaffold_inside"))
							.texture("side", p.modLoc("block/scaffold/" + name + "_scaffold"))
							.texture("casing", p.modLoc("block/" + name + "_casing"))
							.texture("particle", p.modLoc("block/scaffold/" + name + "_scaffold")))
						.build();
				}, MetalScaffoldingBlock.WATERLOGGED, MetalScaffoldingBlock.DISTANCE))
			.onRegister(connectedTextures(
				() -> new MetalScaffoldingCTBehaviour(scaffoldShift, scaffoldInsideShift, casingShift)))
			.transform(pickaxeOnly())
			.tag(BlockTags.CLIMBABLE)
			.item(MetalScaffoldingBlockItem::new)
			.recipe((c, p) -> p.stonecutting(ingredient.get(), c::get, 2))
			.model((c, p) -> p.withExistingParent(c.getName(), p.modLoc("block/" + c.getName())))
			.build();
	}

	public static <B extends ValveHandleBlock> NonNullUnaryOperator<BlockBuilder<B, CreateRegistrate>> valveHandle(
		@Nullable DyeColor color) {
		return b -> b.initialProperties(SharedProperties::copperMetal)
			.blockstate((c, p) -> {
				String variant = color == null ? "copper" : color.getSerializedName();
				p.directionalBlock(c.get(), p.models()
					.withExistingParent(variant + "_valve_handle", p.modLoc("block/valve_handle"))
					.texture("3", p.modLoc("block/valve_handle/valve_handle_" + variant)));
			})
			.tag(AllBlockTags.BRITTLE.tag, AllBlockTags.VALVE_HANDLES.tag)
			.transform(BlockStressDefaults.setGeneratorSpeed(ValveHandleBlock::getSpeedRange))
			.onRegister(ItemUseOverrides::addBlock)
			.item()
			.tag(AllItemTags.VALVE_HANDLES.tag)
			.build();
	}

	public static <B extends CasingBlock> NonNullUnaryOperator<BlockBuilder<B, CreateRegistrate>> casing(
		Supplier<CTSpriteShiftEntry> ct) {
		return b -> b.initialProperties(SharedProperties::stone)
			.properties(p -> p.sound(SoundType.WOOD))
			.transform(axeOrPickaxe())
			.blockstate((c, p) -> p.simpleBlock(c.get()))
			.onRegister(connectedTextures(() -> new EncasedCTBehaviour(ct.get())))
			.onRegister(casingConnectivity((block, cc) -> cc.makeCasing(block, ct.get())))
			.tag(AllBlockTags.CASING.tag)
			.item()
			.tag(AllItemTags.CASING.tag)
			.build();
	}

	public static <B extends CasingBlock> NonNullUnaryOperator<BlockBuilder<B, CreateRegistrate>> layeredCasing(
		Supplier<CTSpriteShiftEntry> ct, Supplier<CTSpriteShiftEntry> ct2) {
		return b -> b.initialProperties(SharedProperties::stone)
			.transform(axeOrPickaxe())
			.blockstate((c, p) -> p.simpleBlock(c.get(), p.models()
				.cubeColumn(c.getName(), ct.get()
					.getOriginalResourceLocation(),
					ct2.get()
						.getOriginalResourceLocation())))
			.onRegister(connectedTextures(() -> new HorizontalCTBehaviour(ct.get(), ct2.get())))
			.onRegister(casingConnectivity((block, cc) -> cc.makeCasing(block, ct.get())))
			.tag(AllBlockTags.CASING.tag)
			.item()
			.tag(AllItemTags.CASING.tag)
			.build();
	}

	public static <B extends BeltTunnelBlock> NonNullUnaryOperator<BlockBuilder<B, CreateRegistrate>> beltTunnel(
		String type, ResourceLocation particleTexture) {
		String prefix = "block/tunnel/" + type + "_tunnel";
		String funnel_prefix = "block/funnel/" + type + "_funnel";
		return b -> b.initialProperties(SharedProperties::stone)
			.addLayer(() -> RenderType::cutoutMipped)
			.properties(BlockBehaviour.Properties::noOcclusion)
			.transform(pickaxeOnly())
			.blockstate((c, p) -> p.getVariantBuilder(c.get())
				.forAllStates(state -> {
					Shape shape = state.getValue(BeltTunnelBlock.SHAPE);
					String window = shape == Shape.WINDOW ? "_window" : "";
					if (shape == BeltTunnelBlock.Shape.CLOSED)
						shape = BeltTunnelBlock.Shape.STRAIGHT;
					String shapeName = shape.getSerializedName();
					return ConfiguredModel.builder()
						.modelFile(p.models()
							.withExistingParent(prefix + "/" + shapeName, p.modLoc("block/belt_tunnel/" + shapeName))
							.texture("top", p.modLoc(prefix + "_top" + window))
							.texture("tunnel", p.modLoc(prefix))
							.texture("direction", p.modLoc(funnel_prefix + "_neutral"))
							.texture("frame", p.modLoc(funnel_prefix + "_frame"))
							.texture("particle", particleTexture))
						.rotationY(state.getValue(BeltTunnelBlock.HORIZONTAL_AXIS) == Axis.X ? 0 : 90)
						.build();
				}))
			.item(BeltTunnelItem::new)
			.model((c, p) -> {
				p.withExistingParent("item/" + type + "_tunnel", p.modLoc("block/belt_tunnel/item"))
					.texture("top", p.modLoc(prefix + "_top"))
					.texture("tunnel", p.modLoc(prefix))
					.texture("direction", p.modLoc(funnel_prefix + "_neutral"))
					.texture("frame", p.modLoc(funnel_prefix + "_frame"))
					.texture("particle", particleTexture);
			})
			.build();
	}

	public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> mechanicalPiston(PistonType type) {
		return b -> b.initialProperties(SharedProperties::stone)
			.properties(p -> p.noOcclusion())
			.blockstate(new MechanicalPistonGenerator(type)::generate)
			.addLayer(() -> RenderType::cutoutMipped)
			.transform(BlockStressDefaults.setImpact(4.0))
			.item()
			.transform(ModelGen.customItemModel("mechanical_piston", type.getSerializedName(), "item"));
	}

	public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> bearing(String prefix,
		String backTexture) {
		ResourceLocation baseBlockModelLocation = Create.asResource("block/bearing/block");
		ResourceLocation baseItemModelLocation = Create.asResource("block/bearing/item");
		ResourceLocation topTextureLocation = Create.asResource("block/bearing_top");
		ResourceLocation sideTextureLocation = Create.asResource("block/" + prefix + "_bearing_side");
		ResourceLocation backTextureLocation = Create.asResource("block/" + backTexture);
		return b -> b.initialProperties(SharedProperties::stone)
			.properties(p -> p.noOcclusion())
			.blockstate((c, p) -> p.directionalBlock(c.get(), p.models()
				.withExistingParent(c.getName(), baseBlockModelLocation)
				.texture("side", sideTextureLocation)
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
			.transform(axeOrPickaxe())
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
						return ConfiguredModel.builder()
							.modelFile(models.get(variant))
							.build();
					});
			})
			.item()
			.properties(p -> type.equals("creative") ? p.rarity(Rarity.EPIC) : p)
			.transform(ModelGen.customItemModel("crate", type, "single"));
	}

	public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> backtank(Supplier<ItemLike> drop) {
		return b -> b.blockstate((c, p) -> p.horizontalBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p)))
			.transform(pickaxeOnly())
			.addLayer(() -> RenderType::cutoutMipped)
			.transform(BlockStressDefaults.setImpact(4.0))
			.loot((lt, block) -> {
				Builder builder = LootTable.lootTable();
				LootItemCondition.Builder survivesExplosion = ExplosionCondition.survivesExplosion();
				lt.add(block, builder.withPool(LootPool.lootPool()
					.when(survivesExplosion)
					.setRolls(ConstantValue.exactly(1))
					.add(LootItem.lootTableItem(drop.get())
						.apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
						.apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
							.copy("Air", "Air"))
						.apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
							.copy("Enchantments", "Enchantments")))));
			});
	}

	public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> bell() {
		return b -> b.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.noOcclusion()
				.sound(SoundType.ANVIL))
			.transform(pickaxeOnly())
			.addLayer(() -> RenderType::cutoutMipped)
			.tag(AllBlockTags.BRITTLE.tag)
			.blockstate((c, p) -> p.horizontalBlock(c.getEntry(), state -> {
				String variant = state.getValue(BlockStateProperties.BELL_ATTACHMENT)
					.getSerializedName();
				return p.models()
					.withExistingParent(c.getName() + "_" + variant, p.modLoc("block/bell_base/block_" + variant));
			}))
			.item()
			.model((c, p) -> p.withExistingParent(c.getName(), p.modLoc("block/" + c.getName())))
			.tag(AllItemTags.CONTRAPTION_CONTROLLED.tag)
			.build();
	}

}
