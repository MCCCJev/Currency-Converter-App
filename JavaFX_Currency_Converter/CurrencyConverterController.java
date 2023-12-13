import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import java.util.Map;
import java.util.prefs.Preferences;
import javafx.application.Platform;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;

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

	@FXML
	public void initialize() {
		// Populate comboboxes with currencies
		populateComboBoxes();

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
		    String url = "https://openexchangerates.org/api/currencies?app_id=YOUR_API_KEY";
		    // Replace "YOUR_API_KEY" with your actual API key
		    String apiKey = System.getenv("OPENEXCHANGERATES_API_KEY");
		    
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
					    
					    fromComboBox.getItems().addAll(currenciesMap.keySet());
					    toComboBox.getItems().addAll(currenciesMap.keySet());

					    fromComboBox.getSelectionModel().selectFirst();
					    toComboBox.getSelectionModel().select(1); // assuming second item is a different currency
					    });
			    } catch (Exception e) {
				    System.err.println("Error fetching currencies: " + e.getMessage());
			    }
	    }
	    private void convertCurrency() {
		    double amount = Double.parseDouble(amountTextField.getText());
		    String fromCurrency = fromComboBox.getSelectionModel().getSelectedItem();
		    String toCurrency = toComboBox.getSelectionModel().getSelectedItem();

		    // Replace "YOUR_API_KEY" with your actual API key
		    String apiKey = System.getenv("OPENEXCHANGERATES_API_KEY");

		    // Build the URL for the conversion rate API call
		    String url = "https://openexchangerates.org/api/latest?base=" + fromCurrency + "&symbols=" + toCurrency + "&app_id=" + apiKey;

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

				    Map<String, Double> ratesMap = new Gson().fromJson(response, Map.class);
				    
				    double conversionRate = ratesMap.get(toCurrency);
				    double convertedAmount = amount * conversionRate;
				    convertedAmountTextField.setText(String.format("%.2f", convertedAmount));
			    });

		    } catch (Exception e) {
			    System.err.println("Error fetching conversion rate: " + e.getMessage());
		    }
	    }
}
