package com.mgaye.yonei.repository;

import com.mgaye.yonei.entity.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByName(String name);

    // ✅ ADD THIS MISSING METHOD
    Optional<User> findByEmailVerificationToken(String token); // this was used but does not works I took the
                                                               // findByVerificationToken()

    // Alternative if the above doesn't work
    @Query("SELECT u FROM User u WHERE u.emailVerificationToken = :token")
    Optional<User> findByVerificationToken(@Param("token") String token);

    // ✅ Even better - include expiry check
    @Query("SELECT u FROM User u WHERE u.emailVerificationToken = :token AND u.tokenExpiry > :now")
    Optional<User> findValidVerificationToken(@Param("token") String token, @Param("now") Instant now);

    // ✅ ADD THESE ADDITIONAL METHODS FOR SECURITY
    boolean existsByEmail(String email);

    boolean existsByName(String username);

    boolean existsByPhoneNumber(String phoneNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT u FROM User u WHERE u.pendingEmail = :email")
    Optional<User> findByPendingEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.stripeCustomerId = :stripeCustomerId")
    Optional<User> findByStripeCustomerId(@Param("stripeCustomerId") String stripeCustomerId);

    // Add this for debugging
    @Query("SELECT COUNT(u) FROM User u WHERE u.email = :email")
    long countByEmail(@Param("email") String email);

}
