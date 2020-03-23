package com.simibubi.create.modules.curiosities.symmetry;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllPackets;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.widgets.Label;
import com.simibubi.create.foundation.gui.widgets.ScrollInput;
import com.simibubi.create.foundation.gui.widgets.SelectionScrollInput;
import com.simibubi.create.foundation.packet.NbtPacket;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.curiosities.symmetry.mirror.CrossPlaneMirror;
import com.simibubi.create.modules.curiosities.symmetry.mirror.EmptyMirror;
import com.simibubi.create.modules.curiosities.symmetry.mirror.PlaneMirror;
import com.simibubi.create.modules.curiosities.symmetry.mirror.SymmetryMirror;
import com.simibubi.create.modules.curiosities.symmetry.mirror.TriplePlaneMirror;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fml.network.PacketDistributor;

public class SymmetryWandScreen extends AbstractSimiScreen {

	private ScrollInput areaType;
	private Label labelType;
	private ScrollInput areaAlign;
	private Label labelAlign;

	private final String mirrorType = Lang.translate("gui.symmetryWand.mirrorType");
	private final String orientation = Lang.translate("gui.symmetryWand.orientation");

	private SymmetryMirror currentElement;
	private float animationProgress;
	private ItemStack wand;
	private Hand hand;

	public SymmetryWandScreen(ItemStack wand, Hand hand) {
		super();

		currentElement = SymmetryWandItem.getMirror(wand);
		if (currentElement instanceof EmptyMirror) {
			currentElement = new PlaneMirror(Vec3d.ZERO);
		}
		this.hand = hand;
		this.wand = wand;
		animationProgress = 0;
	}

	@Override
	public void init() {
		super.init();
		this.setWindowSize(ScreenResources.WAND_SYMMETRY.width + 50, ScreenResources.WAND_SYMMETRY.height + 50);

		labelType = new Label(guiLeft + 122, guiTop + 15, "").colored(0xFFFFFFFF).withShadow();
		labelAlign = new Label(guiLeft + 122, guiTop + 35, "").colored(0xFFFFFFFF).withShadow();

		int state = currentElement instanceof TriplePlaneMirror ? 2
				: currentElement instanceof CrossPlaneMirror ? 1 : 0;
		areaType = new SelectionScrollInput(guiLeft + 119, guiTop + 12, 70, 14)
				.forOptions(SymmetryMirror.getMirrors()).titled(mirrorType).writingTo(labelType).setState(state);

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
				.titled(orientation).writingTo(labelAlign).setState(element.getOrientationIndex())
				.calling(element::setOrientation);

		widgets.add(areaAlign);
	}

	@Override
	public void tick() {
		super.tick();
		animationProgress++;
	}

	@Override
	protected void renderWindow(int mouseX, int mouseY, float partialTicks) {
		ScreenResources.WAND_SYMMETRY.draw(this, guiLeft, guiTop);

		int x = guiLeft + 63;
		int y = guiTop + 15;

		font.drawString(mirrorType, x - 5, y, ScreenResources.FONT_COLOR);
		font.drawString(orientation, x - 5, y + 20, ScreenResources.FONT_COLOR);

		minecraft.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		RenderSystem.enableBlend();

		renderBlock();
		renderBlock();

		RenderSystem.pushLightingAttributes();
		RenderSystem.pushMatrix();

		RenderHelper.enableStandardItemLighting();
		RenderSystem.enableBlend();
		RenderSystem.enableRescaleNormal();
		RenderSystem.enableAlphaTest();
		RenderSystem.alphaFunc(516, 0.1F);
		RenderSystem.blendFunc(RenderSystem.SourceFactor.SRC_ALPHA, RenderSystem.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

		RenderSystem.translated((this.width - this.sWidth) / 2 + 250, this.height / 2 + this.sHeight / 2, 100);
		RenderSystem.rotatef(-30, .4f, 0, -.2f);
		RenderSystem.rotatef(90 + 0.2f * animationProgress, 0, 1, 0);
		RenderSystem.scaled(100, -100, 100);
		itemRenderer.renderItem(wand, itemRenderer.getModelWithOverrides(wand));

		RenderSystem.disableAlphaTest();
		RenderSystem.disableRescaleNormal();
		RenderSystem.disableLighting();

		RenderSystem.popMatrix();
		RenderSystem.popAttributes();
	}

	protected void renderBlock() {
		RenderSystem.pushMatrix();
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		RenderSystem.translated(guiLeft + 15, guiTop - 117, 20);
		RenderSystem.rotatef(-22.5f, .3f, 1f, 0f);
		RenderSystem.scaled(32, -32, 32);
		minecraft.getBlockRendererDispatcher().renderBlock(currentElement.getModel(), new BlockPos(0, -5, 0),
				minecraft.world, buffer, minecraft.world.rand, EmptyModelData.INSTANCE);

		Tessellator.getInstance().draw();
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
