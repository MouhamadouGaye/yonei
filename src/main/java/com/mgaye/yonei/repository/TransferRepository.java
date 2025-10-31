package com.mgaye.yonei.repository;

import com.mgaye.yonei.entity.Transfer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {
    List<Transfer> findByFromUser_IdOrToUser_Id(Long fromUserId, Long toUserId);
}
