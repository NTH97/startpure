package org.dreambot.scripts.startpure.tasks;

import org.dreambot.api.input.Keyboard;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.dreambot.scripts.startpure.Constants;
import org.dreambot.scripts.startpure.ScriptContext;
import org.dreambot.scripts.startpure.ScriptState;
import org.dreambot.scripts.startpure.ScriptTask;

public class WalkToGeTask implements ScriptTask {

    private final ScriptContext ctx;

    // Varbit IDs for game settings
    private static final int VARBIT_DISABLE_LEVEL_UP_INTERFACE = 9452;
    private static final int VARBIT_TRADE_ACCEPT_DELAY = 13691;
    private static final int VARBIT_WORLD_HOP_CONFIRMATION = 13693;

    // All Settings panel widget group
    private static final int ALL_SETTINGS_GROUP = 116;

    private int configStep = 0;

    public WalkToGeTask(ScriptContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public int execute() {
        if (!ctx.isSettingsConfigured()) {
            return configureSettings();
        }

        if (Constants.GE_AREA.contains(Players.getLocal())) {
            ctx.setState(ScriptState.FIND_AND_TRADE);
            return 600;
        }
        Walking.walk(Constants.GE_CENTER);
        return Calculations.random(600, 1800);
    }

    private int configureSettings() {
        switch (configStep) {
            case 0:
                return configureSetting("Disable level-up interface",
                        VARBIT_DISABLE_LEVEL_UP_INTERFACE, 1);
            case 1:
                return configureSetting("accept delay",
                        VARBIT_TRADE_ACCEPT_DELAY, 0);
            case 2:
                return configureSetting("hop confirmation",
                        VARBIT_WORLD_HOP_CONFIRMATION, 0);
            default:
                Tabs.open(Tab.INVENTORY);
                ctx.setSettingsConfigured(true);
                ctx.log("Game settings configured.");
                return Calculations.random(600, 1200);
        }
    }

    private int configureSetting(String searchText, int varbit, int desiredValue) {
        int currentValue = PlayerSettings.getBitValue(varbit);

        if (currentValue == desiredValue) {
            ctx.log("[Settings] '" + searchText + "' already configured (varbit " + varbit + " = " + currentValue + ")");
            configStep++;
            return Calculations.random(300, 600);
        }

        // Step 1: Open the Options tab
        if (!Tabs.isOpen(Tab.OPTIONS)) {
            Tabs.open(Tab.OPTIONS);
            return Calculations.random(600, 1200);
        }

        // Step 2: Check if All Settings panel is already open
        if (!isAllSettingsPanelOpen()) {
            // Find and click the "All settings" button by scanning widget 261 children
            if (!openAllSettingsPanel()) {
                ctx.log("[Settings] Could not find 'All settings' button. Skipping.");
                configStep++;
                return Calculations.random(600, 1200);
            }
            return Calculations.random(600, 1200);
        }

        // Step 3: Click the search bar and type the setting name
        WidgetChild searchBar = findSearchBar();
        if (searchBar != null) {
            searchBar.interact();
            ctx.sleepUntil(() -> false, 400, 100);
            Keyboard.type(searchText, false);
            ctx.sleepUntil(() -> false, 800, 100);
        }

        // Step 4: Find and click the toggle button in results
        clickToggleInResults();
        ctx.sleepUntil(() -> PlayerSettings.getBitValue(varbit) == desiredValue, 2000, 300);

        // Verify
        if (PlayerSettings.getBitValue(varbit) == desiredValue) {
            ctx.log("[Settings] '" + searchText + "' configured successfully.");
        } else {
            ctx.log("[Settings] '" + searchText + "' may not have toggled (varbit " + varbit +
                    " = " + PlayerSettings.getBitValue(varbit) + ", expected " + desiredValue + ").");
        }

        // Clear search bar for next setting
        clearSearchBar();

        configStep++;
        return Calculations.random(600, 1200);
    }

    private boolean isAllSettingsPanelOpen() {
        WidgetChild panel = Widgets.get(ALL_SETTINGS_GROUP, 0);
        return panel != null && panel.isVisible();
    }

    private boolean openAllSettingsPanel() {
        // Scan widget group 261 (Options panel) for a child with "All settings" tooltip or sprite
        WidgetChild optionsPanel = Widgets.get(261, 0);
        if (optionsPanel == null) return false;

        // Try each child of the options root widget
        for (int child = 0; child < 50; child++) {
            WidgetChild widget = Widgets.get(261, child);
            if (widget == null || !widget.isVisible()) continue;

            // Check the widget text/tooltip
            String text = widget.getText();
            String tooltip = widget.getTooltip();

            if (containsIgnoreCase(tooltip, "All settings") || containsIgnoreCase(text, "All settings")) {
                ctx.log("[Settings] Found 'All settings' button at 261:" + child);
                widget.interact();
                return true;
            }

            // Also check grandchildren
            WidgetChild[] grandChildren = widget.getChildren();
            if (grandChildren != null) {
                for (int gc = 0; gc < grandChildren.length; gc++) {
                    WidgetChild gcWidget = grandChildren[gc];
                    if (gcWidget == null || !gcWidget.isVisible()) continue;
                    String gcTooltip = gcWidget.getTooltip();
                    String gcText = gcWidget.getText();

                    if (containsIgnoreCase(gcTooltip, "All settings") || containsIgnoreCase(gcText, "All settings")) {
                        ctx.log("[Settings] Found 'All settings' button at 261:" + child + ":" + gc);
                        gcWidget.interact();
                        return true;
                    }
                }
            }
        }

        ctx.log("[Settings] Could not find 'All settings' button in widget 261.");
        return false;
    }

    private WidgetChild findSearchBar() {
        // Search bar is typically in the All Settings panel (group 116)
        // Try common child IDs for the search input
        for (int child = 0; child < 20; child++) {
            WidgetChild widget = Widgets.get(ALL_SETTINGS_GROUP, child);
            if (widget == null || !widget.isVisible()) continue;

            String text = widget.getText();
            // Search bar usually has placeholder text or is an empty text field
            if (widget.getType() == 4 || containsIgnoreCase(text, "Search")) {
                return widget;
            }
        }
        // Fallback: try child 2 (common position)
        return Widgets.get(ALL_SETTINGS_GROUP, 2);
    }

    private void clickToggleInResults() {
        // Scan the All Settings panel for any child with a "Toggle" action
        for (int child = 0; child < 50; child++) {
            WidgetChild widget = Widgets.get(ALL_SETTINGS_GROUP, child);
            if (widget == null || !widget.isVisible()) continue;

            if (widget.hasAction("Toggle")) {
                widget.interact("Toggle");
                return;
            }

            // Check grandchildren
            WidgetChild[] grandChildren = widget.getChildren();
            if (grandChildren != null) {
                for (WidgetChild gc : grandChildren) {
                    if (gc != null && gc.isVisible() && gc.hasAction("Toggle")) {
                        gc.interact("Toggle");
                        return;
                    }
                }
            }
        }
        ctx.log("[Settings] No toggle button found in search results.");
    }

    private void clearSearchBar() {
        WidgetChild searchBar = findSearchBar();
        if (searchBar != null && searchBar.isVisible()) {
            searchBar.interact();
            ctx.sleepUntil(() -> false, 200, 100);
            // Backspace several times to clear any text
            for (int i = 0; i < 40; i++) {
                Keyboard.typeKey(java.awt.event.KeyEvent.VK_BACK_SPACE);
            }
            ctx.sleepUntil(() -> false, 300, 100);
        }
    }

    private static boolean containsIgnoreCase(String str, String search) {
        return str != null && str.toLowerCase().contains(search.toLowerCase());
    }
}
