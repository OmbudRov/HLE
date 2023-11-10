package com.hle;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.inject.Inject;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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

		resetPrayersButton.setText("Reset Prayers");
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
}
