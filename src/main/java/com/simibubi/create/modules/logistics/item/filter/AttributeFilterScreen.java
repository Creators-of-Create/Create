package com.simibubi.create.modules.logistics.item.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllPackets;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.foundation.gui.widgets.IconButton;
import com.simibubi.create.foundation.gui.widgets.Indicator;
import com.simibubi.create.foundation.gui.widgets.Label;
import com.simibubi.create.foundation.gui.widgets.SelectionScrollInput;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.logistics.item.filter.AttributeFilterContainer.WhitelistMode;
import com.simibubi.create.modules.logistics.item.filter.FilterScreenPacket.Option;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

public class AttributeFilterScreen extends AbstractFilterScreen<AttributeFilterContainer> {

	private static final String PREFIX = "gui.attribute_filter.";

	private IconButton whitelistDis, whitelistCon, blacklist;
	private Indicator whitelistDisIndicator, whitelistConIndicator, blacklistIndicator;
	private IconButton add;

	private String whitelistDisN = Lang.translate(PREFIX + "whitelist_disjunctive");
	private String whitelistDisDESC = Lang.translate(PREFIX + "whitelist_disjunctive.description");
	private String whitelistConN = Lang.translate(PREFIX + "whitelist_conjunctive");
	private String whitelistConDESC = Lang.translate(PREFIX + "whitelist_conjunctive.description");
	private String blacklistN = Lang.translate(PREFIX + "blacklist");
	private String blacklistDESC = Lang.translate(PREFIX + "blacklist.description");

	private String referenceH = Lang.translate(PREFIX + "add_reference_item");
	private String noSelectedT = Lang.translate(PREFIX + "no_selected_attributes");
	private String selectedT = Lang.translate(PREFIX + "selected_attributes");

	private ItemStack lastItemScanned = ItemStack.EMPTY;
	private List<ItemAttribute> attributesOfItem = new ArrayList<>();
	private List<String> selectedAttributes = new ArrayList<>();
	private SelectionScrollInput attributeSelector;
	private Label attributeSelectorLabel;

	public AttributeFilterScreen(AttributeFilterContainer container, PlayerInventory inv, ITextComponent title) {
		super(container, inv, title, ScreenResources.ATTRIBUTE_FILTER);
	}

	@Override
	protected void init() {
		super.init();
		int x = guiLeft;
		int y = guiTop;

		whitelistDis = new IconButton(x + 84, y + 58, ScreenResources.I_WHITELIST_OR);
		whitelistDis.setToolTip(whitelistDisN);
		whitelistCon = new IconButton(x + 102, y + 58, ScreenResources.I_WHITELIST_AND);
		whitelistCon.setToolTip(whitelistConN);
		blacklist = new IconButton(x + 120, y + 58, ScreenResources.I_WHITELIST_NOT);
		blacklist.setToolTip(blacklistN);

		whitelistDisIndicator = new Indicator(x + 84, y + 53, "");
		whitelistConIndicator = new Indicator(x + 102, y + 53, "");
		blacklistIndicator = new Indicator(x + 120, y + 53, "");

		widgets.addAll(Arrays.asList(blacklist, whitelistCon, whitelistDis, blacklistIndicator, whitelistConIndicator,
				whitelistDisIndicator));

		add = new IconButton(x + 159, y + 22, ScreenResources.I_ADD);
		widgets.add(add);
		handleIndicators();

		attributeSelectorLabel = new Label(x + 40, y + 27, "").colored(0xF3EBDE).withShadow();
		attributeSelector = new SelectionScrollInput(x + 37, y + 24, 118, 14);
		attributeSelector.forOptions(Arrays.asList(""));
		attributeSelector.calling(s -> {
		});
		referenceItemChanged(container.filterInventory.getStackInSlot(0));

		widgets.add(attributeSelector);
		widgets.add(attributeSelectorLabel);

		selectedAttributes.clear();
		selectedAttributes
				.add(TextFormatting.YELLOW + (container.selectedAttributes.isEmpty() ? noSelectedT : selectedT));
		container.selectedAttributes.forEach(at -> selectedAttributes.add(TextFormatting.GRAY + "- " + at.format()));

	}

	private void referenceItemChanged(ItemStack stack) {
		lastItemScanned = stack;

		if (stack.isEmpty()) {
			attributeSelector.active = false;
			attributeSelector.visible = false;
			attributeSelectorLabel.text = TextFormatting.ITALIC + referenceH;
			add.active = false;
			attributeSelector.calling(s -> {
			});
			return;
		}

		add.active = true;
		attributeSelector.titled(stack.getDisplayName().getFormattedText() + "...");
		attributesOfItem.clear();
		for (ItemAttribute itemAttribute : ItemAttribute.types)
			attributesOfItem.addAll(itemAttribute.listAttributesOf(stack));
		List<String> options = attributesOfItem.stream().map(ItemAttribute::format).collect(Collectors.toList());
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
	public void renderWindowForeground(int mouseX, int mouseY, float partialTicks) {
		ItemStack stack = container.filterInventory.getStackInSlot(1);
		RenderSystem.pushMatrix();
		RenderSystem.translatef(0.0F, 0.0F, 32.0F);
		this.blitOffset = 200;
		this.itemRenderer.zLevel = 200.0F;
		this.itemRenderer.renderItemOverlayIntoGUI(font, stack, guiLeft + 59, guiTop + 56,
				String.valueOf(selectedAttributes.size() - 1));
		this.blitOffset = 0;
		this.itemRenderer.zLevel = 0.0F;
		RenderSystem.popMatrix();

		super.renderWindowForeground(mouseX, mouseY, partialTicks);
	}

	@Override
	public void tick() {
		super.tick();
		ItemStack stackInSlot = container.filterInventory.getStackInSlot(0);
		if (!stackInSlot.equals(lastItemScanned, false))
			referenceItemChanged(stackInSlot);
	}

	@Override
	protected void renderHoveredToolTip(int mouseX, int mouseY) {
		if (this.minecraft.player.inventory.getItemStack().isEmpty() && this.hoveredSlot != null
				&& this.hoveredSlot.getHasStack()) {
			if (this.hoveredSlot.slotNumber == 37) {
				renderTooltip(selectedAttributes, mouseX, mouseY, font);
				return;
			}
			this.renderTooltip(this.hoveredSlot.getStack(), mouseX, mouseY);
		}
		super.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected List<IconButton> getTooltipButtons() {
		return Arrays.asList(blacklist, whitelistCon, whitelistDis);
	}

	@Override
	protected List<String> getTooltipDescriptions() {
		return Arrays.asList(blacklistDESC, whitelistConDESC, whitelistDisDESC);
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
					selectedAttributes.set(0, TextFormatting.YELLOW + selectedT);
				selectedAttributes.add(TextFormatting.GRAY + "- " + itemAttribute.format());
				return true;
			}
		}

		return mouseClicked;
	}

	@Override
	protected void contentsCleared() {
		selectedAttributes.clear();
		selectedAttributes.add(TextFormatting.YELLOW + noSelectedT);
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
