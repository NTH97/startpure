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
public enum JewelleryBox {

    // Item IDs: Bolt of cloth=8790, Steel bar=2353, Games necklace(8)=3853, Ring of dueling(8)=2552
    BASIC_JEWELLERY_BOX(29142, 458, 4, "Build",
            List.of(new FurnitureItem(8790, 1, 5000),
                    new FurnitureItem(2353, 1, 5000),
                    new FurnitureItem(3853, 3, 5000),
                    new FurnitureItem(2552, 3, 5000)),
            81),
    // Item IDs: Gold leaf=8784, Skills necklace(4)=11968, Combat bracelet(4)=11972
    FANCY_JEWELLERY_BOX(29154, 458, 5, "Upgrade",
            List.of(new FurnitureItem(8784, 1, 300000),
                    new FurnitureItem(11968, 5, 50000),
                    new FurnitureItem(11972, 5, 50000)),
            86),
    // Item IDs: Gold leaf=8784, Amulet of glory(4)=1712, Ring of wealth(5)=2572
    ORNATE_JEWELLERY_BOX(29155, 458, 6, "Upgrade",
            List.of(new FurnitureItem(8784, 2, 300000),
                    new FurnitureItem(1712, 8, 200000),
                    new FurnitureItem(2572, 8, 200000)),
            91);

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

    public static JewelleryBox getJewelleryBox() {
        for (JewelleryBox box : values()) {
            if (box.canBuild()) {
                return box;
            }
        }
        return null;
    }

    public boolean needsToBoost() {
        return HouseUtils.getTrueConstructionLevel() < this.level;
    }
}
