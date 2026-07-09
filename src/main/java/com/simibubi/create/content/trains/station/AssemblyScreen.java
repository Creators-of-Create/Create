package com.simibubi.create.content.trains.station;

import java.lang.ref.WeakReference;
import java.util.List;

import com.simibubi.create.AllPackets;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TrainIconType;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import net.createmod.catnip.api.platform.CatnipServices;
import com.simibubi.create.foundation.utility.CreateLang;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

public class AssemblyScreen extends AbstractStationScreen {

	private IconButton quitAssembly;
	private IconButton toggleAssemblyButton;
	private List<Identifier> iconTypes;
	private ScrollInput iconTypeScroll;

	public AssemblyScreen(StationBlockEntity be, GlobalStation station) {
		super(be, station);
		background = AllGuiTextures.STATION_ASSEMBLING;
	}

	@Override
	protected void init() {
		super.init();
		int x = guiLeft;
		int y = guiTop;
		int by = y + background.getHeight() - 24;

		Renderable widget = renderables.get(0);
		if (widget instanceof IconButton ib) {
			ib.setIcon(AllIcons.I_PRIORITY_VERY_LOW);
			ib.setToolTip(CreateLang.translateDirect("station.close"));
		}

		iconTypes = TrainIconType.REGISTRY.keySet()
			.stream()
			.toList();
		iconTypeScroll = new ScrollInput(x + 4, y + 17, 162, 14).titled(CreateLang.translateDirect("station.icon_type"));
		iconTypeScroll.withRange(0, iconTypes.size());
		iconTypeScroll.withStepFunction(ctx -> -iconTypeScroll.standardStep()
			.apply(ctx));
		iconTypeScroll.calling(s -> {
			Train train = displayedTrain.get();
			if (train != null)
				train.icon = TrainIconType.byId(iconTypes.get(s));
		});
		iconTypeScroll.active = iconTypeScroll.visible = false;
		addRenderableWidget(iconTypeScroll);

		toggleAssemblyButton = new WideIconButton(x + 94, by, AllGuiTextures.I_ASSEMBLE_TRAIN);
		toggleAssemblyButton.active = false;
		toggleAssemblyButton.setToolTip(CreateLang.translateDirect("station.assemble_train"));
		toggleAssemblyButton.withCallback(() -> {
			net.createmod.catnip.api.client.network.ClientNetworkHelper.INSTANCE.sendToServer(StationEditPacket.tryAssemble(blockEntity.getBlockPos()));
		});

		quitAssembly = new IconButton(x + 73, by, AllIcons.I_DISABLE);
		quitAssembly.active = true;
		quitAssembly.setToolTip(CreateLang.translateDirect("station.cancel"));
		quitAssembly.withCallback(() -> {
			net.createmod.catnip.api.client.network.ClientNetworkHelper.INSTANCE.sendToServer(StationEditPacket.configure(blockEntity.getBlockPos(), false, station.name, null));
			minecraft.gui.setScreen(new StationScreen(blockEntity, station));
		});

		addRenderableWidget(toggleAssemblyButton);
		addRenderableWidget(quitAssembly);

		tickTrainDisplay();
	}

	@Override
	public void tick() {
		super.tick();
		tickTrainDisplay();
		Train train = displayedTrain.get();
		toggleAssemblyButton.active = blockEntity.bogeyCount > 0 || train != null;

		if (train != null) {
			net.createmod.catnip.api.client.network.ClientNetworkHelper.INSTANCE.sendToServer(StationEditPacket.configure(blockEntity.getBlockPos(), false, station.name, null));
			minecraft.gui.setScreen(new StationScreen(blockEntity, station));
			for (Carriage carriage : train.carriages)
				carriage.updateConductors();
		}
	}

	private void tickTrainDisplay() {
		if (getImminent() == null) {
			displayedTrain = new WeakReference<>(null);
			quitAssembly.active = true;
			iconTypeScroll.active = iconTypeScroll.visible = false;
			toggleAssemblyButton.setToolTip(CreateLang.translateDirect("station.assemble_train"));
			toggleAssemblyButton.setIcon(AllGuiTextures.I_ASSEMBLE_TRAIN);
			toggleAssemblyButton.withCallback(() -> {
				net.createmod.catnip.api.client.network.ClientNetworkHelper.INSTANCE.sendToServer(StationEditPacket.tryAssemble(blockEntity.getBlockPos()));
			});
		} else {
			net.createmod.catnip.api.client.network.ClientNetworkHelper.INSTANCE.sendToServer(StationEditPacket.configure(blockEntity.getBlockPos(), false, station.name, null));
			minecraft.gui.setScreen(new StationScreen(blockEntity, station));
		}
	}

	@Override
	protected void renderWindow(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		super.renderWindow(graphics, mouseX, mouseY, partialTicks);
		int x = guiLeft;
		int y = guiTop;

		MutableComponent header = CreateLang.translateDirect("station.assembly_title");
		graphics.text(font, header, x + background.getWidth() / 2 - font.width(header) / 2, y + 4, 0x0E2233, false);

		AssemblyException lastAssemblyException = blockEntity.lastException;
		if (lastAssemblyException != null) {
			MutableComponent text = CreateLang.translateDirect("station.failed");
			graphics.text(font, text, x + 97 - font.width(text) / 2, y + 47, 0x775B5B, false);
			int offset = 0;
			if (blockEntity.failedCarriageIndex != -1) {
				graphics.text(font, CreateLang.translateDirect("station.carriage_number", blockEntity.failedCarriageIndex), x + 30,
					y + 67, 0x7A7A7A, false);
				offset += 10;
			}
			graphics.textWithWordWrap(font, lastAssemblyException.component, x + 30, y + 67 + offset, 134, 0x775B5B);
			offset += font.split(lastAssemblyException.component, 134)
				.size() * 9 + 5;
			graphics.textWithWordWrap(font, CreateLang.translateDirect("station.retry"), x + 30, y + 67 + offset, 134, 0x7A7A7A);
			return;
		}

		int bogeyCount = blockEntity.bogeyCount;

		MutableComponent text = CreateLang.translateDirect(
			bogeyCount == 0 ? "station.no_bogeys" : bogeyCount == 1 ? "station.one_bogey" : "station.more_bogeys",
			bogeyCount);
		graphics.text(font, text, x + 97 - font.width(text) / 2, y + 47, 0x7A7A7A, false);

		graphics.textWithWordWrap(font, CreateLang.translateDirect("station.how_to"), x + 28, y + 62, 134, 0x7A7A7A);
		graphics.textWithWordWrap(font, CreateLang.translateDirect("station.how_to_1"), x + 28, y + 94, 134, 0x7A7A7A);
		graphics.textWithWordWrap(font, CreateLang.translateDirect("station.how_to_2"), x + 28, y + 117, 138, 0x7A7A7A);
	}

	@Override
	public void removed() {
		super.removed();
		Train train = displayedTrain.get();
		if (train != null) {
			Identifier iconId = iconTypes.get(iconTypeScroll.getState());
			train.icon = TrainIconType.byId(iconId);
			net.createmod.catnip.api.client.network.ClientNetworkHelper.INSTANCE.sendToServer(new TrainEditPacket.Serverbound(train.id, "", iconId, train.mapColorIndex));
		}
	}

	@Override
	protected PartialModel getFlag(float partialTicks) {
		return AllPartialModels.STATION_ASSEMBLE;
	}

}
