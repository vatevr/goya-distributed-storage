build:
	java -version
	mvn clean package -U

run:
	docker-compose up -d --force-recreate

client:
	cd ./scenarios
	docker build -t client_scenarios:latest ./scenarios -f ./scenarios/Dockerfile
	docker run -d --net=goya-distributed-storage_goya-network -e HOST='manager' --name scenarios client_scenarios:latest

destroy:
	docker-compose down
	docker image rm node
	docker image rm manager
	docker image rm client_scenarios -f
	docker container rm client_scenarios:latest