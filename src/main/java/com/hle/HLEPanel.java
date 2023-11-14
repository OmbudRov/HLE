package com.hle;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

public class HLEPanel extends PluginPanel
{
	private final HLEPlugin plugin;
	private final HLEPrayerReorder hlePrayerReorder;
	private final Client client;


	@Inject
	HLEPanel(final ClientThread clientThread, final HLEPlugin plugin, final Client client, final HLEPrayerReorder hlePrayerReorder)
	{
		this.plugin = plugin;
		this.client = client;
		this.hlePrayerReorder = hlePrayerReorder;

		setBorder(new EmptyBorder(10, 10, 10, 10));
		setBorder(new EmptyBorder(10, 10, 10, 10));
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setLayout(new BorderLayout());

		final JPanel layoutPanel = new JPanel();
		BoxLayout boxLayout = new BoxLayout(layoutPanel, BoxLayout.Y_AXIS);
		layoutPanel.setLayout(boxLayout);
		add(layoutPanel, BorderLayout.NORTH);

		final JPanel topPanel = new JPanel();

		topPanel.setBorder(new EmptyBorder(0, 0, 4, 0));
		topPanel.setLayout(new GridBagLayout());

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(0, 2, 4, 2);


		prayerSetup(constraints,topPanel,layoutPanel,clientThread);
		iconSetup(constraints,topPanel,layoutPanel,clientThread);

	}

	private void prayerSetup(GridBagConstraints constraints, JPanel topPanel, JPanel layoutPanel, ClientThread clientThread)
	{
		JLabel prayersLabel = new JLabel("Prayers");
		prayersLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		layoutPanel.add(prayersLabel);
		layoutPanel.add(Box.createVerticalStrut(5));

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 2;
		JButton randomizePrayersButton = new JButton();
		topPanel.add(randomizePrayersButton, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		JButton resetPrayersButton = new JButton();
		topPanel.add(resetPrayersButton, constraints);

		layoutPanel.add(topPanel);

		randomizePrayersButton.setText("Randomize Prayers");
		randomizePrayersButton.setFocusable(false);

		resetPrayersButton.setText("Reset Prayer");
		resetPrayersButton.setFocusable(false);

		randomizePrayersButton.addActionListener(e -> {
			clientThread.invokeLater(() -> {
				hlePrayerReorder.setShuffledOrder();
				hlePrayerReorder.rebuildPrayers();
			});
		});

		resetPrayersButton.addActionListener(e -> {
			hlePrayerReorder.reset();
		});
	}

	private void iconSetup(GridBagConstraints constraints, JPanel topPanel, JPanel layoutPanel, ClientThread clientThread)
	{
		JLabel iconsLabel = new JLabel("Icons");
		iconsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		layoutPanel.add(iconsLabel);
		layoutPanel.add(Box.createVerticalStrut(5));

		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.gridwidth = 2;
		JButton randomizeIconsButton = new JButton();
		topPanel.add(randomizeIconsButton, constraints);

		constraints.gridx = 0;
		constraints.gridy = 4;
		constraints.gridwidth = 2;
		JButton resetIconsButton = new JButton();
		topPanel.add(resetIconsButton, constraints);

		layoutPanel.add(topPanel);

		randomizeIconsButton.setText("Randomize Icons");
		randomizeIconsButton.setFocusable(false);

		resetIconsButton.setText("Reset Icons");
		resetIconsButton.setFocusable(false);

		randomizeIconsButton.addActionListener(e -> {
			clientThread.invokeLater(plugin::iconShuffler);
		});

		resetIconsButton.addActionListener(e -> {
			plugin.iconShuffler();
		});
	}
}
