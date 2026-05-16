package mcjty.lostcities.client;

import mcjty.lostcities.setup.ClientSetup;
import net.fabricmc.api.ClientModInitializer;

public class LostCitiesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientSetup.init();
	}
}