package application;
	


import java.util.List;

import com.trondelond.webscraper.WebScraper;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		
		final int FORMHEIGHT = 800;
		final int FORMWIDTH = 800;
		WebScraper ws = new WebScraper();
		
		try {
			GridPane root = new GridPane();
			
			root.setAlignment(Pos.CENTER);
			root.setHgap(10);
			root.setVgap(10);
			root.setPadding(new Insets(25,25,25,25));
			//root.setGridLinesVisible(true);
			
			//SET SCENE
			Text sceneTitle = new Text("Scriptalizer v0.1b");
			sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
			
			Label label2 = new Label("URL : ");
			
			List<String> urlList = ws.getScrapedUrls();
			ObservableList<String> options = FXCollections.observableArrayList(urlList);
			final ComboBox<String> comboBoxUrl = new ComboBox<String>(options);
			comboBoxUrl.getSelectionModel().select(0);
			TextField textFieldUrl = new TextField();
			textFieldUrl.setPromptText("Enter URL here");
			textFieldUrl.setText("http://vg.no");
			
			TextArea textFieldScript = new TextArea();

			textFieldScript.setPrefHeight(FORMHEIGHT-100);
			textFieldScript.setPrefWidth(FORMWIDTH-50);
			textFieldScript.setPromptText("Script will appear here");
			
			Label label3 = new Label("Scraped sites : ");
			
			Button btn = new Button();
			btn.setText("Gotime!");
			btn.setAlignment(Pos.BOTTOM_CENTER);
			//btn.getStyleClass().add("btn");
			btn.setId("btn1");
			btn.setOnAction(new EventHandler<ActionEvent>(){
			
	            @Override
	            public void handle(ActionEvent event) {
	            	String url = textFieldUrl.getText();
	            	
	            	if (comboBoxUrl.getValue() == "(scrape new webpage)"){
	            		System.out.println("Scrape new webpage!");
	            		String scrapeResult = ws.scrapeWebPage(url);
	            		System.out.println("scrapeResult : " + scrapeResult);
	            		if (scrapeResult == "OK") {
	            			textFieldScript.setText(ws.getWebPageFromDb(url));
	            		}
	            		else System.out.println("Scrape error : " + scrapeResult);
	            	}
	            	else {
	            		System.out.println("Show existing webpage!");
	            		textFieldScript.setText(ws.getWebPageFromDb(comboBoxUrl.getValue()));
	            	}
	            }
	        });

			root.add(sceneTitle, 0, 0, 2, 1);
			root.add(label2, 0, 1);
			root.add(textFieldUrl, 1,1);
			root.add(textFieldScript, 0,2,2,1);
			root.add(label3,0,3);
			root.add(comboBoxUrl,1,3);
			root.add(btn,0,4);
			
			Scene scene = new Scene(root,FORMHEIGHT,FORMWIDTH);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			
			textFieldUrl.requestFocus();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
