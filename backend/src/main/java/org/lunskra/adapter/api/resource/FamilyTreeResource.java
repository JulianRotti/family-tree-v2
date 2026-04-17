package org.lunskra.adapter.api.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lunskra.adapter.api.mapper.FamilyTreeDtoMapper;
import org.lunskra.adapter.api.service.MemberService;
import org.lunskra.family_tree.api.FamilyTreeApi;
import org.lunskra.family_tree.api.model.FamilyTreeResponseDto;
import org.lunskra.port.in.GenerateFamilyTreeUseCase;

/**
 * REST adapter exposing the family-tree generation use case.
 * Implements the {@code FamilyTreeApi} interface generated from the OpenAPI specification.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class FamilyTreeResource implements FamilyTreeApi {

    private final GenerateFamilyTreeUseCase generateFamilyTreeUseCase;
    private final FamilyTreeDtoMapper mapper;
    private final MemberService service;

    @RolesAllowed("view")
    @Override
    public Response getFamilyTreeByHeadMemberId(
            Integer memberId,
            Float widthOfMemberNode,
            Float spaceBetweenMemberAndSpouse,
            Float spaceBetweenChildren,
            Integer maxDepth
    ) {
        FamilyTreeResponseDto response = mapper.toDto(generateFamilyTreeUseCase.generateFamilyTree(
                memberId, maxDepth, widthOfMemberNode, spaceBetweenMemberAndSpouse, spaceBetweenChildren
        ));

        response.getMembers().forEach(service::resolveImageUrl);

        return Response.ok(response).build();
    }
}
