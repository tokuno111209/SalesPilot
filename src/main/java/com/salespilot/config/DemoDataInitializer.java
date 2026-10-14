
package com.salespilot.config;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.salespilot.entity.Customer;
import com.salespilot.entity.Deal;
import com.salespilot.repository.CustomerRepository;
import com.salespilot.repository.DealRepository;

@Configuration
@Profile("demo")
public class DemoDataInitializer {

    @Bean
    CommandLineRunner initializeDemoData(
            CustomerRepository customerRepository,
            DealRepository dealRepository) {

        return args -> {

            // 既に顧客データがあれば追加しない
            if (customerRepository.count() > 0) {
                return;
            }

            // 架空の顧客データを登録
            Customer yamada = createCustomer(
                    customerRepository,
                    "山田 太郎",
                    "株式会社サンプル商事",
                    "03-0000-0001",
                    "yamada@example.com");

            Customer sato = createCustomer(
                    customerRepository,
                    "佐藤 花子",
                    "株式会社テクノリンク",
                    "03-0000-0002",
                    "sato@example.com");

            Customer suzuki = createCustomer(
                    customerRepository,
                    "鈴木 一郎",
                    "株式会社未来企画",
                    "03-0000-0003",
                    "suzuki@example.com");

            Customer tanaka = createCustomer(
                    customerRepository,
                    "田中 美咲",
                    "株式会社グリーンソリューション",
                    "03-0000-0004",
                    "tanaka@example.com");

            Customer ito = createCustomer(
                    customerRepository,
                    "伊藤 健太",
                    "株式会社ビジネスパートナー",
                    "03-0000-0005",
                    "ito@example.com");

            // 架空の商談データを登録
            createDeal(dealRepository, yamada,
                    "業務管理システム導入",
                    "提案中", 1200000, 7,
                    "初回提案資料を作成中");

            createDeal(dealRepository, yamada,
                    "保守サポート契約",
                    "受注", 300000, -10,
                    "年間保守契約");

            createDeal(dealRepository, sato,
                    "Webサイトリニューアル",
                    "受注", 850000, -5,
                    "デザインと開発を受注");

            createDeal(dealRepository, sato,
                    "追加機能開発",
                    "見積提出", 450000, 14,
                    "見積書を送付済み");

            createDeal(dealRepository, suzuki,
                    "顧客管理システム導入",
                    "見積提出", 1800000, 10,
                    "先方の回答待ち");

            createDeal(dealRepository, tanaka,
                    "営業支援ツール導入",
                    "提案中", 650000, 21,
                    "次回打ち合わせ予定");

            createDeal(dealRepository, ito,
                    "在庫管理システム開発",
                    "失注", 950000, -15,
                    "予算の都合により見送り");

            createDeal(dealRepository, ito,
                    "社内ポータルサイト構築",
                    "受注", 720000, -3,
                    "開発開始予定");
        };
    }

    private Customer createCustomer(
            CustomerRepository repository,
            String name,
            String company,
            String phone,
            String email) {

        Customer customer = new Customer();
        customer.setName(name);
        customer.setCompany(company);
        customer.setPhone(phone);
        customer.setEmail(email);

        return repository.save(customer);
    }

    private void createDeal(
            DealRepository repository,
            Customer customer,
            String title,
            String status,
            long amount,
            int daysFromToday,
            String memo) {

        Deal deal = new Deal();
        deal.setCustomer(customer);
        deal.setTitle(title);
        deal.setStatus(status);
        deal.setAmount(BigDecimal.valueOf(amount));
        deal.setScheduledDate(
                LocalDate.now().plusDays(daysFromToday));
        deal.setMemo(memo);

        repository.save(deal);
    }
}
