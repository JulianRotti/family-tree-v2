package org.lunskra.core.domain;

import java.util.List;

/**
 * A single page of {@link Member} results returned by a paginated query.
 *
 * @param members       the members on the requested page
 * @param totalElements total number of members matching the query across all pages
 */
public record MemberPage(List<Member> members, long totalElements) {}
