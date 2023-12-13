import javafx.application.Application;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.util.*;

import java.io.IOException;

public class CurrencyConverterMain extends Application
{
	public static void main(String[] args)
	{
		// Launch application
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception
	{
		// Load FXML 
		Parent root = FXMLLoader.load(getClass().getResource("MainView.fxml"));

		// Create scene
		Scene scene = new Scene(root);

		// Set scene and title of stage
		stage.setScene(scene);
		stage.setResizable(false);
		stage.setTitle("Currency Converter");

		// Show stage
		stage.show();
	}

	@Override
	public void stop()
	{
		System.out.println("Stop is called in Application class");
	}
}
