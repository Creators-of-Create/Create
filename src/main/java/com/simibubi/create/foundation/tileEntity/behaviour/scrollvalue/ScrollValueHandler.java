package com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue;

import com.simibubi.create.AllItemsNew;
import com.simibubi.create.AllKeys;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform.Sided;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueBehaviour.StepContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ScrollValueHandler {

	@OnlyIn(Dist.CLIENT)
	public static boolean onScroll(double delta) {
		RayTraceResult objectMouseOver = Minecraft.getInstance().objectMouseOver;
		if (!(objectMouseOver instanceof BlockRayTraceResult))
			return false;

		BlockRayTraceResult result = (BlockRayTraceResult) objectMouseOver;
		Minecraft mc = Minecraft.getInstance();
		ClientWorld world = mc.world;
		BlockPos blockPos = result.getPos();

		ScrollValueBehaviour scrolling = TileEntityBehaviour.get(world, blockPos, ScrollValueBehaviour.TYPE);
		if (scrolling == null)
			return false;
		if (!mc.player.isAllowEdit())
			return false;
		if (scrolling.needsWrench && !AllItemsNew.typeOf(AllItemsNew.WRENCH, mc.player.getHeldItemMainhand()))
			return false;
		if (scrolling.slotPositioning instanceof Sided)
			((Sided) scrolling.slotPositioning).fromSide(result.getFace());
		if (!scrolling.testHit(objectMouseOver.getHitVec()))
			return false;

		if (scrolling instanceof BulkScrollValueBehaviour && AllKeys.ctrlDown()) {
			BulkScrollValueBehaviour bulkScrolling = (BulkScrollValueBehaviour) scrolling;
			for (SmartTileEntity smartTileEntity : bulkScrolling.getBulk()) {
				ScrollValueBehaviour other = TileEntityBehaviour.get(smartTileEntity, ScrollValueBehaviour.TYPE);
				if (other != null)
					applyTo(delta, other);
			}

		} else
			applyTo(delta, scrolling);

		return true;
	}

	protected static void applyTo(double delta, ScrollValueBehaviour scrolling) {
		scrolling.ticksUntilScrollPacket = 10;
		int valueBefore = scrolling.scrollableValue;

		StepContext context = new StepContext();
		context.control = AllKeys.ctrlDown();
		context.shift = AllKeys.shiftDown();
		context.currentValue = scrolling.scrollableValue;
		context.forward = delta > 0;

		double newValue = scrolling.scrollableValue + Math.signum(delta) * scrolling.step.apply(context);
		scrolling.scrollableValue = (int) MathHelper.clamp(newValue, scrolling.min, scrolling.max);

		if (valueBefore != scrolling.scrollableValue)
			scrolling.clientCallback.accept(scrolling.scrollableValue);
	}

}
