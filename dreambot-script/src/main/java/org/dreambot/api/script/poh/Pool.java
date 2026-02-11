package org.dreambot.api.script.poh;

import lombok.AllArgsConstructor;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public enum Pool {

    // Item IDs: Limestone brick=3420, Bucket of water=1929, Soul rune=566, Body rune=559
    RESTORATION_POOL(29122, 458, 4, "Build",
            List.of(new FurnitureItem(3420, 5, 2000),
                    new FurnitureItem(1929, 5, 2000),
                    new FurnitureItem(566, 1000, 2000, true),
                    new FurnitureItem(559, 1000, 2000, true)),
            65),
    // Item IDs: Stamina potion(4)=12625
    REVITILIZATION_POOL(29237, 458, 5, "Upgrade",
            List.of(new FurnitureItem(12625, 10, 100000)),
            70),
    // Item IDs: Prayer potion(4)=2434
    REJUVENATION_POOL(29238, 458, 6, "Upgrade",
            List.of(new FurnitureItem(2434, 10, 100000)),
            80),
    // Item IDs: Super restore(4)=3024, Marble block=8786
    FANCY_REJUVENATION_POOL(29239, 458, 7, "Upgrade",
            List.of(new FurnitureItem(3024, 10, 150000),
                    new FurnitureItem(8786, 2, 400000)),
            85),
    // Item IDs: Anti-venom+(4)=12913, Gold leaf=8784, Blood rune=565
    ORNATE_REJUVENATION_POOL(29240, 458, 8, "Upgrade",
            List.of(new FurnitureItem(12913, 10, 150000),
                    new FurnitureItem(8784, 5, 200000),
                    new FurnitureItem(565, 1000, 2000, true)),
            90);

    final int objectId;
    final int widgetParent;
    final int widgetChild;
    final String action;
    final List<FurnitureItem> furnitureItems;
    final int level;

    public void build() {
        WidgetChild widget = Widgets.get(widgetParent, widgetChild);
        if (widget != null && widget.isVisible()) {
            widget.interact();
        }
    }

    public GameObject getObject() {
        return GameObjects.closest(objectId);
    }

    private boolean canBuild() {
        return getObject() != null;
    }

    public void interact() {
        GameObject obj = getObject();
        if (obj != null) {
            obj.interact(action);
        }
    }

    public boolean hasItems() {
        return furnitureItems.stream()
                .allMatch(x -> Inventory.count(x.getId()) >= x.getQuantity());
    }

    public List<FurnitureItem> getMissingItems() {
        return furnitureItems.stream()
                .filter(x -> Inventory.count(x.getId()) < x.getQuantity())
                .collect(Collectors.toList());
    }

    public static Pool getPool() {
        for (Pool pool : values()) {
            if (pool.canBuild()) {
                return pool;
            }
        }
        return null;
    }

    public boolean needsToBoost() {
        return HouseUtils.getTrueConstructionLevel() < this.level;
    }
}
