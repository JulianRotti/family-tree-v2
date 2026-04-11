USE family_tree

SET GLOBAL local_infile = 1;

LOAD DATA INFILE '/docker-entrypoint-initdb.d/test_data/test_data_members.csv'
INTO TABLE members
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(id,first_name,last_name,@initial_last_name,@gender,@birth_date,@death_date,@birth_city,@birth_country,@email,@telephone,@street_number,@plz,@city,@occupation,@notes)
SET 
    initial_last_name = NULLIF(@initial_last_name, ''),
    gender = NULLIF(@gender, ''),
    birth_date = NULLIF(@birth_date, ''),
    death_date = NULLIF(@death_date, ''),
    birth_city = NULLIF(@birth_city, ''),
    birth_country = NULLIF(@birth_country, ''),
    email = NULLIF(@email, ''),
    telephone = NULLIF(@telephone, ''),
    street_number = NULLIF(@street_number, ''),
    plz = NULLIF(@plz, ''),
    city = NULLIF(@city, ''),
    occupation = NULLIF(@occupation, ''),
    notes = NULLIF(@notes, '');

LOAD DATA INFILE '/docker-entrypoint-initdb.d/test_data/test_data_relationships.csv'
INTO TABLE relationships
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(member_1_id, member_2_id, relationship);