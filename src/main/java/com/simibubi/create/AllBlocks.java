package com.simibubi.create;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.block.IHaveColorHandler;
import com.simibubi.create.foundation.block.ProperStairsBlock;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.data.ITaggable;
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
import com.simibubi.create.modules.contraptions.components.millstone.MillstoneBlock;
import com.simibubi.create.modules.contraptions.components.mixer.BasinOperatorBlockItem;
import com.simibubi.create.modules.contraptions.components.mixer.MechanicalMixerBlock;
import com.simibubi.create.modules.contraptions.components.motor.MotorBlock;
import com.simibubi.create.modules.contraptions.components.press.MechanicalPressBlock;
import com.simibubi.create.modules.contraptions.components.saw.SawBlock;
import com.simibubi.create.modules.contraptions.components.turntable.TurntableBlock;
import com.simibubi.create.modules.contraptions.components.waterwheel.WaterWheelBlock;
import com.simibubi.create.modules.contraptions.processing.BasinBlock;
import com.simibubi.create.modules.contraptions.redstone.AnalogLeverBlock;
import com.simibubi.create.modules.contraptions.redstone.ContactBlock;
import com.simibubi.create.modules.contraptions.relays.advanced.SpeedControllerBlock;
import com.simibubi.create.modules.contraptions.relays.advanced.sequencer.SequencedGearshiftBlock;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.modules.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.modules.contraptions.relays.elementary.CogwheelBlockItem;
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
import com.simibubi.create.modules.logistics.block.belts.observer.BeltObserverBlock;
import com.simibubi.create.modules.logistics.block.belts.tunnel.BeltTunnelBlock;
import com.simibubi.create.modules.logistics.block.diodes.FlexpeaterBlock;
import com.simibubi.create.modules.logistics.block.diodes.LatchBlock;
import com.simibubi.create.modules.logistics.block.diodes.PulseRepeaterBlock;
import com.simibubi.create.modules.logistics.block.diodes.ToggleLatchBlock;
import com.simibubi.create.modules.logistics.block.extractor.ExtractorBlock;
import com.simibubi.create.modules.logistics.block.extractor.LinkedExtractorBlock;
import com.simibubi.create.modules.logistics.block.funnel.FunnelBlock;
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
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

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
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

public enum AllBlocks implements NonNullSupplier<Block> {

	__SCHEMATICS__(),
	SCHEMATICANNON(SchematicannonBlock::new),
	CREATIVE_CRATE(CreativeCrateBlock::new),
	SCHEMATIC_TABLE(SchematicTableBlock::new),

	__CONTRAPTIONS__(),
	SHAFT(() -> new ShaftBlock(Properties.from(Blocks.ANDESITE))),
	COGWHEEL(() -> new CogWheelBlock(false), (b, p) -> new CogwheelBlockItem(b, p, false)),
	LARGE_COGWHEEL(() -> new CogWheelBlock(true), (b, p) -> new CogwheelBlockItem(b, p, true)),
	ENCASED_SHAFT(EncasedShaftBlock::new),
	GEARBOX(GearboxBlock::new),
	CLUTCH(ClutchBlock::new),
	GEARSHIFT(GearshiftBlock::new),
	ENCASED_BELT(EncasedBeltBlock::new),
	ADJUSTABLE_PULLEY(AdjustablePulleyBlock::new),
	BELT(BeltBlock::new, ComesWith.NO_BLOCKITEM),
	CREATIVE_MOTOR(MotorBlock::new),
	WATER_WHEEL(WaterWheelBlock::new),
	ENCASED_FAN(EncasedFanBlock::new),
	NOZZLE(NozzleBlock::new),
	TURNTABLE(TurntableBlock::new),
	HAND_CRANK(HandCrankBlock::new),
	CUCKOO_CLOCK(() -> new CuckooClockBlock(false)),
	MYSTERIOUS_CUCKOO_CLOCK(() -> new CuckooClockBlock(true)),

	MILLSTONE(MillstoneBlock::new),
	CRUSHING_WHEEL(CrushingWheelBlock::new),
	CRUSHING_WHEEL_CONTROLLER(CrushingWheelControllerBlock::new, ComesWith.NO_BLOCKITEM),
	MECHANICAL_PRESS(MechanicalPressBlock::new, BasinOperatorBlockItem::new),
	MECHANICAL_MIXER(MechanicalMixerBlock::new, BasinOperatorBlockItem::new),
	BASIN(BasinBlock::new),
	SPEED_GAUGE(() -> new GaugeBlock(GaugeBlock.Type.SPEED)),
	STRESS_GAUGE(() -> new GaugeBlock(GaugeBlock.Type.STRESS)),

	MECHANICAL_PISTON(() -> new MechanicalPistonBlock(false)),
	STICKY_MECHANICAL_PISTON(() -> new MechanicalPistonBlock(true)),
	MECHANICAL_PISTON_HEAD(MechanicalPistonHeadBlock::new, ComesWith.NO_BLOCKITEM),
	PISTON_POLE(PistonPoleBlock::new),
	MECHANICAL_BEARING(MechanicalBearingBlock::new),
	CLOCKWORK_BEARING(ClockworkBearingBlock::new),
	ROPE_PULLEY(PulleyBlock::new),
	ROPE(PulleyBlock.RopeBlock::new, ComesWith.NO_BLOCKITEM),
	PULLEY_MAGNET(PulleyBlock.MagnetBlock::new, ComesWith.NO_BLOCKITEM),
	CART_ASSEMBLER(CartAssemblerBlock::new, ITaggable.create().withVanillaTags(ITaggable.BLOCK, "rails")),
	MINECART_ANCHOR(MinecartAnchorBlock::new, ComesWith.NO_BLOCKITEM),
	TRANSLATION_CHASSIS(LinearChassisBlock::new),
	TRANSLATION_CHASSIS_SECONDARY(LinearChassisBlock::new),
	ROTATION_CHASSIS(RadialChassisBlock::new),
	DRILL(DrillBlock::new),
	SAW(SawBlock::new),
	HARVESTER(HarvesterBlock::new),
	DEPLOYER(DeployerBlock::new),
	PORTABLE_STORAGE_INTERFACE(PortableStorageInterfaceBlock::new),
	ANALOG_LEVER(AnalogLeverBlock::new),

	ANDESITE_CASING(() -> new CasingBlock("andesite_casing")),
	COPPER_CASING(() -> new CasingBlock("copper_casing")),
	BRASS_CASING(() -> new CasingBlock("crafter_top")),

	MECHANICAL_CRAFTER(MechanicalCrafterBlock::new),
	SEQUENCED_GEARSHIFT(SequencedGearshiftBlock::new),
	FLYWHEEL(FlywheelBlock::new),
	FURNACE_ENGINE(FurnaceEngineBlock::new),
	ROTATION_SPEED_CONTROLLER(SpeedControllerBlock::new),

	__LOGISTICS__(),
	CONTACT(ContactBlock::new),
	REDSTONE_BRIDGE(RedstoneLinkBlock::new),
	STOCKSWITCH(StockswitchBlock::new),
	FLEXCRATE(FlexcrateBlock::new),
	EXTRACTOR(ExtractorBlock::new),
	VERTICAL_EXTRACTOR(ExtractorBlock.Vertical::new, ComesWith.NO_BLOCKITEM),
	LINKED_EXTRACTOR(LinkedExtractorBlock::new),
	VERTICAL_LINKED_EXTRACTOR(LinkedExtractorBlock.Vertical::new, ComesWith.NO_BLOCKITEM),
	TRANSPOSER(TransposerBlock::new),
	VERTICAL_TRANSPOSER(TransposerBlock.Vertical::new, ComesWith.NO_BLOCKITEM),
	LINKED_TRANSPOSER(LinkedTransposerBlock::new),
	VERTICAL_LINKED_TRANSPOSER(LinkedTransposerBlock.Vertical::new, ComesWith.NO_BLOCKITEM),
	BELT_FUNNEL(FunnelBlock::new),
	VERTICAL_FUNNEL(FunnelBlock.Vertical::new, ComesWith.NO_BLOCKITEM),
	BELT_TUNNEL(BeltTunnelBlock::new),
	ENTITY_DETECTOR(BeltObserverBlock::new),
	PULSE_REPEATER(PulseRepeaterBlock::new),
	FLEXPEATER(FlexpeaterBlock::new),
	FLEXPULSEPEATER(FlexpeaterBlock::new),
	REDSTONE_LATCH(LatchBlock::new),
	TOGGLE_LATCH(ToggleLatchBlock::new),

	__CURIOSITIES__(),
	SYMMETRY_PLANE(PlaneSymmetryBlock::new, ComesWith.NO_BLOCKITEM),
	SYMMETRY_CROSSPLANE(CrossPlaneSymmetryBlock::new, ComesWith.NO_BLOCKITEM),
	SYMMETRY_TRIPLEPLANE(TriplePlaneSymmetryBlock::new, ComesWith.NO_BLOCKITEM),
	WINDOW_IN_A_BLOCK(WindowInABlockBlock::new, ComesWith.NO_BLOCKITEM),
	COCOA_LOG(CocoaLogBlock::new, ITaggable.create().withVanillaTags(ITaggable.BLOCK, "jungle_logs")),

	__PALETTES__(),
	TILED_GLASS(() -> new GlassBlock(Properties.from(Blocks.GLASS)), ITaggable.create().withVanillaTags(ITaggable.BLOCK, "impermeable").withForgeTags("glass")),
	FRAMED_GLASS(() -> new CTGlassBlock(AllCTs.FRAMED_GLASS, false)),
	HORIZONTAL_FRAMED_GLASS(() -> new HorizontalCTGlassBlock(AllCTs.HORIZONTAL_FRAMED_GLASS, AllCTs.FRAMED_GLASS, false)),
	VERTICAL_FRAMED_GLASS(() -> new VerticalCTGlassBlock(AllCTs.VERTICAL_FRAMED_GLASS, false)),

	OAK_GLASS(() -> new CTWindowBlock(AllCTs.OAK_GLASS, false)),
	SPRUCE_GLASS(() -> new CTWindowBlock(AllCTs.SPRUCE_GLASS, false)),
	BIRCH_GLASS(() -> new CTWindowBlock(AllCTs.BIRCH_GLASS, true)),
	JUNGLE_GLASS(() -> new CTWindowBlock(AllCTs.JUNGLE_GLASS, false)),
	DARK_OAK_GLASS(() -> new CTWindowBlock(AllCTs.DARK_OAK_GLASS, false)),
	ACACIA_GLASS(() -> new CTWindowBlock(AllCTs.ACACIA_GLASS, false)),
	IRON_GLASS(() -> new CTWindowBlock(AllCTs.IRON_GLASS, false)),

	TILED_GLASS_PANE(() -> new GlassPaneBlock(Properties.from(Blocks.GLASS)), ITaggable.create().withForgeTags("glass_panes")),
	FRAMED_GLASS_PANE(() -> new CTGlassPaneBlock(FRAMED_GLASS.get())),
	HORIZONTAL_FRAMED_GLASS_PANE(() -> new CTGlassPaneBlock(HORIZONTAL_FRAMED_GLASS.get())),
	VERTICAL_FRAMED_GLASS_PANE(() -> new CTGlassPaneBlock(VERTICAL_FRAMED_GLASS.get())),
	OAK_GLASS_PANE(() -> new CTGlassPaneBlock(OAK_GLASS.get())),
	SPRUCE_GLASS_PANE(() -> new CTGlassPaneBlock(SPRUCE_GLASS.get())),
	BIRCH_GLASS_PANE(() -> new CTGlassPaneBlock(BIRCH_GLASS.get())),
	JUNGLE_GLASS_PANE(() -> new CTGlassPaneBlock(JUNGLE_GLASS.get())),
	DARK_OAK_GLASS_PANE(() -> new CTGlassPaneBlock(DARK_OAK_GLASS.get())),
	ACACIA_GLASS_PANE(() -> new CTGlassPaneBlock(ACACIA_GLASS.get())),
	IRON_GLASS_PANE(() -> new CTGlassPaneBlock(IRON_GLASS.get())),

	GRANITE_BRICKS(() -> new Block(Properties.from(Blocks.GRANITE))),
	GRANITE_LAYERS(() -> new LayeredCTBlock(Properties.from(Blocks.GRANITE), AllCTs.GRANITE_LAYERS, AllCTs.POLISHED_GRANITE)),
	DIORITE_BRICKS(() -> new Block(Properties.from(Blocks.DIORITE))),
	DIORITE_LAYERS(() -> new LayeredCTBlock(Properties.from(Blocks.DIORITE), AllCTs.DIORITE_LAYERS, AllCTs.POLISHED_DIORITE)),
	ANDESITE_BRICKS(() -> new Block(Properties.from(Blocks.ANDESITE))),
	ANDESITE_LAYERS(() -> 
			new LayeredCTBlock(Properties.from(Blocks.ANDESITE), AllCTs.ANDESITE_LAYERS, AllCTs.POLISHED_ANDESITE)),

	GABBRO(() -> new Block(Properties.from(Blocks.GRANITE)), ITaggable.create().withForgeTags("stone"), ComesWith.STAIRS, ComesWith.SLAB, ComesWith.WALL),
	POLISHED_GABBRO(() -> new Block(Properties.from(GABBRO.get())), ITaggable.create().withForgeTags("stone")),
	GABBRO_BRICKS(() -> new Block(Properties.from(GABBRO.get())), ComesWith.STAIRS, ComesWith.WALL),
	PAVED_GABBRO_BRICKS(() -> new Block(Properties.from(GABBRO.get())), ComesWith.SLAB),
	INDENTED_GABBRO(() -> new Block(Properties.from(GABBRO.get())), ComesWith.SLAB),
	SLIGHTLY_MOSSY_GABBRO_BRICKS(() -> new Block(Properties.from(GABBRO.get()))),
	MOSSY_GABBRO_BRICKS(() -> new Block(Properties.from(GABBRO.get()))),
	GABBRO_LAYERS(() -> new LayeredCTBlock(Properties.from(GABBRO.get()), AllCTs.GABBRO_LAYERS, AllCTs.POLISHED_GABBRO)),

	DOLOMITE(() -> new Block(Properties.from(Blocks.QUARTZ_BLOCK)), ITaggable.create().withForgeTags("stone"), ComesWith.STAIRS, ComesWith.SLAB, ComesWith.WALL),
	DOLOMITE_BRICKS(() -> new Block(Properties.from(DOLOMITE.get()))),
	POLISHED_DOLOMITE(() -> new Block(Properties.from(DOLOMITE.get())), ITaggable.create().withForgeTags("stone")),
	DOLOMITE_PILLAR(() -> new RotatedPillarBlock(Properties.from(DOLOMITE.get()))),
	DOLOMITE_LAYERS(() -> 
			new LayeredCTBlock(Properties.from(DOLOMITE.get()), AllCTs.DOLOMITE_LAYERS, AllCTs.POLISHED_DOLOMITE)),

	LIMESAND(() -> new FallingBlock(Properties.from(Blocks.SAND))),
	LIMESTONE(() -> new Block(Properties.from(Blocks.SANDSTONE)), ITaggable.create().withForgeTags("stone"), ComesWith.STAIRS, ComesWith.SLAB, ComesWith.WALL),
	LIMESTONE_BRICKS(() -> new Block(Properties.from(LIMESTONE.get())), ComesWith.STAIRS, ComesWith.SLAB, ComesWith.WALL),
	POLISHED_LIMESTONE(() -> new Block(Properties.from(LIMESTONE.get())), ITaggable.create().withForgeTags("stone"), ComesWith.SLAB),
	LIMESTONE_PILLAR(() -> new RotatedPillarBlock(Properties.from(LIMESTONE.get()))),
	LIMESTONE_LAYERS(() -> 
			new LayeredCTBlock(Properties.from(LIMESTONE.get()), AllCTs.LIMESTONE_LAYERS, AllCTs.POLISHED_LIMESTONE)),
	WEATHERED_LIMESTONE(() -> new Block(Properties.from(Blocks.ANDESITE)), ITaggable.create().withForgeTags("stone"), ComesWith.STAIRS, ComesWith.SLAB, ComesWith.WALL),
	WEATHERED_LIMESTONE_BRICKS(() -> new Block(Properties.from(WEATHERED_LIMESTONE.get())), ComesWith.STAIRS, ComesWith.SLAB,
			ComesWith.WALL),
	POLISHED_WEATHERED_LIMESTONE(() -> new Block(Properties.from(WEATHERED_LIMESTONE.get())), ITaggable.create().withForgeTags("stone"), ComesWith.SLAB),
	WEATHERED_LIMESTONE_PILLAR(() -> new RotatedPillarBlock(Properties.from(WEATHERED_LIMESTONE.get()))),
	WEATHERED_LIMESTONE_LAYERS(() -> new LayeredCTBlock(Properties.from(WEATHERED_LIMESTONE.get()),
			AllCTs.WEATHERED_LIMESTONE_LAYERS, AllCTs.POLISHED_WEATHERED_LIMESTONE)),

	NATURAL_SCORIA(ScoriaBlock::new),
	SCORIA(() -> new Block(Properties.from(Blocks.ANDESITE)), ITaggable.create().withForgeTags("stone"), ComesWith.STAIRS, ComesWith.SLAB, ComesWith.WALL),
	POLISHED_SCORIA(() -> new Block(Properties.from(SCORIA.get())), ITaggable.create().withForgeTags("stone"), ComesWith.SLAB),
	SCORIA_BRICKS(() -> new Block(Properties.from(SCORIA.get()))),
	SCORIA_LAYERS(() -> new LayeredCTBlock(Properties.from(SCORIA.get()), AllCTs.SCORIA_LAYERS, AllCTs.POLISHED_SCORIA)),
	SCORIA_PILLAR(() -> new RotatedPillarBlock(Properties.from(SCORIA.get()))),

	DARK_SCORIA(() -> new Block(Properties.from(Blocks.ANDESITE))),
	POLISHED_DARK_SCORIA(() -> new Block(Properties.from(DARK_SCORIA.get()))),
	DARK_SCORIA_TILES(() -> new Block(Properties.from(DARK_SCORIA.get())), ComesWith.STAIRS, ComesWith.SLAB),
	DARK_SCORIA_BRICKS(() -> new Block(Properties.from(DARK_SCORIA.get())), ComesWith.STAIRS, ComesWith.SLAB, ComesWith.WALL),

	__MATERIALS__(),
	COPPER_ORE(() -> new OxidizingBlock(Properties.from(Blocks.IRON_ORE), 1), ITaggable.create().withForgeTags("ores/copper")),
	ZINC_ORE(() -> new Block(Properties.from(Blocks.GOLD_ORE).harvestLevel(2).harvestTool(ToolType.PICKAXE)), ITaggable.create().withForgeTags("ores/zinc")),
	COPPER_BLOCK(() -> new OxidizingBlock(Properties.from(Blocks.IRON_BLOCK), 1 / 32f), ITaggable.create().withForgeTags("storage_blocks/copper")),
	COPPER_SHINGLES(() -> new OxidizingBlock(Properties.from(Blocks.IRON_BLOCK), 1 / 32f)),
	ZINC_BLOCK(() -> new Block(Properties.from(Blocks.IRON_BLOCK)), ITaggable.create().withForgeTags("storage_blocks/zinc")),
	BRASS_BLOCK(() -> new Block(Properties.from(Blocks.IRON_BLOCK)), ITaggable.create().withForgeTags("storage_blocks/brass")),

	;
    
	private enum ComesWith {
		NO_BLOCKITEM, WALL, FENCE, FENCE_GATE, SLAB, STAIRS
	}

	private static class CategoryTracker {
		static IModule currentModule;
	}

	public final RegistryEntry<? extends Block> block;
	public final ImmutableList<RegistryEntry<? extends Block>> alsoRegistered;
	public final IModule module;

	AllBlocks() {
		CategoryTracker.currentModule = () -> Lang.asId(name()).replaceAll("__", "");
		this.block = null;
		this.alsoRegistered = ImmutableList.of();
		this.module = CategoryTracker.currentModule;
	}

	AllBlocks(NonNullSupplier<? extends Block> block, ComesWith... comesWith) {
		this(block, ITaggable.create(), comesWith);
	}
	
	AllBlocks(NonNullSupplier<? extends Block> block, NonNullBiFunction<? super Block, Item.Properties, ? extends BlockItem> customItemCreator, ComesWith... comesWith) {
        this(block, customItemCreator, ITaggable.create(), comesWith);
    }

	AllBlocks(NonNullSupplier<? extends Block> block, ITaggable<?> tags, ComesWith... comesWith) {
	    this(block, null, tags, comesWith);
	}

	AllBlocks(NonNullSupplier<? extends Block> block, NonNullBiFunction<? super Block, Item.Properties, ? extends BlockItem> customItemCreator, ITaggable<?> tags, ComesWith... comesWith){
		this.module = CategoryTracker.currentModule;
		
		this.block = Create.registrate().block(Lang.asId(name()), $ -> block.get()) // TODO take properties as input
		        .transform(applyTags(tags))
		        .transform(b -> registerItemBlock(b, customItemCreator, comesWith))
		        .register();

		ImmutableList.Builder<RegistryEntry<? extends Block>> alsoRegistered = ImmutableList.builder();
		for (ComesWith with : comesWith) {
		    if (with != ComesWith.NO_BLOCKITEM) {
		        alsoRegistered.add(makeRelatedBlock(this.block, with));
		    }
		}
		this.alsoRegistered = alsoRegistered.build();
	}

	public static void register() {
	}

	public <B extends Block, P> BlockBuilder<B, P> registerItemBlock(BlockBuilder<B, P> builder, NonNullBiFunction<? super B, Item.Properties, ? extends BlockItem> customItemCreator, ComesWith... comesWith) {
	    if (ArrayUtils.contains(comesWith, ComesWith.NO_BLOCKITEM)) {
	        return builder;
	    }
		return registerAsItem(builder, customItemCreator);
	}

	private <B extends Block, P> BlockBuilder<B, P> registerAsItem(BlockBuilder<B, P> builder, NonNullBiFunction<? super B, Item.Properties, ? extends BlockItem> customItemCreator) {
		ItemBuilder<? extends BlockItem, BlockBuilder<B, P>> itemBuilder = customItemCreator == null ? builder.item() : builder.item(customItemCreator);
		return itemBuilder.properties($ -> AllItems.includeInItemGroup()).build();
	}

	@Override
	public @Nonnull Block get() {
		return block == null ? Blocks.AIR : block.get();
	}

	public BlockState getDefault() {
		return get().getDefaultState();
	}

	public boolean typeOf(BlockState state) {
		return state.getBlock() == get();
	}

	private RegistryEntry<? extends Block> makeRelatedBlock(RegistryEntry<? extends Block> block, ComesWith feature) {
		NonNullFunction<Block.Properties, ? extends Block> creator;
		final Tag<Block> tag;

		switch (feature) {
		case FENCE:
		    creator = FenceBlock::new;
		    tag = BlockTags.FENCES;
			break;
		case SLAB:
		    creator = SlabBlock::new;
            tag = BlockTags.SLABS;
			break;
		case STAIRS:
		    creator = p -> new ProperStairsBlock(block.get());
		    tag = BlockTags.STAIRS;
			break;
		case WALL:
		    creator = WallBlock::new;
		    tag = BlockTags.WALLS;
			break;
		case FENCE_GATE:
		    creator = FenceGateBlock::new;
		    tag = null;
			break;
		default:
		    throw new IllegalArgumentException("Unknown ComesWith type?");
		}

		return Create.registrate().block(block.getId().getPath() + "_" + Lang.asId(feature.name()), creator)
		        .simpleItem()
		        .transform(b -> tag != null ? b.tag(tag) : b)
		        .register();
	}
	
	private <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> applyTags(ITaggable<?> tags) {
	    return b -> {
	        tags.getDataTags(ITaggable.BLOCK).forEach(b::tag);
	        return b;
	    };
	}

	@OnlyIn(Dist.CLIENT)
	public static void registerColorHandlers() {
		BlockColors blockColors = Minecraft.getInstance().getBlockColors();
		for (AllBlocks block : values()) {
			if (block.get() instanceof IHaveColorHandler) {
				blockColors.register(((IHaveColorHandler) block.get()).getColorHandler(), block.get());
			}
		}
	}
}
