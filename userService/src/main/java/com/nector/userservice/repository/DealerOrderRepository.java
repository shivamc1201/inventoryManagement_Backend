package com.nector.userservice.repository;

import com.nector.userservice.model.DealerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DealerOrderRepository extends JpaRepository<DealerOrder, Long> {

    List<DealerOrder> findByDealerIdOrderByDateDesc(Long dealerId);

    List<DealerOrder> findByDistributorIdOrderByDateDesc(Long distributorId);
}
