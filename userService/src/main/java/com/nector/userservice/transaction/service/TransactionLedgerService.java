package com.nector.userservice.transaction.service;

import com.nector.userservice.exception.DuplicateLedgerNameException;
import com.nector.userservice.exception.InvalidLedgerGroupException;
import com.nector.userservice.exception.ReservedLedgerNameException;
import com.nector.userservice.transaction.dto.LedgerRequest;
import com.nector.userservice.transaction.dto.LedgerResponse;
import com.nector.userservice.transaction.entity.TransactionLedger;
import com.nector.userservice.transaction.enums.LedgerType;
import com.nector.userservice.transaction.enums.UnderGroup;
import com.nector.userservice.transaction.repository.TransactionLedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionLedgerService {

    public static final String RECEIVED_FROM_HO = "Received From HO";

    private static final Set<UnderGroup> EXPENSE_GROUPS = EnumSet.of(UnderGroup.DIRECT_EXPENSE, UnderGroup.INDIRECT_EXPENSE);
    private static final Set<UnderGroup> INCOME_GROUPS = EnumSet.of(UnderGroup.DIRECT_INCOME, UnderGroup.INDIRECT_INCOME);

    private final TransactionLedgerRepository ledgerRepository;

    public LedgerResponse createLedger(LedgerRequest request) {
        String ledgerName = request.getLedgerName().trim();

        if (ledgerName.equalsIgnoreCase(RECEIVED_FROM_HO)) {
            throw new ReservedLedgerNameException("'" + RECEIVED_FROM_HO + "' is a reserved ledger name");
        }

        validateGroupPairing(request.getLedgerType(), request.getUnderGroup());

        if (ledgerRepository.existsByLedgerNameIgnoreCase(ledgerName)) {
            throw new DuplicateLedgerNameException("A ledger named '" + ledgerName + "' already exists");
        }

        TransactionLedger ledger = new TransactionLedger();
        ledger.setLedgerName(ledgerName);
        ledger.setLedgerType(request.getLedgerType());
        ledger.setUnderGroup(request.getUnderGroup());
        ledger.setCreatedAt(LocalDate.now());

        TransactionLedger saved = ledgerRepository.save(ledger);
        return toResponse(saved);
    }

    public List<LedgerResponse> getAllLedgers() {
        return ledgerRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<LedgerResponse> getLedgersByType(LedgerType ledgerType) {
        return ledgerRepository.findByLedgerType(ledgerType).stream().map(this::toResponse).toList();
    }

    private void validateGroupPairing(LedgerType ledgerType, UnderGroup underGroup) {
        boolean valid = switch (ledgerType) {
            case EXPENSE -> EXPENSE_GROUPS.contains(underGroup);
            case INCOME -> INCOME_GROUPS.contains(underGroup);
        };
        if (!valid) {
            throw new InvalidLedgerGroupException(
                    "underGroup '" + underGroup + "' is not valid for ledgerType '" + ledgerType + "'");
        }
    }

    private LedgerResponse toResponse(TransactionLedger ledger) {
        return new LedgerResponse(
                ledger.getId(),
                ledger.getLedgerName(),
                ledger.getLedgerType(),
                ledger.getUnderGroup(),
                ledger.getCreatedAt());
    }
}
