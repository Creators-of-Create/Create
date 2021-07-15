package com.simibubi.create.foundation.ponder.content;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.IScreenRenderable;
import com.simibubi.create.foundation.ponder.PonderLocalization;

import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PonderTag implements IScreenRenderable {

	public static final List<PonderTag> LISTED_TAGS = new ArrayList<>();
	
	public static final PonderTag

	KINETIC_RELAYS = new PonderTag("kinetic_relays").item(AllBlocks.COGWHEEL.get(), true, false)
		.defaultLang("Kinetic Blocks", "Components which help relaying Rotational Force elsewhere")
		.addToIndex(),

		KINETIC_SOURCES = new PonderTag("kinetic_sources").item(AllBlocks.WATER_WHEEL.get(), true, false)
			.defaultLang("Kinetic Sources", "Components which generate Rotational Force")
			.addToIndex(),

		KINETIC_APPLIANCES = new PonderTag("kinetic_appliances").item(AllBlocks.MECHANICAL_PRESS.get(), true, false)
			.defaultLang("Kinetic Appliances", "Components which make use of Rotational Force")
			.addToIndex(),

		FLUIDS = new PonderTag("fluids").item(AllBlocks.FLUID_PIPE.get(), true, false)
			.defaultLang("Fluid Manipulators", "Components which help relaying and making use of Fluids")
			.addToIndex(),

		LOGISTICS = new PonderTag("logistics").item(Blocks.CHEST, true, false)
			.defaultLang("Item Transportation", "Components which help moving items around")
			.addToIndex(),

		REDSTONE = new PonderTag("redstone").item(Items.REDSTONE, true, false)
			.defaultLang("Logic Components", "Components which help with redstone engineering")
			.addToIndex(),

		DECORATION = new PonderTag("decoration").item(Items.ROSE_BUSH, true, false)
			.defaultLang("Aesthetics", "Components used mostly for decorative purposes"),

		CREATIVE = new PonderTag("creative").item(AllBlocks.CREATIVE_CRATE.get(), true, false)
			.defaultLang("Creative Mode", "Components not usually available for Survival Mode")
			.addToIndex(),

		MOVEMENT_ANCHOR = new PonderTag("movement_anchor").item(AllBlocks.MECHANICAL_PISTON.get(), true, false)
			.defaultLang("Movement Anchors",
				"Components which allow the creation of moving contraptions, animating an attached structure in a variety of ways")
			.addToIndex(),

		CONTRAPTION_ACTOR = new PonderTag("contraption_actor").item(AllBlocks.MECHANICAL_HARVESTER.get(), true, false)
			.defaultLang("Contraption Actors",
				"Components which expose special behaviour when attached to a moving contraption")
			.addToIndex(),

		CONTRAPTION_ASSEMBLY = new PonderTag("contraption_assembly").item(AllItems.SUPER_GLUE.get(), true, false)
			.defaultLang("Block Attachment Utility",
				"Tools and Components used to assemble structures moved as an animated Contraption")
			.addToIndex(),

		SAILS = new PonderTag("windmill_sails").item(AllBlocks.WINDMILL_BEARING.get(), true, true)
			.defaultLang("Sails for Windmill Bearings",
				"Blocks that count towards the strength of a Windmill Contraption when assembled. Each of these have equal efficiency in doing so."),

		ARM_TARGETS = new PonderTag("arm_targets").item(AllBlocks.MECHANICAL_ARM.get())
			.defaultLang("Targets for Mechanical Arms",
				"Components which can be selected as inputs or outputs to the Mechanical Arm");

	public static class Highlight {
		public static final PonderTag ALL = new PonderTag("_all");
	}

	private final String id;
	private ResourceLocation icon;
	private ItemStack itemIcon = ItemStack.EMPTY;
	private ItemStack mainItem = ItemStack.EMPTY;

	public String getTitle() {
		return PonderLocalization.getTag(id);
	}

	public String getDescription() {
		return PonderLocalization.getTagDescription(id);
	}

	// Builder

	public PonderTag(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public PonderTag defaultLang(String title, String description) {
		PonderLocalization.registerTag(id, title, description);
		return this;
	}

	public ItemStack getMainItem() {
		return mainItem;
	}

	public PonderTag idAsIcon() {
		return icon(id);
	}

	public PonderTag addToIndex() {
		LISTED_TAGS.add(this);
		return this;
	}

	public PonderTag icon(String location) {
		this.icon = new ResourceLocation(com.simibubi.create.Create.ID, "textures/ponder/tag/" + location + ".png");
		return this;
	}

	public PonderTag item(IItemProvider item) {
		return this.item(item, true, true);
	}

	public PonderTag item(IItemProvider item, boolean useAsIcon, boolean useAsMainItem) {
		if (useAsIcon)
			this.itemIcon = new ItemStack(item);
		if (useAsMainItem)
			this.mainItem = new ItemStack(item);
		return this;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void draw(MatrixStack ms, AbstractGui screen, int x, int y) {
		ms.pushPose();
		ms.translate(x, y, 0);
		if (icon != null) {
			Minecraft.getInstance()
				.getTextureManager()
				.bind(icon);
			ms.scale(0.25f, 0.25f, 1);
			// x and y offset, blit z offset, tex x and y, tex width and height, entire tex
			// sheet width and height
			AbstractGui.blit(ms, 0, 0, 0, 0, 0, 64, 64, 64, 64);
		} else if (!itemIcon.isEmpty()) {
			ms.translate(-4, -4, 0);
			ms.scale(1.5f, 1.5f, 1.5f);
			GuiGameElement.of(itemIcon)
				.render(ms);
		}
		ms.popPose();
	}

	// Load class
	public static void register() {}

}
