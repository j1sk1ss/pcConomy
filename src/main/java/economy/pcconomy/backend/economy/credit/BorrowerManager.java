package economy.pcconomy.backend.economy.credit;

import org.bukkit.entity.Player;

import economy.pcconomy.PcConomy;
import economy.pcconomy.backend.db.Loadable;

import java.util.List;
import java.util.ArrayList;


public class BorrowerManager extends Loadable {
    public final List<Borrower> borrowers = new ArrayList<>();

    /**
     * Get borrower object of player
     * @param player Player
     * @return Borrower object
     */
    public static Borrower getBorrowerObject(Player player) {
        for (var borrower : PcConomy.getInstance().borrowerManager.borrowers)
            if (borrower.getBorrower().equals(player.getUniqueId())) return borrower;

        return null;
    }

    /**
     * Update or sets new borrower object of player
     * @param borrowerObject New borrower object
     */
    public static void setBorrowerObject(Borrower borrowerObject) {
        for (var borrower = 0; borrower < PcConomy.getInstance().borrowerManager.borrowers.size(); borrower++)
            if (PcConomy.getInstance().borrowerManager.borrowers.get(borrower).getBorrower().equals(borrowerObject.getBorrower()))
                PcConomy.getInstance().borrowerManager.borrowers.set(borrower, borrowerObject);
    }

    @Override
    public String getName() {
        return "borrowers_data";
    }
}
