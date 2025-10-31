package com.mgaye.yonei.dto;

import com.mgaye.yonei.entity.Transfer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO léger pour exposer un transfert en API sans renvoyer d'entités JPA
 * complètes
 * (évite les problèmes de proxy Hibernate / ByteBuddy lors de la
 * sérialisation).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferDTO {
    private Long id;
    private BigDecimal amount;
    private Instant createdAt;
    private String status; // on renvoie le nom de l'enum (PENDING, COMPLETED, ...)
    private Long fromUserId; // id de l'expéditeur
    private Long toUserId; // id du destinataire interne (null si externe)
    private Long beneficiaryId;// id du beneficiary si transfert externe (null sinon)
    private String recipientName; // NEW: username or beneficiary full name

    /**
     * Convertit une entité Transfer en DTO.
     * Attention : on n'essaie pas de sérialiser les objets User/Beneficiary
     * complets.
     */
    public static TransferDTO from(Transfer t) {
        if (t == null)
            return null;

        Long fromUserId = t.getFromUser() != null ? t.getFromUser().getId() : null;
        Long toUserId = t.getToUser() != null ? t.getToUser().getId() : null;
        Long beneficiaryId = t.getBeneficiary() != null ? t.getBeneficiary().getId() : null;

        // Determine recipient name
        String recipientName = null;
        if (t.getToUser() != null) {
            recipientName = t.getToUser().getUsername();
        } else if (t.getBeneficiary() != null) {
            recipientName = t.getBeneficiary().getFullName();
        }

        return TransferDTO.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .createdAt(t.getCreatedAt())
                .status(t.getStatus() != null ? t.getStatus().name() : null)
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .beneficiaryId(beneficiaryId)
                .recipientName(recipientName)
                .build();
    }
}
