package com.simibubi.create.content.logistics.item.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.logistics.item.filter.AttributeFilterContainer.WhitelistMode;
import com.simibubi.create.content.logistics.item.filter.FilterScreenPacket.Option;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widgets.IconButton;
import com.simibubi.create.foundation.gui.widgets.Indicator;
import com.simibubi.create.foundation.gui.widgets.Label;
import com.simibubi.create.foundation.gui.widgets.SelectionScrollInput;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class AttributeFilterScreen extends AbstractFilterScreen<AttributeFilterContainer> {

	private static final String PREFIX = "gui.attribute_filter.";

	private IconButton whitelistDis, whitelistCon, blacklist;
	private Indicator whitelistDisIndicator, whitelistConIndicator, blacklistIndicator;
	private IconButton add;

	private ITextComponent whitelistDisN = Lang.translate(PREFIX + "whitelist_disjunctive");
	private ITextComponent whitelistDisDESC = Lang.translate(PREFIX + "whitelist_disjunctive.description");
	private ITextComponent whitelistConN = Lang.translate(PREFIX + "whitelist_conjunctive");
	private ITextComponent whitelistConDESC = Lang.translate(PREFIX + "whitelist_conjunctive.description");
	private ITextComponent blacklistN = Lang.translate(PREFIX + "blacklist");
	private ITextComponent blacklistDESC = Lang.translate(PREFIX + "blacklist.description");

	private ITextComponent referenceH = Lang.translate(PREFIX + "add_reference_item");
	private ITextComponent noSelectedT = Lang.translate(PREFIX + "no_selected_attributes");
	private ITextComponent selectedT = Lang.translate(PREFIX + "selected_attributes");

	private ItemStack lastItemScanned = ItemStack.EMPTY;
	private List<ItemAttribute> attributesOfItem = new ArrayList<>();
	private List<ITextComponent> selectedAttributes = new ArrayList<>();
	private SelectionScrollInput attributeSelector;
	private Label attributeSelectorLabel;

	public AttributeFilterScreen(AttributeFilterContainer container, PlayerInventory inv, ITextComponent title) {
		super(container, inv, title, AllGuiTextures.ATTRIBUTE_FILTER);
	}

	@Override
	protected void init() {
		super.init();
		int x = guiLeft;
		int y = guiTop;

		whitelistDis = new IconButton(x + 84, y + 58, AllIcons.I_WHITELIST_OR);
		whitelistDis.setToolTip(whitelistDisN);
		whitelistCon = new IconButton(x + 102, y + 58, AllIcons.I_WHITELIST_AND);
		whitelistCon.setToolTip(whitelistConN);
		blacklist = new IconButton(x + 120, y + 58, AllIcons.I_WHITELIST_NOT);
		blacklist.setToolTip(blacklistN);

		whitelistDisIndicator = new Indicator(x + 84, y + 53, StringTextComponent.EMPTY);
		whitelistConIndicator = new Indicator(x + 102, y + 53, StringTextComponent.EMPTY);
		blacklistIndicator = new Indicator(x + 120, y + 53, StringTextComponent.EMPTY);

		widgets.addAll(Arrays.asList(blacklist, whitelistCon, whitelistDis, blacklistIndicator, whitelistConIndicator,
				whitelistDisIndicator));

		add = new IconButton(x + 159, y + 22, AllIcons.I_ADD);
		widgets.add(add);
		handleIndicators();

		attributeSelectorLabel = new Label(x + 40, y + 27, "").colored(0xF3EBDE).withShadow();
		attributeSelector = new SelectionScrollInput(x + 37, y + 24, 118, 14);
		attributeSelector.forOptions(Collections.singletonList(StringTextComponent.EMPTY));
		attributeSelector.calling(s -> {
		});
		referenceItemChanged(container.filterInventory.getStackInSlot(0));

		widgets.add(attributeSelector);
		widgets.add(attributeSelectorLabel);

		selectedAttributes.clear();
		selectedAttributes
				.add((container.selectedAttributes.isEmpty() ? noSelectedT : selectedT).copy().formatted(TextFormatting.YELLOW));
		container.selectedAttributes.forEach(at -> selectedAttributes.add(new StringTextComponent("- ").append(at.format()).formatted(TextFormatting.GRAY)));

	}

	private void referenceItemChanged(ItemStack stack) {
		lastItemScanned = stack;

		if (stack.isEmpty()) {
			attributeSelector.active = false;
			attributeSelector.visible = false;
			attributeSelectorLabel.text = referenceH.copy().formatted(TextFormatting.ITALIC);
			add.active = false;
			attributeSelector.calling(s -> {
			});
			return;
		}

		add.active = true;
		attributeSelector.titled(stack.getDisplayName().copy().append("..."));
		attributesOfItem.clear();
		for (ItemAttribute itemAttribute : ItemAttribute.types)
			attributesOfItem.addAll(itemAttribute.listAttributesOf(stack, this.client.world));
		List<ITextComponent> options = attributesOfItem.stream().map(ItemAttribute::format).collect(Collectors.toList());
		attributeSelector.forOptions(options);
		attributeSelector.active = true;
		attributeSelector.visible = true;
		attributeSelector.setState(0);
		attributeSelector.calling(i -> {
			attributeSelectorLabel.setTextAndTrim(options.get(i), true, 112);
			ItemAttribute selected = attributesOfItem.get(i);
			for (ItemAttribute existing : container.selectedAttributes) {
				CompoundNBT testTag = new CompoundNBT();
				CompoundNBT testTag2 = new CompoundNBT();
				existing.serializeNBT(testTag);
				selected.serializeNBT(testTag2);
				if (testTag.equals(testTag2)) {
					add.active = false;
					return;
				}
			}
			add.active = true;
		});
		attributeSelector.onChanged();
	}

	@Override
	public void renderWindowForeground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		ItemStack stack = container.filterInventory.getStackInSlot(1);
		matrixStack.push();
		matrixStack.translate(0.0F, 0.0F, 32.0F);
		this.setZOffset(200);
		this.itemRenderer.zLevel = 200.0F;
		this.itemRenderer.renderItemOverlayIntoGUI(textRenderer, stack, guiLeft + 59, guiTop + 56,
				String.valueOf(selectedAttributes.size() - 1));
		this.setZOffset(0);
		this.itemRenderer.zLevel = 0.0F;
		matrixStack.pop();

		super.renderWindowForeground(matrixStack, mouseX, mouseY, partialTicks);
	}

	@Override
	public void tick() {
		super.tick();
		ItemStack stackInSlot = container.filterInventory.getStackInSlot(0);
		if (!stackInSlot.equals(lastItemScanned, false))
			referenceItemChanged(stackInSlot);
	}

	@Override
	protected void drawMouseoverTooltip(MatrixStack matrixStack, int mouseX, int mouseY) {
		if (this.client.player.inventory.getItemStack().isEmpty() && this.hoveredSlot != null
				&& this.hoveredSlot.getHasStack()) {
			if (this.hoveredSlot.slotNumber == 37) {
				renderTooltip(matrixStack, selectedAttributes, mouseX, mouseY);
				return;
			}
			this.renderTooltip(matrixStack, this.hoveredSlot.getStack(), mouseX, mouseY);
		}
		super.drawMouseoverTooltip(matrixStack, mouseX, mouseY);
	}

	@Override
	protected List<IconButton> getTooltipButtons() {
		return Arrays.asList(blacklist, whitelistCon, whitelistDis);
	}

	@Override
	protected List<IFormattableTextComponent> getTooltipDescriptions() {
		return Arrays.asList(blacklistDESC.copy(), whitelistConDESC.copy(), whitelistDisDESC.copy());
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		boolean mouseClicked = super.mouseClicked(x, y, button);

		if (button != 0)
			return mouseClicked;

		if (blacklist.isHovered()) {
			container.whitelistMode = WhitelistMode.BLACKLIST;
			sendOptionUpdate(Option.BLACKLIST);
			return true;
		}

		if (whitelistCon.isHovered()) {
			container.whitelistMode = WhitelistMode.WHITELIST_CONJ;
			sendOptionUpdate(Option.WHITELIST2);
			return true;
		}

		if (whitelistDis.isHovered()) {
			container.whitelistMode = WhitelistMode.WHITELIST_DISJ;
			sendOptionUpdate(Option.WHITELIST);
			return true;
		}

		if (add.isHovered() && add.active) {
			int index = attributeSelector.getState();
			if (index < attributesOfItem.size()) {
				add.active = false;
				CompoundNBT tag = new CompoundNBT();
				ItemAttribute itemAttribute = attributesOfItem.get(index);
				itemAttribute.serializeNBT(tag);
				AllPackets.channel.sendToServer(new FilterScreenPacket(Option.ADD_TAG, tag));
				container.selectedAttributes.add(itemAttribute);
				if (container.selectedAttributes.size() == 1)
					selectedAttributes.set(0, selectedT.copy().formatted(TextFormatting.YELLOW));
				selectedAttributes.add(new StringTextComponent("- ").append(itemAttribute.format()).formatted(TextFormatting.GRAY));
				return true;
			}
		}

		return mouseClicked;
	}

	@Override
	protected void contentsCleared() {
		selectedAttributes.clear();
		selectedAttributes.add(noSelectedT.copy().formatted(TextFormatting.YELLOW));
		if (!lastItemScanned.isEmpty())
			add.active = true;
	}

	@Override
	protected boolean isButtonEnabled(IconButton button) {
		if (button == blacklist)
			return container.whitelistMode != WhitelistMode.BLACKLIST;
		if (button == whitelistCon)
			return container.whitelistMode != WhitelistMode.WHITELIST_CONJ;
		if (button == whitelistDis)
			return container.whitelistMode != WhitelistMode.WHITELIST_DISJ;
		return true;
	}

	@Override
	protected boolean isIndicatorOn(Indicator indicator) {
		if (indicator == blacklistIndicator)
			return container.whitelistMode == WhitelistMode.BLACKLIST;
		if (indicator == whitelistConIndicator)
			return container.whitelistMode == WhitelistMode.WHITELIST_CONJ;
		if (indicator == whitelistDisIndicator)
			return container.whitelistMode == WhitelistMode.WHITELIST_DISJ;
		return false;
	}

}
