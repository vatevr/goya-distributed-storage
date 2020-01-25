build:
	java -version
	mvn clean package -U
	docker-compose up -d --force-recreate

destroy:
	docker-compose down
	docker image rm node
	docker image rm manager