package org.lunskra.core.usecase;

import org.lunskra.core.domain.Member;

import java.util.Map;

/**
 * Mutable counters threaded through the recursive tree-building calls.
 * Passed by reference so every recursive invocation updates the same instance.
 */
public class Counts {
    int numberTotal = 0;
    int numberLiving = 0;
    int numberGenerations = 0;

    /**
     * Increments {@code numberTotal} and, when the member is alive (no {@code deathDate}),
     * also {@code numberLiving}.
     */
    void incrementFor(Integer memberId, Map<Integer, Member> membersById) {
        numberTotal++;
        Member member = membersById.get(memberId);
        if (member != null && member.getDeathDate() == null) {
            numberLiving++;
        }
    }
}