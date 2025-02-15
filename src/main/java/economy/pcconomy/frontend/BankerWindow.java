package economy.pcconomy.frontend;

import economy.pcconomy.PcConomy;
import economy.pcconomy.backend.cash.Cash;
import economy.pcconomy.backend.cash.Balance;

import lombok.experimental.ExtensionMethod;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import org.j1sk1ss.itemmanager.manager.Manager;
import org.j1sk1ss.menuframework.objects.MenuSizes;
import org.j1sk1ss.menuframework.objects.MenuWindow;
import org.j1sk1ss.menuframework.objects.nonInteractive.Margin;
import org.j1sk1ss.menuframework.objects.interactive.components.Icon;
import org.j1sk1ss.menuframework.objects.interactive.components.Panel;
import org.j1sk1ss.menuframework.objects.interactive.components.ClickArea;
import org.j1sk1ss.menuframework.objects.interactive.components.LittleButton;


@ExtensionMethod({Manager.class, Cash.class})
public class BankerWindow {
    private static final MenuWindow BankWindow = new MenuWindow(
        List.of(
            new Panel(
                Arrays.asList(
                    new ClickArea(new Margin(4, 0, 1, 3),
                        (event, menu) -> {
                        /*
                        ============================================
                        Banker put action handler
                        ============================================
                         */
                            var player = (Player) event.getWhoClicked();
                            var option = event.getCurrentItem();
                            if (option == null) return;

                            if (option.getLoreLines().size() < 2) return;
                            var amount = option.getDoubleFromContainer("item-bank-value");

                            if (!PcConomy.getInstance().bankManager.getBank().takeCashFromPlayer(amount, player))
                                player.sendMessage("Увы, временно мы не можем удовлетворить ваш запрос.");

                            BankerWindow.regenerateWindow(player, event.getInventory());
                        }),
                    new ClickArea(new Margin(4, 5, 1, 3),
                        (event, menu) -> {
                        /*
                        ============================================
                        Banker withdraw action handler
                        ============================================
                         */
                            var player = (Player) event.getWhoClicked();
                            var option = event.getCurrentItem();
                            if (option == null) return;

                            if (option.getLoreLines().size() < 2) return;
                            var amount = option.getDoubleFromContainer("item-bank-value");

                            if (!PcConomy.getInstance().bankManager.getBank().giveCash2Player(amount, player))
                                player.sendMessage("Увы, временно мы не можем удовлетворить ваш запрос.");

                            BankerWindow.regenerateWindow(player, event.getInventory());
                        })
                ), "Банк", MenuSizes.SixLines, "\u10D0"
            )
        ), "Bank"
    );

    public static void generateWindow(Player player) {
        regenerateWindow(player, null);
    }

    private static void regenerateWindow(Player player, Inventory inventory) {
        var enableBalance   = PcConomy.getInstance().bankManager.getBank().getDayWithdrawBudget();
        var playerBalance   = Balance.getBalance(player);
        var cashInInventory = player.amountOfCashInInventory(true);
        var textBalance     = (Math.round(playerBalance * 100d) / 100d) + "";
        var charArray       = textBalance.toCharArray();

        var balance = printBalance(charArray, textBalance);
        var actions = printButtons(playerBalance, enableBalance, cashInInventory);

        var components = new ArrayList<org.j1sk1ss.menuframework.objects.interactive.Component>();
        components.addAll(balance);
        components.addAll(actions);
        
        if (inventory == null) BankWindow.getPanel("Банк").getViewWith(player, components);
        else {
            for (int i = 9; i < 54; i++) inventory.setItem(i, null);
            BankWindow.getPanel("Банк").getViewWith(components, inventory);
        }
    }

    // Print action buttons (Default + max-min actions)
    private static List<org.j1sk1ss.menuframework.objects.interactive.Component> printButtons(double playerBalance, double enableBalance, double cashInInventory) {
        var list = new ArrayList<org.j1sk1ss.menuframework.objects.interactive.Component>();

        if (playerBalance < enableBalance) {
            var withdrawMax = new LittleButton(new Margin(4, 5), "Снять максимум", "\n" + (Math.round(playerBalance * 100d) / 100d) + Cash.currencySigh, null, Material.GOLD_INGOT, 7002);
            withdrawMax.setDouble2Container(playerBalance, "item-bank-value");
            list.add(withdrawMax);  
        } 

        var putMax = new LittleButton(new Margin(4, 0), "Положить все средства", "\n-" + (Math.round(cashInInventory * 100d) / 100d) + Cash.currencySigh, null, Material.GOLD_INGOT, 7001);
        putMax.setDouble2Container(cashInInventory, "item-bank-value");
        list.add(putMax);        
        
        for (var i = 0; i < 8; i++) {
            if (enableBalance >= Cash.Denomination.get(i) && playerBalance >= Cash.Denomination.get(i)) list.addAll(printButtons("\n", 41, i));
            if (cashInInventory >= Cash.Denomination.get(i)) list.addAll(printButtons("\n-", 36, i));
        }

        return list;
    }

    // Print action buttons (default actions)
    private static List<org.j1sk1ss.menuframework.objects.interactive.Component> printButtons(String thing, int position, int enabled) {
        var list = new ArrayList<org.j1sk1ss.menuframework.objects.interactive.Component>();
        for (var j = enabled; j < 8; j++) {
            var button = new LittleButton(
                new Margin(j + (position + 5 * (j / 4)), 0, 0), "Действие",
                thing + Cash.Denomination.get(j) + Cash.currencySigh,
                null, Material.GOLD_INGOT,
                thing.equals("\n-") ? 7001 : 7002
            );

            button.setDouble2Container(Cash.Denomination.get(j), "item-bank-value");
            list.add(button);
        }

        return list;
    }

    // Print user balance
    private static List<org.j1sk1ss.menuframework.objects.interactive.Component> printBalance(char[] charArray, String textBalance) {
        var list = new ArrayList<org.j1sk1ss.menuframework.objects.interactive.Component>();
        for (var i = 9; i < Math.min(charArray.length + 9, 27); i++) {
            var currentChar = charArray[i - 9];
            if (currentChar == 'E') {
                list.add(new Icon(new Margin(i, 0, 0), "Баланс", textBalance + Cash.currencySigh, Material.GOLD_INGOT, 7014));
            }
            else if (currentChar == '.') {
                list.add(new Icon(new Margin(i, 0, 0), "Баланс", textBalance + Cash.currencySigh, Material.GOLD_INGOT, 7013));
            }
            else {
                list.add(new Icon(new Margin(i, 0, 0), "Баланс", textBalance + Cash.currencySigh, Material.GOLD_INGOT, 7003 + Character.getNumericValue(currentChar)));
            }
        }

        return list;
    }
}
