package com.simibubi.create.content.trains.station;

import java.lang.ref.WeakReference;
import java.util.List;

import com.jozufozu.flywheel.lib.model.baked.PartialModel;
import com.jozufozu.flywheel.lib.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.CreateClient;
import com.simibubi.create.compat.computercraft.ComputerScreen;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TrainIconType;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.utility.Components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public abstract class AbstractStationScreen extends AbstractSimiScreen {

	protected AllGuiTextures background;
	protected StationBlockEntity blockEntity;
	protected GlobalStation station;

	protected WeakReference<Train> displayedTrain;

	private IconButton confirmButton;

	public AbstractStationScreen(StationBlockEntity be, GlobalStation station) {
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
			minecraft.setScreen(new ComputerScreen(title, () -> Components.literal(station.name),
					this::renderAdditional, this, blockEntity.computerBehaviour::hasAttachedComputer));

		setWindowSize(background.width, background.height);
		super.init();
		clearWidgets();

		int x = guiLeft;
		int y = guiTop;

		confirmButton = new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_CONFIRM);
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
			minecraft.setScreen(new ComputerScreen(title, () -> Components.literal(station.name),
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
			.translate(guiLeft + background.width + 4, guiTop + background.height + 4, 100)
			.scale(40)
			.rotateX(-22)
			.rotateY(63);
		GuiGameElement.of(blockEntity.getBlockState()
			.setValue(BlockStateProperties.WATERLOGGED, false))
			.render(graphics);

		if (blockEntity.resolveFlagAngle()) {
			msr.translate(1 / 16f, -19 / 16f, -12 / 16f);
			StationRenderer.transformFlag(msr, blockEntity, partialTicks, 180, false);
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
