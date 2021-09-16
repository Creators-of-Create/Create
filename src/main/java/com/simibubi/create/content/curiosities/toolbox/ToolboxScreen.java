package com.simibubi.create.content.curiosities.toolbox;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.gui.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.widgets.IconButton;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class ToolboxScreen extends AbstractSimiContainerScreen<ToolboxContainer> {

	AllGuiTextures BG = AllGuiTextures.TOOLBOX;
	AllGuiTextures PLAYER = AllGuiTextures.PLAYER_INVENTORY;
	protected Slot hoveredToolboxSlot;
	private IconButton confirmButton;
	private IconButton disposeButton;

	private List<Rectangle2d> extraAreas = Collections.emptyList();

	public ToolboxScreen(ToolboxContainer container, PlayerInventory inv, ITextComponent title) {
		super(container, inv, title);
		init();
	}

	@Override
	protected void init() {
		super.init();
		widgets.clear();
		setWindowSize(BG.width, 256);
		confirmButton = new IconButton(getGuiLeft() + BG.width - 23, getGuiTop() + BG.height - 24, AllIcons.I_CONFIRM);
		disposeButton = new IconButton(getGuiLeft() + 91, getGuiTop() + 69, AllIcons.I_TOOLBOX);
		disposeButton.setToolTip(Lang.translate("toolbox.depositBox"));
		widgets.add(confirmButton);
		widgets.add(disposeButton);

		extraAreas = ImmutableList.of(new Rectangle2d(118, 155, 80, 100), new Rectangle2d(308, 125, 100, 70));
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		menu.renderPass = true;
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		menu.renderPass = false;
	}

	@Override
	public void setBlitOffset(int p_230926_1_) {
		super.setBlitOffset(p_230926_1_);
	}

	@Override
	protected void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		BG.draw(ms, this, leftPos + 10, topPos);
		PLAYER.draw(ms, this, leftPos + (BG.width - PLAYER.width) / 2 - 26, topPos + imageHeight - PLAYER.height);
		font.draw(ms, title, leftPos + 24, topPos + 4, 0x442000);
		font.draw(ms, inventory.getDisplayName(), leftPos - 13, topPos + 154, 0x404040);

		renderToolbox(ms, mouseX, mouseY, partialTicks);

		hoveredToolboxSlot = null;
		for (int compartment = 0; compartment < 8; compartment++) {
			int baseIndex = compartment * ToolboxInventory.STACKS_PER_COMPARTMENT;
			Slot slot = menu.slots.get(baseIndex);
			ItemStack itemstack = slot.getItem();
			int i = slot.x + leftPos;
			int j = slot.y + topPos;

			if (itemstack.isEmpty())
				itemstack = menu.getFilter(compartment);

			if (!itemstack.isEmpty()) {
				int count = menu.totalCountInCompartment(compartment);
				String s = count + "";
				setBlitOffset(100);
				itemRenderer.blitOffset = 100.0F;
				RenderSystem.enableDepthTest();
				itemRenderer.renderAndDecorateItem(minecraft.player, itemstack, i, j);
				itemRenderer.renderGuiItemDecorations(font, itemstack, i, j, s);
				setBlitOffset(0);
				itemRenderer.blitOffset = 0.0F;
			}

			if (isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
				hoveredToolboxSlot = slot;
				RenderSystem.disableDepthTest();
				RenderSystem.colorMask(true, true, true, false);
				int slotColor = this.getSlotColor(baseIndex);
				fillGradient(ms, i, j, i + 16, j + 16, slotColor, slotColor);
				RenderSystem.colorMask(true, true, true, true);
				RenderSystem.enableDepthTest();
			}
		}
	}

	private void renderToolbox(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		ms.pushPose();
		ms.translate(397, 190, 100);
		MatrixTransformStack.of(ms)
			.scale(50)
			.rotateX(-22)
			.rotateY(-202);

		GuiGameElement.of(AllBlocks.TOOLBOX.getDefaultState())
			.render(ms);

		ms.pushPose();
		MatrixTransformStack.of(ms)
			.translate(0, -6 / 16f, 12 / 16f)
			.rotateX(-105 * menu.contentHolder.lid.getValue(partialTicks))
			.translate(0, 6 / 16f, -12 / 16f);
		GuiGameElement.of(AllBlockPartials.TOOLBOX_LID)
			.render(ms);
		ms.popPose();

		for (int offset : Iterate.zeroAndOne) {
			ms.pushPose();
			ms.translate(0, -offset * 1 / 8f,
				menu.contentHolder.drawers.getValue(partialTicks) * -.175f * (2 - offset));
			GuiGameElement.of(AllBlockPartials.TOOLBOX_DRAWER)
				.render(ms);
			ms.popPose();
		}
		ms.popPose();
	}

	@Override
	protected void renderWindowForeground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (hoveredToolboxSlot != null)
			hoveredSlot = hoveredToolboxSlot;
		super.renderWindowForeground(matrixStack, mouseX, mouseY, partialTicks);
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		boolean mouseClicked = super.mouseClicked(x, y, button);

		if (button == 0) {
			if (confirmButton.isHovered()) {
				minecraft.player.closeContainer();
				return true;
			}
			if (disposeButton.isHovered()) {
				AllPackets.channel.sendToServer(new ToolboxDisposeAllPacket(menu.contentHolder.getBlockPos()));
				return true;
			}
		}

		return mouseClicked;
	}

	@Override
	public List<Rectangle2d> getExtraAreas() {
		return extraAreas;
	}

}
