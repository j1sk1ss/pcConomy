package economy.pcconomy.backend.economy.credit;

import economy.pcconomy.PcConomy;
import economy.pcconomy.backend.economy.Capitalist;
import economy.pcconomy.backend.scripts.BalanceManager;
import economy.pcconomy.backend.scripts.PlayerManager;

import lombok.experimental.ExtensionMethod;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.UUID;


@ExtensionMethod({BalanceManager.class})
public class Loan {
    /**
     * Loan object
     * @param amount Amount what was got as loan
     * @param percentage Percent of loan
     * @param duration Duration of loan
     * @param dayPayment Daily payment
     * @param player Loan owner
     */
    public Loan(double amount, double percentage, int duration, double dayPayment, Player player) {
        this.amount       = amount;
        this.percentage   = percentage;
        this.duration     = duration;
        this.dailyPayment = dayPayment;

        Owner = player.getUniqueId();
    }

    public final UUID Owner;
    public double amount;
    public final double percentage;
    public final int duration;
    public final double dailyPayment;
    public int expired;
    public static double trustCoefficient = .5d; // 1.5f

    /**
     * Add loan to loan owner (Pays starts)
     * @param loanOwner LoanOwner (For example Bank)
     */
    public void addLoan(Capitalist loanOwner) {
        loanOwner.getCreditList().add(this);
        loanOwner.changeBudget(-amount);

        Bukkit.getPlayer(Owner).giveMoney(amount);
    }

    /**
     * Gets percent of current loan
     * @param amount Amount of loan
     * @param duration Duration of loan
     * @return Percent of this loan
     */
    public static double getPercent(double amount, double duration) {
        return Math.round((PcConomy.GlobalBank.DayWithdrawBudget / (amount * duration)) * 1000d) / 1000d;
    }

    /**
     * Gets daily payment of current loan
     * @param amount Amount of loan
     * @param duration Duration of loan
     * @return Daily payment of this loan
     */
    public static double getDailyPayment(double amount, double duration, double percent) {
        return (amount + amount * (percent / 100d)) / duration;
    }

    /**
     * Gets safety factor of current loan
     * @param amount Amount of loan
     * @param duration Duration of loan
     * @return safety factor of this loan
     */
    public static double getSafetyFactor(double amount, int duration, Borrower borrower) {
        var expired = 0;
        if (borrower == null) return ((duration / 100d)) / (expired + (amount / PcConomy.GlobalBank.DayWithdrawBudget));
        for (var loan : borrower.CreditHistory) expired += loan.expired;
        return (borrower.CreditHistory.size() + (duration / 100d)) / (expired + (amount / PcConomy.GlobalBank.DayWithdrawBudget));
    }

    /**
     * Loan status
     * @param loanAmount Amount of loan
     * @param duration Duration of loan
     * @param borrower Borrower who wants to take loan
     * @return Loan status for this borrower
     */
    public static boolean isSafeLoan(double loanAmount, int duration, Player borrower) {
        return (getSafetyFactor(loanAmount, duration, PcConomy.GlobalBorrowerManager.getBorrowerObject(borrower)) >= trustCoefficient
                && blackTown(PlayerManager.getCountryMens(borrower.getUniqueId()))
                && PlayerManager.getPlayerServerDuration(borrower) > 100);
    }

    /**
     * Checks if town have player, that not pay credit
     * @param uuids UUID of players from town
     * @return Status of town
     */
    public static boolean blackTown(List<UUID> uuids) {
        return uuids.parallelStream().anyMatch(uuid -> {
            var loan = getLoan(uuid, PcConomy.GlobalBank);
            return loan != null && loan.expired > 5;
        });
    }

    /**
     * Create loan object
     * @param amount Amount of loan
     * @param duration Duration of loan
     * @param player Player who takes loan
     */
    public static Loan createLoan(double amount, int duration, Player player) {
        var percentage   = getPercent(amount, duration);
        var dailyPayment = getDailyPayment(amount, duration, percentage);
        return new Loan(amount + amount * percentage, percentage, duration, dailyPayment, player);
    }

    /**
     * Pay for all debt
     * @param player Player who close loan
     * @param creditOwner Credit owner
     */
    public static void payOffADebt(Player player, Capitalist creditOwner) {
        var loan = getLoan(player.getUniqueId(), creditOwner);

        if (loan == null) return;
        if (player.solvent(loan.amount)) return;

        player.takeMoney(loan.amount);
        creditOwner.changeBudget(loan.amount);
        destroyLoan(player.getUniqueId(), creditOwner);
    }

    /**
     * Take percent from all borrowers
     * @param moneyTaker Money taker
     */
    public static void takePercentFromBorrowers(Capitalist moneyTaker) {
        for (var loan: moneyTaker.getCreditList()) {
            var owner = Bukkit.getPlayer(loan.Owner);
            if (owner == null) return;
            if (loan.amount <= 0) {
                destroyLoan(loan.Owner, moneyTaker);
                return;
            }

            if (owner.solvent(loan.dailyPayment)) {
                loan.expired += 1;
                continue;
            }

            owner.takeMoney(loan.dailyPayment);
            loan.amount -= loan.dailyPayment;

            moneyTaker.changeBudget(loan.dailyPayment);
        }
    }

    /**
     * Get loan by UUID
     * @param player Player UUID
     * @param creditOwner Credit owner
     * @return Loan object
     */
    public static Loan getLoan(UUID player, Capitalist creditOwner) {
        for (var loan: creditOwner.getCreditList())
            if (loan.Owner.equals(player)) return loan;

        return null;
    }

    /**
     * Destroy loan from credit owner`s list by player UUID
     * @param player Player UUID
     * @param creditOwner Credit owner
     */
    public static void destroyLoan(UUID player, Capitalist creditOwner) {
        var credit   = creditOwner.getCreditList();
        var loan     = getLoan(player, creditOwner);
        var borrower = PcConomy.GlobalBorrowerManager.getBorrowerObject(Bukkit.getPlayer(player));

        if (borrower != null) {
            borrower.CreditHistory.add(loan);
            PcConomy.GlobalBorrowerManager.setBorrowerObject(borrower);
        } else PcConomy.GlobalBorrowerManager.borrowers.add(new Borrower(Objects.requireNonNull(Bukkit.getPlayer(player)), loan));

        credit.remove(getLoan(player, creditOwner));
    }
}
