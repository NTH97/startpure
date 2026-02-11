package org.dreambot.api.script.poh;

import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.wrappers.widgets.WidgetChild;

public class HouseUtils {

    // Interface IDs
    private static final int POH_FURNITURE_CREATION = 458;
    private static final int POH_VIEWER = 422;
    private static final int POH_ADD_ROOM = 212;
    private static final int POH_OPTIONS = 370;

    // Varbit IDs
    public static final int VARBIT_POH_BUILDING_MODE = 2176;

    // Object IDs
    private static final int POH_KITCHEN_SHELVES_7 = 13568;

    // Item IDs
    private static final int CRYSTAL_SAW = 9625;

    public static boolean needsSuperiorGarden() {
        return GameObjects.closest(obj -> obj.getName() != null && obj.getName().toLowerCase().contains("pool")) == null;
    }

    public static boolean needsAchievementGallery() {
        return GameObjects.closest(obj -> obj.getName() != null && obj.getName().toLowerCase().contains("jewellery")) == null;
    }

    public static void changeRoomOrientation() {
        WidgetChild widget = Widgets.get(POH_VIEWER, 65);
        if (widget != null && widget.isVisible()) {
            widget.interact();
        }
    }

    public static boolean isNorthRoomFromCenterSelected() {
        return Widgets.get(POH_VIEWER, 7, 2) != null
                && Widgets.get(POH_VIEWER, 7, 2).isVisible();
    }

    public static void selectNorthRoomFromCenter() {
        WidgetChild widget = Widgets.get(POH_VIEWER, 7, 0);
        if (widget != null) {
            widget.interact();
        }
    }

    public static void deleteRoom() {
        WidgetChild widget = Widgets.get(POH_VIEWER, 67);
        if (widget != null && widget.isVisible()) {
            widget.interact("Delete");
        }
    }

    public static void buildSuperiorGarden(int maxDelay) {
        if (isBuildRoomOpen()) {
            selectSuperiorGarden();
            return;
        }

        if (getSelectedRoomType() > 1 && getSelectedRoomOrientation() != 1) {
            changeRoomOrientation();
            return;
        }

        if (getSelectedRoomType() == 26 && canChangesBeConfirmed() && getSelectedRoomOrientation() == 1) {
            confirmChanges();
            return;
        }

        if (!hasRoomNorthOfCenter()) {
            addRoomNorthOfCenter();
            return;
        } else {
            if (!isNorthRoomFromCenterSelected()) {
                selectNorthRoomFromCenter();
                return;
            }
            if (getSelectedRoomType() == 1) {
                deleteRoom();
            }
        }
    }

    public static void buildAchievementGallery(int maxDelay) {
        if (isBuildRoomOpen()) {
            selectAchievementGallery();
            return;
        }

        if (getSelectedRoomType() > 0 && getSelectedRoomOrientation() != 3) {
            changeRoomOrientation();
            return;
        }

        if (canChangesBeConfirmed()) {
            confirmChanges();
        }

        if (!hasRoomWestOfCenter()) {
            addRoomWestOfCenter();
        }
    }

    public static boolean isFurnitureCreationOpen() {
        return Widgets.isVisible(POH_FURNITURE_CREATION, 0);
    }

    public static boolean canChangesBeConfirmed() {
        return PlayerSettings.getBitValue(5332) == 1;
    }

    public static void selectSuperiorGarden() {
        WidgetChild widget = Widgets.get(POH_ADD_ROOM, 60);
        if (widget != null && widget.isVisible()) {
            widget.interact();
        }
    }

    public static void selectAchievementGallery() {
        WidgetChild widget = Widgets.get(POH_ADD_ROOM, 62);
        if (widget != null && widget.isVisible()) {
            widget.interact();
        }
    }

    public static void confirmChanges() {
        WidgetChild widget = Widgets.get(POH_VIEWER, 69);
        if (widget != null && widget.isVisible()) {
            widget.interact();
        }
    }

    public static boolean hasRoomWestOfCenter() {
        WidgetChild widget = Widgets.get(POH_VIEWER, 8, 0);
        return widget != null && widget.isVisible();
    }

    public static boolean hasRoomNorthOfCenter() {
        WidgetChild widget = Widgets.get(POH_VIEWER, 7, 0);
        return widget != null && widget.isVisible();
    }

    public static boolean isBuildRoomOpen() {
        return Widgets.isVisible(POH_ADD_ROOM, 0);
    }

    public static void addRoomWestOfCenter() {
        WidgetChild viewerGrid = Widgets.get(POH_VIEWER, 5);
        if (viewerGrid != null) {
            WidgetChild slot = viewerGrid.getChild(91);
            if (slot != null) {
                slot.interact();
            }
        }
    }

    public static void addRoomNorthOfCenter() {
        WidgetChild viewerGrid = Widgets.get(POH_VIEWER, 5);
        if (viewerGrid != null) {
            WidgetChild slot = viewerGrid.getChild(101);
            if (slot != null) {
                slot.interact("Add room");
            }
        }
    }

    public static boolean isViewerOpened() {
        return Widgets.isVisible(POH_VIEWER, 0);
    }

    public static int getSelectedRoomIndex() {
        return PlayerSettings.getBitValue(5329);
    }

    public static int getSelectedRoomOrientation() {
        return PlayerSettings.getBitValue(5331);
    }

    public static int getSelectedRoomType() {
        return PlayerSettings.getBitValue(5333);
    }

    public static WidgetChild getGarden() {
        return Widgets.get(POH_VIEWER, 6);
    }

    public static void openViewer() {
        if (!isHouseOptionsOpen()) {
            openHouseOptions();
            return;
        }

        WidgetChild widget = Widgets.get(POH_OPTIONS, 1);
        if (widget != null && widget.isVisible()) {
            widget.interact();
        }
    }

    public static void openBuildMode() {
        if (!isHouseOptionsOpen()) {
            openHouseOptions();
            return;
        }

        WidgetChild widget = Widgets.get(POH_OPTIONS, 5);
        if (widget != null && widget.isVisible()) {
            widget.interact();
        }
    }

    public static void openHouseOptions() {
        if (!Tabs.isOpen(Tab.OPTIONS)) {
            Tabs.open(Tab.OPTIONS);
            return;
        }

        WidgetChild widget = Widgets.get(116, 31);
        if (widget != null && widget.isVisible()) {
            widget.interact();
        }
    }

    public static boolean isHouseOptionsOpen() {
        return Widgets.isVisible(POH_OPTIONS, 0);
    }

    public static boolean isInBuildMode() {
        return PlayerSettings.getBitValue(VARBIT_POH_BUILDING_MODE) == 1;
    }

    public static int getTrueConstructionLevel() {
        boolean isInvisBoosted = Inventory.contains(CRYSTAL_SAW);
        int level = Skills.getBoostedLevel(Skill.CONSTRUCTION);
        if (isInvisBoosted) {
            level += 3;
        }
        return level;
    }

    public static boolean isTeaCraftable() {
        return GameObjects.closest(POH_KITCHEN_SHELVES_7) != null
                && GameObjects.closest(obj -> obj.getName() != null && obj.getName().toLowerCase().contains("sink")) != null
                && GameObjects.closest(obj -> obj.getName() != null && obj.getName().toLowerCase().contains("larder")) != null;
    }
}
