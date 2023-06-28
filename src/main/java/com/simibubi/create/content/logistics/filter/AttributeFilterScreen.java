package com.simibubi.create.content.logistics.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPackets;
import com.simibubi.create.content.logistics.filter.AttributeFilterMenu.WhitelistMode;
import com.simibubi.create.content.logistics.filter.FilterScreenPacket.Option;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Indicator;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class AttributeFilterScreen extends AbstractFilterScreen<AttributeFilterMenu> {

	private static final String PREFIX = "gui.attribute_filter.";

	private Component addDESC = Lang.translateDirect(PREFIX + "add_attribute");
	private Component addInvertedDESC = Lang.translateDirect(PREFIX + "add_inverted_attribute");

	private Component allowDisN = Lang.translateDirect(PREFIX + "allow_list_disjunctive");
	private Component allowDisDESC = Lang.translateDirect(PREFIX + "allow_list_disjunctive.description");
	private Component allowConN = Lang.translateDirect(PREFIX + "allow_list_conjunctive");
	private Component allowConDESC = Lang.translateDirect(PREFIX + "allow_list_conjunctive.description");
	private Component denyN = Lang.translateDirect(PREFIX + "deny_list");
	private Component denyDESC = Lang.translateDirect(PREFIX + "deny_list.description");

	private Component referenceH = Lang.translateDirect(PREFIX + "add_reference_item");
	private Component noSelectedT = Lang.translateDirect(PREFIX + "no_selected_attributes");
	private Component selectedT = Lang.translateDirect(PREFIX + "selected_attributes");

	private IconButton whitelistDis, whitelistCon, blacklist;
	private Indicator whitelistDisIndicator, whitelistConIndicator, blacklistIndicator;
	private IconButton add;
	private IconButton addInverted;

	private ItemStack lastItemScanned = ItemStack.EMPTY;
	private List<ItemAttribute> attributesOfItem = new ArrayList<>();
	private List<Component> selectedAttributes = new ArrayList<>();
	private SelectionScrollInput attributeSelector;
	private Label attributeSelectorLabel;

	public AttributeFilterScreen(AttributeFilterMenu menu, Inventory inv, Component title) {
		super(menu, inv, title, AllGuiTextures.ATTRIBUTE_FILTER);
	}

	@Override
	protected void init() {
		setWindowOffset(-11, 7);
		super.init();

		int x = leftPos;
		int y = topPos;

		whitelistDis = new IconButton(x + 47, y + 61, AllIcons.I_WHITELIST_OR);
		whitelistDis.withCallback(() -> {
			menu.whitelistMode = WhitelistMode.WHITELIST_DISJ;
			sendOptionUpdate(Option.WHITELIST);
		});
		whitelistDis.setToolTip(allowDisN);
		whitelistCon = new IconButton(x + 65, y + 61, AllIcons.I_WHITELIST_AND);
		whitelistCon.withCallback(() -> {
			menu.whitelistMode = WhitelistMode.WHITELIST_CONJ;
			sendOptionUpdate(Option.WHITELIST2);
		});
		whitelistCon.setToolTip(allowConN);
		blacklist = new IconButton(x + 83, y + 61, AllIcons.I_WHITELIST_NOT);
		blacklist.withCallback(() -> {
			menu.whitelistMode = WhitelistMode.BLACKLIST;
			sendOptionUpdate(Option.BLACKLIST);
		});
		blacklist.setToolTip(denyN);

		whitelistDisIndicator = new Indicator(x + 47, y + 55, Components.immutableEmpty());
		whitelistConIndicator = new Indicator(x + 65, y + 55, Components.immutableEmpty());
		blacklistIndicator = new Indicator(x + 83, y + 55, Components.immutableEmpty());

		addRenderableWidgets(blacklist, whitelistCon, whitelistDis, blacklistIndicator, whitelistConIndicator,
			whitelistDisIndicator);

		addRenderableWidget(add = new IconButton(x + 182, y + 23, AllIcons.I_ADD));
		addRenderableWidget(addInverted = new IconButton(x + 200, y + 23, AllIcons.I_ADD_INVERTED_ATTRIBUTE));
		add.withCallback(() -> {
			handleAddedAttibute(false);
		});
		add.setToolTip(addDESC);
		addInverted.withCallback(() -> {
			handleAddedAttibute(true);
		});
		addInverted.setToolTip(addInvertedDESC);

		handleIndicators();

		attributeSelectorLabel = new Label(x + 43, y + 28, Components.immutableEmpty()).colored(0xF3EBDE)
			.withShadow();
		attributeSelector = new SelectionScrollInput(x + 39, y + 23, 137, 18);
		attributeSelector.forOptions(Arrays.asList(Components.immutableEmpty()));
		attributeSelector.removeCallback();
		referenceItemChanged(menu.ghostInventory.getStackInSlot(0));

		addRenderableWidget(attributeSelector);
		addRenderableWidget(attributeSelectorLabel);

		selectedAttributes.clear();
		selectedAttributes.add((menu.selectedAttributes.isEmpty() ? noSelectedT : selectedT).plainCopy()
			.withStyle(ChatFormatting.YELLOW));
		menu.selectedAttributes.forEach(at -> selectedAttributes.add(Components.literal("- ")
			.append(at.getFirst()
				.format(at.getSecond()))
			.withStyle(ChatFormatting.GRAY)));
	}

	private void referenceItemChanged(ItemStack stack) {
		lastItemScanned = stack;

		if (stack.isEmpty()) {
			attributeSelector.active = false;
			attributeSelector.visible = false;
			attributeSelectorLabel.text = referenceH.plainCopy()
				.withStyle(ChatFormatting.ITALIC);
			add.active = false;
			addInverted.active = false;
			attributeSelector.calling(s -> {
			});
			return;
		}

		add.active = true;

		addInverted.active = true;
		attributeSelector.titled(stack.getHoverName()
			.plainCopy()
			.append("..."));
		attributesOfItem.clear();
		for (ItemAttribute itemAttribute : ItemAttribute.types)
			attributesOfItem.addAll(itemAttribute.listAttributesOf(stack, minecraft.level));
		List<Component> options = attributesOfItem.stream()
			.map(a -> a.format(false))
			.collect(Collectors.toList());
		attributeSelector.forOptions(options);
		attributeSelector.active = true;
		attributeSelector.visible = true;
		attributeSelector.setState(0);
		attributeSelector.calling(i -> {
			attributeSelectorLabel.setTextAndTrim(options.get(i), true, 112);
			ItemAttribute selected = attributesOfItem.get(i);
			for (Pair<ItemAttribute, Boolean> existing : menu.selectedAttributes) {
				CompoundTag testTag = new CompoundTag();
				CompoundTag testTag2 = new CompoundTag();
				existing.getFirst()
					.serializeNBT(testTag);
				selected.serializeNBT(testTag2);
				if (testTag.equals(testTag2)) {
					add.active = false;
					addInverted.active = false;
					return;
				}
			}
			add.active = true;
			addInverted.active = true;
		});
		attributeSelector.onChanged();
	}

	@Override
	public void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		ItemStack stack = menu.ghostInventory.getStackInSlot(1);
		PoseStack matrixStack = graphics.pose();
		matrixStack.pushPose();
		matrixStack.translate(0, 0, 150);
		graphics.renderItemDecorations(font, stack, leftPos + 22, topPos + 59,
			String.valueOf(selectedAttributes.size() - 1));
		matrixStack.popPose();

		super.renderForeground(graphics, mouseX, mouseY, partialTicks);
	}

	@Override
	protected void containerTick() {
		super.containerTick();
		ItemStack stackInSlot = menu.ghostInventory.getStackInSlot(0);
		if (!stackInSlot.equals(lastItemScanned, false))
			referenceItemChanged(stackInSlot);
	}

	@Override
	protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
		if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
			if (this.hoveredSlot.index == 37) {
				graphics.renderComponentTooltip(font, selectedAttributes, mouseX, mouseY);
				return;
			}
			graphics.renderTooltip(font, this.hoveredSlot.getItem(), mouseX, mouseY);
		}
		super.renderTooltip(graphics, mouseX, mouseY);
	}

	@Override
	protected List<IconButton> getTooltipButtons() {
		return Arrays.asList(blacklist, whitelistCon, whitelistDis);
	}

	@Override
	protected List<MutableComponent> getTooltipDescriptions() {
		return Arrays.asList(denyDESC.plainCopy(), allowConDESC.plainCopy(), allowDisDESC.plainCopy());
	}

	@Override
	protected List<Indicator> getIndicators() {
		return Arrays.asList(blacklistIndicator, whitelistConIndicator, whitelistDisIndicator);
	}

	protected boolean handleAddedAttibute(boolean inverted) {
		int index = attributeSelector.getState();
		if (index >= attributesOfItem.size())
			return false;
		add.active = false;
		addInverted.active = false;
		CompoundTag tag = new CompoundTag();
		ItemAttribute itemAttribute = attributesOfItem.get(index);
		itemAttribute.serializeNBT(tag);
		AllPackets.getChannel()
			.sendToServer(new FilterScreenPacket(inverted ? Option.ADD_INVERTED_TAG : Option.ADD_TAG, tag));
		menu.appendSelectedAttribute(itemAttribute, inverted);
		if (menu.selectedAttributes.size() == 1)
			selectedAttributes.set(0, selectedT.plainCopy()
				.withStyle(ChatFormatting.YELLOW));
		selectedAttributes.add(Components.literal("- ").append(itemAttribute.format(inverted))
			.withStyle(ChatFormatting.GRAY));
		return true;
	}

	@Override
	protected void contentsCleared() {
		selectedAttributes.clear();
		selectedAttributes.add(noSelectedT.plainCopy()
			.withStyle(ChatFormatting.YELLOW));
		if (!lastItemScanned.isEmpty()) {
			add.active = true;
			addInverted.active = true;
		}
	}

	@Override
	protected boolean isButtonEnabled(IconButton button) {
		if (button == blacklist)
			return menu.whitelistMode != WhitelistMode.BLACKLIST;
		if (button == whitelistCon)
			return menu.whitelistMode != WhitelistMode.WHITELIST_CONJ;
		if (button == whitelistDis)
			return menu.whitelistMode != WhitelistMode.WHITELIST_DISJ;
		return true;
	}

	@Override
	protected boolean isIndicatorOn(Indicator indicator) {
		if (indicator == blacklistIndicator)
			return menu.whitelistMode == WhitelistMode.BLACKLIST;
		if (indicator == whitelistConIndicator)
			return menu.whitelistMode == WhitelistMode.WHITELIST_CONJ;
		if (indicator == whitelistDisIndicator)
			return menu.whitelistMode == WhitelistMode.WHITELIST_DISJ;
		return false;
	}

}
