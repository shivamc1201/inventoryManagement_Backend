package com.nector.userservice.dispatch.repository;

import com.nector.userservice.dispatch.entity.GdnItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GdnItemRepository extends JpaRepository<GdnItem, Long> {
}