package com.simibubi.create.content.curiosities.toolbox;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.container.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public class ToolboxScreen extends AbstractSimiContainerScreen<ToolboxContainer> {

	protected static final AllGuiTextures BG = AllGuiTextures.TOOLBOX;
	protected static final AllGuiTextures PLAYER = AllGuiTextures.PLAYER_INVENTORY;

	protected Slot hoveredToolboxSlot;
	private IconButton confirmButton;
	private IconButton disposeButton;
	private DyeColor color;

	private List<Rect2i> extraAreas = Collections.emptyList();

	public ToolboxScreen(ToolboxContainer container, Inventory inv, Component title) {
		super(container, inv, title);
		init();
	}

	@Override
	protected void init() {
		setWindowSize(30 + BG.width, BG.height + PLAYER.height - 24);
		setWindowOffset(-11, 0);
		super.init();

		color = menu.contentHolder.getColor();

		confirmButton = new IconButton(leftPos + 30 + BG.width - 33, topPos + BG.height - 24, AllIcons.I_CONFIRM);
		confirmButton.withCallback(() -> {
			minecraft.player.closeContainer();
		});
		addRenderableWidget(confirmButton);

		disposeButton = new IconButton(leftPos + 30 + 81, topPos + 69, AllIcons.I_TOOLBOX);
		disposeButton.withCallback(() -> {
			AllPackets.channel.sendToServer(new ToolboxDisposeAllPacket(menu.contentHolder.getBlockPos()));
		});
		disposeButton.setToolTip(Lang.translateDirect("toolbox.depositBox"));
		addRenderableWidget(disposeButton);

		extraAreas = ImmutableList.of(
			new Rect2i(leftPos + 30 + BG.width, topPos + BG.height - 15 - 34 - 6, 72, 68)
		);
	}

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		menu.renderPass = true;
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		menu.renderPass = false;
	}

	@Override
	protected void renderBg(PoseStack ms, float partialTicks, int mouseX, int mouseY) {
		int x = leftPos + imageWidth - BG.width;
		int y = topPos;

		BG.render(ms, x, y, this);
		font.draw(ms, title, x + 15, y + 4, 0x442000);

		int invX = leftPos;
		int invY = topPos + imageHeight - PLAYER.height;
		renderPlayerInventory(ms, invX, invY);

		renderToolbox(ms, x + BG.width + 50, y + BG.height + 12, partialTicks);

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
				String s = String.valueOf(count);
				setBlitOffset(100);
				itemRenderer.blitOffset = 100.0F;
				RenderSystem.enableDepthTest();
				itemRenderer.renderAndDecorateItem(minecraft.player, itemstack, i, j, 0);
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

	private void renderToolbox(PoseStack ms, int x, int y, float partialTicks) {
        TransformStack.cast(ms)
			.pushPose()
			.translate(x, y, 100)
			.scale(50)
			.rotateX(-22)
			.rotateY(-202);

		GuiGameElement.of(AllBlocks.TOOLBOXES.get(color)
			.getDefaultState())
			.render(ms);

        TransformStack.cast(ms)
			.pushPose()
			.translate(0, -6 / 16f, 12 / 16f)
			.rotateX(-105 * menu.contentHolder.lid.getValue(partialTicks))
			.translate(0, 6 / 16f, -12 / 16f);
		GuiGameElement.of(AllBlockPartials.TOOLBOX_LIDS.get(color))
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
	protected void renderForeground(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (hoveredToolboxSlot != null)
			hoveredSlot = hoveredToolboxSlot;
		super.renderForeground(matrixStack, mouseX, mouseY, partialTicks);
	}

	@Override
	public List<Rect2i> getExtraAreas() {
		return extraAreas;
	}

}
