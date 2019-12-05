package com.simibubi.create;

import com.simibubi.create.foundation.block.IBlockWithColorHandler;
import com.simibubi.create.foundation.block.IWithoutBlockItem;
import com.simibubi.create.foundation.block.ProperStairsBlock;
import com.simibubi.create.foundation.block.RenderUtilityAxisBlock;
import com.simibubi.create.foundation.block.RenderUtilityBlock;
import com.simibubi.create.foundation.block.RenderUtilityDirectionalBlock;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.IModule;
import com.simibubi.create.modules.contraptions.generators.MotorBlock;
import com.simibubi.create.modules.contraptions.generators.WaterWheelBlock;
import com.simibubi.create.modules.contraptions.receivers.BasinBlock;
import com.simibubi.create.modules.contraptions.receivers.CrushingWheelBlock;
import com.simibubi.create.modules.contraptions.receivers.CrushingWheelControllerBlock;
import com.simibubi.create.modules.contraptions.receivers.DrillBlock;
import com.simibubi.create.modules.contraptions.receivers.DrillBlock.DrillHeadBlock;
import com.simibubi.create.modules.contraptions.receivers.EncasedFanBlock;
import com.simibubi.create.modules.contraptions.receivers.HarvesterBlock;
import com.simibubi.create.modules.contraptions.receivers.HarvesterBlock.HarvesterBladeBlock;
import com.simibubi.create.modules.contraptions.receivers.MechanicalMixerBlock;
import com.simibubi.create.modules.contraptions.receivers.MechanicalMixerBlock.MechanicalMixerBlockItem;
import com.simibubi.create.modules.contraptions.receivers.MechanicalPressBlock;
import com.simibubi.create.modules.contraptions.receivers.SawBlock;
import com.simibubi.create.modules.contraptions.receivers.TurntableBlock;
import com.simibubi.create.modules.contraptions.receivers.constructs.LinearChassisBlock;
import com.simibubi.create.modules.contraptions.receivers.constructs.RadialChassisBlock;
import com.simibubi.create.modules.contraptions.receivers.constructs.bearing.MechanicalBearingBlock;
import com.simibubi.create.modules.contraptions.receivers.constructs.mounted.CartAssemblerBlock;
import com.simibubi.create.modules.contraptions.receivers.constructs.mounted.CartAssemblerBlock.MinecartAnchorBlock;
import com.simibubi.create.modules.contraptions.receivers.constructs.piston.MechanicalPistonBlock;
import com.simibubi.create.modules.contraptions.receivers.constructs.piston.MechanicalPistonHeadBlock;
import com.simibubi.create.modules.contraptions.receivers.constructs.piston.PistonPoleBlock;
import com.simibubi.create.modules.contraptions.receivers.crafter.MechanicalCrafterBlock;
import com.simibubi.create.modules.contraptions.redstone.ContactBlock;
import com.simibubi.create.modules.contraptions.relays.ClutchBlock;
import com.simibubi.create.modules.contraptions.relays.CogWheelBlock;
import com.simibubi.create.modules.contraptions.relays.EncasedBeltBlock;
import com.simibubi.create.modules.contraptions.relays.EncasedShaftBlock;
import com.simibubi.create.modules.contraptions.relays.GearboxBlock;
import com.simibubi.create.modules.contraptions.relays.GearshiftBlock;
import com.simibubi.create.modules.contraptions.relays.ShaftBlock;
import com.simibubi.create.modules.contraptions.relays.ShaftHalfBlock;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTunnelBlock;
import com.simibubi.create.modules.curiosities.partialWindows.WindowInABlockBlock;
import com.simibubi.create.modules.curiosities.symmetry.block.CrossPlaneSymmetryBlock;
import com.simibubi.create.modules.curiosities.symmetry.block.PlaneSymmetryBlock;
import com.simibubi.create.modules.curiosities.symmetry.block.TriplePlaneSymmetryBlock;
import com.simibubi.create.modules.gardens.CocoaLogBlock;
import com.simibubi.create.modules.logistics.block.RedstoneBridgeBlock;
import com.simibubi.create.modules.logistics.block.StockswitchBlock;
import com.simibubi.create.modules.logistics.block.belts.BeltFunnelBlock;
import com.simibubi.create.modules.logistics.block.belts.EntityDetectorBlock;
import com.simibubi.create.modules.logistics.block.belts.ExtractorBlock;
import com.simibubi.create.modules.logistics.block.belts.LinkedExtractorBlock;
import com.simibubi.create.modules.logistics.block.diodes.FlexpeaterBlock;
import com.simibubi.create.modules.logistics.block.diodes.PulseRepeaterBlock;
import com.simibubi.create.modules.logistics.block.inventories.FlexcrateBlock;
import com.simibubi.create.modules.logistics.management.base.LogisticalCasingBlock;
import com.simibubi.create.modules.logistics.management.base.LogisticalControllerBlock;
import com.simibubi.create.modules.logistics.management.base.LogisticalControllerBlock.LogisticalControllerIndicatorBlock;
import com.simibubi.create.modules.logistics.management.index.LogisticalIndexBlock;
import com.simibubi.create.modules.logistics.transport.villager.LogisticiansTableBlock;
import com.simibubi.create.modules.logistics.transport.villager.PackageFunnelBlock;
import com.simibubi.create.modules.palettes.CTGlassBlock;
import com.simibubi.create.modules.palettes.GlassPaneBlock;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
	BELT_ANIMATION(new RenderUtilityBlock()),
	MOTOR(new MotorBlock()),
	WATER_WHEEL(new WaterWheelBlock()),
	ENCASED_FAN(new EncasedFanBlock()),
	ENCASED_FAN_INNER(new RenderUtilityAxisBlock()),
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
	MECHANICAL_CRAFTER(new MechanicalCrafterBlock()),
	MECHANICAL_CRAFTER_LID(new RenderUtilityDirectionalBlock()),
	MECHANICAL_CRAFTER_ARROW(new RenderUtilityDirectionalBlock()),
	MECHANICAL_CRAFTER_BELT_FRAME(new RenderUtilityDirectionalBlock()),
	MECHANICAL_CRAFTER_BELT(new RenderUtilityDirectionalBlock()),

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

	__LOGISTICS__(),
	CONTACT(new ContactBlock()),
	REDSTONE_BRIDGE(new RedstoneBridgeBlock()),
	STOCKSWITCH(new StockswitchBlock()),
	FLEXCRATE(new FlexcrateBlock()),
	EXTRACTOR(new ExtractorBlock()),
	LINKED_EXTRACTOR(new LinkedExtractorBlock()),
	BELT_FUNNEL(new BeltFunnelBlock()),
	BELT_TUNNEL(new BeltTunnelBlock()),
	BELT_TUNNEL_FLAP(new RenderUtilityBlock()),
	ENTITY_DETECTOR(new EntityDetectorBlock()),
	PULSE_REPEATER(new PulseRepeaterBlock()),
	FLEXPEATER(new FlexpeaterBlock()),
	FLEXPEATER_INDICATOR(new RenderUtilityBlock()),
	LOGISTICAL_CASING(new LogisticalCasingBlock()),
	LOGISTICAL_CONTROLLER(new LogisticalControllerBlock()),
	LOGISTICAL_CONTROLLER_INDICATOR(new LogisticalControllerIndicatorBlock()),
	LOGISTICAL_INDEX(new LogisticalIndexBlock()),
	PACKAGE_FUNNEL(new PackageFunnelBlock()),
	LOGISTICIANS_TABLE(new LogisticiansTableBlock()),
	LOGISTICIANS_TABLE_INDICATOR(new RenderUtilityBlock()),

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

	ANDESITE_BRICKS(new Block(Properties.from(Blocks.ANDESITE))),
	DIORITE_BRICKS(new Block(Properties.from(Blocks.DIORITE))),
	GRANITE_BRICKS(new Block(Properties.from(Blocks.GRANITE))),
	GABBRO(new Block(Properties.from(Blocks.GRANITE)), ComesWith.STAIRS, ComesWith.SLAB, ComesWith.WALL),
	POLISHED_GABBRO(new Block(Properties.from(GABBRO.block))),
	GABBRO_BRICKS(new Block(Properties.from(GABBRO.block)), ComesWith.STAIRS, ComesWith.WALL),
	PAVED_GABBRO_BRICKS(new Block(Properties.from(GABBRO.block)), ComesWith.SLAB),
	INDENTED_GABBRO(new Block(Properties.from(GABBRO.block)), ComesWith.SLAB),
	SLIGHTLY_MOSSY_GABBRO_BRICKS(new Block(Properties.from(GABBRO.block))),
	MOSSY_GABBRO_BRICKS(new Block(Properties.from(GABBRO.block))),
	LIMESAND(new FallingBlock(Properties.from(Blocks.SAND))),
	LIMESTONE(new Block(Properties.from(Blocks.SANDSTONE)), ComesWith.STAIRS, ComesWith.SLAB, ComesWith.WALL),
	LIMESTONE_BRICKS(new Block(Properties.from(LIMESTONE.block)), ComesWith.STAIRS, ComesWith.SLAB, ComesWith.WALL),
	POLISHED_LIMESTONE(new Block(Properties.from(LIMESTONE.block)), ComesWith.SLAB),
	LIMESTONE_PILLAR(new RotatedPillarBlock(Properties.from(LIMESTONE.block))),
	WEATHERED_LIMESTONE(new Block(Properties.from(Blocks.ANDESITE)), ComesWith.STAIRS, ComesWith.SLAB, ComesWith.WALL),
	WEATHERED_LIMESTONE_BRICKS(new Block(Properties.from(WEATHERED_LIMESTONE.block)), ComesWith.STAIRS, ComesWith.SLAB,
			ComesWith.WALL),
	POLISHED_WEATHERED_LIMESTONE(new Block(Properties.from(WEATHERED_LIMESTONE.block)), ComesWith.SLAB),
	WEATHERED_LIMESTONE_PILLAR(new RotatedPillarBlock(Properties.from(WEATHERED_LIMESTONE.block))),
	DOLOMITE(new Block(Properties.from(Blocks.QUARTZ_BLOCK)), ComesWith.STAIRS, ComesWith.SLAB, ComesWith.WALL),
	DOLOMITE_BRICKS(new Block(Properties.from(DOLOMITE.block))),
	POLISHED_DOLOMITE(new Block(Properties.from(DOLOMITE.block))),
	DOLOMITE_PILLAR(new RotatedPillarBlock(Properties.from(DOLOMITE.block))),

	VOLCANIC_ROCK(new VolcanicRockBlock()),

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
			if (block.get() == null)
				continue;
			if (block.get() instanceof IWithoutBlockItem)
				continue;

			registerAsItem(registry, block.get());
			for (Block extra : block.alsoRegistered)
				registerAsItem(registry, extra);
		}
	}

	private static void registerAsItem(IForgeRegistry<Item> registry, Block blockIn) {
		BlockItem blockItem = null;
		net.minecraft.item.Item.Properties standardItemProperties = AllItems.standardItemProperties();

		if (blockIn == AllBlocks.MECHANICAL_MIXER.get())
			blockItem = new MechanicalMixerBlockItem(standardItemProperties);
		else
			blockItem = new BlockItem(blockIn, standardItemProperties);

		registry.register(blockItem.setRegistryName(blockIn.getRegistryName()));
	}

	public Block get() {
		return block;
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
			if (block.block instanceof IBlockWithColorHandler) {
				blockColors.register(((IBlockWithColorHandler) block.block).getColorHandler(), block.block);
			}
		}
	}

}
