package com.simibubi.create.content.logistics.block.display;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.logistics.block.display.source.DisplaySource;
import com.simibubi.create.content.logistics.block.display.source.SingleLineDisplaySource;
import com.simibubi.create.content.logistics.block.display.target.DisplayTarget;
import com.simibubi.create.content.logistics.block.display.target.DisplayTargetStats;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import com.simibubi.create.foundation.gui.widget.TooltipArea;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class DisplayLinkScreen extends AbstractSimiScreen {

	private static final ItemStack FALLBACK = new ItemStack(Items.BARRIER);

	private AllGuiTextures background;
	private DisplayLinkTileEntity te;
	private IconButton confirmButton;

	BlockState sourceState;
	BlockState targetState;
	ItemStack sourceIcon = FALLBACK;
	ItemStack targetIcon = FALLBACK;
	List<DisplaySource> sources;
	DisplayTarget target;

	ScrollInput sourceTypeSelector;
	Label sourceTypeLabel;
	ScrollInput targetLineSelector;
	Label targetLineLabel;

	Couple<Set<Pair<AbstractWidget, String>>> configWidgets;

	public DisplayLinkScreen(DisplayLinkTileEntity te) {
		this.background = AllGuiTextures.DATA_GATHERER;
		this.te = te;
		sources = Collections.emptyList();
		configWidgets = Couple.create(HashSet::new);
		target = null;
	}

	@Override
	protected void init() {
		setWindowSize(background.width, background.height);
		super.init();
		clearWidgets();

		int x = guiLeft;
		int y = guiTop;

		if (sourceState == null || targetState == null)
			initGathererOptions();

		confirmButton = new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_CONFIRM);
		confirmButton.withCallback(this::onClose);
		addRenderableWidget(confirmButton);
	}

	@Override
	public void tick() {
		super.tick();
		if (sourceState != null && sourceState.getBlock() != minecraft.level.getBlockState(te.getSourcePosition())
			.getBlock()
			|| targetState != null && targetState.getBlock() != minecraft.level.getBlockState(te.getTargetPosition())
				.getBlock())
			initGathererOptions();
	}

	private void initGathererOptions() {
		sourceState = minecraft.level.getBlockState(te.getSourcePosition());
		targetState = minecraft.level.getBlockState(te.getTargetPosition());

		Item asItem;
		int x = guiLeft;
		int y = guiTop;

		Block sourceBlock = sourceState.getBlock();
		Block targetBlock = targetState.getBlock();

		asItem = sourceBlock.asItem();
		sourceIcon = asItem == null || asItem == Items.AIR ? FALLBACK : new ItemStack(asItem);
		asItem = targetBlock.asItem();
		targetIcon = asItem == null || asItem == Items.AIR ? FALLBACK : new ItemStack(asItem);

		sources = AllDisplayBehaviours.sourcesOf(minecraft.level, te.getSourcePosition());
		target = AllDisplayBehaviours.targetOf(minecraft.level, te.getTargetPosition());

		removeWidget(targetLineSelector);
		removeWidget(targetLineLabel);
		removeWidget(sourceTypeSelector);
		removeWidget(sourceTypeLabel);

		configWidgets.forEach(s -> s.forEach(p -> removeWidget(p.getFirst())));

		targetLineSelector = null;
		sourceTypeSelector = null;

		if (target != null) {
			DisplayTargetStats stats = target.provideStats(new DisplayLinkContext(minecraft.level, te));
			int rows = stats.maxRows();
			int startIndex = Math.min(te.targetLine, rows);

			targetLineLabel = new Label(x + 65, y + 109, TextComponent.EMPTY).withShadow();
			targetLineLabel.text = target.getLineOptionText(startIndex);

			if (rows > 1) {
				targetLineSelector = new ScrollInput(x + 61, y + 105, 135, 16).withRange(0, rows)
					.titled(Lang.translate("display_link.display_on"))
					.inverted()
					.calling(i -> targetLineLabel.text = target.getLineOptionText(i))
					.setState(startIndex);
				addRenderableWidget(targetLineSelector);
			}

			addRenderableWidget(targetLineLabel);
		}

		if (!sources.isEmpty()) {
			int startIndex = Math.max(sources.indexOf(te.activeSource), 0);

			sourceTypeLabel = new Label(x + 65, y + 30, TextComponent.EMPTY).withShadow();
			sourceTypeLabel.text = sources.get(startIndex)
				.getName();

			if (sources.size() > 1) {
				List<Component> options = sources.stream()
					.map(DisplaySource::getName)
					.toList();
				sourceTypeSelector = new SelectionScrollInput(x + 61, y + 26, 135, 16).forOptions(options)
					.writingTo(sourceTypeLabel)
					.titled(Lang.translate("display_link.information_type"))
					.calling(this::initGathererSourceSubOptions)
					.setState(startIndex);
				sourceTypeSelector.onChanged();
				addRenderableWidget(sourceTypeSelector);
			} else
				initGathererSourceSubOptions(0);

			addRenderableWidget(sourceTypeLabel);
		}

	}

	private void initGathererSourceSubOptions(int i) {
		DisplaySource source = sources.get(i);
		source.populateData(new DisplayLinkContext(te.getLevel(), te));
		
		if (targetLineSelector != null)
			targetLineSelector
				.titled(source instanceof SingleLineDisplaySource ? Lang.translate("display_link.display_on")
					: Lang.translate("display_link.display_on_multiline"));

		configWidgets.forEach(s -> {
			s.forEach(p -> removeWidget(p.getFirst()));
			s.clear();
		});

		DisplayLinkContext context = new DisplayLinkContext(minecraft.level, te);
		configWidgets.forEachWithContext((s, first) -> source.initConfigurationWidgets(context,
			new LineBuilder(s, guiLeft + 60, guiTop + (first ? 51 : 72)), first));

		configWidgets.forEach(s -> s.forEach(p -> {
			loadValue(te.getSourceConfig(), p);
			if (p.getFirst() instanceof TooltipArea)
				addRenderableOnly(p.getFirst());
			else
				addRenderableWidget(p.getFirst());
		}));
	}

	public class LineBuilder {

		private Set<Pair<AbstractWidget, String>> targetSet;
		private int x;
		private int y;

		public LineBuilder(Set<Pair<AbstractWidget, String>> targetSet, int x, int y) {
			this.targetSet = targetSet;
			this.x = x;
			this.y = y;
		}

		public LineBuilder addScrollInput(int x, int width, BiConsumer<ScrollInput, Label> inputTransform,
			String dataKey) {
			ScrollInput input = new ScrollInput(x + this.x, y - 4, width, 18);
			addScrollInput(input, inputTransform, dataKey);
			return this;
		}

		public LineBuilder addSelectionScrollInput(int x, int width,
			BiConsumer<SelectionScrollInput, Label> inputTransform, String dataKey) {
			SelectionScrollInput input = new SelectionScrollInput(x + this.x, y - 4, width, 18);
			addScrollInput(input, inputTransform, dataKey);
			return this;
		}

		private <T extends ScrollInput> void addScrollInput(T input, BiConsumer<T, Label> inputTransform,
			String dataKey) {
			Label label = new Label(input.x + 5, y, TextComponent.EMPTY);
			label.withShadow();
			inputTransform.accept(input, label);
			input.writingTo(label);
			targetSet.add(Pair.of(label, "Dummy"));
			targetSet.add(Pair.of(input, dataKey));
		}

		public LineBuilder addTextInput(int x, int width, BiConsumer<EditBox, TooltipArea> inputTransform,
			String dataKey) {
			EditBox input = new EditBox(font, x + this.x + 5, y, width - 9, 8, TextComponent.EMPTY);
			input.setBordered(false);
			input.setTextColor(0xffffff);
			input.changeFocus(false);
			input.mouseClicked(0, 0, 0);
			TooltipArea tooltipArea = new TooltipArea(this.x + x, y - 4, width, 18);
			inputTransform.accept(input, tooltipArea);
			targetSet.add(Pair.of(input, dataKey));
			targetSet.add(Pair.of(tooltipArea, "Dummy"));
			return this;
		}

	}

	private void saveValue(CompoundTag data, Pair<AbstractWidget, String> widget) {
		AbstractWidget w = widget.getFirst();
		String key = widget.getSecond();
		if (w instanceof EditBox eb)
			data.putString(key, eb.getValue());
		if (w instanceof ScrollInput si)
			data.putInt(key, si.getState());
	}

	private void loadValue(CompoundTag data, Pair<AbstractWidget, String> widget) {
		AbstractWidget w = widget.getFirst();
		String key = widget.getSecond();
		if (!data.contains(key))
			return;
		if (w instanceof EditBox eb)
			eb.setValue(data.getString(key));
		if (w instanceof ScrollInput si)
			si.setState(data.getInt(key));
	}

	@Override
	public void onClose() {
		super.onClose();
		CompoundTag sourceData = new CompoundTag();

		if (!sources.isEmpty()) {
			sourceData.putString("Id",
				sources.get(sourceTypeSelector == null ? 0 : sourceTypeSelector.getState()).id.toString());
			configWidgets.forEach(s -> s.forEach(p -> saveValue(sourceData, p)));
		}

		AllPackets.channel.sendToServer(new DisplayLinkConfigurationPacket(te.getBlockPos(), sourceData,
			targetLineSelector == null ? 0 : targetLineSelector.getState()));
	}

	@Override
	protected void renderWindow(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		int x = guiLeft;
		int y = guiTop;

		background.render(ms, x, y, this);
		MutableComponent header = Lang.translate("display_link.title");
		font.draw(ms, header, x + background.width / 2 - font.width(header) / 2, y + 4, 0x442000);

		if (sources.isEmpty())
			font.drawShadow(ms, Lang.translate("display_link.no_source"), x + 65, y + 30, 0xD3D3D3);
		if (target == null)
			font.drawShadow(ms, Lang.translate("display_link.no_target"), x + 65, y + 109, 0xD3D3D3);

		if (!sourceIcon.isEmpty())
			minecraft.getItemRenderer()
				.renderGuiItem(sourceIcon, x + 37, y + 26);
		if (!targetIcon.isEmpty())
			minecraft.getItemRenderer()
				.renderGuiItem(targetIcon, x + 37, y + 105);

		configWidgets.forEachWithContext((s, first) -> s.forEach(p -> {
			if (p.getSecond()
				.equals("Dummy"))
				return;
			renderWidgetBG(ms, p.getFirst(), first);
		}));

		ms.pushPose();
		TransformStack.cast(ms)
			.pushPose()
			.translate(x + background.width + 4, y + background.height + 4, 100)
			.scale(40)
			.rotateX(-22)
			.rotateY(63);
		GuiGameElement.of(te.getBlockState()
			.setValue(DisplayLinkBlock.FACING, Direction.UP))
			.render(ms);
		ms.popPose();
	}

	protected void renderWidgetBG(PoseStack ms, AbstractWidget aw, boolean firstLine) {
		int x = aw.x;
		int width = aw.getWidth();
		int y = guiTop + (firstLine ? 46 : 67);

		if (aw instanceof EditBox) {
			x -= 5;
			width += 9;
		}

		UIRenderHelper.drawStretched(ms, x, y, width, 18, -100, AllGuiTextures.DATA_AREA);
		AllGuiTextures.DATA_AREA_START.render(ms, x, y);
		AllGuiTextures.DATA_AREA_END.render(ms, x + width - 2, y);
	}

	@Override
	protected void renderWindowForeground(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		super.renderWindowForeground(ms, mouseX, mouseY, partialTicks);
		int mX = mouseX - guiLeft;
		int mY = mouseY - guiTop;

		if (sourceState != null && mX >= 33 && mX < 53 && mY >= 24 && mY < 44) {
			renderComponentTooltip(ms,
				ImmutableList.of(Lang.translate("display_link.reading_from"), sourceState.getBlock()
					.getName()
					.withStyle(s -> s.withColor(sources.isEmpty() ? 0xF68989 : 0xF2C16D)),
					Lang.translate("display_link.attached_side"), Lang.translate("display_link.view_compatible")
						.withStyle(ChatFormatting.GRAY)),
				mouseX, mouseY);
		}

		if (targetState != null && mX >= 33 && mX < 53 && mY >= 102 && mY < 122) {
			renderComponentTooltip(ms,
				ImmutableList.of(Lang.translate("display_link.writing_to"), targetState.getBlock()
					.getName()
					.withStyle(s -> s.withColor(target == null ? 0xF68989 : 0xF2C16D)),
					Lang.translate("display_link.targeted_location"), Lang.translate("display_link.view_compatible")
						.withStyle(ChatFormatting.GRAY)),
				mouseX, mouseY);
		}

	}

	@Override
	protected void removeWidget(GuiEventListener p_169412_) {
		if (p_169412_ != null)
			super.removeWidget(p_169412_);
	}

}
