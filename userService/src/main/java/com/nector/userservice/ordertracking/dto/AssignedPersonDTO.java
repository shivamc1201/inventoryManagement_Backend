package com.nector.userservice.ordertracking.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignedPersonDTO {
    private String name;
    private String role;
    private String contact;
    private String email;
}
