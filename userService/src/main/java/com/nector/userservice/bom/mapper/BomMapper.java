package com.nector.userservice.bom.mapper;

import com.nector.userservice.bom.dto.*;
import com.nector.userservice.bom.entity.*;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {})
public interface BomMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "components", ignore = true)
    @Mapping(target = "additionalCosts", ignore = true)
    @Mapping(target = "materialCount", ignore = true)
    @Mapping(target = "totalComponentCost", ignore = true)
    @Mapping(target = "totalAdditionalCost", ignore = true)
    @Mapping(target = "effectiveCost", ignore = true)
    @Mapping(target = "effectiveRatePerUnit", ignore = true)
    BillOfMaterial toEntity(BomRequestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bom", ignore = true)
    @Mapping(target = "amount", ignore = true)
    @Mapping(target = "contributionPercent", ignore = true)
    BomComponent toComponentEntity(BomComponentDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bom", ignore = true)
    @Mapping(target = "amount", ignore = true)
    AdditionalCost toAdditionalCostEntity(AdditionalCostDto dto);

    @Mapping(target = "components", source = "components")
    @Mapping(target = "additionalCosts", source = "additionalCosts")
    BomResponseDto toResponseDto(BillOfMaterial entity);

    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "contributionPercent", source = "contributionPercent")
    BomComponentDto toComponentDto(BomComponent entity);

    @Mapping(target = "amount", source = "amount")
    AdditionalCostDto toAdditionalCostDto(AdditionalCost entity);

    List<BomComponentDto> toComponentDtoList(List<BomComponent> components);

    List<AdditionalCostDto> toAdditionalCostDtoList(List<AdditionalCost> additionalCosts);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "components", ignore = true)
    @Mapping(target = "additionalCosts", ignore = true)
    @Mapping(target = "materialCount", ignore = true)
    @Mapping(target = "totalComponentCost", ignore = true)
    @Mapping(target = "totalAdditionalCost", ignore = true)
    @Mapping(target = "effectiveCost", ignore = true)
    @Mapping(target = "effectiveRatePerUnit", ignore = true)
    void updateEntityFromDto(BomRequestDto dto, @MappingTarget BillOfMaterial entity);

    default void updateComponentsFromDto(List<BomComponentDto> componentDtos, BillOfMaterial entity) {
        if (componentDtos == null || componentDtos.isEmpty()) {
            entity.getComponents().clear();
            return;
        }

        entity.getComponents().clear();
        for (BomComponentDto dto : componentDtos) {
            BomComponent component = toComponentEntity(dto);
            component.setBom(entity);
            entity.getComponents().add(component);
        }
    }

    default void updateAdditionalCostsFromDto(List<AdditionalCostDto> additionalCostDtos, BillOfMaterial entity) {
        if (additionalCostDtos == null) {
            entity.getAdditionalCosts().clear();
            return;
        }

        entity.getAdditionalCosts().clear();
        for (AdditionalCostDto dto : additionalCostDtos) {
            AdditionalCost additionalCost = toAdditionalCostEntity(dto);
            additionalCost.setBom(entity);
            entity.getAdditionalCosts().add(additionalCost);
        }
    }

    @AfterMapping
    default void afterMapping(@MappingTarget BillOfMaterial entity, BomRequestDto dto) {
        updateComponentsFromDto(dto.getComponents(), entity);
        updateAdditionalCostsFromDto(dto.getAdditionalCosts(), entity);
    }
}
