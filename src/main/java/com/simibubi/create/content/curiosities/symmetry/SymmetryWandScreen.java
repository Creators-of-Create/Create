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
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class SymmetryWandScreen extends AbstractSimiScreen {

	private AllGuiTextures background;

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
		background = AllGuiTextures.WAND_OF_SYMMETRY;

		currentElement = SymmetryWandItem.getMirror(wand);
		if (currentElement instanceof EmptyMirror) {
			currentElement = new PlaneMirror(Vector3d.ZERO);
		}
		this.hand = hand;
		this.wand = wand;
	}

	@Override
	public void init() {
		setWindowSize(background.width, background.height);
		setWindowOffset(-20, 0);
		super.init();
		widgets.clear();

		int x = guiLeft;
		int y = guiTop;

		labelType = new Label(x + 49, y + 28, StringTextComponent.EMPTY).colored(0xFFFFFFFF)
			.withShadow();
		labelAlign = new Label(x + 49, y + 50, StringTextComponent.EMPTY).colored(0xFFFFFFFF)
			.withShadow();

		int state =
			currentElement instanceof TriplePlaneMirror ? 2 : currentElement instanceof CrossPlaneMirror ? 1 : 0;
		areaType = new SelectionScrollInput(x + 45, y + 21, 109, 18).forOptions(SymmetryMirror.getMirrors())
			.titled(mirrorType.plainCopy())
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
			initAlign(currentElement, x, y);
		});

		initAlign(currentElement, x, y);

		widgets.add(labelAlign);
		widgets.add(areaType);
		widgets.add(labelType);

		confirmButton = new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_CONFIRM);
		widgets.add(confirmButton);
	}

	private void initAlign(SymmetryMirror element, int x, int y) {
		if (areaAlign != null)
			widgets.remove(areaAlign);

		areaAlign = new SelectionScrollInput(x + 45, y + 43, 109, 18).forOptions(element.getAlignToolTips())
			.titled(orientation.plainCopy())
			.writingTo(labelAlign)
			.setState(element.getOrientationIndex())
			.calling(element::setOrientation);

		widgets.add(areaAlign);
	}

	@Override
	protected void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		int x = guiLeft;
		int y = guiTop;

		background.draw(ms, this, x, y);
		font.draw(ms, wand.getHoverName(), x + 11, y + 4, 0x6B3802);

		renderBlock(ms, x, y);
		GuiGameElement.of(wand)
				.scale(4)
				.rotate(-70, 20, 20)
				.at(x + 178, y + 448, -150)
				.render(ms);
	}

	protected void renderBlock(MatrixStack ms, int x, int y) {
		ms.pushPose();
		ms.translate(x + 26, y + 39, 20);
		ms.scale(16, 16, 16);
		ms.mulPose(new Vector3f(.3f, 1f, 0f).rotationDegrees(-22.5f));
		currentElement.applyModelTransform(ms);
		// RenderSystem.multMatrix(ms.peek().getModel());
		GuiGameElement.of(currentElement.getModel())
			.render(ms);

		ms.popPose();
	}

	@Override
	public void removed() {
		SymmetryWandItem.configureSettings(wand, currentElement);
		AllPackets.channel.sendToServer(new ConfigureSymmetryWandPacket(hand, currentElement));
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		if (confirmButton.isHovered()) {
			onClose();
			return true;
		}

		return super.mouseClicked(x, y, button);
	}

}
