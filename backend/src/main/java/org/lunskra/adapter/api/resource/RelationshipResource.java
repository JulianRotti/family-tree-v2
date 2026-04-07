package org.lunskra.adapter.api.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lunskra.adapter.api.service.RelationshipService;
import org.lunskra.family_tree.api.RelationshipsApi;
import org.lunskra.family_tree.api.model.RelationshipDto;
import org.lunskra.family_tree.api.model.RelationshipUpdateRequestDto;

import java.net.URI;

/**
 * REST adapter for relationship CRUD operations.
 * Implements the {@code RelationshipsApi} interface generated from the OpenAPI specification.
 * All business logic is delegated to {@link RelationshipService}.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class RelationshipResource implements RelationshipsApi {

    private final RelationshipService relationshipService;

    @RolesAllowed("view")
    @Override
    public Response listRelationships() {
        log.atInfo().setMessage("Listing all relationships").log();
        return Response.ok(relationshipService.listRelationships()).build();
    }

    @RolesAllowed("view")
    @Override
    public Response getRelationshipByMemberPair(Integer firstMemberId, Integer secondMemberId) {
        log.atInfo().addArgument(firstMemberId).addArgument(secondMemberId).setMessage(
                "Get relationship for firstMemberId={} and secondMemberId={}"
        ).log();
        return Response.ok(relationshipService.getRelationshipByMemberPair(firstMemberId, secondMemberId)).build();
    }

    @RolesAllowed("create")
    @Override
    public Response createRelationship(RelationshipDto body) {
        log.atInfo().addArgument(body.getFirstMemberId()).addArgument(body.getSecondMemberId()).addArgument(body.getRelationship()).setMessage(
                "Creating relationship between firstMemberId={} and secondMemberId={} of type={}"
        ).log();
        RelationshipDto created = relationshipService.createRelationship(body);
        return Response
                .created(URI.create("/api/relationships/" + created.getFirstMemberId() + "/" + created.getSecondMemberId()))
                .entity(created)
                .build();
    }

    @RolesAllowed("delete")
    @Override
    public Response deleteRelationshipByMemberPair(Integer firstMemberId, Integer secondMemberId) {
        log.atInfo().addArgument(firstMemberId).addArgument(secondMemberId).setMessage(
                "Deleting relationship between firstMemberId={} and secondMemberId={}"
        ).log();
        relationshipService.deleteRelationshipByMemberPair(firstMemberId, secondMemberId);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @RolesAllowed("edit")
    @Override
    public Response updateRelationshipByMemberPair(Integer firstMemberId, Integer secondMemberId,
                                                    RelationshipUpdateRequestDto relationshipUpdateRequestDto) {
        log.atInfo().addArgument(firstMemberId).addArgument(secondMemberId).addArgument(relationshipUpdateRequestDto.getRelationship()).setMessage(
                "Updating relationship between firstMemberId={} and secondMemberId={} to type={}"
        ).log();
        return Response.ok(
                relationshipService.updateRelationshipByMemberPair(firstMemberId, secondMemberId, relationshipUpdateRequestDto)
        ).build();
    }
}
