package org.lunskra.adapter.api.resource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lunskra.adapter.api.mapper.FamilyTreeAncestorDtoMapper;
import org.lunskra.adapter.api.service.MemberService;
import org.lunskra.family_tree.api.FamilyTreeAncestorApi;
import org.lunskra.family_tree.api.model.FamilyTreeAncestorResponseDto;
import org.lunskra.port.in.GenerateFamilyTreeAncestorUseCase;

/**
 * REST adapter exposing the family-tree generation use case.
 * Implements the {@code FamilyTreeApi} interface generated from the OpenAPI specification.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class FamilyTreeAncestorResource implements FamilyTreeAncestorApi {

    private final GenerateFamilyTreeAncestorUseCase useCase;
    private final FamilyTreeAncestorDtoMapper mapper;
    private final MemberService service;



    @Override
    public Response getFamilyTreeAncestorByChildMemberId(Integer memberId, Float widthOfMemberNode, Float spaceBetweenMemberNodes, Integer maxDepth) {

        FamilyTreeAncestorResponseDto response = mapper.toDto(
                useCase.generateFamilyTreeAncestor(memberId, maxDepth, widthOfMemberNode, spaceBetweenMemberNodes)
        );

        response.getMembers().forEach(service::resolveImageUrl);

        return Response.ok(response).build();
    }
}
