
package com.salespilot.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.salespilot.entity.Customer;
import com.salespilot.entity.Deal;
import com.salespilot.repository.CustomerRepository;
import com.salespilot.repository.DealRepository;

@Controller
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final DealRepository dealRepository;

    // コンストラクタ
    public CustomerController(
            CustomerRepository customerRepository,
            DealRepository dealRepository) {

        this.customerRepository = customerRepository;
        this.dealRepository = dealRepository;
    }

    // 顧客一覧表示・検索機能
    @GetMapping("/customers")
    public String customerList(
            @RequestParam(name = "keyword", required = false)
            String keyword,
            Model model) {

        List<Customer> customers;

        // 検索キーワードが入力されている場合
        if (keyword != null && !keyword.trim().isEmpty()) {

            String searchKeyword = keyword.trim();

            // 顧客名または会社名で部分一致検索
            customers = customerRepository
                    .findByNameContainingIgnoreCaseOrCompanyContainingIgnoreCase(
                            searchKeyword,
                            searchKeyword);

        } else {

            // キーワードがない場合は全件表示
            customers = customerRepository.findAll();
        }

        // 検索結果を画面に渡す
        model.addAttribute("customers", customers);

        // 検索キーワードを画面に保持
        model.addAttribute("keyword", keyword);

        return "customers/list";
    }

    // 顧客ごとの商談履歴を表示
    @GetMapping("/customers/{id}/deals")
    public String customerDeals(
            @PathVariable Long id,
            Model model) {

        // 対象の顧客を取得
        Customer customer = customerRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "顧客が見つかりません"));

        // この顧客に紐づく商談だけを取得
        List<Deal> deals =
                dealRepository.findByCustomerId(id);

        // 顧客情報を画面に渡す
        model.addAttribute("customer", customer);

        // 商談履歴を画面に渡す
        model.addAttribute("deals", deals);

        return "customers/deals";
    }

    // 新規登録画面を表示
    @GetMapping("/customers/new")
    public String newCustomer(Model model) {

        model.addAttribute("customer", new Customer());

        return "customers/form";
    }

    // 顧客情報を保存
    @PostMapping("/customers")
    public String saveCustomer(
            @ModelAttribute Customer customer,
            Model model,
            RedirectAttributes redirectAttributes) {

        // 顧客名が未入力、または空白だけの場合
        if (customer.getName() == null
                || customer.getName().trim().isEmpty()) {

            model.addAttribute(
                    "errorMessage",
                    "顧客名を入力してください。");

            // 入力内容を保持して登録画面へ戻る
            model.addAttribute("customer", customer);

            return "customers/form";
        }

        // 顧客名の前後の空白を除去
        String customerName = customer.getName().trim();

        // 顧客名が100文字を超える場合
        if (customerName.length() > 100) {

            model.addAttribute(
                    "errorMessage",
                    "顧客名は100文字以内で入力してください。");

            model.addAttribute("customer", customer);

            return "customers/form";
        }

        // 顧客名を設定
        customer.setName(customerName);

        // 新規登録として保存
        customer.setId(null);

        customerRepository.save(customer);

        // 登録完了メッセージ
        redirectAttributes.addFlashAttribute(
                "message", "顧客を登録しました。");

        return "redirect:/customers";
    }

    // 顧客編集画面を表示
    @GetMapping("/customers/{id}/edit")
    public String editCustomer(
            @PathVariable Long id,
            Model model) {

        Customer customer = customerRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "顧客が見つかりません"));

        model.addAttribute("customer", customer);

        return "customers/edit";
    }

    // 顧客情報を更新
    @PostMapping("/customers/{id}/edit")
    public String updateCustomer(
            @PathVariable Long id,
            @ModelAttribute Customer form,
            Model model,
            RedirectAttributes redirectAttributes) {

        // 更新対象の顧客を取得
        Customer customer = customerRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "顧客が見つかりません"));

        // 顧客名が未入力、または空白だけの場合
        if (form.getName() == null
                || form.getName().trim().isEmpty()) {

            model.addAttribute(
                    "errorMessage",
                    "顧客名を入力してください。");

            // 入力内容を保持して編集画面へ戻る
            form.setId(id);
            model.addAttribute("customer", form);

            return "customers/edit";
        }

        // 顧客名の前後の空白を除去
        String customerName = form.getName().trim();

        // 顧客名が100文字を超える場合
        if (customerName.length() > 100) {

            model.addAttribute(
                    "errorMessage",
                    "顧客名は100文字以内で入力してください。");

            // 入力内容を保持して編集画面へ戻る
            form.setId(id);
            model.addAttribute("customer", form);

            return "customers/edit";
        }

        // 入力チェックを通過した場合のみ更新
        customer.setName(customerName);
        customer.setCompany(form.getCompany());
        customer.setPhone(form.getPhone());
        customer.setEmail(form.getEmail());

        // データベースに保存
        customerRepository.save(customer);

        // 更新完了メッセージ
        redirectAttributes.addFlashAttribute(
                "message", "顧客情報を更新しました。");

        return "redirect:/customers";
    }

    // 顧客情報を削除
    @PostMapping("/customers/{id}/delete")
    public String deleteCustomer(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        // 顧客が存在するか確認
        if (!customerRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "顧客が見つかりません");
        }

        // 紐づく商談がある場合は削除しない
        List<Deal> deals =
                dealRepository.findByCustomerId(id);

        if (!deals.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "message",
                    "この顧客には商談が登録されているため削除できません。"
                    + "先に商談を削除してください。");

            return "redirect:/customers";
        }

        // データベースから削除
        customerRepository.deleteById(id);

        // 削除完了メッセージ
        redirectAttributes.addFlashAttribute(
                "message", "顧客情報を削除しました。");

        return "redirect:/customers";
    }
}
