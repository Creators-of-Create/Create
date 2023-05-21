package com.simibubi.create.foundation.ponder;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.element.ScreenElement;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PonderTag implements ScreenElement {

	public static final PonderTag HIGHLIGHT_ALL = new PonderTag(Create.asResource("_all"));

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

}
