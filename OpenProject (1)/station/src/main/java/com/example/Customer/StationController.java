package com.example.Customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;


@RestController
@RequestMapping("/stations")
public class StationController {


    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPass;

    @Autowired
    private Producer producer;

    @Autowired
    private Consumer consumer;


    private Connection dbConnect() throws SQLException {
        return DriverManager.getConnection(dbUrl + "?user=" + dbUser + "&password=" + dbPass);
    }

    @GetMapping("/{customerId}")
    public Double generateRepostForCustomer(@PathVariable("customerId") String customerId) {

        Double totalCharges = 0.0;
        try {
            Connection connection = dbConnect();
            String sql = "SELECT * FROM station";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            ResultSet resultSet = preparedStatement.executeQuery();

            //DataCollectionReceiver
            while (resultSet.next()) {
                String url = "http://" + resultSet.getString("db_url") + "/station/" + customerId;
                String totalCharge = StationDataCollector(url);
                if (totalCharge != null) {
                    totalCharges = Double.parseDouble(totalCharge) + totalCharges;
                    producer.sendMessage(totalCharge);
                }

            }
            return totalCharges;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        consumer.processMessage(String.valueOf(totalCharges));
        return 0.0;
    }


    private String StationDataCollector(String url) throws IOException {
        URL urlObj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
        connection.setRequestMethod("GET");
        try {
            if (connection.getResponseCode() != 200) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder responseBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            responseBuilder.append(line).append("\n");
        }
        reader.close();

        return responseBuilder.toString();
    }

}
