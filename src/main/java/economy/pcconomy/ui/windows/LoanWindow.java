package economy.pcconomy.ui.windows;

import economy.pcconomy.PcConomy;
import economy.pcconomy.bank.scripts.LoanWorker;
import economy.pcconomy.cash.scripts.CashWorker;
import economy.pcconomy.scripts.ItemWorker;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class LoanWindow {

    private final static int countOfAmountSteps = 9;
    private final static List<Integer> durationSteps = Arrays.asList(20, 30, 40, 50, 60, 70, 80, 90, 100);

    public static Inventory GetLoanWindow(Player player) {
        var window = Bukkit.createInventory(player, 27, "Кредит");

        for (var i = 0; i < countOfAmountSteps; i++) {
            window.setItem(i, GetAmountButton(i, 18, player));
            window.setItem(i + 18, ItemWorker.SetName(new ItemStack(Material.GREEN_STAINED_GLASS),
                    durationSteps.get(i) + "дней"));
        }

        return window;
    }

    public static Inventory GetLoanWindow(Inventory window, Player player, int option) {
        for (var i = 0; i < countOfAmountSteps; i++) {
            window.setItem(i, GetAmountButton(i, option, player));

            if (i == option - 18) continue;
            window.setItem(i + 18, ItemWorker.SetName(new ItemStack(Material.GREEN_STAINED_GLASS),
                    durationSteps.get(i) + "дней"));
        }
        return window;
    }

    private static ItemStack GetAmountButton(int i, int option, Player player) {
        var maxLoanSize = PcConomy.GlobalBank.GetUsefulAmountOfBudget() * 2;
        boolean isSafe = LoanWorker.isSafeLoan(maxLoanSize / (i + 1), durationSteps.get(option - 18), player);

        ItemStack tempItem = new ItemStack(Material.RED_WOOL, 1);
        tempItem = ItemWorker.SetName(tempItem, Math.round(maxLoanSize / (i + 1) * 100) / 100 + "$");

        if (isSafe) {
            tempItem = ItemWorker.SetLore(tempItem, "Банк одобрит данный займ.\nПроцент: " +
                    (Math.round(LoanWorker.getPercent(maxLoanSize / (i + 1),
                            durationSteps.get(option - 18)) * 100) * 100d) / 100d + "%");
            tempItem = ItemWorker.SetMaterial(tempItem, Material.GREEN_WOOL);
        } else {
            tempItem = ItemWorker.SetLore(tempItem, "Банк не одобрит данный займ.");
        }
        return tempItem;
    }

    public static int GetDuration(Inventory window) {
        for (ItemStack button:
             window) {
            if (button == null) return 20;
            if (ItemWorker.GetMaterial(button).equals(Material.PURPLE_WOOL)) {
                System.out.println(Integer.parseInt(ItemWorker.GetName(button).replace("дней", "")));
                return Integer.parseInt(ItemWorker.GetName(button).replace("дней", ""));
            }
        }

        return 20;
    }

    public static double GetAmount(Inventory window) {
        for (ItemStack button:
                window) {
            if (button == null) return 0;
            if (ItemWorker.GetMaterial(button).equals(Material.LIGHT_BLUE_WOOL))
                return Double.parseDouble(ItemWorker.GetName(button).replace(CashWorker.currencySigh, ""));
        }

        return 0;
    }
}
