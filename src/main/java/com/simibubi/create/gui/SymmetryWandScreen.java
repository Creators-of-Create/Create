package com.simibubi.create.gui;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.gui.widgets.Label;
import com.simibubi.create.gui.widgets.SelectionScrollInput;
import com.simibubi.create.gui.widgets.ScrollInput;
import com.simibubi.create.item.SymmetryWandItem;
import com.simibubi.create.item.symmetry.SymmetryCrossPlane;
import com.simibubi.create.item.symmetry.SymmetryElement;
import com.simibubi.create.item.symmetry.SymmetryEmptySlot;
import com.simibubi.create.item.symmetry.SymmetryPlane;
import com.simibubi.create.item.symmetry.SymmetryTriplePlane;
import com.simibubi.create.networking.NbtPacket;
import com.simibubi.create.networking.AllPackets;

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

	private SymmetryElement currentElement;
	private float animationProgress;
	private ItemStack wand;

	public SymmetryWandScreen(ItemStack wand) {
		super();

		currentElement = SymmetryWandItem.getMirror(wand);
		if (currentElement instanceof SymmetryEmptySlot) {
			currentElement = new SymmetryPlane(Vec3d.ZERO);
		}
		this.wand = wand;
		animationProgress = 0;
	}

	@Override
	public void init() {
		super.init();
		this.setWindowSize(ScreenResources.WAND_SYMMETRY.width + 50, ScreenResources.WAND_SYMMETRY.height + 50);

		labelType = new Label(topLeftX + 122, topLeftY + 15, "").colored(0xFFFFFFFF).withShadow();
		labelAlign = new Label(topLeftX + 122, topLeftY + 35, "").colored(0xFFFFFFFF).withShadow();

		int state = currentElement instanceof SymmetryTriplePlane ? 2
				: currentElement instanceof SymmetryCrossPlane ? 1 : 0;
		areaType = new SelectionScrollInput(topLeftX + 119, topLeftY + 12, 70, 14)
				.forOptions(SymmetryElement.TOOLTIP_ELEMENTS).titled("Type of Mirror").writingTo(labelType)
				.setState(state);

		areaType.calling(position -> {
			switch (position) {
			case 0:
				currentElement = new SymmetryPlane(currentElement.getPosition());
				break;
			case 1:
				currentElement = new SymmetryCrossPlane(currentElement.getPosition());
				break;
			case 2:
				currentElement = new SymmetryTriplePlane(currentElement.getPosition());
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

	private void initAlign(SymmetryElement element) {
		if (areaAlign != null) {
			widgets.remove(areaAlign);
		}

		areaAlign = new SelectionScrollInput(topLeftX + 119, topLeftY + 32, 70, 14).forOptions(element.getAlignToolTips())
				.titled("Direction").writingTo(labelAlign).setState(element.getOrientationIndex())
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
		ScreenResources.WAND_SYMMETRY.draw(this, topLeftX, topLeftY);

		int x = topLeftX + 63;
		int y = topLeftY + 15;

		font.drawString("Symmetry", x, y, ScreenResources.FONT_COLOR);
		font.drawString("Direction", x, y + 20, ScreenResources.FONT_COLOR);

		minecraft.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.enableBlend();

		renderBlock();
		renderBlock();

		GlStateManager.pushLightingAttributes();
		GlStateManager.pushMatrix();
		
		RenderHelper.enableStandardItemLighting();
		GlStateManager.enableBlend();
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableAlphaTest();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);		
		
		GlStateManager.translated((this.width - this.sWidth) / 2 + 250, 250, 100);
		GlStateManager.rotatef(-30, .4f, 0, -.2f);
		GlStateManager.rotatef(90 + 0.2f * animationProgress, 0, 1, 0);
		GlStateManager.scaled(100, -100, 100);
		itemRenderer.renderItem(wand, itemRenderer.getModelWithOverrides(wand));
		
		GlStateManager.disableAlphaTest();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableLighting();
		
		GlStateManager.popMatrix();
		GlStateManager.popAttributes();
	}

	protected void renderBlock() {
		GlStateManager.pushMatrix();
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		GlStateManager.translated(topLeftX + 15, topLeftY - 117, 20);
		GlStateManager.rotatef(-22.5f, .3f, 1f, 0f);
		GlStateManager.scaled(32, -32, 32);
		minecraft.getBlockRendererDispatcher().renderBlock(currentElement.getModel(), new BlockPos(0, -5, 0),
				minecraft.world, buffer, minecraft.world.rand, EmptyModelData.INSTANCE);

		Tessellator.getInstance().draw();
		GlStateManager.popMatrix();
	}

	@Override
	public void removed() {
		ItemStack heldItemMainhand = minecraft.player.getHeldItemMainhand();
		CompoundNBT compound = heldItemMainhand.getTag();
		compound.put(SymmetryWandItem.$SYMMETRY, currentElement.writeToNbt());
		heldItemMainhand.setTag(compound);
		AllPackets.channel.send(PacketDistributor.SERVER.noArg(), new NbtPacket(heldItemMainhand));
		minecraft.player.setHeldItem(Hand.MAIN_HAND, heldItemMainhand);
		super.removed();
	}

}
