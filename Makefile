app_start:
	./gradlew bootRun

infra_up:
	docker compose -f infra/compose.yaml up -d

infra_down:
	docker compose -f infra/compose.yaml down

data_init:
	psql -h localhost -p 5432 -U username -d shop -f infra/data.sql
	# Password: password (see compose.yaml)

data_clean:
	psql -h localhost -p 5432 -U username -d shop -f infra/clean.sql
	# Password: password (see compose.yaml)

images_copy:
	infra/copy_images.sh

images_clean:
	rm -rf uploads/images/*
