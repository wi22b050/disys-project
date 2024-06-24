package at.technikum.javafx;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OrderController {

    private static final String BASE_URL = "http://localhost:7777/invoice/";

    @FXML
    private TextField customerId;

    @FXML
    private Label responseLabel;

    @FXML
    private Button downloadButton;

    @FXML
    private TextField drinkInput;

    @FXML
    private ListView<String> orderList;

    @FXML
    private void checkCustomerDetail() throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(BASE_URL + customerId.getText()))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if (response != null) {
            responseLabel.setText(response.body());
            responseLabel.setStyle("-fx-text-fill: red;");
            downloadButton.setVisible(true);
        }


    }

    public void downloadInvoice(ActionEvent actionEvent) throws URISyntaxException {
        String customer = customerId.getText();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(BASE_URL + customer))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpClient.newBuilder()
                .build()
                .sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    // Define the target directory and ensure it exists
                    String homeDir = System.getProperty("user.home");
                    Path targetDir = Paths.get(homeDir, "Downloads");
                    if (!Files.exists(targetDir)) {
                        try {
                            Files.createDirectories(targetDir);
                        } catch (IOException e) {
                            Platform.runLater(() -> {
                                responseLabel.setText("Error creating target directory: " + e.getMessage());
                            });
                            return;
                        }
                    }
                    String filename = "invoice_" + customer + ".pdf";
                    Path filePath = targetDir.resolve(filename);

                    try (FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {
                        outputStream.write(response);

                        Platform.runLater(() -> {
                            responseLabel.setText("Invoice downloaded successfully: " + filePath);
                            customerId.setText("");
                            downloadButton.setVisible(false);
                        });
                    } catch (IOException e) {
                        Platform.runLater(() -> {
                            responseLabel.setText("Error downloading invoice");
                        });
                    }
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        responseLabel.setText("Error: " + throwable.getMessage());
                    });
                    return null;
                });
    }


}
