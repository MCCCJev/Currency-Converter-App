import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
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
import java.net.URLEncoder;
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
		

    	
		// Handle convert button click
		convertButton.setOnAction(event -> {
		    try {
			String amountText = amountTextField.getText();
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
	    private void convertCurrency() throws IOException {
		    String fromCurrency = fromComboBox.getSelectionModel().getSelectedItem();
		    String toCurrency = toComboBox.getSelectionModel().getSelectedItem();	    
		    // Replace "YOUR_API_KEY" with your actual API key
		    String apiKey = System.getenv("OPENEXCHANGERATES_API_KEY");
		    // Build the URL for the conversion rate API call
		    String url = "https://openexchangerates.org/api/latest.json?base=" + fromCurrency + "&symbols=" + toCurrency + "&app_id=" + apiKey;
		    HttpClient client = HttpClient.newHttpClient();
		    HttpRequest request = HttpRequest.newBuilder()
		    .GET()
		    .uri(URI.create(url))
		    .build();
			    
			    client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
			    .thenApply(HttpResponse::body)
			    .whenComplete((response, error) -> {
				    if (error != null) {
					    // Handle network error with Alert
					    Platform.runLater(() -> {
						    Alert alert = new Alert(Alert.AlertType.ERROR);
						    alert.setTitle("Error");
						    alert.setHeaderText("Network Error");
						    alert.setContentText("Unable to connect to the currency exchange service. Please check your internet connection and try again.");
						    alert.showAndWait();
					    });
					    return;
				    }

				    try {
					    // Parse JSON response to CurrencyRates object
					    Map<String, Object> responseMap = new Gson().fromJson(response, Map.class);

					    // Extract rates map
					    Map<String, Double> ratesMap = (Map<String, Double>) responseMap.get("rates");

					     // Check if requested currency rate exists
					     if (!ratesMap.containsKey(toCurrency)) {
						     Platform.runLater(() -> {
							     Alert alert = new Alert(Alert.AlertType.ERROR);
							     alert.setTitle("Error");
							     alert.setHeaderText("Rate Unavailable");
							     alert.setContentText("Unable to obtain the exchange rate for the selected currency. Please try again later.");
							     alert.showAndWait();
						     });
						     return;
					     }

					     // Get and apply conversion rate
					     double amount = Double.parseDouble(amountTextField.getText());
					     double rate = ratesMap.get(toCurrency);

					     // Update UI with converted amount
					     Platform.runLater(() -> {
						     convertedAmountTextField.setText(String.format("%.2f", amount * rate));
					     });
				    } catch (Exception e) {
					    // Handle parsing or conversion errors
					    Platform.runLater(() -> {
						    Alert alert = new Alert(Alert.AlertType.ERROR);
						    alert.setTitle("Error");
						    alert.setHeaderText("Conversion Error");
						    alert.setContentText("An error occurred while converting the amount. Please ensure you entered a valid number.");
						    alert.showAndWait();
					    });
				    }
			    });
	    }
}
