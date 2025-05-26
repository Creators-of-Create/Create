package com.simibubi.create.content.trains.platform;

import java.lang.ref.WeakReference;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.CreateClient;
import com.simibubi.create.compat.computercraft.ComputerScreen;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TrainIconType;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public abstract class AbstractPlatformScreen extends AbstractSimiScreen {

	protected AllGuiTextures background;
	protected PlatformBlockEntity blockEntity;
	protected GlobalPlatform station;

	protected WeakReference<Train> displayedTrain;

	private IconButton confirmButton;

	public AbstractPlatformScreen(PlatformBlockEntity be, GlobalPlatform station) {
		super(be.getBlockState()
			.getBlock()
			.getName());
		this.blockEntity = be;
		this.station = station;
		displayedTrain = new WeakReference<>(null);
	}

	@Override
	protected void init() {
		if (blockEntity.computerBehaviour.hasAttachedComputer())
			minecraft.setScreen(new ComputerScreen(title, () ->
                Component.literal(station.name),
				this::renderAdditional, this, blockEntity.computerBehaviour::hasAttachedComputer));

		setWindowSize(background.getWidth(), background.getHeight());
		super.init();
		clearWidgets();

		int x = guiLeft;
		int y = guiTop;

		confirmButton = new IconButton(x + background.getWidth() - 33, y + background.getHeight() - 24, AllIcons.I_CONFIRM);
		confirmButton.withCallback(this::onClose);
		addRenderableWidget(confirmButton);
	}

	public int getTrainIconWidth(Train train) {
		TrainIconType icon = train.icon;
		List<Carriage> carriages = train.carriages;

		int w = icon.getIconWidth(TrainIconType.ENGINE);
		if (carriages.size() == 1)
			return w;

		for (int i = 1; i < carriages.size(); i++) {
			if (i == carriages.size() - 1 && train.doubleEnded) {
				w += icon.getIconWidth(TrainIconType.FLIPPED_ENGINE) + 1;
				break;
			}
			Carriage carriage = carriages.get(i);
			w += icon.getIconWidth(carriage.bogeySpacing) + 1;
		}

		return w;
	}

	@Override
	public void tick() {
		super.tick();

		if (blockEntity.computerBehaviour.hasAttachedComputer())
			minecraft.setScreen(new ComputerScreen(title, () ->
                Component.literal(station.name),
				this::renderAdditional, this, blockEntity.computerBehaviour::hasAttachedComputer));
	}

	@Override
	protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		int x = guiLeft;
		int y = guiTop;

		background.render(graphics, x, y);
		renderAdditional(graphics, mouseX, mouseY, partialTicks, x, y, background);
	}

	private void renderAdditional(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, int guiLeft, int guiTop, AllGuiTextures background) {
		PoseStack ms = graphics.pose();
		ms.pushPose();
		var msr = TransformStack.of(ms);
		msr.pushPose()
			.translate(guiLeft + background.getWidth() + 4, guiTop + background.getHeight() + 4, 100)
			.scale(40)
			.rotateXDegrees(-22)
			.rotateYDegrees(63);
		GuiGameElement.of(blockEntity.getBlockState()
			.setValue(BlockStateProperties.WATERLOGGED, false))
			.render(graphics);

		if (blockEntity.resolveFlagAngle()) {
			msr.translate(1 / 16f, -19 / 16f, -12 / 16f);
			PlatformRenderer.transformFlag(msr, blockEntity, partialTicks, 180, false);
			GuiGameElement.of(getFlag(partialTicks))
				.render(graphics);
		}

		ms.popPose();
	}

	protected abstract PartialModel getFlag(float partialTicks);

	protected Train getImminent() {
		return blockEntity.imminentTrain == null ? null : CreateClient.RAILWAYS.trains.get(blockEntity.imminentTrain);
	}

	protected boolean trainPresent() {
		return blockEntity.trainPresent;
	}

}
