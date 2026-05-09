package br.com.tisaicore.controller;

import br.com.tisaicore.dto.request.SalesReportFilter;
import br.com.tisaicore.dto.response.SalesReportResponse;
import br.com.tisaicore.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private static final DateTimeFormatter FILE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/sales")
    public ResponseEntity<SalesReportResponse> sales(@ModelAttribute SalesReportFilter filter) {
        return ResponseEntity.ok(reportService.buildSalesReport(filter));
    }

    @GetMapping("/sales/pdf")
    public ResponseEntity<byte[]> salesPdf(@ModelAttribute SalesReportFilter filter) {
        byte[] pdf = reportService.buildSalesReportPdf(filter);
        String filename = "relatorio-vendas-" + filter.getStartDate().format(FILE_FMT)
                + "-a-" + filter.getEndDate().format(FILE_FMT) + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }
}
