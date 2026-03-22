package com.nector.userservice.interceptors.distributor.service;

import com.nector.userservice.interceptors.distributor.model.Distributor;
import com.nector.userservice.interceptors.distributor.model.DistributorRequestDTO;
import com.nector.userservice.interceptors.distributor.model.DistributorResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class DistributorMapper {
    
    public Distributor toEntity(DistributorRequestDTO dto) {
        Distributor distributor = new Distributor();
        distributor.setName(dto.getName());
        distributor.setAssignedPerson(dto.getAssignedPerson());
        distributor.setSalespersonId(dto.getSalespersonId());
        distributor.setSalesPersonRoleType(dto.getSalesPersonRoleType());
        distributor.setDistributorType(dto.getDistributorType());
        distributor.setCompanyType(dto.getCompanyType());
        distributor.setContactEmail(dto.getContactEmail());
        distributor.setPhoneNumber(dto.getPhoneNumber());
        distributor.setAlternateContact(dto.getAlternateContact());
        distributor.setAddress(dto.getAddress());
        distributor.setAadhaarNumber(dto.getAadhaarNumber());
        distributor.setPanNumber(dto.getPanNumber());
        distributor.setGstNumber(dto.getGstNumber());
        distributor.setStatus(dto.getStatus());
        distributor.setUsername(dto.getUsername());
        distributor.setPassword(dto.getPassword());
        distributor.setAccountName(dto.getAccountName());
        distributor.setAccountNumber(dto.getAccountNumber());
        distributor.setIfsc(dto.getIfsc());


        return distributor;
    }
    
    public DistributorResponseDTO toResponseDTO(Distributor entity) {
        DistributorResponseDTO dto = new DistributorResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setAssignedPerson(entity.getAssignedPerson());
        dto.setSalespersonId(entity.getSalespersonId());
        dto.setSalesPersonRoleType(entity.getSalesPersonRoleType());
        dto.setDistributorType(entity.getDistributorType());
        dto.setCompanyType(entity.getCompanyType());
        dto.setContactEmail(entity.getContactEmail());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setAlternateContact(entity.getAlternateContact());
        dto.setAddress(entity.getAddress());
        dto.setAadhaarNumber(entity.getAadhaarNumber());
        dto.setPanNumber(entity.getPanNumber());
        dto.setGstNumber(entity.getGstNumber());
        dto.setStatus(entity.getStatus());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setPassword(entity.getPassword());
        dto.setUsername(entity.getUsername());
        dto.setEmail(entity.getContactEmail());
        dto.setAccountNumber(entity.getAccountNumber());
        dto.setAccountName(entity.getAccountName());
        dto.setIFSC(entity.getIfsc());
        return dto;
    }
    
    public void updateEntity(Distributor entity, DistributorRequestDTO dto) {
        entity.setName(dto.getName());
        entity.setAssignedPerson(dto.getAssignedPerson());
        entity.setSalespersonId(dto.getSalespersonId());
        entity.setSalesPersonRoleType(dto.getSalesPersonRoleType());
        entity.setDistributorType(dto.getDistributorType());
        entity.setCompanyType(dto.getCompanyType());
        entity.setContactEmail(dto.getContactEmail());
        entity.setPhoneNumber(dto.getPhoneNumber());
        entity.setAlternateContact(dto.getAlternateContact());
        entity.setAddress(dto.getAddress());
        entity.setAadhaarNumber(dto.getAadhaarNumber());
        entity.setPanNumber(dto.getPanNumber());
        entity.setGstNumber(dto.getGstNumber());
        entity.setStatus(dto.getStatus());
        entity.setPassword(dto.getPassword());
        entity.setUsername(dto.getUsername());
    }
}