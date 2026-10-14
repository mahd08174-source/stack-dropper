package com.stackdropper;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class StackDropperClient implements ClientModInitializer {
	public static KeyBinding dropKey;
	public static KeyBinding menuKey;
	public static boolean enabled = false;

	private static long lastDrop = System.nanoTime();

	/** Slot order in the player screen handler: hotbar (36-44) first, then main inventory (9-35). */
	private static final int[] SLOT_ORDER = buildSlotOrder();

	private static int[] buildSlotOrder() {
		int[] order = new int[36];
		int i = 0;
		for (int s = 36; s <= 44; s++) order[i++] = s;
		for (int s = 9; s <= 35; s++) order[i++] = s;
		return order;
	}

	@Override
	public void onInitializeClient() {
		Config.load();

		KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("stackdropper", "main"));

		dropKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.stackdropper.toggle", InputUtil.Type.KEYSYM, Config.get().keyCode, category));
		menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.stackdropper.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_TAB, category));

		ClientTickEvents.END_CLIENT_TICK.register(StackDropperClient::tick);
	}

	/** Rebinds the drop key and saves it. Used by the menu. */
	public static void rebind(int keyCode) {
		dropKey.setBoundKey(InputUtil.Type.KEYSYM.createFromCode(keyCode));
		KeyBinding.updateKeysByCode();
		Config.get().keyCode = keyCode;
		Config.save();
	}

	private static void tick(MinecraftClient client) {
		if (client.player == null || client.interactionManager == null) {
			while (dropKey.wasPressed()) { }
			while (menuKey.wasPressed()) { }
			return;
		}

		while (menuKey.wasPressed()) {
			if (client.currentScreen == null) {
				client.setScreen(new DropperScreen());
			}
		}

		while (dropKey.wasPressed()) {
			enabled = !enabled;
			lastDrop = System.nanoTime();
			client.player.sendMessage(Text.literal("Stack Dropper: " + (enabled ? "ON" : "OFF")), true);
		}

		// Pause while any screen (including the menu) is open.
		if (!enabled || client.currentScreen != null) {
			lastDrop = System.nanoTime();
			return;
		}

		long interval = (long) (Config.get().intervalSeconds * 1_000_000_000.0);
		long now = System.nanoTime();
		int guard = 0;
		while (now - lastDrop >= interval && guard < 50) {
			dropStacks(client);
			lastDrop += interval;
			guard++;
		}
		if (guard >= 50) {
			lastDrop = now;
		}
	}

	private static void dropStacks(MinecraftClient client) {
		PlayerEntity player = client.player;
		Config cfg = Config.get();
		int remaining = cfg.wholeInventory ? SLOT_ORDER.length : cfg.stacksPerDrop;

		for (int slot : SLOT_ORDER) {
			if (remaining <= 0) break;
			ItemStack stack = player.playerScreenHandler.getSlot(slot).getStack();
			if (stack.isEmpty()) continue;
			// THROW with button 1 drops the whole stack.
			client.interactionManager.clickSlot(0, slot, 1, SlotActionType.THROW, player);
			remaining--;
		}
	}
}
