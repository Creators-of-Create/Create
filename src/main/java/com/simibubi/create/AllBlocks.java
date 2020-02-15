package com.simibubi.create;

import com.simibubi.create.foundation.block.IHaveColorHandler;
import com.simibubi.create.foundation.block.IHaveCustomBlockItem;
import com.simibubi.create.foundation.block.IHaveNoBlockItem;
import com.simibubi.create.foundation.block.ProperStairsBlock;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.world.OxidizingBlock;
import com.simibubi.create.modules.IModule;
import com.simibubi.create.modules.contraptions.CasingBlock;
import com.simibubi.create.modules.contraptions.components.actors.DrillBlock;
import com.simibubi.create.modules.contraptions.components.actors.HarvesterBlock;
import com.simibubi.create.modules.contraptions.components.actors.PortableStorageInterfaceBlock;
import com.simibubi.create.modules.contraptions.components.clock.CuckooClockBlock;
import com.simibubi.create.modules.contraptions.components.contraptions.bearing.ClockworkBearingBlock;
import com.simibubi.create.modules.contraptions.components.contraptions.bearing.MechanicalBearingBlock;
import com.simibubi.create.modules.contraptions.components.contraptions.chassis.LinearChassisBlock;
import com.simibubi.create.modules.contraptions.components.contraptions.chassis.RadialChassisBlock;
import com.simibubi.create.modules.contraptions.components.contraptions.mounted.CartAssemblerBlock;
import com.simibubi.create.modules.contraptions.components.contraptions.mounted.CartAssemblerBlock.MinecartAnchorBlock;
import com.simibubi.create.modules.contraptions.components.contraptions.piston.MechanicalPistonBlock;
import com.simibubi.create.modules.contraptions.components.contraptions.piston.MechanicalPistonHeadBlock;
import com.simibubi.create.modules.contraptions.components.contraptions.piston.PistonPoleBlock;
import com.simibubi.create.modules.contraptions.components.contraptions.pulley.PulleyBlock;
import com.simibubi.create.modules.contraptions.components.crafter.MechanicalCrafterBlock;
import com.simibubi.create.modules.contraptions.components.crank.HandCrankBlock;
import com.simibubi.create.modules.contraptions.components.crusher.CrushingWheelBlock;
import com.simibubi.create.modules.contraptions.components.crusher.CrushingWheelControllerBlock;
import com.simibubi.create.modules.contraptions.components.deployer.DeployerBlock;
import com.simibubi.create.modules.contraptions.components.fan.EncasedFanBlock;
import com.simibubi.create.modules.contraptions.components.fan.NozzleBlock;
import com.simibubi.create.modules.contraptions.components.flywheel.FlywheelBlock;
import com.simibubi.create.modules.contraptions.components.flywheel.engine.FurnaceEngineBlock;
import com.simibubi.create.modules.contraptions.components.mixer.MechanicalMixerBlock;
import com.simibubi.create.modules.contraptions.components.motor.MotorBlock;
import com.simibubi.create.modules.contraptions.components.press.MechanicalPressBlock;
import com.simibubi.create.modules.contraptions.components.saw.SawBlock;
import com.simibubi.create.modules.contraptions.components.turntable.TurntableBlock;
import com.simibubi.create.modules.contraptions.components.waterwheel.WaterWheelBlock;
import com.simibubi.create.modules.contraptions.processing.BasinBlock;
import com.simibubi.create.modules.contraptions.redstone.AnalogLeverBlock;
import com.simibubi.create.modules.contraptions.redstone.ContactBlock;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTunnelBlock;
import com.simibubi.create.modules.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.modules.contraptions.relays.elementary.ShaftBlock;
import com.simibubi.create.modules.contraptions.relays.encased.AdjustablePulleyBlock;
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
import com.simibubi.create.modules.palettes.CTGlassPaneBlock;
import com.simibubi.create.modules.palettes.CTWindowBlock;
import com.simibubi.create.modules.palettes.GlassPaneBlock;
import com.simibubi.create.modules.palettes.HorizontalCTGlassBlock;
import com.simibubi.create.modules.palettes.LayeredCTBlock;
import com.simibubi.create.modules.palettes.ScoriaBlock;
import com.simibubi.create.modules.palettes.VerticalCTGlassBlock;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

public enum AllBlocks {

	__SCHEMATICS__(),
	SCHEMATICANNON(new SchematicannonBlock()),
	CREATIVE_CRATE(new CreativeCrateBlock()),
	SCHEMATIC_TABLE(new SchematicTableBlock()),

	__CONTRAPTIONS__(),
	SHAFT(new ShaftBlock(Properties.from(Blocks.ANDESITE))),
	COGWHEEL(new CogWheelBlock(false)),
	LARGE_COGWHEEL(new CogWheelBlock(true)),
	ENCASED_SHAFT(new EncasedShaftBlock()),
	GEARBOX(new GearboxBlock()),
	CLUTCH(new ClutchBlock()),
	GEARSHIFT(new GearshiftBlock()),
	ENCASED_BELT(new EncasedBeltBlock()),
	ADJUSTABLE_PULLEY(new AdjustablePulleyBlock()),
	BELT(new BeltBlock()),
	CREATIVE_MOTOR(new MotorBlock()),
	WATER_WHEEL(new WaterWheelBlock()),
	ENCASED_FAN(new EncasedFanBlock()),
	NOZZLE(new NozzleBlock()),
	TURNTABLE(new TurntableBlock()),
	HAND_CRANK(new HandCrankBlock()),
	CUCKOO_CLOCK(new CuckooClockBlock(false)),
	MYSTERIOUS_CUCKOO_CLOCK(new CuckooClockBlock(true)),

	CRUSHING_WHEEL(new CrushingWheelBlock()),
	CRUSHING_WHEEL_CONTROLLER(new CrushingWheelControllerBlock()),
	MECHANICAL_PRESS(new MechanicalPressBlock()),
	MECHANICAL_MIXER(new MechanicalMixerBlock()),
	BASIN(new BasinBlock()),
	SPEED_GAUGE(new GaugeBlock(GaugeBlock.Type.SPEED)),
	STRESS_GAUGE(new GaugeBlock(GaugeBlock.Type.STRESS)),

	MECHANICAL_PISTON(new MechanicalPistonBlock(false)),
	STICKY_MECHANICAL_PISTON(new MechanicalPistonBlock(true)),
	MECHANICAL_PISTON_HEAD(new MechanicalPistonHeadBlock()),
	PISTON_POLE(new PistonPoleBlock()),
	MECHANICAL_BEARING(new MechanicalBearingBlock()),
	CLOCKWORK_BEARING(new ClockworkBearingBlock()),
	ROPE_PULLEY(new PulleyBlock()),
	ROPE(new PulleyBlock.RopeBlock()),
	PULLEY_MAGNET(new PulleyBlock.MagnetBlock()),
	CART_ASSEMBLER(new CartAssemblerBlock()),
	MINECART_ANCHOR(new MinecartAnchorBlock()),
	TRANSLATION_CHASSIS(new LinearChassisBlock()),
	TRANSLATION_CHASSIS_SECONDARY(new LinearChassisBlock()),
	ROTATION_CHASSIS(new RadialChassisBlock()),
	DRILL(new DrillBlock()),
	SAW(new SawBlock()),
	HARVESTER(new HarvesterBlock()),
	DEPLOYER(new DeployerBlock()),
	PORTABLE_STORAGE_INTERFACE(new PortableStorageInterfaceBlock()),
	ANALOG_LEVER(new AnalogLeverBlock()),

	ANDESITE_CASING(new CasingBlock("andesite_casing")),
	COPPER_CASING(new CasingBlock("copper_casing")),
	BRASS_CASING(new CasingBlock("crafter_top")),

	MECHANICAL_CRAFTER(new MechanicalCrafterBlock()),
	FLYWHEEL(new FlywheelBlock()),
	FURNACE_ENGINE(new FurnaceEngineBlock()),
	
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
	ENTITY_DETECTOR(new BeltObserverBlock()),
	PULSE_REPEATER(new PulseRepeaterBlock()),
	FLEXPEATER(new FlexpeaterBlock()),
	FLEXPULSEPEATER(new FlexpeaterBlock()),

	__CURIOSITIES__(),
	SYMMETRY_PLANE(new PlaneSymmetryBlock()),
	SYMMETRY_CROSSPLANE(new CrossPlaneSymmetryBlock()),
	SYMMETRY_TRIPLEPLANE(new TriplePlaneSymmetryBlock()),
	WINDOW_IN_A_BLOCK(new WindowInABlockBlock()),
	COCOA_LOG(new CocoaLogBlock()),

	__PALETTES__(),
	TILED_GLASS(new GlassBlock(Properties.from(Blocks.GLASS))),
	FRAMED_GLASS(new CTGlassBlock(AllCTs.FRAMED_GLASS, false)),
	HORIZONTAL_FRAMED_GLASS(new HorizontalCTGlassBlock(AllCTs.HORIZONTAL_FRAMED_GLASS, AllCTs.FRAMED_GLASS, false)),
	VERTICAL_FRAMED_GLASS(new VerticalCTGlassBlock(AllCTs.VERTICAL_FRAMED_GLASS, false)),

	OAK_GLASS(new CTWindowBlock(AllCTs.OAK_GLASS, false)),
	SPRUCE_GLASS(new CTWindowBlock(AllCTs.SPRUCE_GLASS, false)),
	BIRCH_GLASS(new CTWindowBlock(AllCTs.BIRCH_GLASS, true)),
	JUNGLE_GLASS(new CTWindowBlock(AllCTs.JUNGLE_GLASS, false)),
	DARK_OAK_GLASS(new CTWindowBlock(AllCTs.DARK_OAK_GLASS, false)),
	ACACIA_GLASS(new CTWindowBlock(AllCTs.ACACIA_GLASS, false)),
	IRON_GLASS(new CTWindowBlock(AllCTs.IRON_GLASS, false)),

	TILED_GLASS_PANE(new GlassPaneBlock(Properties.from(Blocks.GLASS))),
	FRAMED_GLASS_PANE(new CTGlassPaneBlock(FRAMED_GLASS.block)),
	HORIZONTAL_FRAMED_GLASS_PANE(new CTGlassPaneBlock(HORIZONTAL_FRAMED_GLASS.block)),
	VERTICAL_FRAMED_GLASS_PANE(new CTGlassPaneBlock(VERTICAL_FRAMED_GLASS.block)),
	OAK_GLASS_PANE(new CTGlassPaneBlock(OAK_GLASS.block)),
	SPRUCE_GLASS_PANE(new CTGlassPaneBlock(SPRUCE_GLASS.block)),
	BIRCH_GLASS_PANE(new CTGlassPaneBlock(BIRCH_GLASS.block)),
	JUNGLE_GLASS_PANE(new CTGlassPaneBlock(JUNGLE_GLASS.block)),
	DARK_OAK_GLASS_PANE(new CTGlassPaneBlock(DARK_OAK_GLASS.block)),
	ACACIA_GLASS_PANE(new CTGlassPaneBlock(ACACIA_GLASS.block)),
	IRON_GLASS_PANE(new CTGlassPaneBlock(IRON_GLASS.block)),

	GRANITE_BRICKS(new Block(Properties.from(Blocks.GRANITE))),
	GRANITE_LAYERS(new LayeredCTBlock(Properties.from(Blocks.GRANITE), AllCTs.GRANITE_LAYERS, AllCTs.POLISHED_GRANITE)),
	DIORITE_BRICKS(new Block(Properties.from(Blocks.DIORITE))),
	DIORITE_LAYERS(new LayeredCTBlock(Properties.from(Blocks.DIORITE), AllCTs.DIORITE_LAYERS, AllCTs.POLISHED_DIORITE)),
	ANDESITE_BRICKS(new Block(Properties.from(Blocks.ANDESITE))),
	ANDESITE_LAYERS(
			new LayeredCTBlock(Properties.from(Blocks.ANDESITE), AllCTs.ANDESITE_LAYERS, AllCTs.POLISHED_ANDESITE)),

	GABBRO(new Block(Properties.from(Blocks.GRANITE)), ComesWith.STAIRS, ComesWith.SLAB, ComesWith.WALL),
	POLISHED_GABBRO(new Block(Properties.from(GABBRO.block))),
	GABBRO_BRICKS(new Block(Properties.from(GABBRO.block)), ComesWith.STAIRS, ComesWith.WALL),
	PAVED_GABBRO_BRICKS(new Block(Properties.from(GABBRO.block)), ComesWith.SLAB),
	INDENTED_GABBRO(new Block(Properties.from(GABBRO.block)), ComesWith.SLAB),
	SLIGHTLY_MOSSY_GABBRO_BRICKS(new Block(Properties.from(GABBRO.block))),
	MOSSY_GABBRO_BRICKS(new Block(Properties.from(GABBRO.block))),
	GABBRO_LAYERS(new LayeredCTBlock(Properties.from(GABBRO.block), AllCTs.GABBRO_LAYERS, AllCTs.POLISHED_GABBRO)),

	DOLOMITE(new Block(Properties.from(Blocks.QUARTZ_BLOCK)), ComesWith.STAIRS, ComesWith.SLAB, ComesWith.WALL),
	DOLOMITE_BRICKS(new Block(Properties.from(DOLOMITE.block))),
	POLISHED_DOLOMITE(new Block(Properties.from(DOLOMITE.block))),
	DOLOMITE_PILLAR(new RotatedPillarBlock(Properties.from(DOLOMITE.block))),
	DOLOMITE_LAYERS(
			new LayeredCTBlock(Properties.from(DOLOMITE.block), AllCTs.DOLOMITE_LAYERS, AllCTs.POLISHED_DOLOMITE)),

	LIMESAND(new FallingBlock(Properties.from(Blocks.SAND))),
	LIMESTONE(new Block(Properties.from(Blocks.SANDSTONE)), ComesWith.STAIRS, ComesWith.SLAB, ComesWith.WALL),
	LIMESTONE_BRICKS(new Block(Properties.from(LIMESTONE.block)), ComesWith.STAIRS, ComesWith.SLAB, ComesWith.WALL),
	POLISHED_LIMESTONE(new Block(Properties.from(LIMESTONE.block)), ComesWith.SLAB),
	LIMESTONE_PILLAR(new RotatedPillarBlock(Properties.from(LIMESTONE.block))),
	LIMESTONE_LAYERS(
			new LayeredCTBlock(Properties.from(LIMESTONE.block), AllCTs.LIMESTONE_LAYERS, AllCTs.POLISHED_LIMESTONE)),
	WEATHERED_LIMESTONE(new Block(Properties.from(Blocks.ANDESITE)), ComesWith.STAIRS, ComesWith.SLAB, ComesWith.WALL),
	WEATHERED_LIMESTONE_BRICKS(new Block(Properties.from(WEATHERED_LIMESTONE.block)), ComesWith.STAIRS, ComesWith.SLAB,
			ComesWith.WALL),
	POLISHED_WEATHERED_LIMESTONE(new Block(Properties.from(WEATHERED_LIMESTONE.block)), ComesWith.SLAB),
	WEATHERED_LIMESTONE_PILLAR(new RotatedPillarBlock(Properties.from(WEATHERED_LIMESTONE.block))),
	WEATHERED_LIMESTONE_LAYERS(new LayeredCTBlock(Properties.from(WEATHERED_LIMESTONE.block),
			AllCTs.WEATHERED_LIMESTONE_LAYERS, AllCTs.POLISHED_WEATHERED_LIMESTONE)),

	NATURAL_SCORIA(new ScoriaBlock()),
	SCORIA(new Block(Properties.from(Blocks.ANDESITE)), ComesWith.STAIRS, ComesWith.SLAB, ComesWith.WALL),
	POLISHED_SCORIA(new Block(Properties.from(SCORIA.block)), ComesWith.SLAB),
	SCORIA_BRICKS(new Block(Properties.from(SCORIA.block))),
	SCORIA_LAYERS(new LayeredCTBlock(Properties.from(SCORIA.block), AllCTs.SCORIA_LAYERS, AllCTs.POLISHED_SCORIA)),
	SCORIA_PILLAR(new RotatedPillarBlock(Properties.from(SCORIA.block))),

	DARK_SCORIA(new Block(Properties.from(Blocks.ANDESITE))),
	POLISHED_DARK_SCORIA(new Block(Properties.from(DARK_SCORIA.block))),
	DARK_SCORIA_TILES(new Block(Properties.from(DARK_SCORIA.block)), ComesWith.STAIRS, ComesWith.SLAB),
	DARK_SCORIA_BRICKS(new Block(Properties.from(DARK_SCORIA.block)), ComesWith.STAIRS, ComesWith.SLAB, ComesWith.WALL),

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
		Item.Properties standardItemProperties = AllItems.includeInItemGroup();

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
