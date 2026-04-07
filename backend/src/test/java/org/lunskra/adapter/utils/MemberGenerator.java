package org.lunskra.adapter.utils;

import org.lunskra.core.domain.Gender;
import org.lunskra.core.domain.Member;

import java.time.LocalDate;

public class MemberGenerator {

    public static Member createLivingMemberWithRequiredData() {
        Member member = new Member();
        member.setId(1);
        member.setFirstName("John");
        member.setLastName("Cena");
        member.setGender(Gender.M);
        member.setBirthDate(LocalDate.of(1990,1,1));
        member.setBirthCountry("USA");
        member.setBirthCity("New York");

        return member;
    }

    public static Member createLivingMemberWithAllData() {
        Member member = new Member();

        member.setId(2);
        member.setFirstName("Katharina");
        member.setLastName("Schneider");
        member.setInitialLastName("Becker");
        member.setGender(Gender.F);
        member.setBirthDate(LocalDate.of(1987, 9, 3));
        member.setDeathDate(null); // alive
        member.setBirthCity("Hamburg");
        member.setBirthCountry("Deutschland");
        member.setEmail("katharina.schneider@example.de");
        member.setTelephone("+49 40 12345678");
        member.setStreetAndNumber("Jungfernstieg 12");
        member.setPostcode("20095");
        member.setCity("Hamburg");

        return member;
    }

    public static Member createLivingMemberWithAllDataAndNoId() {
        Member member = new Member();

        member.setFirstName("Katharina");
        member.setLastName("Schneider");
        member.setInitialLastName("Becker");
        member.setGender(Gender.F);
        member.setBirthDate(LocalDate.of(1987, 9, 3));
        member.setDeathDate(null); // alive
        member.setBirthCity("Hamburg");
        member.setBirthCountry("Deutschland");
        member.setEmail("katharina.schneider@example.de");
        member.setTelephone("+49 40 12345678");
        member.setStreetAndNumber("Jungfernstieg 12");
        member.setPostcode("20095");
        member.setCity("Hamburg");

        return member;
    }
}
