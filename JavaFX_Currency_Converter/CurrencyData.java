public class CurrencyData {
	private Rates rates;
  	private String base;
  	private String timestamp;

  	// Getters
  	public Rates getRates() {
		return rates;
	}

	public String getBase() {
		return base;
	}
	
	public String getDate() {
		return timestamp;
	}

  	// Setters
  	public void setRates(Rates rates) {
    		this.rates = rates;
  	}

  	public void setBase(String base) {
    		this.base = base;
  	}

  	public void setDate(String timestamp) {
    		this.timestamp = timestamp;
  	}
}
