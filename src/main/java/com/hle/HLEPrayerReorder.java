package com.hle;

import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.EnumID;
import net.runelite.api.ParamID;
import net.runelite.api.ScriptID;
import net.runelite.api.Varbits;
import net.runelite.api.annotations.Interface;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import static net.runelite.api.widgets.WidgetConfig.DRAG;
import static net.runelite.api.widgets.WidgetConfig.DRAG_ON;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.prayer.PrayerConfig;
import org.apache.commons.lang3.ArrayUtils;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
class HLEPrayerReorder
{
	private static final int PRAYER_X_OFFSET = 37;
	private static final int PRAYER_Y_OFFSET = 37;
	private static final int QUICK_PRAYER_SPRITE_X_OFFSET = 2;
	private static final int QUICK_PRAYER_SPRITE_Y_OFFSET = 2;
	private static final int PRAYER_COLUMN_COUNT = 5;


	private final Client client;
	private final ClientThread clientThread;
	private final ConfigManager configManager;


	void startUp()
	{
			clientThread.invokeLater(this::redrawPrayers);
	}

	void shutDown()
	{
		clientThread.invokeLater(this::redrawPrayers);
	}

	void reset()
	{
		for (var key : configManager.getConfigurationKeys(PrayerConfig.GROUP + ".prayer_order_book"))
		{
			String[] str = key.split("\\.", 2);
			if (str.length == 2)
			{
				configManager.unsetConfiguration(str[0], str[1]);
			}
		}
		for (var key : configManager.getConfigurationKeys(PrayerConfig.GROUP + ".prayer_hidden_book"))
		{
			String[] str = key.split("\\.", 2);
			if (str.length == 2)
			{
				configManager.unsetConfiguration(str[0], str[1]);
			}
		}

		clientThread.invokeLater(this::redrawPrayers);
	}

	private int[] getPrayerOrder(int prayerbook)
	{
		var s = configManager.getConfiguration(PrayerConfig.GROUP, "prayer_order_book_" + prayerbook);
		if (s == null)
		{
			return null;
		}
		return Arrays.stream(s.split(","))
			.mapToInt(Integer::parseInt)
			.toArray();
	}


	protected void setShuffledOrder()
	{
		var s = Arrays.stream(generateUniqueArray())
			.mapToObj(Integer::toString)
			.collect(Collectors.joining(","));
		configManager.setConfiguration(PrayerConfig.GROUP, "prayer_order_book_" + 0, s);
	}


	private static int[] generateUniqueArray()
	{
		List<Integer> uniqueValues = new ArrayList<>();
		for (int i = 0; i <= 28; i++)
		{
			uniqueValues.add(i);
		}

		Collections.shuffle(uniqueValues);

		int[] result = new int[uniqueValues.size()];
		for (int i = 0; i < uniqueValues.size(); i++)
		{
			result[i] = uniqueValues.get(i);
		}

		return result;
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired scriptPostFired)
	{
		int scriptId = scriptPostFired.getScriptId();
		if (
			scriptId == ScriptID.PRAYER_UPDATEBUTTON ||
				scriptId == ScriptID.PRAYER_REDRAW ||
				scriptId == ScriptID.QUICKPRAYER_INIT
		)
		{
			rebuildPrayers();
		}
	}

	private EnumComposition getPrayerBookEnum(int prayerbook)
	{
		var enumId = prayerbook == 1 ? EnumID.PRAYERS_RUINOUS : EnumID.PRAYERS_NORMAL;
		return client.getEnum(enumId);
	}

	private int[] defaultPrayerOrder(EnumComposition prayerEnum)
	{
		return Arrays.stream(prayerEnum.getKeys())
			.boxed() // IntStream does not accept a custom comparator
			.sorted(Comparator.comparing(id ->
			{
				var prayerObjId = prayerEnum.getIntValue(id);
				var prayerObj = client.getItemDefinition(prayerObjId);
				return prayerObj.getIntValue(ParamID.OC_PRAYER_LEVEL);
			}))
			.mapToInt(i -> i)
			.toArray();
	}


	private void redrawPrayers()
	{
		Widget w = client.getWidget(InterfaceID.PRAYER, 0);
		if (w != null)
		{
			client.runScript(w.getOnVarTransmitListener());
		}
	}

	protected void rebuildPrayers()
	{
		var prayerbook = client.getVarbitValue(Varbits.PRAYERBOOK);
		var prayerBookEnum = getPrayerBookEnum(prayerbook);
		var prayerIds = MoreObjects.firstNonNull(getPrayerOrder(prayerbook), defaultPrayerOrder(prayerBookEnum));

		if (isInterfaceOpen(InterfaceID.PRAYER))
		{
			int index = 0;
			for (int prayerId : prayerIds)
			{
				var prayerObjId = prayerBookEnum.getIntValue(prayerId);
				var prayerObj = client.getItemDefinition(prayerObjId);
				var prayerWidget = client.getWidget(prayerObj.getIntValue(ParamID.OC_PRAYER_COMPONENT));

				assert prayerWidget != null;

				int widgetConfig = prayerWidget.getClickMask();
				// allow dragging of this widget
				widgetConfig |= DRAG;
				// allow this widget to be dragged on
				widgetConfig |= DRAG_ON;

				prayerWidget.setClickMask(widgetConfig);


				int x = index % PRAYER_COLUMN_COUNT;
				int y = index / PRAYER_COLUMN_COUNT;
				int widgetX = x * PRAYER_X_OFFSET;
				int widgetY = y * PRAYER_Y_OFFSET;

				prayerWidget.setPos(widgetX, widgetY);
				prayerWidget.revalidate();

				++index;
			}
		}

		if (isInterfaceOpen(InterfaceID.QUICK_PRAYER))
		{
			Widget prayersContainer = client.getWidget(ComponentID.QUICK_PRAYER_PRAYERS);
			if (prayersContainer == null)
			{
				return;
			}

			Widget[] prayerWidgets = prayersContainer.getDynamicChildren();
			if (prayerWidgets == null || prayerWidgets.length != prayerBookEnum.size() * 3)
			{
				return;
			}

			var sortedPrayers = defaultPrayerOrder(prayerBookEnum);
			int index = 0;
			for (int prayerId : prayerIds)
			{
				int x = index % PRAYER_COLUMN_COUNT;
				int y = index / PRAYER_COLUMN_COUNT;

				Widget prayerWidget = prayerWidgets[prayerId];
				prayerWidget.setPos(x * PRAYER_X_OFFSET, y * PRAYER_Y_OFFSET);
				prayerWidget.revalidate();

				int sortedIdx = ArrayUtils.indexOf(sortedPrayers, prayerId);
				int childId = prayerBookEnum.size() + 2 * sortedIdx;

				Widget prayerSpriteWidget = prayerWidgets[childId];
				prayerSpriteWidget.setPos(
					QUICK_PRAYER_SPRITE_X_OFFSET + x * PRAYER_X_OFFSET,
					QUICK_PRAYER_SPRITE_Y_OFFSET + y * PRAYER_Y_OFFSET);
				prayerSpriteWidget.revalidate();

				Widget prayerToggleWidget = prayerWidgets[childId + 1];
				prayerToggleWidget.setPos(
					x * PRAYER_X_OFFSET,
					y * PRAYER_Y_OFFSET);
				prayerToggleWidget.revalidate();

				++index;
			}
		}
	}

	private boolean isInterfaceOpen(@Interface int interfaceId)
	{
		return client.getWidget(interfaceId, 0) != null;
	}
}