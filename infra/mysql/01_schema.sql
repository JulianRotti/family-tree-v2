CREATE DATABASE IF NOT EXISTS family_tree;
USE family_tree;

-- Disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS members;
DROP TABLE IF EXISTS relationships;
DROP PROCEDURE IF EXISTS get_family_tree;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE members (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    initial_last_name VARCHAR(50),
    gender ENUM('M','F','D'),
    birth_date DATE,
    death_date DATE,
    birth_city VARCHAR(50),
    birth_country VARCHAR(50),
    birth_lat DECIMAL(9,6),
    birth_lng DECIMAL(9,6),
    email VARCHAR(50),
    telephone VARCHAR(20),
    street_number VARCHAR(100),
    plz VARCHAR(10),
    city VARCHAR(50),
    image_path VARCHAR(255),
    occupation VARCHAR(100),
    notes VARCHAR(500),
    tenant_id CHAR(36) NOT NULL,
    UNIQUE KEY unique_member (tenant_id, first_name, last_name, birth_date)
) ENGINE=InnoDB;

CREATE TABLE relationships (
    id INT AUTO_INCREMENT PRIMARY KEY,
    member_1_id INT NOT NULL,
    member_2_id INT NOT NULL,
    relationship ENUM('PARENT', 'CURRENT_MARRIED_SPOUSE', 'CURRENT_SPOUSE', 'EX_SPOUSE') NOT NULL,
    connection_hash CHAR(32) AS (CONCAT(LEAST(member_1_id, member_2_id), '-', GREATEST(member_1_id, member_2_id))) STORED,
    tenant_id CHAR(36) NOT NULL,
    FOREIGN KEY (member_1_id) REFERENCES members(id), -- ON DELETE CASCADE,
    FOREIGN KEY (member_2_id) REFERENCES members(id), -- ON DELETE CASCADE,
    UNIQUE (connection_hash, relationship)
) ENGINE=InnoDB;

-- #FIXME Workaround for failing 'ON DELETE CASCADE' in relationships table
DELIMITER //

CREATE TRIGGER before_member_delete
BEFORE DELETE ON members
FOR EACH ROW
BEGIN
    DELETE FROM relationships
    WHERE member_1_id = OLD.id
       OR member_2_id = OLD.id;
END //

DELIMITER ;

DELIMITER //

CREATE PROCEDURE get_family_tree(IN memberId INT, IN maxDepth INT, IN p_tenant_id VARCHAR(36))
BEGIN
    DROP TEMPORARY TABLE IF EXISTS temp_combined;
    CREATE TEMPORARY TABLE temp_combined AS

    WITH RECURSIVE descendants AS (
        SELECT
            member_1_id,
            member_2_id,
            relationship,
            1 AS depth
        FROM relationships
        WHERE member_1_id = memberId AND relationship = 'PARENT' AND tenant_id = p_tenant_id

        UNION ALL

        SELECT
            r.member_1_id,
            r.member_2_id,
            r.relationship,
            d.depth + 1
        FROM relationships r
        JOIN descendants d ON r.member_1_id = d.member_2_id
        WHERE r.relationship = 'PARENT'
          AND r.tenant_id = p_tenant_id
          AND (maxDepth IS NULL OR d.depth < maxDepth)
    ),
    -- Track the actual depth of every member in the tree
    -- member_1_id is at (depth - 1), member_2_id is at depth
    member_depths AS (
        SELECT member_2_id AS id, depth     FROM descendants
        UNION
        SELECT member_1_id AS id, depth - 1 FROM descendants
    ),
    member_min_depths AS (
        SELECT id, MIN(depth) AS depth FROM member_depths GROUP BY id
    ),
    all_descendants_ids AS (
        SELECT id FROM member_min_depths
    ),
    spouses AS (
        SELECT r.member_1_id, r.member_2_id, r.relationship
        FROM relationships r
        WHERE
            (r.member_1_id IN (SELECT id FROM all_descendants_ids)
            OR r.member_2_id IN (SELECT id FROM all_descendants_ids))
            AND r.relationship IN ('CURRENT_MARRIED_SPOUSE', 'CURRENT_SPOUSE', 'EX_SPOUSE')
            AND r.tenant_id = p_tenant_id
    ),
    -- ✅ Only spouses of members who have NOT yet reached maxDepth
    -- These are the only spouses whose children should be included
    eligible_spouse_parents AS (
        SELECT DISTINCT
            CASE WHEN r.member_1_id = m.id THEN r.member_2_id ELSE r.member_1_id END AS id
        FROM relationships r
        JOIN member_min_depths m ON (r.member_1_id = m.id OR r.member_2_id = m.id)
        WHERE r.relationship IN ('CURRENT_MARRIED_SPOUSE', 'CURRENT_SPOUSE', 'EX_SPOUSE')
          AND r.tenant_id = p_tenant_id
          AND (maxDepth IS NULL OR m.depth < maxDepth)  -- ✅ depth guard
    ),
    spouse_descendants AS (
        SELECT member_1_id, member_2_id, relationship
        FROM relationships
        WHERE relationship = 'PARENT'
          AND tenant_id = p_tenant_id
          AND member_1_id IN (SELECT id FROM eligible_spouse_parents)
    ),
    -- Always include current spouse relationships of the head, even when the head has no children.
    -- Without this, all_descendants_ids is empty when the head is childless,
    -- so the spouses CTE never picks up the head's own spouse relationship.
    head_current_spouses AS (
        SELECT member_1_id, member_2_id, relationship
        FROM relationships
        WHERE (member_1_id = memberId OR member_2_id = memberId)
          AND relationship IN ('CURRENT_MARRIED_SPOUSE', 'CURRENT_SPOUSE')
          AND tenant_id = p_tenant_id
    )
    SELECT DISTINCT member_1_id, member_2_id, relationship FROM (
        SELECT member_1_id, member_2_id, relationship FROM descendants
        UNION
        SELECT member_1_id, member_2_id, relationship FROM spouses
        UNION
        SELECT member_1_id, member_2_id, relationship FROM spouse_descendants
        UNION
        SELECT member_1_id, member_2_id, relationship FROM head_current_spouses
    ) AS combined_results_temp;

    SELECT * FROM temp_combined;

    SELECT * FROM members m
    WHERE (
        EXISTS (
            SELECT 1 FROM temp_combined tc
            WHERE tc.member_1_id = m.id OR tc.member_2_id = m.id
        ) OR m.id = memberId
    ) AND m.tenant_id = p_tenant_id;

    DROP TEMPORARY TABLE IF EXISTS temp_combined;
END //

DELIMITER ;

DELIMITER //

CREATE PROCEDURE get_family_tree_reverse(IN memberId INT, IN maxDepth INT, IN p_tenant_id VARCHAR(36))
BEGIN
    DROP TEMPORARY TABLE IF EXISTS temp_combined;
    CREATE TEMPORARY TABLE temp_combined AS

    WITH RECURSIVE ancestors AS (
        SELECT
            member_1_id,
            member_2_id,
            relationship,
            1 AS depth
        FROM relationships
        WHERE member_2_id = memberId AND relationship = 'PARENT' AND tenant_id = p_tenant_id

        UNION ALL

        SELECT
            r.member_1_id,
            r.member_2_id,
            r.relationship,
            a.depth + 1
        FROM relationships r
        JOIN ancestors a ON r.member_2_id = a.member_1_id
        WHERE r.relationship = 'PARENT'
          AND r.tenant_id = p_tenant_id
          AND (maxDepth IS NULL OR a.depth < maxDepth)
    ),
    all_ancestor_ids AS (
        SELECT DISTINCT * FROM (
            SELECT member_1_id FROM ancestors
            UNION
            SELECT member_2_id FROM ancestors
        ) AS all_ancestor_ids_temp
    ),
    ancestor_spouses AS (
        SELECT
            r.member_1_id,
            r.member_2_id,
            r.relationship
        FROM relationships r
        WHERE
            r.member_1_id IN (SELECT * FROM all_ancestor_ids)
            AND r.member_2_id IN (SELECT * FROM all_ancestor_ids)
            AND r.relationship IN ('CURRENT_MARRIED_SPOUSE', 'CURRENT_SPOUSE', 'EX_SPOUSE')
            AND r.tenant_id = p_tenant_id
    )
    SELECT DISTINCT member_1_id, member_2_id, relationship FROM (
        SELECT member_1_id, member_2_id, relationship FROM ancestors
        UNION
        SELECT member_1_id, member_2_id, relationship FROM ancestor_spouses
    ) AS combined;

    SELECT * FROM temp_combined;

    SELECT * FROM members m
    WHERE (
        EXISTS (
            SELECT 1 FROM temp_combined tc
            WHERE tc.member_1_id = m.id OR tc.member_2_id = m.id
        ) OR m.id = memberId
    ) AND m.tenant_id = p_tenant_id;

    DROP TEMPORARY TABLE IF EXISTS temp_combined;
END //

DELIMITER ;