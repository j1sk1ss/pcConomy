package economy.pcconomy;

/*
 Что работает:
 - Купюры, банкноты и подобное. Все методы для работы с ней в "Cash"
 - Банк. Печатка денег, кредитование, расчёт процентов, взятие процентов
 - Towny. А именно снятие со счёта через город и пополнение счёта через город. Взаимосвязанно с "Cash"
 - Обьект города. См. папку "town"
 - Торговля меж игроками. Создание торговцев, настройка
 - Лицензии на торговлю со стороны мэра и со стороны игрока
 - НПС торговцы игроковских городов
  - Аренда на один день
 - Кредиторы игроковских городов
 - Сохранение данных
  - Города
  - Банк
   - Кредиты
  - НПС

 Чего нету:

 */

import economy.pcconomy.backend.bank.scripts.BorrowerManager;

import economy.pcconomy.backend.license.scripts.LicenseWorker;
import economy.pcconomy.backend.link.Manager;
import economy.pcconomy.backend.npc.NPC;
import economy.pcconomy.backend.npc.listener.NPCLoader;
import economy.pcconomy.backend.placeholderapi.PcConomyPAPI;
import economy.pcconomy.backend.save.Loader;
import economy.pcconomy.backend.town.listener.TownyListener;
import economy.pcconomy.backend.bank.Bank;
import economy.pcconomy.backend.town.scripts.TownWorker;

import economy.pcconomy.frontend.ui.windows.bank.BankerListener;
import economy.pcconomy.frontend.ui.windows.license.LicensorListener;
import economy.pcconomy.frontend.ui.windows.loan.LoanListener;
import economy.pcconomy.frontend.ui.windows.loan.NPCLoanerListener;
import economy.pcconomy.frontend.ui.windows.mayor.MayorListener;
import economy.pcconomy.frontend.ui.windows.npcTrade.NPCTraderListener;
import economy.pcconomy.frontend.ui.windows.trade.TraderListener;

import me.yic.xconomy.api.XConomyAPI;

import org.bukkit.Bukkit;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public final class PcConomy extends JavaPlugin { // Гл класс плагина. Тут обьявляйте в статике нужные API
    // Так же желательно тут регистрировать Listeners
    // Ну и обработчики команд с командами тоже bruh
    public static FileConfiguration Config;
    public static XConomyAPI xConomyAPI;
    public static NPC GlobalNPC;

    public static Bank GlobalBank;
    public static BorrowerManager globalBorrowerManager;
    public static TownWorker GlobalTownWorker;
    public static LicenseWorker GlobalLicenseWorker;

    @Override
    public void onEnable() {
        saveConfig();

        Config               = PcConomy.getPlugin(PcConomy.class).getConfig();
        GlobalBank           = new Bank();
        globalBorrowerManager = new BorrowerManager();
        GlobalTownWorker     = new TownWorker();
        GlobalLicenseWorker  = new LicenseWorker();

        try {
            if (new File("NPCData.txt").exists())
                GlobalNPC = Loader.LoadNPC("NPCData");
            if (new File("BankData.txt").exists())
                GlobalBank = Loader.LoadBank("BankData");
            if (new File("TownsData.txt").exists())
                GlobalTownWorker = Loader.LoadTowns("TownsData");
            if (new File("LicenseData.txt").exists())
                GlobalLicenseWorker = Loader.LoadLicenses("LicenseData");
            if (new File("BorrowersData.txt").exists())
                globalBorrowerManager = Loader.LoadBorrowers("BorrowersData");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var listeners = Arrays.asList(new NPCLoader(), new LoanListener(), new TownyListener(),
                new MayorListener(), new BankerListener(), new TraderListener(), new LicensorListener(),
                new NPCTraderListener(), new NPCLoanerListener());

        for (Listener listener:
             listeners) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }

        xConomyAPI  = new XConomyAPI(); // Общий API XConomy этого плагина. Брать только от сюда
        var manager = new Manager(); // Обработчик тестовых комманд

        getCommand("take_cash").setExecutor(manager);
        getCommand("create_cash").setExecutor(manager);
        getCommand("reload_towns").setExecutor(manager);
        getCommand("save_data").setExecutor(manager);
        getCommand("put_cash_to_bank").setExecutor(manager);
        getCommand("create_banker").setExecutor(manager);
        getCommand("create_loaner").setExecutor(manager);
        getCommand("create_trader").setExecutor(manager);
        getCommand("create_npc_trader").setExecutor(manager);
        getCommand("create_licensor").setExecutor(manager);
        getCommand("switch_town_to_npc").setExecutor(manager);
        getCommand("town_menu").setExecutor(manager);
        getCommand("add_trade_to_town").setExecutor(manager);
        getCommand("reload_npc").setExecutor(manager);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) { // Регистрация PAPI
        	new PcConomyPAPI().register();
        }

    }

    public static void SaveData() {
        try {
            GlobalNPC.SaveNPC("NPCData");
            GlobalBank.SaveBank("BankData");
            GlobalTownWorker.SaveTown("TownsData");
            GlobalLicenseWorker.SaveLicenses("LicenseData");
            globalBorrowerManager.SaveBorrowers("BorrowersData");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() { // Тут будет сохранение всего и вся. Делайте не каскадом из 9999 строк, а разбейте
        // на разные классы. Но кого я учу, верно?
        SaveData();
    }
}
