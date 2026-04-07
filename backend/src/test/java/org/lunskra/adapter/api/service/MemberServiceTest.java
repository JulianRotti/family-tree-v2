package org.lunskra.adapter.api.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lunskra.adapter.api.mapper.MemberDtoMapper;
import org.lunskra.adapter.utils.MemberDtoGenerator;
import org.lunskra.adapter.utils.MemberGenerator;
import org.lunskra.core.domain.MemberPage;
import org.lunskra.family_tree.api.model.MemberDto;
import org.lunskra.family_tree.api.model.MemberPageDto;
import org.lunskra.port.in.ValidateMemberUseCase;
import org.lunskra.port.out.ImageStoragePort;
import org.lunskra.port.out.MemberRepositoryPort;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    MemberRepositoryPort memberRepositoryPort;

    @Mock
    ValidateMemberUseCase validateMemberUseCase;

    @Mock
    ImageStoragePort imageStoragePort;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        MemberDtoMapper memberDtoMapper = Mappers.getMapper(MemberDtoMapper.class);
        memberService = new MemberService(memberRepositoryPort, memberDtoMapper, validateMemberUseCase, imageStoragePort);
    }
    @Test
    void testListMembers_WhenTwoUsersExist_ThenReturnTwoUsers() {
        when(memberRepositoryPort.listMembers(any(), any(), any(), anyInt(), anyInt())).thenReturn(
                new MemberPage(
                        List.of(
                                MemberGenerator.createLivingMemberWithRequiredData(),
                                MemberGenerator.createLivingMemberWithAllData()
                        ),
                        2L
                )
        );
        MemberPageDto result = memberService.listMembers(null, null, null, 0, 20);
        Assertions.assertEquals(2, result.getContent().size());
        Assertions.assertEquals(2L, result.getTotalElements());
    }

    @Test
    void testGetMember_WhenIdExists_ThenReturnMember() {
        when(memberRepositoryPort.getMember(1)).thenReturn(MemberGenerator.createLivingMemberWithRequiredData());
        Assertions.assertEquals("John", memberService.getMember(1).getFirstName());
    }

    @Test
    void testDeleteMember_WhenCalled_ThenDelegatesToRepo() {
        when(memberRepositoryPort.getMember(1)).thenReturn(MemberGenerator.createLivingMemberWithRequiredData());
        memberService.deleteMember(1);

        verify(memberRepositoryPort).deleteMember(1);
    }

    @Test
    void testCreateMember_WhenValid_ThenValidatesAndReturnsDto() {
        when(memberRepositoryPort.createMember(any())).thenReturn(MemberGenerator.createLivingMemberWithRequiredData());
        MemberDto input = MemberDtoGenerator.createLivingMemberDtoWithRequiredData();

        MemberDto result = memberService.createMember(input, null);

        assertEquals("John", result.getFirstName());
        verify(validateMemberUseCase).validateNewMember(any());
        verify(memberRepositoryPort).createMember(any());
    }

    @Test
    void testUpdateMember_WhenValid_ThenValidatesAndReturnsDto() {
        when(memberRepositoryPort.updateMember(any())).thenReturn(MemberGenerator.createLivingMemberWithRequiredData());
        MemberDto input = MemberDtoGenerator.createLivingMemberDtoWithRequiredData();

        MemberDto result = memberService.updateMember(1, input, null);

        assertEquals("John", result.getFirstName());
        verify(validateMemberUseCase).validateExistingMember(any());
        verify(memberRepositoryPort).updateMember(any());
    }
}