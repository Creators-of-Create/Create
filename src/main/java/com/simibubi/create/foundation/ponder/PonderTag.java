package com.simibubi.create.foundation.ponder;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.element.ScreenElement;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PonderTag implements ScreenElement {

	public static final PonderTag

	KINETIC_RELAYS = create("kinetic_relays").item(AllBlocks.COGWHEEL.get())
		.defaultLang("Kinetic Blocks", "Components which help relaying Rotational Force elsewhere")
		.addToIndex(),

		KINETIC_SOURCES = create("kinetic_sources").item(AllBlocks.WATER_WHEEL.get())
			.defaultLang("Kinetic Sources", "Components which generate Rotational Force")
			.addToIndex(),

		KINETIC_APPLIANCES = create("kinetic_appliances").item(AllBlocks.MECHANICAL_PRESS.get())
			.defaultLang("Kinetic Appliances", "Components which make use of Rotational Force")
			.addToIndex(),

		FLUIDS = create("fluids").item(AllBlocks.FLUID_PIPE.get())
			.defaultLang("Fluid Manipulators", "Components which help relaying and making use of Fluids")
			.addToIndex(),

		LOGISTICS = create("logistics").item(Blocks.CHEST)
			.defaultLang("Item Transportation", "Components which help moving items around")
			.addToIndex(),

		REDSTONE = create("redstone").item(Items.REDSTONE)
			.defaultLang("Logic Components", "Components which help with redstone engineering")
			.addToIndex(),

		DECORATION = create("decoration").item(Items.ROSE_BUSH)
			.defaultLang("Aesthetics", "Components used mostly for decorative purposes"),

		CREATIVE = create("creative").item(AllBlocks.CREATIVE_CRATE.get())
			.defaultLang("Creative Mode", "Components not usually available for Survival Mode")
			.addToIndex(),

		MOVEMENT_ANCHOR = create("movement_anchor").item(AllBlocks.MECHANICAL_PISTON.get())
			.defaultLang("Movement Anchors",
				"Components which allow the creation of moving contraptions, animating an attached structure in a variety of ways")
			.addToIndex(),

		CONTRAPTION_ACTOR = create("contraption_actor").item(AllBlocks.MECHANICAL_HARVESTER.get())
			.defaultLang("Contraption Actors",
				"Components which expose special behaviour when attached to a moving contraption")
			.addToIndex(),

		CONTRAPTION_ASSEMBLY = create("contraption_assembly").item(AllItems.SUPER_GLUE.get())
			.defaultLang("Block Attachment Utility",
				"Tools and Components used to assemble structures moved as an animated Contraption")
			.addToIndex(),

		SAILS = create("windmill_sails").item(AllBlocks.WINDMILL_BEARING.get(), true, true)
			.defaultLang("Sails for Windmill Bearings",
				"Blocks that count towards the strength of a Windmill Contraption when assembled. Each of these have equal efficiency in doing so."),

		ARM_TARGETS = create("arm_targets").item(AllBlocks.MECHANICAL_ARM.get(), true, true)
			.defaultLang("Targets for Mechanical Arms",
				"Components which can be selected as inputs or outputs to the Mechanical Arm"),

		TRAIN_RELATED = create("train_related").item(AllBlocks.TRACK.get())
			.defaultLang("Railway Equipment", "Components used in the construction or management of Train Contraptions")
			.addToIndex(),

		RECENTLY_UPDATED = create("recently_updated").item(AllBlocks.CLIPBOARD.get())
			.defaultLang("Recent Changes",
				"Components that have been added or changed significantly in the latest versions of Create")
			.addToIndex(),

		DISPLAY_SOURCES = create("display_sources").item(AllBlocks.DISPLAY_LINK.get(), true, true)
			.defaultLang("Sources for Display Links",
				"Components or Blocks which offer some data that can be read with a Display Link"),

		DISPLAY_TARGETS = create("display_targets").item(AllBlocks.DISPLAY_LINK.get(), true, true)
			.defaultLang("Targets for Display Links",
				"Components or Blocks which can process and display the data received from a Display Link");

	public static class Highlight {
		public static final PonderTag ALL = create("_all");
	}

	private final ResourceLocation id;
	private ResourceLocation icon;
	private ItemStack itemIcon = ItemStack.EMPTY;
	private ItemStack mainItem = ItemStack.EMPTY;

	public PonderTag(ResourceLocation id) {
		this.id = id;
	}

	public ResourceLocation getId() {
		return id;
	}

	public ItemStack getMainItem() {
		return mainItem;
	}

	public String getTitle() {
		return PonderLocalization.getTag(id);
	}

	public String getDescription() {
		return PonderLocalization.getTagDescription(id);
	}

	// Builder

	public PonderTag defaultLang(String title, String description) {
		PonderLocalization.registerTag(id, title, description);
		return this;
	}

	public PonderTag addToIndex() {
		PonderRegistry.TAGS.listTag(this);
		return this;
	}

	public PonderTag icon(ResourceLocation location) {
		this.icon = new ResourceLocation(location.getNamespace(), "textures/ponder/tag/" + location.getPath() + ".png");
		return this;
	}

	public PonderTag icon(String location) {
		this.icon = new ResourceLocation(id.getNamespace(), "textures/ponder/tag/" + location + ".png");
		return this;
	}

	public PonderTag idAsIcon() {
		return icon(id);
	}

	public PonderTag item(ItemLike item, boolean useAsIcon, boolean useAsMainItem) {
		if (useAsIcon)
			this.itemIcon = new ItemStack(item);
		if (useAsMainItem)
			this.mainItem = new ItemStack(item);
		return this;
	}

	public PonderTag item(ItemLike item) {
		return this.item(item, true, false);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void render(PoseStack ms, int x, int y) {
		ms.pushPose();
		ms.translate(x, y, 0);
		if (icon != null) {
			RenderSystem.setShaderTexture(0, icon);
			ms.scale(0.25f, 0.25f, 1);
			GuiComponent.blit(ms, 0, 0, 0, 0, 0, 64, 64, 64, 64);
		} else if (!itemIcon.isEmpty()) {
			ms.translate(-2, -2, 0);
			ms.scale(1.25f, 1.25f, 1.25f);
			GuiGameElement.of(itemIcon)
				.render(ms);
		}
		ms.popPose();
	}

	private static PonderTag create(String id) {
		return new PonderTag(Create.asResource(id));
	}

	// Load class
	public static void register() {}

}
