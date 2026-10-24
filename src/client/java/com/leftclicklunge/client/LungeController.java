package com.leftclicklunge.client;

import com.leftclicklunge.client.mixin.MinecraftAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.phys.Vec3;

public final class LungeController {
	private LungeController() {
	}

	/** True while WE are calling the game's click routine, so our own mixin doesn't intercept it again. */
	private static boolean performing = false;

	/** Counts client ticks (20 per second). Used to space out repeated lunges. */
	private static long tickCounter = 0;
	private static long lastLungeTick = -1000;

	// ------------------------------------------------------------------ tick hooks

	/** Runs at the very start of every client tick, before the game handles mouse clicks. */
	public static void onStartTick(Minecraft mc) {
		tickCounter++;

		// Holding left click = keep lunging. (A single tap is handled by handleLeftClick below.)
		if (mc.options.keyAttack.isDown()) {
			tryLunge(mc);
		}
	}

	/** Runs at the end of every client tick. Handles the on/off keybind. */
	public static void onEndTick(Minecraft mc) {
		while (LeftClickLungeClient.toggleKey.consumeClick()) {
			LungeConfig cfg = LungeConfig.get();
			cfg.enabled = !cfg.enabled;
			LungeConfig.save();
			if (cfg.actionBarMessages && mc.player != null) {
				Component msg = Component.literal("Left Click Lunge: " + (cfg.enabled ? "ON" : "OFF"))
						.withStyle(cfg.enabled ? ChatFormatting.GREEN : ChatFormatting.RED);
				mc.player.sendOverlayMessage(msg);
			}
		}
	}

	// ------------------------------------------------------------------ called from the mixin

	/**
	 * Called for every real left click. Returns true if the macro took the click
	 * (the normal click is then cancelled), false to let the normal click happen.
	 */
	public static boolean handleLeftClick(Minecraft mc) {
		if (performing) {
			return false; // this is our own jab, let it through
		}
		return tryLunge(mc) != Result.NOT_ACTIVE;
	}

	/** True while the macro is active and left click is held, so vanilla shouldn't start mining. */
	public static boolean shouldBlockMining(Minecraft mc) {
		return !performing && isActive(mc) && findSpearSlot(mc.player) >= 0;
	}

	// ------------------------------------------------------------------ the lunge itself

	private enum Result {
		/** Macro doesn't apply right now: normal click behaviour. */
		NOT_ACTIVE,
		/** Macro applies but the repeat delay hasn't passed yet. The click is swallowed. */
		COOLING_DOWN,
		/** Lunge was performed. */
		FIRED
	}

	private static Result tryLunge(Minecraft mc) {
		if (!isActive(mc)) {
			return Result.NOT_ACTIVE;
		}
		LocalPlayer player = mc.player;
		int spearSlot = findSpearSlot(player);
		if (spearSlot < 0) {
			return Result.NOT_ACTIVE; // no Lunge spear on the hotbar: behave like normal Minecraft
		}
		if (tickCounter - lastLungeTick < LungeConfig.get().repeatIntervalTicks) {
			return Result.COOLING_DOWN;
		}
		lastLungeTick = tickCounter;
		performSwapLunge(mc, spearSlot);
		return Result.FIRED;
	}

	/**
	 * The attribute swap, all inside ONE client tick:
	 *   1. switch to the Lunge spear (and tell the server)
	 *   2. do a normal left click (the spear jab, which triggers Lunge)
	 *   3. switch straight back to what you were holding (and tell the server)
	 * Because the server only refreshes your attributes once per tick, the jab is judged with the
	 * attack speed of the item you were holding before, which is why holding an empty hand or food is fastest.
	 */
	private static void performSwapLunge(Minecraft mc, int spearSlot) {
		LocalPlayer player = mc.player;
		Inventory inventory = player.getInventory();
		int originalSlot = inventory.getSelectedSlot();
		boolean swapping = spearSlot != originalSlot;

		performing = true;
		try {
			if (swapping) {
				inventory.setSelectedSlot(spearSlot);
				sendSelectedSlot(mc, spearSlot);
			}
			((MinecraftAccessor) mc).leftclicklunge$startAttack();
		} finally {
			if (swapping) {
				inventory.setSelectedSlot(originalSlot);
				sendSelectedSlot(mc, originalSlot);
			}
			performing = false;
		}
	}

	private static void sendSelectedSlot(Minecraft mc, int slot) {
		if (mc.getConnection() != null) {
			mc.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
		}
	}

	// ------------------------------------------------------------------ conditions

	/** Everything that must be true for the macro to take over left click (except cooldown and spear). */
	private static boolean isActive(Minecraft mc) {
		LungeConfig cfg = LungeConfig.get();
		LocalPlayer player = mc.player;
		if (!cfg.enabled || player == null || mc.level == null || mc.gameMode == null) {
			return false;
		}
		if (mc.gui.screen() != null || !mc.mouseHandler.isMouseGrabbed()) {
			return false; // menus / inventory open
		}
		if (player.isSpectator() || player.isUsingItem()) {
			return false; // eating, drawing a bow, blocking, ...
		}
		if (player.getFoodData().getFoodLevel() < cfg.lowestFoodLevelToLunge) {
			return false; // Lunge needs hunger
		}
		// THE SAFETY RULE: never run while any player/mob is within the safe radius.
		if (hasEntityWithin(mc, player, cfg.safeRadius)) {
			return false;
		}
		return true;
	}

	/** True if any other player/entity is within `radius` blocks of you (measured to its nearest edge). */
	private static boolean hasEntityWithin(Minecraft mc, LocalPlayer player, double radius) {
		boolean onlyLiving = LungeConfig.get().onlyLivingEntities;
		Vec3 eyes = player.getEyePosition();
		double radiusSqr = radius * radius;

		for (Entity other : mc.level.getEntities(player, player.getBoundingBox().inflate(radius))) {
			if (other == player || other.isSpectator()) {
				continue;
			}
			if (onlyLiving && (!(other instanceof LivingEntity) || other instanceof ArmorStand)) {
				continue;
			}
			// Don't count the horse/boat you are riding or whoever rides with you.
			if (player.isPassenger() && other.getRootVehicle() == player.getRootVehicle()) {
				continue;
			}
			if (other.getBoundingBox().distanceToSqr(eyes) <= radiusSqr) {
				return true;
			}
		}
		return false;
	}

	// ------------------------------------------------------------------ finding the spear

	/** Returns the hotbar slot (0-8) of the Lunge spear to use, or -1 if there is none. */
	private static int findSpearSlot(LocalPlayer player) {
		if (player == null) {
			return -1;
		}
		Inventory inventory = player.getInventory();
		int fixed = LungeConfig.get().fixedSpearSlot;
		if (fixed >= 0 && fixed <= 8) {
			return lungeLevel(inventory.getItem(fixed)) > 0 ? fixed : -1;
		}

		int bestSlot = -1;
		int bestLevel = 0;
		for (int slot = 0; slot < 9; slot++) {
			int level = lungeLevel(inventory.getItem(slot));
			if (level > bestLevel) {
				bestLevel = level;
				bestSlot = slot;
			}
		}
		return bestSlot;
	}

	private static int lungeLevel(ItemStack stack) {
		if (stack.isEmpty()) {
			return 0;
		}
		ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
		if (enchantments == null || enchantments.isEmpty()) {
			return 0;
		}
		for (Holder<Enchantment> enchantment : enchantments.keySet()) {
			if (enchantment.is(Enchantments.LUNGE)) {
				return enchantments.getLevel(enchantment);
			}
		}
		return 0;
	}
}
