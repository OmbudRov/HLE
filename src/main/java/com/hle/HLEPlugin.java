package com.hle;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemComposition;
import net.runelite.api.events.PostItemComposition;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(name = "HLE")
public class HLEPlugin extends Plugin
{

	@Inject
	private HLEPrayerReorder hlePrayerReorder;
	@Inject
	private ClientToolbar clientToolbar;
	@Inject
	private Client client;
	@Inject
	private ClientThread clientThread;

	private HLEPanel panel;
	private NavigationButton navButton;
	private ItemManager itemManager;
	private boolean shuffleIcons = false;

	@Inject
	private OverlayManager overlayManager;

	@Override
	protected void startUp() throws Exception
	{
		panel = injector.getInstance(HLEPanel.class);
		final BufferedImage icon = ImageUtil.loadImageResource(HLEPlugin.class, "/Hunter.png");
		navButton = NavigationButton.builder()
			.tooltip("HLE")
			.priority(1)
			.icon(icon)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			hlePrayerReorder.startUp();
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
		hlePrayerReorder.shutDown();

		panel = null;
		resetCaches();
	}

	@Override
	public void resetConfiguration()
	{
		hlePrayerReorder.reset();
	}

	@Subscribe
	public void onPostItemComposition(PostItemComposition event)
	{
		if (shuffleIcons)
		{
			ItemComposition itemComposition = event.getItemComposition();
			itemComposition.setName("?");
			itemComposition.getInventoryActions()[0] = "?";
			Random random = new Random();
			int N = random.nextInt(27000);
			itemComposition.setInventoryModel(N);
		}
	}

	@Provides
	HLEConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HLEConfig.class);
	}


	private void resetCaches()
	{
		clientThread.invokeLater(() -> {
			client.getItemCompositionCache().reset();
			client.getItemModelCache().reset();
			client.getItemSpriteCache().reset();
		});
	}

	void iconShuffler()
	{
		shuffleIcons = !shuffleIcons;
		resetCaches();
	}
}