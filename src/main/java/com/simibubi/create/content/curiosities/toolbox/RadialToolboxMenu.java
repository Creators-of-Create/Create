package com.simibubi.create.content.curiosities.toolbox;

import java.util.List;

import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllKeys;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

public class RadialToolboxMenu extends AbstractSimiScreen {

	public static enum State {
		SELECT_BOX, SELECT_ITEM, SELECT_ITEM_UNEQUIP, DETACH
	}

	private State state;
	private int ticksOpen;
	private int hoveredSlot;

	private List<ToolboxTileEntity> toolboxes;
	private ToolboxTileEntity selectedBox;
	final int UNEQUIP = -5;

	public RadialToolboxMenu(List<ToolboxTileEntity> toolboxes, State state) {
		this.toolboxes = toolboxes;
		this.state = state;
		hoveredSlot = -1;

		if (state == State.SELECT_ITEM_UNEQUIP || state == State.SELECT_ITEM)
			selectedBox = toolboxes.get(0);
	}

	@Override
	protected void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		float fade = MathHelper.clamp((ticksOpen + AnimationTickHolder.getPartialTicks()) / 10f, 1 / 512f, 1);

		hoveredSlot = -1;
		MainWindow window = getMinecraft().getWindow();
		float hoveredX = mouseX - window.getGuiScaledWidth() / 2;
		float hoveredY = mouseY - window.getGuiScaledHeight() / 2;

		float distance = hoveredX * hoveredX + hoveredY * hoveredY;
		if (distance > 25 && distance < 20000)
			hoveredSlot =
				(MathHelper.floor((AngleHelper.deg(MathHelper.atan2(hoveredY, hoveredX)) + 360 + 180 - 22.5f)) % 360)
					/ 45;
		boolean renderCenterSlot = state == State.SELECT_ITEM_UNEQUIP;
		if (renderCenterSlot && distance <= 150)
			hoveredSlot = UNEQUIP;

		ms.pushPose();
		ms.translate(width / 2, height / 2, 0);
		ITextComponent tip = null;

		if (state == State.DETACH) {

			tip = Lang.translate("toolbox.outOfRange");
			if (hoveredX > -20 && hoveredX < 20 && hoveredY > -80 && hoveredY < -20)
				hoveredSlot = UNEQUIP;

			ms.pushPose();
			AllGuiTextures.TOOLBELT_INACTIVE_SLOT.draw(ms, this, -12, -12);
			GuiGameElement.of(AllBlocks.TOOLBOX.asStack())
				.at(-9, -9)
				.render(ms);

			ms.translate(0, -40 + (10 * (1 - fade) * (1 - fade)), 0);
			AllGuiTextures.TOOLBELT_SLOT.draw(ms, this, -12, -12);
			ms.translate(-0.5, 0.5, 0);
			AllIcons.I_DISABLE.draw(ms, this, -9, -9);
			ms.translate(0.5, -0.5, 0);
			if (hoveredSlot == UNEQUIP) {
				AllGuiTextures.TOOLBELT_SLOT_HIGHLIGHT.draw(ms, this, -13, -13);
				tip = Lang.translate("toolbox.detach")
					.withStyle(TextFormatting.GOLD);
			}
			ms.popPose();

		} else {
			for (int slot = 0; slot < 8; slot++) {
				ms.pushPose();
				MatrixTransformStack.of(ms)
					.rotateZ(slot * 45 - 45)
					.translate(0, -40 + (10 * (1 - fade) * (1 - fade)), 0)
					.rotateZ(-slot * 45 + 45);
				ms.translate(-12, -12, 0);

				if (state == State.SELECT_ITEM || state == State.SELECT_ITEM_UNEQUIP) {
					ToolboxInventory inv = selectedBox.inventory;
					ItemStack stackInSlot = inv.filters.get(slot);

					if (!stackInSlot.isEmpty()) {
						boolean empty = inv.getStackInSlot(slot * ToolboxInventory.STACKS_PER_COMPARTMENT)
							.isEmpty();

						(empty ? AllGuiTextures.TOOLBELT_INACTIVE_SLOT : AllGuiTextures.TOOLBELT_SLOT).draw(ms, this, 0,
							0);
						GuiGameElement.of(stackInSlot)
							.at(3, 3)
							.render(ms);

						if (slot == hoveredSlot && !empty) {
							AllGuiTextures.TOOLBELT_SLOT_HIGHLIGHT.draw(ms, this, -1, -1);
							tip = stackInSlot.getHoverName();
						}
					} else
						AllGuiTextures.TOOLBELT_EMPTY_SLOT.draw(ms, this, 0, 0);

				} else if (state == State.SELECT_BOX) {

					if (slot < toolboxes.size()) {
						AllGuiTextures.TOOLBELT_SLOT.draw(ms, this, 0, 0);
						GuiGameElement.of(AllBlocks.TOOLBOX.asStack())
							.at(3, 3)
							.render(ms);

						if (slot == hoveredSlot) {
							AllGuiTextures.TOOLBELT_SLOT_HIGHLIGHT.draw(ms, this, -1, -1);
							tip = toolboxes.get(slot)
								.getDisplayName();
						}
					} else
						AllGuiTextures.TOOLBELT_EMPTY_SLOT.draw(ms, this, 0, 0);

				}

				ms.popPose();
			}

			if (renderCenterSlot) {
				ms.pushPose();
				AllGuiTextures.TOOLBELT_SLOT.draw(ms, this, -12, -12);
				AllIcons.I_TRASH.draw(ms, this, -9, -9);
				if (UNEQUIP == hoveredSlot) {
					AllGuiTextures.TOOLBELT_SLOT_HIGHLIGHT.draw(ms, this, -13, -13);
					tip = Lang.translate("toolbox.unequip", minecraft.player.getMainHandItem()
						.getHoverName())
						.withStyle(TextFormatting.GOLD);
				}
				ms.popPose();
			}
		}
		ms.popPose();

		if (tip != null) {
			int i1 = (int) (fade * 255.0F);
			if (i1 > 255)
				i1 = 255;

			if (i1 > 8) {
				ms.pushPose();
				ms.translate((float) (width / 2), (float) (height - 68), 0.0F);
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				int k1 = 16777215;
				int k = i1 << 24 & -16777216;
				int l = font.width(tip);
//				this.drawBackdrop(ms, font, -4, l, 16777215 | k);
				font.draw(ms, tip, (float) (-l / 2), -4.0F, k1 | k);
				RenderSystem.disableBlend();
				ms.popPose();
			}
		}

	}

	@Override
	public void renderBackground(MatrixStack p_238651_1_, int p_238651_2_) {
		int a = ((int) (0x50 * Math.min(1, (ticksOpen + AnimationTickHolder.getPartialTicks()) / 20f))) << 24;
		fillGradient(p_238651_1_, 0, 0, this.width, this.height, 0x101010 | a, 0x101010 | a);
	}

	@Override
	public void tick() {
		ticksOpen++;
		super.tick();
	}

	@Override
	public void removed() {
		super.removed();

		if (state == State.SELECT_BOX)
			return;

		if (state == State.DETACH) {
			if (hoveredSlot == UNEQUIP)
				AllPackets.channel.sendToServer(
					new ToolboxEquipPacket(null, hoveredSlot, Minecraft.getInstance().player.inventory.selected));
			return;
		}

		if (hoveredSlot == UNEQUIP)
			AllPackets.channel.sendToServer(new ToolboxEquipPacket(selectedBox.getBlockPos(), hoveredSlot,
				Minecraft.getInstance().player.inventory.selected));

		if (hoveredSlot < 0)
			return;
		ToolboxInventory inv = selectedBox.inventory;
		ItemStack stackInSlot = inv.filters.get(hoveredSlot);
		if (stackInSlot.isEmpty())
			return;
		if (inv.getStackInSlot(hoveredSlot * ToolboxInventory.STACKS_PER_COMPARTMENT)
			.isEmpty())
			return;

		AllPackets.channel.sendToServer(new ToolboxEquipPacket(selectedBox.getBlockPos(), hoveredSlot,
			Minecraft.getInstance().player.inventory.selected));
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		if (state == State.SELECT_BOX && hoveredSlot >= 0 && hoveredSlot < toolboxes.size()) {
			state = State.SELECT_ITEM;
			selectedBox = toolboxes.get(hoveredSlot);
			return true;
		}

		if (state == State.SELECT_ITEM || state == State.SELECT_ITEM_UNEQUIP) {
			if (hoveredSlot == UNEQUIP || hoveredSlot >= 0) {
				onClose();
				ToolboxHandlerClient.COOLDOWN = 10;
			}
		}

		return super.mouseClicked(x, y, button);
	}

	@Override
	public boolean keyReleased(int code, int p_keyPressed_2_, int p_keyPressed_3_) {
		InputMappings.Input mouseKey = InputMappings.getKey(code, p_keyPressed_2_);
		if (AllKeys.TOOLBELT.getKeybind()
			.isActiveAndMatches(mouseKey)) {
			this.onClose();
			return true;
		}
		return super.keyReleased(code, p_keyPressed_2_, p_keyPressed_3_);
	}

}