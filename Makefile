GEONAMES_COUNTRIES := infra/mysql/geo_data/countries_slim.tsv
GEONAMES_CITIES    := infra/mysql/geo_data/cities_slim.tsv

up: geodata
	docker compose up -d

geodata: $(GEONAMES_COUNTRIES) $(GEONAMES_CITIES)

$(GEONAMES_COUNTRIES) $(GEONAMES_CITIES):
	@echo "→ GeoNames data missing, running download script..."
	./dl_and_prepare_location_data.sh

geodata-refresh:
	rm -f $(GEONAMES_COUNTRIES) $(GEONAMES_CITIES)
	./dl_and_prepare_location_data.sh

reset-db:
	docker compose down -v
	$(MAKE) up

.PHONY: up geodata geodata-refresh reset-db
