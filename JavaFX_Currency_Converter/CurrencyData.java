public class CurrencyData {
    private String currency;
    private double rate;

    public CurrencyData(String currency, double rate) {
        this.currency = currency;
        this.rate = rate;
    }

    public String getCurrency() {
        return currency;
    }

    public double getRate() {
        return rate;
    }

    @Override
    public String toString() {
        return currency + " (" + rate + ")";
    }
}
