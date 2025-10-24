package com.spring.fit.backend.report.service;

import com.spring.fit.backend.order.domain.entity.Order;
import java.io.ByteArrayOutputStream;
import java.util.List;

public interface PdfReportService {
    ByteArrayOutputStream generateDailyReportPdf(List<Order> orders);
    ByteArrayOutputStream testSimplePdfGeneration();
}
