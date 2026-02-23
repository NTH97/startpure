package org.dreambot.scripts.startpure.tasks;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.scripts.startpure.Constants;
import org.dreambot.scripts.startpure.ScriptContext;
import org.dreambot.scripts.startpure.ScriptState;
import org.dreambot.scripts.startpure.ScriptTask;

import java.util.Objects;

public class EquipGearTask implements ScriptTask {

    private final ScriptContext ctx;

    public EquipGearTask(ScriptContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public int execute() {
        // Equip iron armour
        equipItem(Constants.IRON_FULL_HELM);
        equipItem(Constants.IRON_PLATEBODY);
        equipItem(Constants.IRON_PLATELEGS);
        equipItem(Constants.IRON_KITESHIELD);
        equipItem(Constants.IRON_SCIMITAR);

        // Enable auto-retaliate
        Combat.toggleAutoRetaliate(true);

        ctx.log("Gear equipped.");
        ctx.setState(ScriptState.WALK_TO_TRAINING);
        return Calculations.random(600, 1200);
    }

    private void equipItem(int itemId) {
        if (Inventory.contains(itemId)) {
            Objects.requireNonNull(Inventory.get(itemId)).interact();
        }
    }
}
