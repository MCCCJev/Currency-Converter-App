import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import java.util.Map;
import java.util.Locale;
import java.util.prefs.Preferences;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;
import java.text.*;

import com.google.gson.Gson;

public class CurrencyConverterController {

	@FXML
    	private ComboBox<String> fromComboBox;
	@FXML
	private ComboBox<String> toComboBox;
	@FXML
	private TextField amountTextField;
	@FXML
	private TextField convertedAmountTextField;
	@FXML
	private Button convertButton;

	public static final String FROM_CURRENCY_KEY = "user.preferred.from.currency";
	public static final String TO_CURRENCY_KEY = "user.preferred.to.currency";

	public double amount = 0.0;

	@FXML
	public void initialize() {
		// Populate comboboxes with currencies
		populateComboBoxes();

		// Update preferred currency based on saved values
		Preferences prefs = Preferences.userRoot().node("currency_converter_settings");
		String savedFromCurrency = prefs.get(FROM_CURRENCY_KEY, Locale.getDefault().getCountry());
		String savedToCurrency = prefs.get(TO_CURRENCY_KEY, Locale.getDefault().getCountry());
		fromComboBox.getSelectionModel().select(savedFromCurrency);
		toComboBox.getSelectionModel().select(savedToCurrency);

		// Hande currency selection changes
		fromComboBox.setOnAction(event -> {
			String selectedCurrency = fromComboBox.getSelectionModel().getSelectedItem();
			prefs.put(FROM_CURRENCY_KEY, selectedCurrency);
		});
		toComboBox.setOnAction(event -> {
			String selectedCurrency = toComboBox.getSelectionModel().getSelectedItem();
			prefs.put(TO_CURRENCY_KEY, selectedCurrency);
		});

		// Text listener for amount input
		amountTextField.textProperty().addListener((observable, oldValue, newValue) -> {
			try {
				amount = Double.parseDouble(newValue);
			} catch (NumberFormatException e) {
				System.err.println("Invalid amount entered: " + e.getMessage());
			}
		});
    		
		// Handle convert button click
		convertButton.setOnAction(event -> {
		    try {
			convertCurrency();
		    } catch (Exception e) {
			// Handle conversion errors
			System.err.println("Error converting currency: " + e.getMessage());
		    }
		});
	    }
	    private void populateComboBoxes() {
		    // URL for OpenExchangeRates API to get latest currencies
		    String url = "https://openexchangerates.org/api/currencies.json";
		    
		    try (HttpClient client = HttpClient.newHttpClient()) {
			    HttpRequest request = HttpRequest.newBuilder()
			    .GET()
			    .uri(URI.create(url))
			    .build();
			    
			    client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
			    .thenApply(HttpResponse::body)
			    .whenComplete((response, error) -> {
				    if (error != null) {
					    System.err.println("Error fetching currencies: " + error.getMessage());
					    return;
					    }

					    Map<String, String> currenciesMap = new Gson().fromJson(response, Map.class);

					    Platform.runLater(() -> {
						    // Add new currency list to comboboxes
						    fromComboBox.getItems().addAll(currenciesMap.keySet());
						    toComboBox.getItems().addAll(currenciesMap.keySet());
			    });
		    });
			    } catch (Exception e) {
				    System.err.println("Error fetching currencies: " + e.getMessage());
			    }
	    }
	    private void convertCurrency() {
		    String fromCurrency = fromComboBox.getSelectionModel().getSelectedItem();
		    String toCurrency = toComboBox.getSelectionModel().getSelectedItem();
		    
		    // Replace "YOUR_API_KEY" with your actual API key
		    String apiKey = System.getenv("OPENEXCHANGERATES_API_KEY");
		    // Build the URL for the conversion rate API call
		    String url = "https://openexchangerates.org/api/latest.json?base=" + fromCurrency + "&symbols=" + toCurrency + "&app_id=" + apiKey;

		    try (HttpClient client = HttpClient.newHttpClient()) {
			    HttpRequest request = HttpRequest.newBuilder()
			    .GET()
			    .uri(URI.create(url))
			    .build();
			    client.sendAsync(request, HttpResponse.BodyHandlers.ofString())

			    .thenApply(HttpResponse::body)
			    .whenComplete((response, error) -> {
				    if (error != null) {
					    System.err.println("Error fetching conversion rate: " + error.getMessage());
					    return;
				    }

				    try {
					    Map<String, Double> ratesMap = new Gson().fromJson(response, Map.class);
					    double conversionRate = ratesMap.get(toCurrency);

					    if (conversionRate == 0.0) {
						    System.err.println("Invalid conversion rate for " + fromCurrency + " to " + toCurrency);
					    }
					    
					    double convertedAmount = amount * conversionRate;
					    Platform.runLater(() -> {
						    convertedAmountTextField.setText(String.format("%.2f", convertedAmount));
				    });
			    } catch (NumberFormatException e) {
				    System.err.println("Invalid amount entered: " + e.getMessage());
			    }
		    });
		    } catch (Exception e) {
			    System.err.println("Error fetching conversion rate: " + e.getMessage());
		    }
	    }
}
