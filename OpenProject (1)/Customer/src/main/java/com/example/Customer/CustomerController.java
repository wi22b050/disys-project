package com.example.Customer;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;



@RestController
@RequestMapping("/invoice")
public class CustomerController {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPass;

    private Connection dbConnect() throws SQLException {
        return DriverManager.getConnection(dbUrl + "?user=" + dbUser + "&password=" + dbPass);
    }

//    private final static String BROKER_URL = "localhost";

    @GetMapping("/{customerId}")
    public String generatingDataCollectionJob(@PathVariable("customerId") Integer customerId) throws SQLException {

        if (customerId != null) {
            try {
                Connection connection = dbConnect();
                String sql = "SELECT * FROM customer WHERE id=" + customerId;
                PreparedStatement preparedStatement = connection.prepareStatement(sql);

                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {

                    //DataCollectionDispatcher
                    String url = "http://localhost:7778/stations/" + customerId;
                    String totalCharge = callApi(url);
                    if (totalCharge != null) {

                        return "Data Collection presses started " + totalCharge;
                    }
                    return "Data Collection presses started";
                } else {
                    return "No Customer data found";
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return "something went wrong";
            }

        }
        return "No Customer data found";
    }

    private String callApi(String url) throws IOException {
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

    @PostMapping("/{customerId}")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable String customerId) throws IOException {

        CustomerDetail detail = new CustomerDetail();

        try {
            Connection connection = dbConnect();
            String sql = "SELECT * FROM customer WHERE id=" + customerId;
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                //TODO call API and Dump data
                String url = "http://localhost:7778/stations/" + customerId;
                String totalCharge = callApi(url);
                if (totalCharge != null) {
                    // todo store data in data base
                    detail.setId(Integer.valueOf(customerId));
                    detail.setFirstName(resultSet.getString("first_name"));
                    detail.setLastName(resultSet.getString("last_name"));
                    detail.setTotalCharge(Double.valueOf(totalCharge));
                }

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());

        }

        byte[] pdfBytes = generateInvoice(detail);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=invoice_" + customerId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);

    }

    public byte[] generateInvoice(CustomerDetail detail) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(byteArrayOutputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Fonts
        PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // Company Name
        document.add(new Paragraph("EcoCharge Solutions")
                .setFont(bold)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.LEFT));

        // Company details
        document.add(new Paragraph("Höchstädtplatz 6\nPhone: (123) 987-654321")
                .setFont(regular)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.LEFT));

        // Invoice title and details table
        Table invoiceDetails = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth();

        invoiceDetails.addCell(createCell("INVOICE", bold, 20, TextAlignment.RIGHT, 2, ColorConstants.LIGHT_GRAY));
        invoiceDetails.addCell(createCell("INVOICE #", bold, 10, TextAlignment.RIGHT, ColorConstants.LIGHT_GRAY));
        invoiceDetails.addCell(createCell("[123456]", regular, 10, TextAlignment.RIGHT));
        invoiceDetails.addCell(createCell("DATE", bold, 10, TextAlignment.RIGHT, ColorConstants.LIGHT_GRAY));
        invoiceDetails.addCell(createCell(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyy HH:mm:ss")), regular, 10, TextAlignment.RIGHT));

        document.add(invoiceDetails);

        // Add spacing
        document.add(new Paragraph("\n"));

        // Bill to section
        document.add(new Paragraph("BILL TO")
                .setFont(bold)
                .setFontSize(12)
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.LEFT));
        document.add(new Paragraph(detail.getFirstName() + " "+detail.getLastName()+"\nCustomer Id : "+detail.getId())
                .setFont(regular)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.LEFT));

        // Add spacing
        document.add(new Paragraph("\n"));

        // Description table
        Table descriptionTable = new Table(UnitValue.createPercentArray(new float[]{4, 1}))
                .useAllAvailableWidth();

        descriptionTable.addHeaderCell(createCell("DESCRIPTION", bold, 10, TextAlignment.LEFT, ColorConstants.LIGHT_GRAY));
        descriptionTable.addHeaderCell(createCell("-", bold, 10, TextAlignment.RIGHT, ColorConstants.LIGHT_GRAY));

        descriptionTable.addCell(createCell("Total Charge in kwh", regular, 10, TextAlignment.LEFT));
        descriptionTable.addCell(createCell(detail.getTotalCharge().toString(), regular, 10, TextAlignment.RIGHT));


        document.add(descriptionTable);

        // Add spacing
        document.add(new Paragraph("\n"));

        // Total table
        Table totalTable = new Table(UnitValue.createPercentArray(new float[]{4, 1}))
                .useAllAvailableWidth();
        totalTable.addCell(createCell("Thank you for your loyalty!", bold, 10, TextAlignment.LEFT, 1, ColorConstants.LIGHT_GRAY));
        totalTable.addCell(createCell("TOTAL", bold, 10, TextAlignment.RIGHT, ColorConstants.LIGHT_GRAY));
        totalTable.addCell(createCell("Total Payment (5$/kwh) ", bold, 10, TextAlignment.LEFT));
        totalTable.addCell(createCell(String.valueOf((detail.getTotalCharge()*5)), bold, 10, TextAlignment.RIGHT));

        document.add(totalTable);

        // Add spacing
        document.add(new Paragraph("\n"));

        // Footer
        document.add(new Paragraph("If you have any questions about this invoice, please contact\n[Sara Azzam, Jelena Kocic, Boni Vircheva, ecocharge@hotmail.com]")
                .setFont(regular)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER));

        document.close();

        return byteArrayOutputStream.toByteArray();
    }

    private Cell createCell(String content, PdfFont font, int fontSize, TextAlignment alignment) {
        return new Cell()
                .add(new Paragraph(content)
                        .setFont(font)
                        .setFontSize(fontSize))
                .setTextAlignment(alignment)
                .setPadding(5);
    }

    private Cell createCell(String content, PdfFont font, int fontSize, TextAlignment alignment, Color backgroundColor) {
        return new Cell()
                .add(new Paragraph(content)
                        .setFont(font)
                        .setFontSize(fontSize))
                .setTextAlignment(alignment)
                .setBackgroundColor(backgroundColor)
                .setPadding(5);
    }

    private Cell createCell(String content, PdfFont font, int fontSize, TextAlignment alignment, int colspan, Color backgroundColor) {
        Cell cell = new Cell(1, colspan)
                .add(new Paragraph(content)
                        .setFont(font)
                        .setFontSize(fontSize))
                .setTextAlignment(alignment)
                .setBackgroundColor(backgroundColor)
                .setPadding(5);
        return cell;
    }



}
