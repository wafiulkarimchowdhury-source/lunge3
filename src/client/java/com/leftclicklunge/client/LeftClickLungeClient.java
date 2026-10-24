package com.leftclicklunge.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.sdl.SDLScancode;

public class LeftClickLungeClient implements ClientModInitializer {
	public static final String MOD_ID = "leftclicklunge";

	/** The on/off key. Shows up in Options > Controls > Key Binds under "Left Click Lunge". */
	public static KeyMapping toggleKey;

	@Override
	public void onInitializeClient() {
		LungeConfig.load();

		KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));

		toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.leftclicklunge.toggle",
				InputConstants.Type.KEYBOARD,
				SDLScancode.SDL_SCANCODE_O, // default key; change it in Controls
				category
		));

		ClientTickEvents.START_CLIENT_TICK.register(LungeController::onStartTick);
		ClientTickEvents.END_CLIENT_TICK.register(LungeController::onEndTick);
	}
}
