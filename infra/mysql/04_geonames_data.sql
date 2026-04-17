LOAD DATA INFILE '/docker-entrypoint-initdb.d/countries_slim.tsv'
INTO TABLE geonames_countries
FIELDS TERMINATED BY '\t'
LINES  TERMINATED BY '\n'
(country_code, country_name);

LOAD DATA INFILE '/docker-entrypoint-initdb.d/cities_slim.tsv'
INTO TABLE geonames_cities
FIELDS TERMINATED BY '\t'
LINES  TERMINATED BY '\n'
(id, name, ascii_name, lat, lng, country_code, population);