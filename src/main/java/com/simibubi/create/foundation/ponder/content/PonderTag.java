package com.simibubi.create.foundation.ponder.content;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.IScreenRenderable;
import com.simibubi.create.foundation.ponder.PonderLocalization;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PonderTag implements IScreenRenderable {

	//

	public static final PonderTag

	KINETICS = new PonderTag("kinetics").item(AllBlocks.COGWHEEL.get()
		.asItem(), true, false)
		.defaultLang("Kinetic Blocks", "Components which help generating, relaying and making use of Rotational Force"),
		FLUID_TRANSFER = new PonderTag("fluid_transfer").idAsIcon(),
		OPEN_INVENTORY = new PonderTag("open_inventory").item(AllBlocks.BASIN.get()
			.asItem()),
		ARM_ACCESS = new PonderTag("arm_access").item(AllBlocks.MECHANICAL_ARM.get()
			.asItem())
			.defaultLang("Targets for Mechanical Arms",
				"Components which can be selected as inputs or outputs to the Mechanical Arm"),
		REDSTONE_CONTROL = new PonderTag("redstone_control").item(Items.REDSTONE, true, false),
		ITEM_TRANSFER = new PonderTag("item_transfer").idAsIcon();

	public static class Highlight {
		public static final PonderTag ALL = new PonderTag("_all");
	}

	//

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

	public PonderTag icon(String location) {
		this.icon = new ResourceLocation(com.simibubi.create.Create.ID, "textures/ponder/tag/" + location + ".png");
		return this;
	}

	public PonderTag item(Item item) {
		return this.item(item, true, true);
	}

	public PonderTag item(Item item, boolean useAsIcon, boolean useAsMainItem) {
		if (useAsIcon)
			this.itemIcon = new ItemStack(item);
		if (useAsMainItem)
			this.mainItem = new ItemStack(item);
		return this;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void draw(AbstractGui screen, int x, int y) {
		RenderSystem.pushMatrix();
		RenderSystem.translated(x, y, 0);
		if (icon != null) {
			Minecraft.getInstance()
				.getTextureManager()
				.bindTexture(icon);
			RenderSystem.scaled(0.25, 0.25, 1);
			// x and y offset, blit z offset, tex x and y, tex width and height, entire tex
			// sheet width and height
			AbstractGui.blit(0, 0, 0, 0, 0, 64, 64, 64, 64);
		} else if (!itemIcon.isEmpty()) {
			RenderSystem.translated(-4, -4, 0);
			RenderSystem.scaled(1.5, 1.5, 1.5);
			GuiGameElement.of(itemIcon)
				.render();
		}
		RenderSystem.popMatrix();
	}

	// Load class
	public static void register() {}

}
