package com.simibubi.create;

import com.simibubi.create.foundation.block.IHaveColorHandler;
import com.simibubi.create.foundation.block.IHaveCustomBlockItem;
import com.simibubi.create.foundation.block.IHaveNoBlockItem;
import com.simibubi.create.foundation.block.ProperStairsBlock;
import com.simibubi.create.foundation.block.RenderUtilityAxisBlock;
import com.simibubi.create.foundation.block.RenderUtilityBlock;
import com.simibubi.create.foundation.block.RenderUtilityDirectionalBlock;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter.CTType;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.world.OxidizingBlock;
import com.simibubi.create.modules.IModule;
import com.simibubi.create.modules.contraptions.CasingBlock;
import com.simibubi.create.modules.contraptions.components.actors.DrillBlock;
import com.simibubi.create.modules.contraptions.components.actors.DrillBlock.DrillHeadBlock;
import com.simibubi.create.modules.contraptions.components.actors.HarvesterBlock;
import com.simibubi.create.modules.contraptions.components.actors.HarvesterBlock.HarvesterBladeBlock;
import com.simibubi.create.modules.contraptions.components.contraptions.bearing.MechanicalBearingBlock;
import com.simibubi.create.modules.contraptions.components.contraptions.chassis.LinearChassisBlock;
import com.simibubi.create.modules.contraptions.components.contraptions.chassis.RadialChassisBlock;
import com.simibubi.create.modules.contraptions.components.contraptions.mounted.CartAssemblerBlock;
import com.simibubi.create.modules.contraptions.components.contraptions.mounted.CartAssemblerBlock.MinecartAnchorBlock;
import com.simibubi.create.modules.contraptions.components.contraptions.piston.MechanicalPistonBlock;
import com.simibubi.create.modules.contraptions.components.contraptions.piston.MechanicalPistonHeadBlock;
import com.simibubi.create.modules.contraptions.components.contraptions.piston.PistonPoleBlock;
import com.simibubi.create.modules.contraptions.components.crafter.MechanicalCrafterBlock;
import com.simibubi.create.modules.contraptions.components.crusher.CrushingWheelBlock;
import com.simibubi.create.modules.contraptions.components.crusher.CrushingWheelControllerBlock;
import com.simibubi.create.modules.contraptions.components.deployer.DeployerBlock;
import com.simibubi.create.modules.contraptions.components.fan.EncasedFanBlock;
import com.simibubi.create.modules.contraptions.components.mixer.MechanicalMixerBlock;
import com.simibubi.create.modules.contraptions.components.motor.MotorBlock;
import com.simibubi.create.modules.contraptions.components.press.MechanicalPressBlock;
import com.simibubi.create.modules.contraptions.components.saw.SawBlock;
import com.simibubi.create.modules.contraptions.components.turntable.TurntableBlock;
import com.simibubi.create.modules.contraptions.components.waterwheel.WaterWheelBlock;
import com.simibubi.create.modules.contraptions.processing.BasinBlock;
import com.simibubi.create.modules.contraptions.redstone.ContactBlock;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTunnelBlock;
import com.simibubi.create.modules.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.modules.contraptions.relays.elementary.ShaftBlock;
import com.simibubi.create.modules.contraptions.relays.elementary.ShaftHalfBlock;
import com.simibubi.create.modules.contraptions.relays.encased.ClutchBlock;
import com.simibubi.create.modules.contraptions.relays.encased.EncasedBeltBlock;
import com.simibubi.create.modules.contraptions.relays.encased.EncasedShaftBlock;
import com.simibubi.create.modules.contraptions.relays.encased.GearshiftBlock;
import com.simibubi.create.modules.contraptions.relays.gauge.GaugeBlock;
import com.simibubi.create.modules.contraptions.relays.gearbox.GearboxBlock;
import com.simibubi.create.modules.curiosities.partialWindows.WindowInABlockBlock;
import com.simibubi.create.modules.curiosities.symmetry.block.CrossPlaneSymmetryBlock;
import com.simibubi.create.modules.curiosities.symmetry.block.PlaneSymmetryBlock;
import com.simibubi.create.modules.curiosities.symmetry.block.TriplePlaneSymmetryBlock;
import com.simibubi.create.modules.gardens.CocoaLogBlock;
import com.simibubi.create.modules.logistics.block.RedstoneLinkBlock;
import com.simibubi.create.modules.logistics.block.StockswitchBlock;
import com.simibubi.create.modules.logistics.block.belts.BeltObserverBlock;
import com.simibubi.create.modules.logistics.block.belts.FunnelBlock;
import com.simibubi.create.modules.logistics.block.diodes.FlexpeaterBlock;
import com.simibubi.create.modules.logistics.block.diodes.PulseRepeaterBlock;
import com.simibubi.create.modules.logistics.block.extractor.ExtractorBlock;
import com.simibubi.create.modules.logistics.block.extractor.LinkedExtractorBlock;
import com.simibubi.create.modules.logistics.block.inventories.FlexcrateBlock;
import com.simibubi.create.modules.logistics.block.transposer.LinkedTransposerBlock;
import com.simibubi.create.modules.logistics.block.transposer.TransposerBlock;
import com.simibubi.create.modules.palettes.CTGlassBlock;
import com.simibubi.create.modules.palettes.GlassPaneBlock;
import com.simibubi.create.modules.palettes.LayeredCTBlock;
import com.simibubi.create.modules.palettes.VolcanicRockBlock;
import com.simibubi.create.modules.schematics.block.CreativeCrateBlock;
import com.simibubi.create.modules.schematics.block.SchematicTableBlock;
import com.simibubi.create.modules.schematics.block.SchematicannonBlock;

import net.minecraft.block.Block;
import net.minecraft.block.Block.Properties;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.GlassBlock;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

public enum AllBlocks {

	__SCHEMATICS__(),
	SCHEMATICANNON(new SchematicannonBlock()),
	SCHEMATICANNON_CONNECTOR(new RenderUtilityBlock()),
	SCHEMATICANNON_PIPE(new RenderUtilityBlock()),
	CREATIVE_CRATE(new CreativeCrateBlock()),
	SCHEMATIC_TABLE(new SchematicTableBlock()),

	__CONTRAPTIONS__(),
	SHAFT(new ShaftBlock(Properties.from(Blocks.ANDESITE))),
	COGWHEEL(new CogWheelBlock(false)),
	LARGE_COGWHEEL(new CogWheelBlock(true)),
	SHAFTLESS_COGWHEEL(new RenderUtilityAxisBlock()),
	ENCASED_SHAFT(new EncasedShaftBlock()),
	ENCASED_BELT(new EncasedBeltBlock()),
	CLUTCH(new ClutchBlock()),
	GEARSHIFT(new GearshiftBlock()),
	GEARBOX(new GearboxBlock()),
	BELT(new BeltBlock()),
	BELT_PULLEY(new RenderUtilityAxisBlock()),
	MOTOR(new MotorBlock()),
	WATER_WHEEL(new WaterWheelBlock()),
	ENCASED_FAN(new EncasedFanBlock()),
	ENCASED_FAN_INNER(new RenderUtilityDirectionalBlock()),
	TURNTABLE(new TurntableBlock()),
	SHAFT_HALF(new ShaftHalfBlock()),

	CRUSHING_WHEEL(new CrushingWheelBlock()),
	CRUSHING_WHEEL_CONTROLLER(new CrushingWheelControllerBlock()),
	MECHANICAL_PRESS(new MechanicalPressBlock()),
	MECHANICAL_PRESS_HEAD(new MechanicalPressBlock.Head()),
	MECHANICAL_MIXER(new MechanicalMixerBlock()),
	MECHANICAL_MIXER_POLE(new RenderUtilityBlock()),
	MECHANICAL_MIXER_HEAD(new RenderUtilityBlock()),
	BASIN(new BasinBlock()),
	DEPLOYER(new DeployerBlock()),
	DEPLOYER_POLE(new RenderUtilityBlock()),
	DEPLOYER_HAND_POINTING(new RenderUtilityBlock()),
	DEPLOYER_HAND_PUNCHING(new RenderUtilityBlock()),
	DEPLOYER_HAND_HOLDING(new RenderUtilityBlock()),
	MECHANICAL_CRAFTER(new MechanicalCrafterBlock()),
	MECHANICAL_CRAFTER_LID(new RenderUtilityBlock()),
	MECHANICAL_CRAFTER_ARROW(new RenderUtilityBlock()),
	MECHANICAL_CRAFTER_BELT_FRAME(new RenderUtilityBlock()),
	MECHANICAL_CRAFTER_BELT(new RenderUtilityBlock()),
	SPEED_GAUGE(new GaugeBlock(GaugeBlock.Type.SPEED)),
	STRESS_GAUGE(new GaugeBlock(GaugeBlock.Type.STRESS)),
	GAUGE_DIAL(new RenderUtilityBlock()),
	GAUGE_INDICATOR(new RenderUtilityBlock()),
	GAUGE_HEAD(new GaugeBlock.Head()),

	MECHANICAL_PISTON(new MechanicalPistonBlock(false)),
	STICKY_MECHANICAL_PISTON(new MechanicalPistonBlock(true)),
	MECHANICAL_PISTON_HEAD(new MechanicalPistonHeadBlock()),
	PISTON_POLE(new PistonPoleBlock()),
	MECHANICAL_BEARING(new MechanicalBearingBlock()),
	MECHANICAL_BEARING_TOP(new ShaftHalfBlock()),
	TRANSLATION_CHASSIS(new LinearChassisBlock()),
	TRANSLATION_CHASSIS_SECONDARY(new LinearChassisBlock()),
	ROTATION_CHASSIS(new RadialChassisBlock()),
	DRILL(new DrillBlock()),
	DRILL_HEAD(new DrillHeadBlock()),
	SAW(new SawBlock()),
	HARVESTER(new HarvesterBlock()),
	HARVESTER_BLADE(new HarvesterBladeBlock()),
	CART_ASSEMBLER(new CartAssemblerBlock()),
	MINECART_ANCHOR(new MinecartAnchorBlock()),

	ANDESITE_CASING(new CasingBlock("andesite_casing")),
	COPPER_CASING(new CasingBlock("copper_casing")),
	BRASS_CASING(new CasingBlock("crafter_top")),

	__LOGISTICS__(),
	CONTACT(new ContactBlock()),
	REDSTONE_BRIDGE(new RedstoneLinkBlock()),
	STOCKSWITCH(new StockswitchBlock()),
	FLEXCRATE(new FlexcrateBlock()),
	EXTRACTOR(new ExtractorBlock()),
	VERTICAL_EXTRACTOR(new ExtractorBlock.Vertical()),
	LINKED_EXTRACTOR(new LinkedExtractorBlock()),
	VERTICAL_LINKED_EXTRACTOR(new LinkedExtractorBlock.Vertical()),
	TRANSPOSER(new TransposerBlock()),
	VERTICAL_TRANSPOSER(new TransposerBlock.Vertical()),
	LINKED_TRANSPOSER(new LinkedTransposerBlock()),
	VERTICAL_LINKED_TRANSPOSER(new LinkedTransposerBlock.Vertical()),
	BELT_FUNNEL(new FunnelBlock()),
	VERTICAL_FUNNEL(new FunnelBlock.Vertical()),
	BELT_TUNNEL(new BeltTunnelBlock()),
	BELT_TUNNEL_FLAP(new RenderUtilityBlock()),
	BELT_TUNNEL_INDICATOR(new RenderUtilityBlock()),
	ENTITY_DETECTOR(new BeltObserverBlock()),
	PULSE_REPEATER(new PulseRepeaterBlock()),
	FLEXPEATER(new FlexpeaterBlock()),
	FLEXPEATER_INDICATOR(new RenderUtilityBlock()),
//	LOGISTICAL_CASING(new NewLogisticalCasingBlock()),
//	LOGISTICAL_CONTROLLER(new LogisticalControllerBlock()),
//	LOGISTICAL_CONTROLLER_INDICATOR(new LogisticalControllerIndicatorBlock()),
//	LOGISTICAL_INDEX(new LogisticalIndexBlock()),
//	PACKAGE_FUNNEL(new PackageFunnelBlock()),
//	LOGISTICIANS_TABLE(new LogisticiansTableBlock()),
//	LOGISTICIANS_TABLE_INDICATOR(new RenderUtilityBlock()),

	__CURIOSITIES__(),
	SYMMETRY_PLANE(new PlaneSymmetryBlock()),
	SYMMETRY_CROSSPLANE(new CrossPlaneSymmetryBlock()),
	SYMMETRY_TRIPLEPLANE(new TriplePlaneSymmetryBlock()),
	WINDOW_IN_A_BLOCK(new WindowInABlockBlock()),

	__GARDENS__(),
	COCOA_LOG(new CocoaLogBlock()),

	__PALETTES__(),
	TILED_GLASS(new GlassBlock(Properties.from(Blocks.GLASS))),
	TILED_GLASS_PANE(new GlassPaneBlock(Properties.from(Blocks.GLASS))),
	FRAMED_GLASS(new CTGlassBlock(true)),

	GRANITE_BRICKS(new Block(Properties.from(Blocks.GRANITE))),
	GRANITE_LAYERS(new LayeredCTBlock(Properties.from(Blocks.GRANITE),
			CTSpriteShifter.get(CTType.HORIZONTAL, "granite_layers"),
			CTSpriteShifter.get(CTType.OMNIDIRECTIONAL, new ResourceLocation("block/polished_granite"), "polished_granite"))),
	DIORITE_BRICKS(new Block(Properties.from(Blocks.DIORITE))),
	DIORITE_LAYERS(new LayeredCTBlock(Properties.from(Blocks.DIORITE),
			CTSpriteShifter.get(CTType.HORIZONTAL, "diorite_layers"),
			CTSpriteShifter.get(CTType.OMNIDIRECTIONAL, new ResourceLocation("block/polished_diorite"), "polished_diorite"))),
	ANDESITE_BRICKS(new Block(Properties.from(Blocks.ANDESITE))),
	ANDESITE_LAYERS(new LayeredCTBlock(Properties.from(Blocks.ANDESITE),
			CTSpriteShifter.get(CTType.HORIZONTAL, "andesite_layers"),
			CTSpriteShifter.get(CTType.OMNIDIRECTIONAL, new ResourceLocation("block/polished_andesite"),
					"polished_andesite"))),

	GABBRO(new Block(Properties.from(Blocks.GRANITE)), ComesWith.STAIRS, ComesWith.SLAB, ComesWith.WALL),
	POLISHED_GABBRO(new Block(Properties.from(GABBRO.block))),
	GABBRO_BRICKS(new Block(Properties.from(GABBRO.block)), ComesWith.STAIRS, ComesWith.WALL),
	PAVED_GABBRO_BRICKS(new Block(Properties.from(GABBRO.block)), ComesWith.SLAB),
	INDENTED_GABBRO(new Block(Properties.from(GABBRO.block)), ComesWith.SLAB),
	SLIGHTLY_MOSSY_GABBRO_BRICKS(new Block(Properties.from(GABBRO.block))),
	MOSSY_GABBRO_BRICKS(new Block(Properties.from(GABBRO.block))),
	GABBRO_LAYERS(
			new LayeredCTBlock(Properties.from(GABBRO.block), CTSpriteShifter.get(CTType.HORIZONTAL, "gabbro_layers"),
					CTSpriteShifter.get(CTType.OMNIDIRECTIONAL, "polished_gabbro"))),
	LIMESAND(new FallingBlock(Properties.from(Blocks.SAND))),
	LIMESTONE(new Block(Properties.from(Blocks.SANDSTONE)), ComesWith.STAIRS, ComesWith.SLAB, ComesWith.WALL),
	LIMESTONE_BRICKS(new Block(Properties.from(LIMESTONE.block)), ComesWith.STAIRS, ComesWith.SLAB, ComesWith.WALL),
	POLISHED_LIMESTONE(new Block(Properties.from(LIMESTONE.block)), ComesWith.SLAB),
	LIMESTONE_PILLAR(new RotatedPillarBlock(Properties.from(LIMESTONE.block))),
	LIMESTONE_LAYERS(new LayeredCTBlock(Properties.from(LIMESTONE.block),
			CTSpriteShifter.get(CTType.HORIZONTAL, "limestone_layers"),
			CTSpriteShifter.get(CTType.OMNIDIRECTIONAL, "polished_limestone"))),
	WEATHERED_LIMESTONE(new Block(Properties.from(Blocks.ANDESITE)), ComesWith.STAIRS, ComesWith.SLAB, ComesWith.WALL),
	WEATHERED_LIMESTONE_BRICKS(new Block(Properties.from(WEATHERED_LIMESTONE.block)), ComesWith.STAIRS, ComesWith.SLAB,
			ComesWith.WALL),
	POLISHED_WEATHERED_LIMESTONE(new Block(Properties.from(WEATHERED_LIMESTONE.block)), ComesWith.SLAB),
	WEATHERED_LIMESTONE_PILLAR(new RotatedPillarBlock(Properties.from(WEATHERED_LIMESTONE.block))),
	WEATHERED_LIMESTONE_LAYERS(new LayeredCTBlock(Properties.from(WEATHERED_LIMESTONE.block),
			CTSpriteShifter.get(CTType.HORIZONTAL, "weathered_limestone_layers"),
			CTSpriteShifter.get(CTType.OMNIDIRECTIONAL, "polished_weathered_limestone"))),
	DOLOMITE(new Block(Properties.from(Blocks.QUARTZ_BLOCK)), ComesWith.STAIRS, ComesWith.SLAB, ComesWith.WALL),
	DOLOMITE_BRICKS(new Block(Properties.from(DOLOMITE.block))),
	POLISHED_DOLOMITE(new Block(Properties.from(DOLOMITE.block))),
	DOLOMITE_PILLAR(new RotatedPillarBlock(Properties.from(DOLOMITE.block))),
	DOLOMITE_LAYERS(new LayeredCTBlock(Properties.from(DOLOMITE.block),
			CTSpriteShifter.get(CTType.HORIZONTAL, "dolomite_layers"),
			CTSpriteShifter.get(CTType.OMNIDIRECTIONAL, "polished_dolomite"))),

	VOLCANIC_ROCK(new VolcanicRockBlock()),

	__MATERIALS__(),
	COPPER_ORE(new OxidizingBlock(Properties.from(Blocks.IRON_ORE), 1)),
	ZINC_ORE(new Block(Properties.from(Blocks.GOLD_ORE).harvestLevel(2).harvestTool(ToolType.PICKAXE))),

	;

	private enum ComesWith {
		WALL, FENCE, FENCE_GATE, SLAB, STAIRS;
	}

	private static class CategoryTracker {
		static IModule currentModule;
	}

	public Block block;
	public Block[] alsoRegistered;
	public IModule module;

	private AllBlocks() {
		CategoryTracker.currentModule = new IModule() {
			@Override
			public String getModuleName() {
				return Lang.asId(name()).replaceAll("__", "");
			}
		};
	}

	private AllBlocks(Block block, ComesWith... comesWith) {
		this.block = block;
		this.block.setRegistryName(Create.ID, Lang.asId(name()));
		this.module = CategoryTracker.currentModule;

		alsoRegistered = new Block[comesWith.length];
		for (int i = 0; i < comesWith.length; i++)
			alsoRegistered[i] = makeRelatedBlock(block, comesWith[i]);
	}

	public static void register(RegistryEvent.Register<Block> event) {
		IForgeRegistry<Block> registry = event.getRegistry();

		for (AllBlocks block : values()) {
			if (block.get() == null)
				continue;

			registry.register(block.block);
			for (Block extra : block.alsoRegistered)
				registry.register(extra);
		}
	}

	public static void registerItemBlocks(IForgeRegistry<Item> registry) {
		for (AllBlocks block : values()) {
			Block def = block.get();
			if (def == null)
				continue;
			if (def instanceof IHaveNoBlockItem && !((IHaveNoBlockItem) def).hasBlockItem())
				continue;

			registerAsItem(registry, def);
			for (Block extra : block.alsoRegistered)
				registerAsItem(registry, extra);
		}
	}

	private static void registerAsItem(IForgeRegistry<Item> registry, Block blockIn) {
		BlockItem blockItem = null;
		net.minecraft.item.Item.Properties standardItemProperties = AllItems.standardItemProperties();

		if (blockIn instanceof IHaveCustomBlockItem)
			blockItem = ((IHaveCustomBlockItem) blockIn).getCustomItem(standardItemProperties);
		else
			blockItem = new BlockItem(blockIn, standardItemProperties);

		registry.register(blockItem.setRegistryName(blockIn.getRegistryName()));
	}

	public Block get() {
		return block;
	}

	public BlockState getDefault() {
		return block.getDefaultState();
	}

	public boolean typeOf(BlockState state) {
		return state.getBlock() == block;
	}

	private Block makeRelatedBlock(Block block, ComesWith feature) {
		Properties properties = Properties.from(block);
		Block featured = null;

		switch (feature) {
		case FENCE:
			featured = new FenceBlock(properties);
			break;
		case SLAB:
			featured = new SlabBlock(properties);
			break;
		case STAIRS:
			featured = new ProperStairsBlock(block);
			break;
		case WALL:
			featured = new WallBlock(properties);
			break;
		case FENCE_GATE:
			featured = new FenceGateBlock(properties);
			break;
		default:
			return null;
		}

		return featured.setRegistryName(Create.ID, block.getRegistryName().getPath() + "_" + Lang.asId(feature.name()));
	}

	@OnlyIn(Dist.CLIENT)
	public static void registerColorHandlers() {
		BlockColors blockColors = Minecraft.getInstance().getBlockColors();
		for (AllBlocks block : values()) {
			if (block.block instanceof IHaveColorHandler) {
				blockColors.register(((IHaveColorHandler) block.block).getColorHandler(), block.block);
			}
		}
	}

}
