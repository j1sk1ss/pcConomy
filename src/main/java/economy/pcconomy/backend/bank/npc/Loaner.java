package economy.pcconomy.backend.bank.npc;

import economy.pcconomy.frontend.ui.Window;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import org.bukkit.event.EventHandler;

@TraitName("Loaner")
public class Loaner extends Trait {
    public Loaner() {
        super("Loaner");
    }

    @EventHandler
    public void onClick(NPCRightClickEvent event) {
        if (!event.getNPC().equals(this.getNPC())) return;
        var player = event.getClicker();

        Window.OpenLoanWindow(player, false);
    }
}
