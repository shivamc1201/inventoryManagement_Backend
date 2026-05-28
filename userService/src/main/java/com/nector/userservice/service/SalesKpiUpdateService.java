package com.nector.userservice.service;

import com.nector.userservice.dto.CustomResponse;
import com.nector.userservice.dto.MeetingDetailRequest;
import com.nector.userservice.dto.SalesKpiUpdateRequest;
import com.nector.userservice.enums.KPIStatus;
import com.nector.userservice.model.EmployeeKpiAssignment;
import com.nector.userservice.model.SalesKpiMeetingDetail;
import com.nector.userservice.model.SalesKpiUpdate;
import com.nector.userservice.model.SalesPerson;
import com.nector.userservice.repository.EmployeeKpiAssignmentRepository;
import com.nector.userservice.repository.SalesKpiUpdateRepository;
import com.nector.userservice.repository.SalesPersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesKpiUpdateService {

    private final SalesKpiUpdateRepository salesKpiUpdateRepository;
    private final SalesPersonRepository salesPersonRepository;
    private final EmployeeKpiAssignmentRepository assignmentRepository;

    @Transactional
    public CustomResponse saveAll(List<SalesKpiUpdateRequest> requests) {
        List<SalesKpiUpdate> saved = new ArrayList<>();

        for (SalesKpiUpdateRequest req : requests) {
            SalesKpiUpdate entity = new SalesKpiUpdate();
            entity.setUserName(req.getUserName());
            entity.setEmpCode(req.getEmpCode());
            entity.setDate(LocalDate.parse(req.getDate()));
            entity.setTotalDistanceInKm(req.getTotalDistanceInKm());
            entity.setNoOfMeetings(req.getNoOfMeetings());
            entity.setFarmerVisit(req.getFarmerVisit());
            entity.setDistributorVisit(req.getDistributorVisit());
            entity.setDealerVisit(req.getDealerVisit());
            entity.setDealerOnboard(req.getDealerOnboard());
            entity.setDistributorOnboard(req.getDistributorOnboard());
            entity.setRetailerVisit(req.getRetailerVisit());

            Optional<SalesPerson> spOpt = salesPersonRepository.findByEmployeeRollNo(req.getEmpCode());
            if (spOpt.isPresent()) {
                SalesPerson salesPerson = spOpt.get();
                entity.setSalesPerson(salesPerson);
                updateKpiAssignments(salesPerson.getId(), req);
            } else {
                log.warn("No SalesPerson found for empCode: {}", req.getEmpCode());
            }

            if (req.getMeetingDetails() != null) {
                for (MeetingDetailRequest mdReq : req.getMeetingDetails()) {
                    SalesKpiMeetingDetail detail = new SalesKpiMeetingDetail();
                    detail.setSalesKpiUpdate(entity);
                    detail.setClientName(mdReq.getClientName());
                    detail.setContactPerson(mdReq.getContactPerson());
                    detail.setClientContactNo(mdReq.getClientContactNo());
                    detail.setClientEmail(mdReq.getClientEmail());
                    detail.setType(mdReq.getType());
                    detail.setMeetingAddress(mdReq.getMeetingAddress());
                    detail.setCreateDate(mdReq.getCreateDate());
                    detail.setCreateTime(mdReq.getCreateTime());
                    entity.getMeetingDetails().add(detail);
                }
            }

            saved.add(salesKpiUpdateRepository.save(entity));
        }

        return CustomResponse.success(saved, "Sales KPI data saved successfully");
    }

    private void updateKpiAssignments(Long salesPersonId, SalesKpiUpdateRequest req) {
        Map<String, Integer> kpiValues = Map.of(
                "farmer Visit",       req.getFarmerVisit()       != null ? req.getFarmerVisit()       : 0,
                "Distributor Visit",  req.getDistributorVisit()  != null ? req.getDistributorVisit()  : 0,
                "Dealer Visit",       req.getDealerVisit()       != null ? req.getDealerVisit()       : 0,
                "Dealer Onboard",     req.getDealerOnboard()     != null ? req.getDealerOnboard()     : 0,
                "Distributor Onboard",req.getDistributorOnboard()!= null ? req.getDistributorOnboard(): 0,
                "Retailer Visit",     req.getRetailerVisit()     != null ? req.getRetailerVisit()     : 0
        );

        for (Map.Entry<String, Integer> entry : kpiValues.entrySet()) {
            if (entry.getValue() <= 0) continue;

            List<EmployeeKpiAssignment> assignments =
                    assignmentRepository.findActiveByEmployeeIdAndKpiName(salesPersonId, entry.getKey());

            if (assignments.isEmpty()) {
                log.warn("No active KPI assignment found for salesPersonId={} kpiName='{}'",
                        salesPersonId, entry.getKey());
                continue;
            }

            for (EmployeeKpiAssignment assignment : assignments) {
                BigDecimal newValue = assignment.getAchievedValue().add(BigDecimal.valueOf(entry.getValue()));
                assignment.updateAchievedValue(newValue);
                assignmentRepository.save(assignment);
            }
        }
    }
}
