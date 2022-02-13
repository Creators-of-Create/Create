package com.simibubi.create.content.curiosities.toolbox;

import static com.simibubi.create.content.curiosities.toolbox.ToolboxInventory.STACKS_PER_COMPARTMENT;

import java.util.List;

import javax.annotation.Nullable;

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
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.DyeColor;
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
	private boolean scrollMode;
	private int scrollSlot = 0;
	private List<ToolboxTileEntity> toolboxes;
	private ToolboxTileEntity selectedBox;

	private static final int DEPOSIT = -7;
	private static final int UNEQUIP = -5;

	public RadialToolboxMenu(List<ToolboxTileEntity> toolboxes, State state, @Nullable ToolboxTileEntity selectedBox) {
		this.toolboxes = toolboxes;
		this.state = state;
		hoveredSlot = -1;

		if (selectedBox != null)
			this.selectedBox = selectedBox;
	}

	public void prevSlot(int slot) {
		scrollSlot = slot;
	}

	@Override
	protected void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		float fade = MathHelper.clamp((ticksOpen + AnimationTickHolder.getPartialTicks()) / 10f, 1 / 512f, 1);

		hoveredSlot = -1;
		MainWindow window = getMinecraft().getWindow();
		float hoveredX = mouseX - window.getGuiScaledWidth() / 2;
		float hoveredY = mouseY - window.getGuiScaledHeight() / 2;

		float distance = hoveredX * hoveredX + hoveredY * hoveredY;
		if (distance > 25 && distance < 10000)
			hoveredSlot =
				(MathHelper.floor((AngleHelper.deg(MathHelper.atan2(hoveredY, hoveredX)) + 360 + 180 - 22.5f)) % 360)
					/ 45;
		boolean renderCenterSlot = state == State.SELECT_ITEM_UNEQUIP;
		if (scrollMode && distance > 150)
			scrollMode = false;
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
			GuiGameElement.of(AllBlocks.TOOLBOXES.get(DyeColor.BROWN)
				.asStack())
				.at(-9, -9)
				.render(ms);

			ms.translate(0, -40 + (10 * (1 - fade) * (1 - fade)), 0);
			AllGuiTextures.TOOLBELT_SLOT.draw(ms, this, -12, -12);
			ms.translate(-0.5, 0.5, 0);
			AllIcons.I_DISABLE.draw(ms, this, -9, -9);
			ms.translate(0.5, -0.5, 0);
			if (!scrollMode && hoveredSlot == UNEQUIP) {
				AllGuiTextures.TOOLBELT_SLOT_HIGHLIGHT.draw(ms, this, -13, -13);
				tip = Lang.translate("toolbox.detach")
					.withStyle(TextFormatting.GOLD);
			}
			ms.popPose();

		} else {

			if (hoveredX > 60 && hoveredX < 100 && hoveredY > -20 && hoveredY < 20)
				hoveredSlot = DEPOSIT;

			ms.pushPose();
			ms.translate(80 + (-5 * (1 - fade) * (1 - fade)), 0, 0);
			AllGuiTextures.TOOLBELT_SLOT.draw(ms, this, -12, -12);
			ms.translate(-0.5, 0.5, 0);
			AllIcons.I_TOOLBOX.draw(ms, this, -9, -9);
			ms.translate(0.5, -0.5, 0);
			if (!scrollMode && hoveredSlot == DEPOSIT) {
				AllGuiTextures.TOOLBELT_SLOT_HIGHLIGHT.draw(ms, this, -13, -13);
				tip = Lang.translate(state == State.SELECT_BOX ? "toolbox.depositAll" : "toolbox.depositBox")
					.withStyle(TextFormatting.GOLD);
			}
			ms.popPose();

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
						boolean empty = inv.getStackInSlot(slot * STACKS_PER_COMPARTMENT)
							.isEmpty();

						(empty ? AllGuiTextures.TOOLBELT_INACTIVE_SLOT : AllGuiTextures.TOOLBELT_SLOT).draw(ms, this, 0,
							0);
						GuiGameElement.of(stackInSlot)
							.at(3, 3)
							.render(ms);

						if (slot == (scrollMode ? scrollSlot : hoveredSlot) && !empty) {
							AllGuiTextures.TOOLBELT_SLOT_HIGHLIGHT.draw(ms, this, -1, -1);
							tip = stackInSlot.getHoverName();
						}
					} else
						AllGuiTextures.TOOLBELT_EMPTY_SLOT.draw(ms, this, 0, 0);

				} else if (state == State.SELECT_BOX) {

					if (slot < toolboxes.size()) {
						AllGuiTextures.TOOLBELT_SLOT.draw(ms, this, 0, 0);
						ToolboxTileEntity toolboxTileEntity = toolboxes.get(slot);
						GuiGameElement.of(AllBlocks.TOOLBOXES.get(toolboxTileEntity.getColor())
							.asStack())
							.at(3, 3)
							.render(ms);

						if (slot == (scrollMode ? scrollSlot : hoveredSlot)) {
							AllGuiTextures.TOOLBELT_SLOT_HIGHLIGHT.draw(ms, this, -1, -1);
							tip = toolboxTileEntity.getDisplayName();
						}
					} else
						AllGuiTextures.TOOLBELT_EMPTY_SLOT.draw(ms, this, 0, 0);

				}

				ms.popPose();
			}

			if (renderCenterSlot) {
				ms.pushPose();
				AllGuiTextures.TOOLBELT_SLOT.draw(ms, this, -12, -12);
				(scrollMode ? AllIcons.I_REFRESH : AllIcons.I_FLIP).draw(ms, this, -9, -9);
				if (!scrollMode && UNEQUIP == hoveredSlot) {
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

		int selected = (scrollMode ? scrollSlot : hoveredSlot);

		if (selected == DEPOSIT) {
			if (state == State.DETACH)
				return;
			else if (state == State.SELECT_BOX)
				toolboxes.forEach(te -> AllPackets.channel.sendToServer(new ToolboxDisposeAllPacket(te.getBlockPos())));
			else
				AllPackets.channel.sendToServer(new ToolboxDisposeAllPacket(selectedBox.getBlockPos()));
			return;
		}

		if (state == State.SELECT_BOX)
			return;

		if (state == State.DETACH) {
			if (selected == UNEQUIP)
				AllPackets.channel.sendToServer(
					new ToolboxEquipPacket(null, selected, Minecraft.getInstance().player.inventory.selected));
			return;
		}

		if (selected == UNEQUIP)
			AllPackets.channel.sendToServer(new ToolboxEquipPacket(selectedBox.getBlockPos(), selected,
				Minecraft.getInstance().player.inventory.selected));

		if (selected < 0)
			return;
		ToolboxInventory inv = selectedBox.inventory;
		ItemStack stackInSlot = inv.filters.get(selected);
		if (stackInSlot.isEmpty())
			return;
		if (inv.getStackInSlot(selected * STACKS_PER_COMPARTMENT)
			.isEmpty())
			return;

		AllPackets.channel.sendToServer(new ToolboxEquipPacket(selectedBox.getBlockPos(), selected,
			Minecraft.getInstance().player.inventory.selected));
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		MainWindow window = getMinecraft().getWindow();
		double hoveredX = mouseX - window.getGuiScaledWidth() / 2;
		double hoveredY = mouseY - window.getGuiScaledHeight() / 2;
		double distance = hoveredX * hoveredX + hoveredY * hoveredY;
		if (distance <= 150) {
			scrollMode = true;
			scrollSlot = (((int) (scrollSlot - delta)) + 8) % 8;
			for (int i = 0; i < 10; i++) {

				if (state == State.SELECT_ITEM || state == State.SELECT_ITEM_UNEQUIP) {
					ToolboxInventory inv = selectedBox.inventory;
					ItemStack stackInSlot = inv.filters.get(scrollSlot);
					if (!stackInSlot.isEmpty() && !inv.getStackInSlot(scrollSlot * STACKS_PER_COMPARTMENT)
						.isEmpty())
						break;
				}

				if (state == State.SELECT_BOX)
					if (scrollSlot < toolboxes.size())
						break;

				if (state == State.DETACH)
					break;

				scrollSlot -= MathHelper.sign(delta);
				scrollSlot = (scrollSlot + 8) % 8;
			}
			return true;
		}

		return super.mouseScrolled(mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		int selected = (scrollMode ? scrollSlot : hoveredSlot);

		if (button == 0) {
			if (selected == DEPOSIT) {
				onClose();
				ToolboxHandlerClient.COOLDOWN = 2;
				return true;
			}

			if (state == State.SELECT_BOX && selected >= 0 && selected < toolboxes.size()) {
				state = State.SELECT_ITEM;
				selectedBox = toolboxes.get(selected);
				return true;
			}

			if (state == State.DETACH || state == State.SELECT_ITEM || state == State.SELECT_ITEM_UNEQUIP) {
				if (selected == UNEQUIP || selected >= 0) {
					onClose();
					ToolboxHandlerClient.COOLDOWN = 2;
					return true;
				}
			}
		}

		if (button == 1) {
			if (state == State.SELECT_ITEM && toolboxes.size() > 1) {
				state = State.SELECT_BOX;
				return true;
			}

			if (state == State.SELECT_ITEM_UNEQUIP && selected == UNEQUIP) {
				if (toolboxes.size() > 1) {
					AllPackets.channel.sendToServer(new ToolboxEquipPacket(selectedBox.getBlockPos(), selected,
						Minecraft.getInstance().player.inventory.selected));
					state = State.SELECT_BOX;
					return true;
				}

				onClose();
				ToolboxHandlerClient.COOLDOWN = 2;
				return true;
			}
		}

		return super.mouseClicked(x, y, button);
	}

	@Override
	public boolean keyPressed(int code, int p_keyPressed_2_, int p_keyPressed_3_) {
		KeyBinding[] hotbarBinds = minecraft.options.keyHotbarSlots;
		for (int i = 0; i < hotbarBinds.length && i < 8; i++) {
			if (hotbarBinds[i].matches(code, p_keyPressed_2_)) {

				if (state == State.SELECT_ITEM || state == State.SELECT_ITEM_UNEQUIP) {
					ToolboxInventory inv = selectedBox.inventory;
					ItemStack stackInSlot = inv.filters.get(i);
					if (stackInSlot.isEmpty() || inv.getStackInSlot(i * STACKS_PER_COMPARTMENT)
						.isEmpty())
						return false;
				}

				if (state == State.SELECT_BOX)
					if (i >= toolboxes.size())
						return false;

				scrollMode = true;
				scrollSlot = i;
				mouseClicked(0, 0, 0);
				return true;
			}
		}

		return super.keyPressed(code, p_keyPressed_2_, p_keyPressed_3_);
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