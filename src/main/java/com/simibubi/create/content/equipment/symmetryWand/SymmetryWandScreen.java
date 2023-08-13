package com.simibubi.create.content.equipment.symmetryWand;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.simibubi.create.AllPackets;
import com.simibubi.create.content.equipment.symmetryWand.mirror.CrossPlaneMirror;
import com.simibubi.create.content.equipment.symmetryWand.mirror.EmptyMirror;
import com.simibubi.create.content.equipment.symmetryWand.mirror.PlaneMirror;
import com.simibubi.create.content.equipment.symmetryWand.mirror.SymmetryMirror;
import com.simibubi.create.content.equipment.symmetryWand.mirror.TriplePlaneMirror;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.utility.lang.Components;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class SymmetryWandScreen extends AbstractSimiScreen {

	private AllGuiTextures background;

	private ScrollInput areaType;
	private Label labelType;
	private ScrollInput areaAlign;
	private Label labelAlign;
	private IconButton confirmButton;

	private final Component mirrorType = CreateLang.translateDirect("gui.symmetryWand.mirrorType");
	private final Component orientation = CreateLang.translateDirect("gui.symmetryWand.orientation");

	private SymmetryMirror currentElement;
	private ItemStack wand;
	private InteractionHand hand;

	public SymmetryWandScreen(ItemStack wand, InteractionHand hand) {
		background = AllGuiTextures.WAND_OF_SYMMETRY;

		currentElement = SymmetryWandItem.getMirror(wand);
		if (currentElement instanceof EmptyMirror) {
			currentElement = new PlaneMirror(Vec3.ZERO);
		}
		this.hand = hand;
		this.wand = wand;
	}

	@Override
	public void init() {
		setWindowSize(background.getWidth(), background.getHeight());
		setWindowOffset(-20, 0);
		super.init();

		int x = guiLeft;
		int y = guiTop;

		labelType = new Label(x + 49, y + 28, Components.immutableEmpty()).colored(0xFFFFFFFF)
			.withShadow();
		labelAlign = new Label(x + 49, y + 50, Components.immutableEmpty()).colored(0xFFFFFFFF)
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

		addRenderableWidget(labelAlign);
		addRenderableWidget(areaType);
		addRenderableWidget(labelType);

		confirmButton = new IconButton(x + background.getWidth() - 33, y + background.getHeight() - 24, AllIcons.I_CONFIRM);
		confirmButton.withCallback(() -> {
			onClose();
		});
		addRenderableWidget(confirmButton);
	}

	private void initAlign(SymmetryMirror element, int x, int y) {
		if (areaAlign != null)
			removeWidget(areaAlign);

		areaAlign = new SelectionScrollInput(x + 45, y + 43, 109, 18).forOptions(element.getAlignToolTips())
			.titled(orientation.plainCopy())
			.writingTo(labelAlign)
			.setState(element.getOrientationIndex())
			.calling(element::setOrientation);

		addRenderableWidget(areaAlign);
	}

	@Override
	protected void renderWindow(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		int x = guiLeft;
		int y = guiTop;

		background.render(ms, x, y, this);
		font.draw(ms, wand.getHoverName(), x + 11, y + 4, 0x592424);

		renderBlock(ms, x, y);
		GuiGameElement.of(wand)
				.scale(4)
				.rotate(-70, 20, 20)
				.at(x + 178, y + 448, -150)
				.render(ms);
	}

	protected void renderBlock(PoseStack ms, int x, int y) {
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
		AllPackets.getChannel().sendToServer(new ConfigureSymmetryWandPacket(hand, currentElement));
	}

}
