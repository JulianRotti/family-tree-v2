package org.lunskra.core.usecase;

/**
 * Groups the three UI spacing values passed into the use case so they can be
 * threaded through the recursive calls without a long parameter list.
 *
 * @param widthOfMemberNode          width of a single member node in the frontend layout
 * @param spaceBetweenMemberAndSpouse horizontal gap between a member and their spouse
 * @param spaceBetweenChildren        horizontal gap between sibling nodes
 */
public record Measures(Float widthOfMemberNode,
                       Float spaceBetweenMemberAndSpouse,
                       Float spaceBetweenChildren) {};