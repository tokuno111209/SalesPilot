
package com.salespilot.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

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
public class DealController {

    private final DealRepository dealRepository;
    private final CustomerRepository customerRepository;

    // 使用できる進捗状況
    private static final Set<String> VALID_STATUSES =
            Set.of("提案中", "見積提出", "受注", "失注");

    // コンストラクタ
    public DealController(
            DealRepository dealRepository,
            CustomerRepository customerRepository) {

        this.dealRepository = dealRepository;
        this.customerRepository = customerRepository;
    }

    // 商談一覧画面を表示（商談名・進捗状況で検索）
    @GetMapping("/deals")
    public String dealList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            Model model) {

        List<Deal> deals;

        // 商談名が入力されているか確認
        boolean hasKeyword =
                keyword != null && !keyword.trim().isEmpty();

        // 進捗状況が選択されているか確認
        boolean hasStatus =
                status != null && !status.trim().isEmpty();

        // 商談名と進捗状況の両方が指定されている場合
        if (hasKeyword && hasStatus) {

            deals = dealRepository
                    .findByTitleContainingIgnoreCaseAndStatus(
                            keyword.trim(),
                            status.trim());

        // 商談名だけ指定されている場合
        } else if (hasKeyword) {

            deals = dealRepository
                    .findByTitleContainingIgnoreCase(
                            keyword.trim());

        // 進捗状況だけ指定されている場合
        } else if (hasStatus) {

            deals = dealRepository.findByStatus(
                    status.trim());

        // 検索条件がない場合は全件表示
        } else {

            deals = dealRepository.findAll();
        }

        // 検索結果を画面に渡す
        model.addAttribute("deals", deals);

        // 検索条件を画面に渡す
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);

        return "deals/list";
    }

    // 商談の新規登録画面を表示
    // 顧客IDが指定されていれば、その顧客を初期設定する
    @GetMapping("/deals/new")
    public String newDeal(
            @RequestParam(required = false) Long customerId,
            Model model) {

        // 新しい商談データを作成
        Deal deal = new Deal();

        // 顧客IDが指定されている場合
        if (customerId != null) {

            // 対象の顧客を取得
            Customer customer = customerRepository
                    .findById(customerId)
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "顧客が見つかりません"));

            // 新しい商談に顧客を設定
            deal.setCustomer(customer);
        }

        // 商談データを画面に渡す
        model.addAttribute("deal", deal);

        // 顧客一覧を画面に渡す
        model.addAttribute(
                "customers",
                customerRepository.findAll());

        return "deals/form";
    }

    // 商談情報を保存
    @PostMapping("/deals")
    public String saveDeal(
            @ModelAttribute Deal deal,
            @RequestParam(required = false) Long customerId,
            Model model,
            RedirectAttributes redirectAttributes) {

        // 入力内容をチェック
        String errorMessage =
                validateDeal(deal, customerId);

        // エラーがある場合は登録画面へ戻る
        if (errorMessage != null) {

            return showDealError(
                    deal,
                    customerId,
                    errorMessage,
                    "deals/form",
                    model);
        }

        // 顧客を取得
        Customer customer = customerRepository
                .findById(customerId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "指定された顧客が見つかりません"));

        // 商談に顧客を紐づける
        deal.setCustomer(customer);

        // 商談名の前後の空白を除去
        deal.setTitle(deal.getTitle().trim());

        // 新規登録として保存
        deal.setId(null);
        dealRepository.save(deal);

        // 登録完了メッセージ
        redirectAttributes.addFlashAttribute(
                "message", "商談を登録しました。");

        return "redirect:/deals";
    }

    // 商談編集画面を表示
    @GetMapping("/deals/{id}/edit")
    public String editDeal(
            @PathVariable Long id,
            Model model) {

        // 編集対象の商談を取得
        Deal deal = dealRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "商談が見つかりません"));

        // 登録済みの顧客一覧を取得
        model.addAttribute(
                "customers",
                customerRepository.findAll());

        // 編集画面にデータを渡す
        model.addAttribute("deal", deal);

        return "deals/edit";
    }

    // 商談情報を更新する
    @PostMapping("/deals/{id}/edit")
    public String updateDeal(
            @PathVariable Long id,
            @ModelAttribute Deal formDeal,
            @RequestParam(required = false) Long customerId,
            Model model,
            RedirectAttributes redirectAttributes) {

        // 更新対象の商談を取得
        Deal deal = dealRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "商談が見つかりません"));

        // 入力内容をチェック
        String errorMessage =
                validateDeal(formDeal, customerId);

        // エラーがある場合は編集画面へ戻る
        if (errorMessage != null) {

            // 編集対象のIDを保持
            formDeal.setId(id);

            return showDealError(
                    formDeal,
                    customerId,
                    errorMessage,
                    "deals/edit",
                    model);
        }

        // 選択された顧客を取得
        Customer customer = customerRepository
                .findById(customerId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "指定された顧客が見つかりません"));

        // 編集した内容を反映
        deal.setTitle(formDeal.getTitle().trim());
        deal.setCustomer(customer);
        deal.setAmount(formDeal.getAmount());
        deal.setStatus(formDeal.getStatus());
        deal.setScheduledDate(formDeal.getScheduledDate());
        deal.setMemo(formDeal.getMemo());

        // データベースに保存
        dealRepository.save(deal);

        // 更新完了メッセージ
        redirectAttributes.addFlashAttribute(
                "message", "商談情報を更新しました。");

        return "redirect:/deals";
    }

    // 商談を削除する
    @PostMapping("/deals/{id}/delete")
    public String deleteDeal(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        // 削除対象の商談が存在するか確認
        if (!dealRepository.existsById(id)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "商談が見つかりません");
        }

        // データベースから商談を削除
        dealRepository.deleteById(id);

        // 削除完了メッセージ
        redirectAttributes.addFlashAttribute(
                "message", "商談を削除しました。");

        return "redirect:/deals";
    }

    // 商談の入力チェック（新規登録・編集で共通利用）
    private String validateDeal(
            Deal deal,
            Long customerId) {

        // 商談名の未入力チェック
        if (deal.getTitle() == null
                || deal.getTitle().trim().isEmpty()) {

            return "商談名を入力してください。";
        }

        // 顧客の未選択チェック
        if (customerId == null) {

            return "顧客を選択してください。";
        }

        // 選択された顧客の存在チェック
        if (!customerRepository.existsById(customerId)) {

            return "選択された顧客が見つかりません。";
        }

        // 商談金額のマイナスチェック
        BigDecimal amount = deal.getAmount();

        if (amount != null
                && amount.compareTo(BigDecimal.ZERO) < 0) {

            return "商談金額は0円以上で入力してください。";
        }

        // 進捗状況の未入力チェック
        if (deal.getStatus() == null
                || deal.getStatus().trim().isEmpty()) {

            return "進捗状況を選択してください。";
        }

        // 想定外の進捗状況を禁止
        if (!VALID_STATUSES.contains(deal.getStatus())) {

            return "正しい進捗状況を選択してください。";
        }

        // エラーなし
        return null;
    }

    // エラー時に入力内容を保持してフォームへ戻る
    private String showDealError(
            Deal deal,
            Long customerId,
            String errorMessage,
            String viewName,
            Model model) {

        // 顧客の選択状態を保持
        if (customerId != null
                && customerRepository.existsById(customerId)) {

            Customer customer = customerRepository
                    .findById(customerId)
                    .orElseThrow();

            deal.setCustomer(customer);

        } else {

            deal.setCustomer(null);
        }

        // 入力内容・顧客一覧・エラーメッセージを渡す
        model.addAttribute("deal", deal);
        model.addAttribute(
                "customers",
                customerRepository.findAll());
        model.addAttribute("errorMessage", errorMessage);

        return viewName;
    }
}
