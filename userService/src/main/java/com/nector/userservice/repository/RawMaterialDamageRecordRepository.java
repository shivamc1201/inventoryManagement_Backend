package com.nector.userservice.repository;

import com.nector.userservice.model.RawMaterialDamageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RawMaterialDamageRecordRepository extends JpaRepository<RawMaterialDamageRecord, Long> {

    List<RawMaterialDamageRecord> findByRawMaterialIdOrderByDamagedAtDesc(Long rawMaterialId);
}
