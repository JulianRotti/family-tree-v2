CREATE TABLE geonames_countries (
    country_code CHAR(2)      NOT NULL,
    country_name VARCHAR(100) NOT NULL,
    PRIMARY KEY (country_code)
) ENGINE=InnoDB;

CREATE TABLE geonames_cities (
    id           INT          NOT NULL,
    name         VARCHAR(200) NOT NULL,
    ascii_name   VARCHAR(200) NOT NULL,
    lat          DECIMAL(9,6) NOT NULL,
    lng          DECIMAL(9,6) NOT NULL,
    country_code CHAR(2)      NOT NULL,
    population   INT          NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_ascii_name (ascii_name),
    INDEX idx_country    (country_code),
    FOREIGN KEY (country_code) REFERENCES geonames_countries(country_code)
) ENGINE=InnoDB;