package com.nector.userservice.enums;

import java.util.Arrays;
import java.util.Optional;

public enum IndianStateCode {
    JAMMU_AND_KASHMIR(1, "jammu and kashmir"),
    HIMACHAL_PRADESH(2, "himachal pradesh"),
    PUNJAB(3, "punjab"),
    CHANDIGARH(4, "chandigarh"),
    UTTARAKHAND(5, "uttarakhand"),
    HARYANA(6, "haryana"),
    DELHI(7, "delhi"),
    RAJASTHAN(8, "rajasthan"),
    UTTAR_PRADESH(9, "uttar pradesh"),
    BIHAR(10, "bihar"),
    SIKKIM(11, "sikkim"),
    ARUNACHAL_PRADESH(12, "arunachal pradesh"),
    NAGALAND(13, "nagaland"),
    MANIPUR(14, "manipur"),
    MIZORAM(15, "mizoram"),
    TRIPURA(16, "tripura"),
    MEGHALAYA(17, "meghalaya"),
    ASSAM(18, "assam"),
    WEST_BENGAL(19, "west bengal"),
    JHARKHAND(20, "jharkhand"),
    ODISHA(21, "orissa"),
    CHHATTISGARH(22, "chhattisgarh"),
    MADHYA_PRADESH(23, "madhya pradesh"),
    GUJARAT(24, "gujarat"),
    DADRA_AND_NAGAR_HAVELI(26, "dadra and nagar haveli & daman and diu"),
    MAHARASHTRA(27, "maharashtra"),
    KARNATAKA(29, "karnataka"),
    GOA(30, "goa"),
    LAKSHADWEEP(31, "lakshadweep"),
    KERALA(32, "kerala"),
    TAMIL_NADU(33, "tamil nadu"),
    PUDUCHERRY(34, "puducherry"),
    ANDAMAN_AND_NICOBAR(35, "andaman and nicobar"),
    TELANGANA(36, "telangana"),
    ANDHRA_PRADESH(37, "andhra pradesh"),
    LADAKH(38, "ladakh");

    private final int code;
    private final String stateName;

    IndianStateCode(int code, String stateName) {
        this.code = code;
        this.stateName = stateName;
    }

    public int getCode() {
        return code;
    }

    public String getStateName() {
        return stateName;
    }

    public String getCodeAsString() {
        return String.valueOf(code);
    }

    public static Optional<IndianStateCode> fromStateName(String stateName) {
        if (stateName == null) return Optional.empty();
        String normalized = stateName.trim().toLowerCase();
        return Arrays.stream(values())
                .filter(s -> s.stateName.equals(normalized))
                .findFirst();
    }
}
