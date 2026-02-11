package org.dreambot.api.script.poh;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FurnitureItem {

    int id;
    int quantity;
    int price;
    boolean stackable;
    boolean needsToPurchase;

    public FurnitureItem(int id, int quantity, int price, boolean stackable) {
        this.id = id;
        this.quantity = quantity;
        this.price = price;
        this.stackable = stackable;
        this.needsToPurchase = false;
    }

    public FurnitureItem(int id, int quantity, int price) {
        this.id = id;
        this.quantity = quantity;
        this.price = price;
        this.stackable = false;
        this.needsToPurchase = false;
    }
}
