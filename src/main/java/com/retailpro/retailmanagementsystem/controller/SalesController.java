package com.retailpro.retailmanagementsystem.controller;

import com.retailpro.retailmanagementsystem.entity.Sale;
import com.retailpro.retailmanagementsystem.service.SalesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for Sales operations
 * Handles sales viewing and reporting
 */
@Controller
public class SalesController {

    @Autowired
    private SalesService salesService;

    /**
     * Display all sales
     */
    @GetMapping("/sales")
    public String listSales(Model model) {
        List<Sale> sales = salesService.getAllSales();
        model.addAttribute("sales", sales);
        return "sales/list";
    }

    /**
     * Display today's sales
     */
    @GetMapping("/sales/today")
    public String todaySales(Model model) {
        List<Sale> sales = salesService.getTodaySales();
        model.addAttribute("sales", sales);
        model.addAttribute("title", "Today's Sales");
        return "sales/list";
    }

    /**
     * Display sales between dates
     */
    @GetMapping("/sales/daterange")
    public String salesBetweenDates(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
                                     Model model) {
        List<Sale> sales = salesService.getSalesBetweenDates(startDate, endDate);
        model.addAttribute("sales", sales);
        model.addAttribute("title", "Sales from " + startDate + " to " + endDate);
        return "sales/list";
    }

    /**
     * View sale details
     */
    @GetMapping("/sales/view/{id}")
    public String viewSale(@PathVariable Long id, Model model) {
        Sale sale = salesService.getSaleById(id);
        model.addAttribute("sale", sale);
        return "sales/details";
    }
}
