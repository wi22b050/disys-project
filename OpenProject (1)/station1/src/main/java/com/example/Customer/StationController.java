package com.example.Customer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;


@RestController
@RequestMapping("/station")
public class StationController {



    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPass;

    private Connection dbConnect() throws SQLException {
        return DriverManager.getConnection(dbUrl+"?user="+dbUser+"&password="+dbPass);
    }

    @GetMapping("/{customerId}")
    public Double generateRepostForCustomer(@PathVariable("customerId") Integer customerId){

        try {
            Connection connection = dbConnect();
            String sql = "SELECT sum(kwh) as totalCharge FROM charge where customer_id="+customerId;
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("totalCharge");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return 0.0;
    }

}
