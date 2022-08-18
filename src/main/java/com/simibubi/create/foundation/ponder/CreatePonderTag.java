package com.simibubi.create.foundation.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;

import net.createmod.ponder.foundation.PonderTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class CreatePonderTag extends PonderTag {

	public static final PonderTag KINETIC_RELAYS = create("kinetic_relays")
			.item(AllBlocks.COGWHEEL.get(), true, false)
			.defaultLang("Kinetic Blocks", "Components which help relaying Rotational Force elsewhere")
			.addToIndex();

	public static final PonderTag KINETIC_SOURCES = create("kinetic_sources")
			.item(AllBlocks.WATER_WHEEL.get(), true, false)
			.defaultLang("Kinetic Sources", "Components which generate Rotational Force")
			.addToIndex();
	public static final PonderTag KINETIC_APPLIANCES = create("kinetic_appliances")
			.item(AllBlocks.MECHANICAL_PRESS.get(), true, false)
			.defaultLang("Kinetic Appliances", "Components which make use of Rotational Force")
			.addToIndex();
	public static final PonderTag FLUIDS = create("fluids")
			.item(AllBlocks.FLUID_PIPE.get(), true, false)
			.defaultLang("Fluid Manipulators", "Components which help relaying and making use of Fluids")
			.addToIndex();
	public static final PonderTag LOGISTICS = create("logistics")
			.item(Blocks.CHEST, true, false)
			.defaultLang("Item Transportation", "Components which help moving items around")
			.addToIndex();
	public static final PonderTag REDSTONE = create("redstone")
			.item(Items.REDSTONE, true, false)
			.defaultLang("Logic Components", "Components which help with redstone engineering")
			.addToIndex();
	public static final PonderTag DECORATION = create("decoration")
			.item(Items.ROSE_BUSH, true, false)
			.defaultLang("Aesthetics", "Components used mostly for decorative purposes");
	public static final PonderTag CREATIVE = create("creative").item(AllBlocks.CREATIVE_CRATE.get(), true, false)
			.defaultLang("Creative Mode", "Components not usually available for Survival Mode")
			.addToIndex();
	public static final PonderTag MOVEMENT_ANCHOR = create("movement_anchor")
			.item(AllBlocks.MECHANICAL_PISTON.get(), true, false)
			.defaultLang("Movement Anchors",
				"Components which allow the creation of moving contraptions, animating an attached structure in a variety of ways")
			.addToIndex();
	public static final PonderTag CONTRAPTION_ACTOR = create("contraption_actor")
			.item(AllBlocks.MECHANICAL_HARVESTER.get(), true, false)
			.defaultLang("Contraption Actors",
				"Components which expose special behaviour when attached to a moving contraption")
			.addToIndex();
	public static final PonderTag CONTRAPTION_ASSEMBLY = create("contraption_assembly")
			.item(AllItems.SUPER_GLUE.get(), true, false)
			.defaultLang("Block Attachment Utility",
				"Tools and Components used to assemble structures moved as an animated Contraption")
			.addToIndex();
	public static final PonderTag SAILS = create("windmill_sails")
			.item(AllBlocks.WINDMILL_BEARING.get())
			.defaultLang("Sails for Windmill Bearings",
				"Blocks that count towards the strength of a Windmill Contraption when assembled. Each of these have equal efficiency in doing so.");
	public static final PonderTag ARM_TARGETS = create("arm_targets")
			.item(AllBlocks.MECHANICAL_ARM.get())
			.defaultLang("Targets for Mechanical Arms",
				"Components which can be selected as inputs or outputs to the Mechanical Arm");
	public static final PonderTag TRAIN_RELATED = create("train_related")
			.item(AllBlocks.TRACK.get())
			.defaultLang("Railway Equipment", "Components used in the construction or management of Train Contraptions")
			.addToIndex();
	public static final PonderTag DISPLAY_SOURCES = create("display_sources")
			.item(AllBlocks.DISPLAY_LINK.get(), true, false)
			.item(AllBlocks.DISPLAY_LINK.get(), false, true)
			.defaultLang("Sources for Display Links",
				"Components or Blocks which offer some data that can be read with a Display Link");
	public static final PonderTag DISPLAY_TARGETS = create("display_targets")
			.item(AllBlocks.DISPLAY_LINK.get(), true, false)
			.item(AllBlocks.DISPLAY_LINK.get(), false, true)
			.defaultLang("Targets for Display Links",
				"Components or Blocks which can process and display the data received from a Display Link");

	public CreatePonderTag(ResourceLocation id) {
		super(id);
	}

	private static PonderTag create(String id) {
		return create(Create.ID, id);
	}

	// Make sure class is loaded; Lang registration happens with builder calls
	public static void register() {}
}
