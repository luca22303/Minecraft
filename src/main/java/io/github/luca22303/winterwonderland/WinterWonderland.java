package io.github.luca22303.winterwonderland;

import io.github.luca22303.winterwonderland.block.ModBlocks;
import io.github.luca22303.winterwonderland.config.WinterConfig;
import io.github.luca22303.winterwonderland.winter.WinterClimate;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WinterWonderland implements ModInitializer {
	public static final String MOD_ID = "winterwonderland";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		WinterConfig.load();
		ModBlocks.init();
		ModSounds.init();
		ModParticles.init();

		// Sort every biome (vanilla and modded) into a winter climate before any world loads.
		ServerLifecycleEvents.SERVER_STARTING.register(server -> WinterClimate.classifyAll(server.registryAccess()));
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> WinterClimate.classifyAll(server.registryAccess()));

		LOGGER.info("Winter Wonderland loaded - let it snow!");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
