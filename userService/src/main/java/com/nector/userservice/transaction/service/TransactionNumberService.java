package com.nector.userservice.transaction.service;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionNumberService {

    @PersistenceContext
    private EntityManager entityManager;

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initSequences() {
        jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS voucher_seq START WITH 1 INCREMENT BY 1");
        jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS fund_seq START WITH 1 INCREMENT BY 1");
    }

    @Transactional
    public String nextVoucherNo() {
        Number next = (Number) entityManager.createNativeQuery("SELECT nextval('voucher_seq')").getSingleResult();
        return String.format("VCH-%03d", next.longValue());
    }

    @Transactional
    public String nextFundNo() {
        Number next = (Number) entityManager.createNativeQuery("SELECT nextval('fund_seq')").getSingleResult();
        return String.format("FND-%03d", next.longValue());
    }
}
