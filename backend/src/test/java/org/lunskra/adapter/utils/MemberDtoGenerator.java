package org.lunskra.adapter.utils;

import org.lunskra.family_tree.api.model.GenderDto;
import org.lunskra.family_tree.api.model.MemberDto;

import java.time.LocalDate;

public class MemberDtoGenerator {

    public static MemberDto createLivingMemberDtoWithRequiredData() {
        return new MemberDto()
                .id(1)
                .firstName("John")
                .lastName("Cena")
                .gender(GenderDto.M)
                .birthDate(LocalDate.of(1990,1,1))
                .birthCountry("USA")
                .birthCity("New York");
    }

    public static MemberDto createLivingMemberDtoWithAllData() {
        return new MemberDto()
                .id(2)
                .firstName("Katharina")
                .lastName("Schneider")
                .initialLastName("Becker")
                .gender(GenderDto.F)
                .birthDate(LocalDate.of(1987, 9, 3))
                .deathDate(null) // alive
                .birthCity("Hamburg")
                .birthCountry("Deutschland")
                .email("katharina.schneider@example.de")
                .telephone("+49 40 12345678")
                .streetAndNumber("Jungfernstieg 12")
                .postcode("20095")
                .city("Hamburg");
    }

    public static MemberDto createLivingMemberDtoWithAllDataWithoutId() {
        return new MemberDto()
                .firstName("Katharina")
                .lastName("Schneider")
                .initialLastName("Becker")
                .gender(GenderDto.F)
                .birthDate(LocalDate.of(1987, 9, 3))
                .deathDate(null) // alive
                .birthCity("Hamburg")
                .birthCountry("Deutschland")
                .email("katharina.schneider@example.de")
                .telephone("+49 40 12345678")
                .streetAndNumber("Jungfernstieg 12")
                .postcode("20095")
                .city("Hamburg");
    }
}
