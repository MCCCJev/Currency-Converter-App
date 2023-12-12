import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Currency;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class Main {

	public static void main(String[] args) throws IOException, InterruptedException {
		String apiKey = "USE YOUR API KEY"; // Replace with your actual API key
		CurrencyData currencyData = getCurrencyData(apiKey);
		
		System.out.println("Base currency: " + currencyData.getBase());
		System.out.println("Timestamp: " + currencyData.getDate());

    		Rates rates = currencyData.getRates();
	        // This is just for testing, I'm not typing these for every single rate
    		System.out.println("USD exchange rate: " + rates.getUSD());
    		System.out.println("EUR exchange rate: " + rates.getEUR());
    		System.out.println("JPY exchange rate: " + rates.getJPY());

	}

	private static CurrencyData getCurrencyData(String apiKey) throws IOException, InterruptedException {
		String url = "https://openexchangerates.org/api/latest.json?app_id=" + apiKey;
    		HttpClient client = HttpClient.newHttpClient();
    		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
    		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    		String jsonString = response.body();
		
		if (response.statusCode() != 200) {
			throw new IOException("API request failed with status code: " + response.statusCode());
		}

			Gson gson = new GsonBuilder().create();
			return gson.fromJson(jsonString, CurrencyData.class);
	}
}
