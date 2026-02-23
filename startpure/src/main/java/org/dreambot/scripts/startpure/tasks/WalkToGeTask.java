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

    // All Settings panel widget IDs
    private static final int SETTINGS_ALL_GROUP = 116;
    private static final int SETTINGS_SEARCH_CHILD = 2;
    private static final int SETTINGS_TOGGLE_LIST_CHILD = 12;

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
                return configureSetting("Trade/duel accept delay",
                        VARBIT_TRADE_ACCEPT_DELAY, 0);
            case 2:
                return configureSetting("World hop confirmation",
                        VARBIT_WORLD_HOP_CONFIRMATION, 0);
            default:
                closeSettingsPanel();
                ctx.setSettingsConfigured(true);
                ctx.log("Game settings configured.");
                return Calculations.random(600, 1200);
        }
    }

    private int configureSetting(String name, int varbit, int desiredValue) {
        int currentValue = PlayerSettings.getBitValue(varbit);

        if (currentValue == desiredValue) {
            ctx.log("[Settings] " + name + " already configured (varbit " + varbit + " = " + currentValue + ")");
            configStep++;
            return Calculations.random(300, 600);
        }

        // Open settings tab if not open
        if (!Tabs.isOpen(Tab.OPTIONS)) {
            Tabs.open(Tab.OPTIONS);
            return Calculations.random(600, 1200);
        }

        // Open "All Settings" panel — click the star/cog icon in the settings tab
        WidgetChild allSettingsPanel = Widgets.get(SETTINGS_ALL_GROUP, SETTINGS_TOGGLE_LIST_CHILD);
        if (allSettingsPanel == null || !allSettingsPanel.isVisible()) {
            // Click the "All settings" button (Widget 261, child 1, grandchild 0)
            WidgetChild allSettingsBtn = Widgets.get(261, 1, 0);
            if (allSettingsBtn != null && allSettingsBtn.isVisible()) {
                allSettingsBtn.interact();
                return Calculations.random(600, 1200);
            }
            return Calculations.random(300, 600);
        }

        // Type the setting name in the search bar
        WidgetChild searchBar = Widgets.get(SETTINGS_ALL_GROUP, SETTINGS_SEARCH_CHILD);
        if (searchBar != null && searchBar.isVisible()) {
            searchBar.interact();
            ctx.sleepUntil(() -> false, 300, 100);
            Keyboard.type(name);
            ctx.sleepUntil(() -> false, 600, 100);
        }

        // Find and click the toggle — first clickable child in the results list
        WidgetChild toggleList = Widgets.get(SETTINGS_ALL_GROUP, SETTINGS_TOGGLE_LIST_CHILD);
        if (toggleList != null && toggleList.isVisible()) {
            WidgetChild[] children = toggleList.getChildren();
            if (children != null) {
                for (WidgetChild child : children) {
                    if (child != null && child.isVisible() && child.hasAction("Toggle")) {
                        child.interact("Toggle");
                        ctx.sleepUntil(() -> PlayerSettings.getBitValue(varbit) == desiredValue, 2000, 300);
                        break;
                    }
                }
            }
        }

        // Verify and advance
        if (PlayerSettings.getBitValue(varbit) == desiredValue) {
            ctx.log("[Settings] " + name + " configured successfully.");
        } else {
            ctx.log("[Settings] " + name + " may not have toggled (varbit " + varbit +
                    " = " + PlayerSettings.getBitValue(varbit) + ", expected " + desiredValue +
                    "). Continuing anyway.");
        }

        // Clear search for next setting
        clearSearch();

        configStep++;
        return Calculations.random(600, 1200);
    }

    private void clearSearch() {
        WidgetChild searchBar = Widgets.get(SETTINGS_ALL_GROUP, SETTINGS_SEARCH_CHILD);
        if (searchBar != null && searchBar.isVisible()) {
            searchBar.interact();
            ctx.sleepUntil(() -> false, 200, 100);
            // Select all and delete
            Keyboard.holdControl(() -> { Keyboard.type("a"); return false; }, 500);
            Keyboard.typeKey(java.awt.event.KeyEvent.VK_BACK_SPACE);
            ctx.sleepUntil(() -> false, 300, 100);
        }
    }

    private void closeSettingsPanel() {
        // Close settings by opening inventory tab
        Tabs.open(Tab.INVENTORY);
    }
}
