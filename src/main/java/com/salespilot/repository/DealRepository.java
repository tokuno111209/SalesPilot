package com.salespilot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salespilot.entity.Deal;

public interface DealRepository
        extends JpaRepository<Deal, Long> {

    // 指定した顧客に紐づく商談を取得
    List<Deal> findByCustomerId(Long customerId);

    // 商談名で部分一致検索
    List<Deal> findByTitleContainingIgnoreCase(
            String title);

    // 進捗状況で商談を検索
    List<Deal> findByStatus(String status);

    // 商談名と進捗状況の両方で検索
    List<Deal> findByTitleContainingIgnoreCaseAndStatus(
            String title,
            String status);
}
