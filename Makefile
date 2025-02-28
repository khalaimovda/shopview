infra_up:
	docker compose -f infra/compose.yaml up -d

infra_down:
	docker compose -f infra/compose.yaml down