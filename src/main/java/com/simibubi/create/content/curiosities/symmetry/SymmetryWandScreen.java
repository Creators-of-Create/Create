package com.simibubi.create.content.curiosities.symmetry;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.content.curiosities.symmetry.mirror.CrossPlaneMirror;
import com.simibubi.create.content.curiosities.symmetry.mirror.EmptyMirror;
import com.simibubi.create.content.curiosities.symmetry.mirror.PlaneMirror;
import com.simibubi.create.content.curiosities.symmetry.mirror.SymmetryMirror;
import com.simibubi.create.content.curiosities.symmetry.mirror.TriplePlaneMirror;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.widgets.Label;
import com.simibubi.create.foundation.gui.widgets.ScrollInput;
import com.simibubi.create.foundation.gui.widgets.SelectionScrollInput;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.networking.NbtPacket;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.PacketDistributor;

public class SymmetryWandScreen extends AbstractSimiScreen {

	private ScrollInput areaType;
	private Label labelType;
	private ScrollInput areaAlign;
	private Label labelAlign;

	private final String mirrorType = Lang.translate("gui.symmetryWand.mirrorType");
	private final String orientation = Lang.translate("gui.symmetryWand.orientation");

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
		this.setWindowSize(AllGuiTextures.WAND_SYMMETRY.width + 50, AllGuiTextures.WAND_SYMMETRY.height + 50);

		labelType = new Label(guiLeft + 122, guiTop + 15, "").colored(0xFFFFFFFF)
			.withShadow();
		labelAlign = new Label(guiLeft + 122, guiTop + 35, "").colored(0xFFFFFFFF)
			.withShadow();

		int state =
			currentElement instanceof TriplePlaneMirror ? 2 : currentElement instanceof CrossPlaneMirror ? 1 : 0;
		areaType = new SelectionScrollInput(guiLeft + 119, guiTop + 12, 70, 14).forOptions(SymmetryMirror.getMirrors())
			.titled(mirrorType)
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

	}

	private void initAlign(SymmetryMirror element) {
		if (areaAlign != null) {
			widgets.remove(areaAlign);
		}

		areaAlign = new SelectionScrollInput(guiLeft + 119, guiTop + 32, 70, 14).forOptions(element.getAlignToolTips())
			.titled(orientation)
			.writingTo(labelAlign)
			.setState(element.getOrientationIndex())
			.calling(element::setOrientation);

		widgets.add(areaAlign);
	}

	@Override
	protected void renderWindow(int mouseX, int mouseY, float partialTicks) {
		AllGuiTextures.WAND_SYMMETRY.draw(this, guiLeft, guiTop);

		int x = guiLeft + 63;
		int y = guiTop + 15;

		font.drawString(mirrorType, x - 5, y, AllGuiTextures.FONT_COLOR);
		font.drawString(orientation, x - 5, y + 20, AllGuiTextures.FONT_COLOR);

		renderBlock();

		RenderSystem.pushMatrix();
		RenderSystem.translated(0, 0, 200);
		RenderSystem.rotatef(-20, -3.5f, 1, 1);
		GuiGameElement.of(wand)
			.at(guiLeft + 220, guiTop + 220)
			.scale(4)
			.render();
		RenderSystem.popMatrix();
	}

	protected void renderBlock() {
		RenderSystem.pushMatrix();

		MatrixStack ms = new MatrixStack();
		ms.translate(guiLeft + 18, guiTop + 11, 20);
		ms.multiply(new Vector3f(.3f, 1f, 0f).getDegreesQuaternion(-22.5f));
		ms.scale(32, -32, 32);
		currentElement.applyModelTransform(ms);
		RenderSystem.multMatrix(ms.peek()
			.getModel());
		GuiGameElement.of(currentElement.getModel())
			.render();

		RenderSystem.popMatrix();
	}

	@Override
	public void removed() {
		ItemStack heldItem = minecraft.player.getHeldItem(hand);
		CompoundNBT compound = heldItem.getTag();
		compound.put(SymmetryWandItem.SYMMETRY, currentElement.writeToNbt());
		heldItem.setTag(compound);
		AllPackets.channel.send(PacketDistributor.SERVER.noArg(), new NbtPacket(heldItem, hand));
		minecraft.player.setHeldItem(hand, heldItem);
		super.removed();
	}

}
