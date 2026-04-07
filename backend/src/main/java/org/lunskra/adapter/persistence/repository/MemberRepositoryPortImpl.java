package org.lunskra.adapter.persistence.repository;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lunskra.adapter.persistence.jpa.MemberEntity;
import org.lunskra.adapter.persistence.mapper.MemberJpaMapper;
import org.lunskra.core.domain.Member;
import org.lunskra.core.domain.MemberPage;
import org.lunskra.port.out.MemberRepositoryPort;

import java.time.LocalDate;
import java.util.List;

/**
 * Persistence adapter that implements {@link MemberRepositoryPort} using Quarkus
 * Panache and Hibernate ORM.
 * <p>
 * {@code listMembers} performs a case-insensitive LIKE query on first name and last
 * name, and an exact match on birth date. All filters are optional — passing
 * {@code null} or blank strings skips the corresponding predicate.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class MemberRepositoryPortImpl implements MemberRepositoryPort {

    private final MemberJpaMapper mapper;
    private final MemberPanacheRepository memberPanacheRepository;

    /** {@inheritDoc} */
    @Override
    public MemberPage listMembers(String firstName, String lastName, LocalDate birthDate, int page, int pageSize) {
        log.atDebug().addArgument(firstName).addArgument(lastName).addArgument(birthDate)
                .addArgument(page).addArgument(pageSize)
                .setMessage("Querying members from DB with firstName={}, lastName={}, birthDate={}, page={}, pageSize={}").log();
        String jpql = """
        (:firstName is null or lower(firstName) like concat('%',lower(:firstName),'%'))
        and (:lastName is null or lower(lastName) like concat('%',lower(:lastName),'%'))
        and (:birthDate is null or birthDate = :birthDate)
        """;
        var query = memberPanacheRepository.find(
                jpql,
                Parameters
                        .with("firstName", blankToNull(firstName))
                        .and("lastName", blankToNull(lastName))
                        .and("birthDate", birthDate)
        );
        long totalElements = query.count();
        List<MemberEntity> members = query.page(Page.of(page, pageSize)).list();
        return new MemberPage(mapper.toDomain(members), totalElements);
    }

    /** {@inheritDoc} */
    @Override
    public Member getMember(Integer id) {
        log.atDebug().addArgument(id).setMessage("Fetching member with id={} from DB").log();
        return memberPanacheRepository
                .findByIdOptional(id)
                .map(mapper::toDomain)
                .orElseThrow(() -> new EntityNotFoundException("Member with id " + id + " not found"));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void deleteMember(Integer id) {
        log.atInfo().addArgument(id).setMessage("Deleting member with id={} from DB").log();
        if (!memberPanacheRepository.deleteById(id)) {
            throw new EntityNotFoundException("Member with id " + id + " not found");
        };
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public Member createMember(Member member) {
        log.atInfo().addArgument(member.getFirstName()).addArgument(member.getLastName())
                .setMessage("Persisting new member {} {} to DB").log();
        MemberEntity entity = mapper.toEntity(member);
        memberPanacheRepository.persist(entity);
        return mapper.toDomain(entity);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public Member updateMember(Member member) {
        log.atInfo().addArgument(member.getId()).setMessage("Updating member with id={} in DB").log();
        Integer id = member.getId();
        MemberEntity entityStored = memberPanacheRepository.findByIdOptional(id)
                .orElseThrow(
                        () -> new EntityNotFoundException("Member with id " + id + " not found")
                );

        mapper.updateEntity(member, entityStored);

        return mapper.toDomain(entityStored);
    }

    /**
     * Converts a blank or {@code null} string to {@code null} so that JPQL can
     * treat it as "no filter" via the {@code :param is null} predicate.
     */
    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
