package com.salespilot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salespilot.entity.Customer;

public interface CustomerRepository
        extends JpaRepository<Customer, Long> {

    // 顧客名または会社名で部分一致検索
    List<Customer> findByNameContainingIgnoreCaseOrCompanyContainingIgnoreCase(
            String name,
            String company
    );

}


