package br.com.tisaicore.service;

import br.com.tisaicore.dto.response.SalesReportResponse;
import br.com.tisaicore.dto.response.SalesReportResponse.BrandBreakdown;
import br.com.tisaicore.dto.response.SalesReportResponse.CategoryBreakdown;
import br.com.tisaicore.dto.response.SalesReportResponse.ExpiringBatch;
import br.com.tisaicore.dto.response.SalesReportResponse.Kpis;
import br.com.tisaicore.dto.response.SalesReportResponse.OrderRow;
import br.com.tisaicore.dto.response.SalesReportResponse.StatusBreakdown;
import br.com.tisaicore.dto.response.SalesReportResponse.TopCustomer;
import br.com.tisaicore.dto.response.SalesReportResponse.TopProduct;
import br.com.tisaicore.entity.OrderStatus;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class SalesReportPdfService {

    private static final Locale BR = Locale.of("pt", "BR");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Font TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(33, 47, 79));
    private static final Font SECTION = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(33, 47, 79));
    private static final Font BODY = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
    private static final Font BODY_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
    private static final Font SMALL = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
    private static final Font KPI_LABEL = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY);
    private static final Font KPI_VALUE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(33, 47, 79));

    private static final Color HEADER_BG = new Color(33, 47, 79);
    private static final Color HEADER_FG = Color.WHITE;
    private static final Color ZEBRA = new Color(245, 247, 250);
    private static final Color KPI_BG = new Color(245, 247, 250);

    public byte[] render(SalesReportResponse report) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 48, 48);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            addHeader(doc, report);
            addKpis(doc, report.kpis());
            addStatusBreakdown(doc, report.statusBreakdown());
            addTopProducts(doc, report.topProducts());
            addTopCustomers(doc, report.topCustomers());
            addCategoryBreakdown(doc, report.categoryBreakdown());
            addBrandBreakdown(doc, report.brandBreakdown());
            addExpiringBatches(doc, report.expiringBatches());
            addOrders(doc, report.orders());

            doc.close();
            return baos.toByteArray();
        } catch (DocumentException | java.io.IOException e) {
            throw new RuntimeException("Falha ao gerar PDF do relatório de vendas", e);
        }
    }

    private void addHeader(Document doc, SalesReportResponse report) throws DocumentException {
        Paragraph title = new Paragraph("Relatório de Vendas", TITLE);
        title.setSpacingAfter(2f);
        doc.add(title);

        String period = "Período: " + DATE_FMT.format(report.periodStart())
                + " a " + DATE_FMT.format(report.periodEnd());
        String generated = "Gerado em " + DT_FMT.format(LocalDateTime.now());
        Paragraph sub = new Paragraph(period + "  •  " + generated, SMALL);
        sub.setSpacingAfter(12f);
        doc.add(sub);
    }

    private void addKpis(Document doc, Kpis k) throws DocumentException {
        section(doc, "Indicadores");

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingAfter(14f);

        addKpi(table, "Faturamento", money(k.totalRevenue()));
        addKpi(table, "Ticket médio", money(k.averageTicket()));
        addKpi(table, "Pedidos", String.valueOf(k.totalOrders()));
        addKpi(table, "Itens vendidos", String.valueOf(k.totalItems()));
        addKpi(table, "Pedidos pendentes", String.valueOf(k.pendingOrders()));
        addKpi(table, "Pedidos entregues", String.valueOf(k.deliveredOrders()));
        addKpi(table, "Cancelados", String.valueOf(k.cancelledOrders()));
        addKpi(table, "Receita perdida", money(k.cancelledRevenue()));

        doc.add(table);
    }

    private void addKpi(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(KPI_BG);
        cell.setBorderColor(new Color(220, 226, 234));
        cell.setBorderWidth(0.5f);
        cell.setPadding(8f);
        Paragraph lbl = new Paragraph(label.toUpperCase(BR), KPI_LABEL);
        Paragraph val = new Paragraph(value, KPI_VALUE);
        val.setSpacingBefore(2f);
        cell.addElement(lbl);
        cell.addElement(val);
        table.addCell(cell);
    }

    private void addStatusBreakdown(Document doc, List<StatusBreakdown> rows) throws DocumentException {
        section(doc, "Distribuição por status");
        PdfPTable t = newTable(new float[]{3f, 1.5f, 2.5f}, "Status", "Pedidos", "Total");
        int i = 0;
        for (StatusBreakdown r : rows) {
            Color bg = (i++ % 2 == 0) ? Color.WHITE : ZEBRA;
            addCell(t, statusLabel(r.status()), BODY, bg, Element.ALIGN_LEFT);
            addCell(t, String.valueOf(r.count()), BODY, bg, Element.ALIGN_RIGHT);
            addCell(t, money(r.revenue()), BODY, bg, Element.ALIGN_RIGHT);
        }
        doc.add(t);
    }

    private void addTopProducts(Document doc, List<TopProduct> rows) throws DocumentException {
        section(doc, "Top produtos");
        if (rows.isEmpty()) {
            doc.add(empty());
            return;
        }
        PdfPTable t = newTable(new float[]{0.6f, 5f, 1.5f, 2.5f}, "#", "Produto", "Qtd", "Receita");
        int i = 0;
        for (TopProduct r : rows) {
            Color bg = (i % 2 == 0) ? Color.WHITE : ZEBRA;
            addCell(t, String.valueOf(++i), BODY, bg, Element.ALIGN_CENTER);
            addCell(t, r.name(), BODY, bg, Element.ALIGN_LEFT);
            addCell(t, String.valueOf(r.quantity()), BODY, bg, Element.ALIGN_RIGHT);
            addCell(t, money(r.revenue()), BODY, bg, Element.ALIGN_RIGHT);
        }
        doc.add(t);
    }

    private void addTopCustomers(Document doc, List<TopCustomer> rows) throws DocumentException {
        section(doc, "Top clientes");
        if (rows.isEmpty()) {
            doc.add(empty());
            return;
        }
        PdfPTable t = newTable(new float[]{0.6f, 5f, 1.5f, 2.5f}, "#", "Cliente", "Pedidos", "Receita");
        int i = 0;
        for (TopCustomer r : rows) {
            Color bg = (i % 2 == 0) ? Color.WHITE : ZEBRA;
            addCell(t, String.valueOf(++i), BODY, bg, Element.ALIGN_CENTER);
            addCell(t, r.name(), BODY, bg, Element.ALIGN_LEFT);
            addCell(t, String.valueOf(r.orders()), BODY, bg, Element.ALIGN_RIGHT);
            addCell(t, money(r.revenue()), BODY, bg, Element.ALIGN_RIGHT);
        }
        doc.add(t);
    }

    private void addCategoryBreakdown(Document doc, List<CategoryBreakdown> rows) throws DocumentException {
        section(doc, "Vendas por categoria");
        if (rows.isEmpty()) {
            doc.add(empty());
            return;
        }
        PdfPTable t = newTable(new float[]{4f, 1.5f, 2.5f}, "Categoria", "Qtd", "Receita");
        int i = 0;
        for (CategoryBreakdown r : rows) {
            Color bg = (i++ % 2 == 0) ? Color.WHITE : ZEBRA;
            addCell(t, r.name(), BODY, bg, Element.ALIGN_LEFT);
            addCell(t, String.valueOf(r.quantity()), BODY, bg, Element.ALIGN_RIGHT);
            addCell(t, money(r.revenue()), BODY, bg, Element.ALIGN_RIGHT);
        }
        doc.add(t);
    }

    private void addBrandBreakdown(Document doc, List<BrandBreakdown> rows) throws DocumentException {
        section(doc, "Vendas por marca");
        if (rows.isEmpty()) {
            doc.add(empty());
            return;
        }
        PdfPTable t = newTable(new float[]{4f, 1.5f, 2.5f}, "Marca", "Qtd", "Receita");
        int i = 0;
        for (BrandBreakdown r : rows) {
            Color bg = (i++ % 2 == 0) ? Color.WHITE : ZEBRA;
            addCell(t, r.name(), BODY, bg, Element.ALIGN_LEFT);
            addCell(t, String.valueOf(r.quantity()), BODY, bg, Element.ALIGN_RIGHT);
            addCell(t, money(r.revenue()), BODY, bg, Element.ALIGN_RIGHT);
        }
        doc.add(t);
    }

    private void addExpiringBatches(Document doc, List<ExpiringBatch> rows) throws DocumentException {
        if (rows.isEmpty()) return;
        section(doc, "Lotes vencendo nos próximos 90 dias");
        PdfPTable t = newTable(new float[]{2f, 4f, 2f, 1.5f, 1.5f},
                "Lote", "Produto", "Validade", "Estoque", "Dias");
        int i = 0;
        for (ExpiringBatch r : rows) {
            Color bg = (i++ % 2 == 0) ? Color.WHITE : ZEBRA;
            addCell(t, r.code(), BODY, bg, Element.ALIGN_LEFT);
            addCell(t, r.productName(), BODY, bg, Element.ALIGN_LEFT);
            addCell(t, DATE_FMT.format(r.expirationDate()), BODY, bg, Element.ALIGN_CENTER);
            addCell(t, String.valueOf(r.currentQuantity()), BODY, bg, Element.ALIGN_RIGHT);
            addCell(t, String.valueOf(r.daysUntilExpiration()), BODY, bg, Element.ALIGN_RIGHT);
        }
        doc.add(t);
    }

    private void addOrders(Document doc, List<OrderRow> rows) throws DocumentException {
        section(doc, "Pedidos no período");
        if (rows.isEmpty()) {
            doc.add(empty());
            return;
        }
        PdfPTable t = newTable(new float[]{0.8f, 2.2f, 3.5f, 2.5f, 1.8f, 0.8f, 2f},
                "#", "Data", "Cliente", "Vendedor", "Status", "Itens", "Total");
        int i = 0;
        for (OrderRow r : rows) {
            Color bg = (i++ % 2 == 0) ? Color.WHITE : ZEBRA;
            addCell(t, String.valueOf(r.orderId()), BODY, bg, Element.ALIGN_CENTER);
            addCell(t, DT_FMT.format(r.createdAt()), BODY, bg, Element.ALIGN_LEFT);
            addCell(t, r.customer(), BODY, bg, Element.ALIGN_LEFT);
            addCell(t, r.seller(), BODY, bg, Element.ALIGN_LEFT);
            addCell(t, statusLabel(r.status()), BODY, bg, Element.ALIGN_LEFT);
            addCell(t, String.valueOf(r.itemCount()), BODY, bg, Element.ALIGN_RIGHT);
            addCell(t, money(r.totalAmount()), BODY, bg, Element.ALIGN_RIGHT);
        }
        doc.add(t);
    }

    private void section(Document doc, String text) throws DocumentException {
        Paragraph p = new Paragraph(text, SECTION);
        p.setSpacingBefore(6f);
        p.setSpacingAfter(6f);
        doc.add(p);
    }

    private PdfPTable newTable(float[] widths, String... headers) throws DocumentException {
        PdfPTable t = new PdfPTable(widths.length);
        t.setWidthPercentage(100);
        t.setWidths(widths);
        t.setSpacingAfter(14f);
        t.setHeaderRows(1);
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, HEADER_FG)));
            cell.setBackgroundColor(HEADER_BG);
            cell.setBorder(Rectangle.BOTTOM);
            cell.setBorderColor(HEADER_BG);
            cell.setPadding(6f);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            t.addCell(cell);
        }
        return t;
    }

    private void addCell(PdfPTable t, String text, Font font, Color bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "—", font));
        cell.setBackgroundColor(bg);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(new Color(220, 226, 234));
        cell.setBorderWidth(0.5f);
        cell.setPadding(5f);
        cell.setHorizontalAlignment(align);
        t.addCell(cell);
    }

    private Paragraph empty() {
        Paragraph p = new Paragraph("Sem dados no período.", SMALL);
        p.setSpacingAfter(10f);
        return p;
    }

    private String money(BigDecimal v) {
        if (v == null) v = BigDecimal.ZERO;
        return NumberFormat.getCurrencyInstance(BR).format(v);
    }

    private String statusLabel(OrderStatus s) {
        return switch (s) {
            case PENDING -> "Pendente";
            case CONFIRMED -> "Confirmado";
            case SHIPPED -> "Enviado";
            case DELIVERED -> "Entregue";
            case CANCELLED -> "Cancelado";
        };
    }
}
