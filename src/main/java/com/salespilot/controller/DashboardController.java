package com.salespilot.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.salespilot.entity.Deal;
import com.salespilot.repository.CustomerRepository;
import com.salespilot.repository.DealRepository;

@Controller
public class DashboardController {

    private final CustomerRepository customerRepository;
    private final DealRepository dealRepository;

    public DashboardController(
            CustomerRepository customerRepository,
            DealRepository dealRepository) {

        this.customerRepository = customerRepository;
        this.dealRepository = dealRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        // 登録顧客数
        long customerCount = customerRepository.count();

        // 商談件数
        long dealCount = dealRepository.count();

        // 商談データをすべて取得
        List<Deal> deals = dealRepository.findAll();
        

     // ステータス別の商談件数
     long proposingCount = 0;
     long estimateCount = 0;
     long lostCount = 0;


        // 受注件数
        long wonCount = 0;

        // 受注金額
        BigDecimal wonAmount = BigDecimal.ZERO;

        // 商談データを1件ずつ確認
        for (Deal deal : deals) {

        	// 商談の進捗状況を取得
        	String status = deal.getStatus();

        	// ステータスごとに件数を集計
        	if ("提案中".equals(status)) {
        	    proposingCount++;

        	} else if ("見積提出".equals(status)) {
        	    estimateCount++;

        	} else if ("失注".equals(status)) {
        	    lostCount++;
        	}


            // 進捗状況が「受注」の場合だけ集計
            if ("受注".equals(deal.getStatus())) {

                // 受注件数を1件増やす
                wonCount++;

                // 商談金額が登録されている場合
                if (deal.getAmount() != null) {

                    // 受注金額に加算
                    wonAmount = wonAmount.add(
                            deal.getAmount());
                }
            }
        }

        // HTMLに集計結果を渡す
        model.addAttribute("customerCount", customerCount);
        model.addAttribute("dealCount", dealCount);
        model.addAttribute("wonCount", wonCount);
        model.addAttribute("wonAmount", wonAmount);

     // グラフ表示用のデータ
     model.addAttribute("proposingCount", proposingCount);
     model.addAttribute("estimateCount", estimateCount);
     model.addAttribute("lostCount", lostCount);


        return "dashboard";
    }
}

