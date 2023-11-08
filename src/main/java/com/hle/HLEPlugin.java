package com.hle;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(name = "HLE")
public class HLEPlugin extends Plugin
{

	@Inject
	private HLEReorder hleReorder;
	@Inject
	private ClientToolbar clientToolbar;
	@Inject
	private Client client;

	private HLEPanel panel;
	private NavigationButton navButton;

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
			hleReorder.startUp();
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
		hleReorder.shutDown();

		panel = null;
	}

	@Override
	public void resetConfiguration()
	{
		hleReorder.reset();
	}

	@Provides
	HLEConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HLEConfig.class);
	}
}