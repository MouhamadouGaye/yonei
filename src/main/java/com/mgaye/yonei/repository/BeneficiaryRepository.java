package com.mgaye.yonei.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mgaye.yonei.entity.Beneficiary;
import com.mgaye.yonei.entity.User;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
    // Find by owner (user)
    List<Beneficiary> findByOwner(User owner);

    // Find by owner ID
    List<Beneficiary> findByOwnerId(Long ownerId);

    // Check if beneficiary exists for this owner
    boolean existsByPhoneNumberAndOwner(String phoneNumber, User owner);

    // Find specific beneficiary for this owner
    Optional<Beneficiary> findByIdAndOwnerId(Long id, Long ownerId);

}
