package org.lunskra.adapter.api.resource;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lunskra.adapter.api.mapper.MemberDtoAssembler;
import org.lunskra.adapter.api.service.MemberService;
import org.lunskra.family_tree.api.MembersApi;
import org.lunskra.family_tree.api.model.GenderDto;
import org.lunskra.family_tree.api.model.MemberDto;
import org.lunskra.family_tree.api.model.MemberPageDto;

import java.io.InputStream;
import java.net.URI;
import java.time.LocalDate;

/**
 * REST adapter for member CRUD operations.
 * Implements the {@code MembersApi} interface generated from the OpenAPI specification.
 * Form parameters are assembled into a {@link org.lunskra.family_tree.api.model.MemberDto}
 * by {@link org.lunskra.adapter.api.mapper.MemberDtoAssembler} before being forwarded to
 * {@link MemberService}.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class MemberResource implements MembersApi {

    private final MemberService memberService;
    private final MemberDtoAssembler memberDtoAssembler;

    @RolesAllowed("view")
    @Override
    public Response listMembers(String firstName, String lastName, LocalDate birthDate, Integer page, Integer pageSize) {
        log.atInfo().addArgument(firstName).addArgument(lastName).addArgument(birthDate)
                .addArgument(page).addArgument(pageSize)
                .setMessage("Listing members with filters firstName={}, lastName={}, birthDate={}, page={}, pageSize={}").log();
        MemberPageDto result = memberService.listMembers(firstName, lastName, birthDate, page, pageSize);
        return Response.ok(result).build();
    }

    @RolesAllowed("edit")
    @Override
    public Response updateMemberById(Integer memberId, String firstName, String lastName, GenderDto gender, LocalDate birthDate, String birthCity, String birthCountry, String initialLastName, LocalDate deathDate, String email, String telephone, String streetAndNumber, String postcode, String city, String occupation, String notes, InputStream imageFileInputStream) {
        log.atInfo().addArgument(memberId).setMessage("Updating member with id={}").log();
        MemberDto memberStored = memberService.updateMember(
                memberId,
                memberDtoAssembler.fromFormParams(
                        firstName,
                        lastName,
                        initialLastName,
                        gender,
                        birthDate,
                        birthCity,
                        birthCountry,
                        deathDate,
                        email,
                        telephone,
                        streetAndNumber,
                        postcode,
                        city,
                        occupation,
                        notes
                ),
                imageFileInputStream
        );
        return Response
                .created(URI.create("/api/members/" + memberStored.getId()))
                .entity(memberStored)
                .build();
    }

    @RolesAllowed("create")
    @Override
    public Response createMember(String firstName, String lastName, GenderDto gender, LocalDate birthDate, String birthCity, String birthCountry, String initialLastName, LocalDate deathDate, String email, String telephone, String streetAndNumber, String postcode, String city, String occupation, String notes, InputStream imageFileInputStream) {
        log.atInfo().addArgument(firstName).addArgument(lastName).setMessage("Creating member {} {}").log();
        MemberDto memberStored = memberService.createMember(
                memberDtoAssembler.fromFormParams(
                        firstName,
                        lastName,
                        initialLastName,
                        gender,
                        birthDate,
                        birthCity,
                        birthCountry,
                        deathDate,
                        email,
                        telephone,
                        streetAndNumber,
                        postcode,
                        city,
                        occupation,
                        notes
                ),
                imageFileInputStream
        );
        return Response
                .created(URI.create("/api/members/" + memberStored.getId()))
                .entity(memberStored)
                .build();
    }

    @RolesAllowed("delete")
    @Override
    public Response deleteMemberById(Integer memberId) {
        log.atInfo().addArgument(memberId).setMessage("Deleting member with id={}").log();
        memberService.deleteMember(memberId);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @RolesAllowed("view")
    @Override
    public Response getMemberById(Integer memberId) {
        log.atInfo().addArgument(memberId).setMessage("Getting member with id={}").log();
        return Response.ok(memberService.getMember(memberId)).build();
    }

}
