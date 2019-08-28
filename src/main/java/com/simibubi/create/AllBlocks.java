package com.simibubi.create;

import com.simibubi.create.foundation.block.IWithoutBlockItem;
import com.simibubi.create.foundation.block.ProperStairsBlock;
import com.simibubi.create.foundation.block.RenderUtilityAxisBlock;
import com.simibubi.create.foundation.block.RenderUtilityBlock;
import com.simibubi.create.modules.contraptions.base.HalfAxisBlock;
import com.simibubi.create.modules.contraptions.generators.MotorBlock;
import com.simibubi.create.modules.contraptions.generators.WaterWheelBlock;
import com.simibubi.create.modules.contraptions.receivers.CrushingWheelBlock;
import com.simibubi.create.modules.contraptions.receivers.CrushingWheelControllerBlock;
import com.simibubi.create.modules.contraptions.receivers.DrillBlock;
import com.simibubi.create.modules.contraptions.receivers.HarvesterBlock;
import com.simibubi.create.modules.contraptions.receivers.TurntableBlock;
import com.simibubi.create.modules.contraptions.receivers.constructs.ChassisBlock;
import com.simibubi.create.modules.contraptions.receivers.constructs.MechanicalPistonBlock;
import com.simibubi.create.modules.contraptions.receivers.constructs.MechanicalPistonHeadBlock;
import com.simibubi.create.modules.contraptions.receivers.constructs.PistonPoleBlock;
import com.simibubi.create.modules.contraptions.redstone.ContactBlock;
import com.simibubi.create.modules.contraptions.relays.AxisBlock;
import com.simibubi.create.modules.contraptions.relays.AxisTunnelBlock;
import com.simibubi.create.modules.contraptions.relays.BeltBlock;
import com.simibubi.create.modules.contraptions.relays.ClutchBlock;
import com.simibubi.create.modules.contraptions.relays.CogWheelBlock;
import com.simibubi.create.modules.contraptions.relays.EncasedBeltBlock;
import com.simibubi.create.modules.contraptions.relays.GearboxBlock;
import com.simibubi.create.modules.contraptions.relays.GearshiftBlock;
import com.simibubi.create.modules.economy.ShopShelfBlock;
import com.simibubi.create.modules.gardens.CocoaLogBlock;
import com.simibubi.create.modules.logistics.block.ExtractorBlock;
import com.simibubi.create.modules.logistics.block.FlexcrateBlock;
import com.simibubi.create.modules.logistics.block.LinkedExtractorBlock;
import com.simibubi.create.modules.logistics.block.RedstoneBridgeBlock;
import com.simibubi.create.modules.logistics.block.StockswitchBlock;
import com.simibubi.create.modules.schematics.block.CreativeCrateBlock;
import com.simibubi.create.modules.schematics.block.SchematicTableBlock;
import com.simibubi.create.modules.schematics.block.SchematicannonBlock;
import com.simibubi.create.modules.symmetry.block.CrossPlaneSymmetryBlock;
import com.simibubi.create.modules.symmetry.block.PlaneSymmetryBlock;
import com.simibubi.create.modules.symmetry.block.TriplePlaneSymmetryBlock;

import net.minecraft.block.Block;
import net.minecraft.block.Block.Properties;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

public enum AllBlocks {

	// Schematics
	SCHEMATICANNON(new SchematicannonBlock()),
	SCHEMATICANNON_CONNECTOR(new RenderUtilityBlock()),
	SCHEMATICANNON_PIPE(new RenderUtilityBlock()),
	CREATIVE_CRATE(new CreativeCrateBlock()),
	SCHEMATIC_TABLE(new SchematicTableBlock()),

	// Kinetics
	AXIS(new AxisBlock(Properties.from(Blocks.ANDESITE))),
	GEAR(new CogWheelBlock(false)),
	LARGE_GEAR(new CogWheelBlock(true)),
	AXIS_TUNNEL(new AxisTunnelBlock()),
	ENCASED_BELT(new EncasedBeltBlock()),
	CLUTCH(new ClutchBlock()),
	GEARSHIFT(new GearshiftBlock()),
	GEARBOX(new GearboxBlock()),
	BELT(new BeltBlock()),
	BELT_PULLEY(new RenderUtilityAxisBlock()),
	BELT_ANIMATION(new RenderUtilityBlock()),

	MOTOR(new MotorBlock()),
	WATER_WHEEL(new WaterWheelBlock()),

	TURNTABLE(new TurntableBlock()),
	HALF_AXIS(new HalfAxisBlock()),
	CRUSHING_WHEEL(new CrushingWheelBlock()),
	CRUSHING_WHEEL_CONTROLLER(new CrushingWheelControllerBlock()),

	MECHANICAL_PISTON(new MechanicalPistonBlock(false)),
	STICKY_MECHANICAL_PISTON(new MechanicalPistonBlock(true)),
	MECHANICAL_PISTON_HEAD(new MechanicalPistonHeadBlock()),
	PISTON_POLE(new PistonPoleBlock()),
	CONSTRUCT(new ChassisBlock(ChassisBlock.Type.NORMAL)),
	STICKY_CONSTRUCT(new ChassisBlock(ChassisBlock.Type.STICKY)),
	RELOCATION_CONSTRUCT(new ChassisBlock(ChassisBlock.Type.RELOCATING)),

	DRILL(new DrillBlock()),
	HARVESTER(new HarvesterBlock()),
	CONTACT(new ContactBlock()),

	// Logistics
	REDSTONE_BRIDGE(new RedstoneBridgeBlock()),
	STOCKSWITCH(new StockswitchBlock()),
	FLEXCRATE(new FlexcrateBlock()),
	EXTRACTOR(new ExtractorBlock()),
	LINKED_EXTRACTOR(new LinkedExtractorBlock()),

	// Symmetry
	SYMMETRY_PLANE(new PlaneSymmetryBlock()),
	SYMMETRY_CROSSPLANE(new CrossPlaneSymmetryBlock()),
	SYMMETRY_TRIPLEPLANE(new TriplePlaneSymmetryBlock()),

	// Gardens
	COCOA_LOG(new CocoaLogBlock()),

	// Economy
	SHOP_SHELF(new ShopShelfBlock()),

	// Palettes
	ANDESITE_BRICKS(new Block(Properties.from(Blocks.ANDESITE))),
	DIORITE_BRICKS(new Block(Properties.from(Blocks.DIORITE))),
	GRANITE_BRICKS(new Block(Properties.from(Blocks.GRANITE))),

	GABBRO(new Block(Properties.from(Blocks.ANDESITE))),
	POLISHED_GABBRO(new Block(Properties.from(GABBRO.block))),
	GABBRO_BRICKS(new Block(Properties.from(GABBRO.block))),
	PAVED_GABBRO_BRICKS(new Block(Properties.from(GABBRO.block))),
	INDENTED_GABBRO(new Block(Properties.from(GABBRO.block))),
	SLIGHTLY_MOSSY_GABBRO_BRICKS(new Block(Properties.from(GABBRO.block))),
	MOSSY_GABBRO_BRICKS(new Block(Properties.from(GABBRO.block))),

	LIMESTONE(new Block(Properties.from(Blocks.SANDSTONE))),
	POLISHED_LIMESTONE(new Block(Properties.from(LIMESTONE.block))),
	LIMESTONE_BRICKS(new Block(Properties.from(LIMESTONE.block))),
	LIMESTONE_PILLAR(new RotatedPillarBlock(Properties.from(LIMESTONE.block))),

	QUARTZIORITE(new Block(Properties.from(Blocks.QUARTZ_BLOCK))),
	QUARTZIORITE_BRICKS(new Block(Properties.from(QUARTZIORITE.block))),
	POLISHED_QUARTZIORITE(new Block(Properties.from(QUARTZIORITE.block))),

	DOLOMITE(new Block(Properties.from(Blocks.GRANITE))),
	DOLOMITE_BRICKS(new Block(Properties.from(DOLOMITE.block))),
	POLISHED_DOLOMITE(new Block(Properties.from(DOLOMITE.block))),
	DOLOMITE_PILLAR(new RotatedPillarBlock(Properties.from(DOLOMITE.block))),

	;

	private enum ComesWith {
		WALL, FENCE, FENCE_GATE, SLAB, STAIRS;
	}

	public Block block;
	public Block[] alsoRegistered;

	private AllBlocks(Block block, ComesWith... comesWith) {
		this.block = block;
		this.block.setRegistryName(Create.ID, this.name().toLowerCase());

		alsoRegistered = new Block[comesWith.length];
		for (int i = 0; i < comesWith.length; i++)
			alsoRegistered[i] = makeRelatedBlock(block, comesWith[i]);
	}

	public static void registerBlocks(IForgeRegistry<Block> registry) {
		for (AllBlocks block : values()) {
			registry.register(block.block);
			for (Block extra : block.alsoRegistered)
				registry.register(extra);
		}
	}

	public static void registerItemBlocks(IForgeRegistry<Item> registry) {
		for (AllBlocks block : values()) {
			if (block.get() instanceof IWithoutBlockItem)
				continue;

			registerAsItem(registry, block.get());
			for (Block extra : block.alsoRegistered)
				registerAsItem(registry, extra);
		}
	}

	private static void registerAsItem(IForgeRegistry<Item> registry, Block blockIn) {
		registry.register(
				new BlockItem(blockIn, AllItems.standardProperties()).setRegistryName(blockIn.getRegistryName()));
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

		return featured.setRegistryName(Create.ID,
				block.getRegistryName().getPath() + "_" + feature.name().toLowerCase());
	}

}
