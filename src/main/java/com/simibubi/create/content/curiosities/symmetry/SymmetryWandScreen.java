package com.simibubi.create.content.curiosities.symmetry;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.curiosities.symmetry.mirror.CrossPlaneMirror;
import com.simibubi.create.content.curiosities.symmetry.mirror.EmptyMirror;
import com.simibubi.create.content.curiosities.symmetry.mirror.PlaneMirror;
import com.simibubi.create.content.curiosities.symmetry.mirror.SymmetryMirror;
import com.simibubi.create.content.curiosities.symmetry.mirror.TriplePlaneMirror;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.widgets.IconButton;
import com.simibubi.create.foundation.gui.widgets.Label;
import com.simibubi.create.foundation.gui.widgets.ScrollInput;
import com.simibubi.create.foundation.gui.widgets.SelectionScrollInput;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.networking.NbtPacket;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.PacketDistributor;

public class SymmetryWandScreen extends AbstractSimiScreen {

	private ScrollInput areaType;
	private Label labelType;
	private ScrollInput areaAlign;
	private Label labelAlign;
	private IconButton confirmButton;

	private final ITextComponent mirrorType = Lang.translate("gui.symmetryWand.mirrorType");
	private final ITextComponent orientation = Lang.translate("gui.symmetryWand.orientation");

	private SymmetryMirror currentElement;
	private ItemStack wand;
	private Hand hand;

	public SymmetryWandScreen(ItemStack wand, Hand hand) {
		super();

		currentElement = SymmetryWandItem.getMirror(wand);
		if (currentElement instanceof EmptyMirror) {
			currentElement = new PlaneMirror(Vector3d.ZERO);
		}
		this.hand = hand;
		this.wand = wand;
	}

	@Override
	public void init() {
		super.init();
		AllGuiTextures background = AllGuiTextures.WAND_OF_SYMMETRY;
		this.setWindowSize(background.width + 50, background.height + 50);

		labelType = new Label(guiLeft + 49, guiTop + 26, "").colored(0xFFFFFFFF)
			.withShadow();
		labelAlign = new Label(guiLeft + 49, guiTop + 48, "").colored(0xFFFFFFFF)
			.withShadow();

		int state =
			currentElement instanceof TriplePlaneMirror ? 2 : currentElement instanceof CrossPlaneMirror ? 1 : 0;
		areaType = new SelectionScrollInput(guiLeft + 45, guiTop + 21, 109, 18).forOptions(SymmetryMirror.getMirrors())
			.titled(mirrorType.copy())
			.writingTo(labelType)
			.setState(state);

		areaType.calling(position -> {
			switch (position) {
			case 0:
				currentElement = new PlaneMirror(currentElement.getPosition());
				break;
			case 1:
				currentElement = new CrossPlaneMirror(currentElement.getPosition());
				break;
			case 2:
				currentElement = new TriplePlaneMirror(currentElement.getPosition());
				break;
			default:
				break;
			}
			initAlign(currentElement);
		});

		widgets.clear();

		initAlign(currentElement);

		widgets.add(labelAlign);
		widgets.add(areaType);
		widgets.add(labelType);
		
		confirmButton = new IconButton(guiLeft + background.width - 33, guiTop + background.height - 24, AllIcons.I_CONFIRM);
		widgets.add(confirmButton);

	}

	private void initAlign(SymmetryMirror element) {
		if (areaAlign != null) 
			widgets.remove(areaAlign);

		areaAlign = new SelectionScrollInput(guiLeft + 45, guiTop + 43, 109, 18).forOptions(element.getAlignToolTips())
			.titled(orientation.copy())
			.writingTo(labelAlign)
			.setState(element.getOrientationIndex())
			.calling(element::setOrientation);

		widgets.add(areaAlign);
	}

	@Override
	protected void renderWindow(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		AllGuiTextures.WAND_OF_SYMMETRY.draw(matrixStack, this, guiLeft, guiTop);

		textRenderer.drawWithShadow(matrixStack, wand.getDisplayName(), guiLeft + 11, guiTop + 3, 0xffffff);

		renderBlock(matrixStack);
		GuiGameElement.of(wand)
			.at(guiLeft + 170, guiTop + 200)
			.scale(4)
			.rotate(-70, 20, 20)
			.render(matrixStack);
	}

	protected void renderBlock(MatrixStack ms) {
		ms.push();
		ms.translate(guiLeft + 18, guiTop + 11, 20);
		ms.translate(guiLeft + 25.5f, guiTop + 21, 20);
		ms.multiply(new Vector3f(.3f, 1f, 0f).getDegreesQuaternion(-22.5f));
		ms.scale(16, -16, 16);
		currentElement.applyModelTransform(ms);
		// RenderSystem.multMatrix(ms.peek().getModel());
		GuiGameElement.of(currentElement.getModel())
			.render(ms);

		ms.pop();
	}

	@Override
	public void removed() {
		ItemStack heldItem = client.player.getHeldItem(hand);
		CompoundNBT compound = heldItem.getTag();
		compound.put(SymmetryWandItem.SYMMETRY, currentElement.writeToNbt());
		heldItem.setTag(compound);
		AllPackets.channel.send(PacketDistributor.SERVER.noArg(), new NbtPacket(heldItem, hand));
		client.player.setHeldItem(hand, heldItem);
		super.removed();
	}
	
	@Override
	public boolean mouseClicked(double x, double y, int button) {
		if (confirmButton.isHovered()) {
			Minecraft.getInstance().player.closeScreen();
			return true;
		}

		return super.mouseClicked(x, y, button);
	}

}
