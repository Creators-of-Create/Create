package com.simibubi.create;

import com.simibubi.create.foundation.block.IWithoutBlockItem;
import com.simibubi.create.foundation.block.RenderUtilityBlock;
import com.simibubi.create.modules.kinetics.base.HalfAxisBlock;
import com.simibubi.create.modules.kinetics.generators.MotorBlock;
import com.simibubi.create.modules.kinetics.receivers.TurntableBlock;
import com.simibubi.create.modules.kinetics.relays.AxisBlock;
import com.simibubi.create.modules.kinetics.relays.AxisTunnelBlock;
import com.simibubi.create.modules.kinetics.relays.BeltBlock;
import com.simibubi.create.modules.kinetics.relays.CogWheelBlock;
import com.simibubi.create.modules.kinetics.relays.GearboxBlock;
import com.simibubi.create.modules.kinetics.relays.GearshifterBlock;
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
	GEARSHIFTER(new GearshifterBlock()),
	BELT(new BeltBlock()),
	BELT_ANIMATION(new RenderUtilityBlock()),
	
	TURNTABLE(new TurntableBlock()),
	HALF_AXIS(new HalfAxisBlock()),
	GEARBOX(new GearboxBlock()),
	MOTOR(new MotorBlock()),
	
	// Symmetry
	SYMMETRY_PLANE(new PlaneSymmetryBlock()),
	SYMMETRY_CROSSPLANE(new CrossPlaneSymmetryBlock()),
	SYMMETRY_TRIPLEPLANE(new TriplePlaneSymmetryBlock()),
	
	;

	public Block block;

	private AllBlocks(Block block) {
		this.block = block;
		this.block.setRegistryName(Create.ID, this.name().toLowerCase());
	}

	public static void registerBlocks(IForgeRegistry<Block> registry) {
		for (AllBlocks block : values()) {
			registry.register(block.block);
		}
	}

	public static void registerItemBlocks(IForgeRegistry<Item> registry) {
		for (AllBlocks block : values()) {
			if (block.get() instanceof IWithoutBlockItem)
				continue;

			registry.register(new BlockItem(block.get(), AllItems.standardProperties())
					.setRegistryName(block.get().getRegistryName()));
		}
	}

	public Block get() {
		return block;
	}

	public boolean typeOf(BlockState state) {
		return state.getBlock() == block;
	}

}
