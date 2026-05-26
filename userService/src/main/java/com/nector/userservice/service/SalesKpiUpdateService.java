package com.nector.userservice.service;

import com.nector.userservice.dto.CustomResponse;
import com.nector.userservice.dto.MeetingDetailRequest;
import com.nector.userservice.dto.SalesKpiUpdateRequest;
import com.nector.userservice.model.SalesKpiMeetingDetail;
import com.nector.userservice.model.SalesKpiUpdate;
import com.nector.userservice.model.User;
import com.nector.userservice.repository.SalesKpiUpdateRepository;
import com.nector.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesKpiUpdateService {

    private final SalesKpiUpdateRepository salesKpiUpdateRepository;
    private final UserRepository userRepository;

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

            Optional<User> userOpt = userRepository.findByEmployeeRollNo(req.getEmpCode());
            if (userOpt.isPresent()) {
                entity.setUser(userOpt.get());
            } else {
                log.warn("No user found for empCode: {}", req.getEmpCode());
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
}
