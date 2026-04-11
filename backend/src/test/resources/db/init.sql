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
    email VARCHAR(50),
    telephone VARCHAR(20),
    street_number VARCHAR(100),
    plz VARCHAR(10),
    city VARCHAR(50),
    image_path VARCHAR(255),
    occupation VARCHAR(100),
    notes VARCHAR(500),
    UNIQUE KEY unique_member (first_name, last_name, birth_date)
) ENGINE=InnoDB;

CREATE TABLE relationships (
    id INT AUTO_INCREMENT PRIMARY KEY,
    member_1_id INT NOT NULL,
    member_2_id INT NOT NULL,
    relationship ENUM('PARENT', 'CURRENT_MARRIED_SPOUSE', 'CURRENT_SPOUSE', 'EX_SPOUSE') NOT NULL,
    connection_hash CHAR(32) AS (CONCAT(LEAST(member_1_id, member_2_id), '-', GREATEST(member_1_id, member_2_id))) STORED,
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

CREATE PROCEDURE get_family_tree(IN memberId INT)
BEGIN
    DROP TABLE IF EXISTS temp_combined;
    -- Create a temporary table to hold combined results
    CREATE TABLE temp_combined AS

    -- Use a CTE to get descendants
    WITH RECURSIVE descendants AS (
        -- Base case: Start with the given member
        SELECT
            member_1_id,
            member_2_id,
            relationship
        FROM relationships
        WHERE member_1_id = memberId AND relationship = 'PARENT'

        UNION ALL

        -- Recursive case: Find descendants of the previously found descendants
        SELECT
            r.member_1_id,
            r.member_2_id,
            r.relationship
        FROM
            relationships r
        JOIN
            descendants d ON r.member_1_id = d.member_2_id
        WHERE
            r.relationship = 'PARENT'
    ),
    all_descendants_ids AS (
        SELECT DISTINCT * FROM (SELECT member_1_id FROM descendants UNION SELECT member_2_id FROM descendants) AS all_descendants_ids_temp
        ),
    spouses AS (
        -- Find spouses of the members in the descendants CTE
        SELECT
            r.member_1_id AS member_1_id,
            r.member_2_id AS member_2_id,
            r.relationship
        FROM
            relationships r
        WHERE
            (r.member_1_id IN (SELECT * FROM all_descendants_ids)
            OR r.member_2_id IN (SELECT * FROM all_descendants_ids))
            AND (r.relationship = 'CURRENT_MARRIED_SPOUSE' OR r.relationship = 'CURRENT_SPOUSE' or r.relationship = 'EX_SPOUSE')
    ),
    all_spouses_ids AS (
        SELECT DISTINCT * FROM (SELECT member_1_id FROM spouses UNION SELECT member_2_id FROM spouses) AS all_spouses_ids_temp
    ),
        -- Now include children of the spouses recursively
    spouse_descendants AS (
        SELECT
            member_1_id,
            member_2_id,
            relationship
        FROM
            relationships
        WHERE
            relationship = 'PARENT' AND member_1_id IN (SELECT * FROM all_spouses_ids)
    )
    -- Insert descendants and spouses into temp_combined
    SELECT DISTINCT * FROM (
        SELECT * FROM descendants
        UNION
        SELECT * FROM spouses
        UNION
        SELECT * FROM spouse_descendants) AS combined_results_temp;

    -- First result set: Return the combined relationships
    SELECT * FROM temp_combined;

    SELECT * FROM members
    WHERE id IN (SELECT member_2_id FROM temp_combined UNION SELECT member_1_id FROM temp_combined) OR id = memberId;

    -- Cleanup: Drop the temporary table
    DROP TABLE IF EXISTS temp_combined;
END //

DELIMITER ;