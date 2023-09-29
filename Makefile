help:
	@cat Makefile  | grep  "^[a-z-].*" | grep -v help
	@echo "build commit push <-- common one"
	@echo "deploy <-- Build and test, deploy, commit + push, sleep in 30 seconds"



#dynamo-up:
#	docker-compose up -d
#
## auto launched by tests
#dynamo-recreate: dynamo-up
#	AWS_PAGER="" aws dynamodb delete-table --table-name fc-ext-similar-items  --endpoint-url http://localhost:8002 --region us-east-1 || true
#	AWS_PAGER="" aws dynamodb create-table --table-name fc-ext-similar-items  --endpoint-url http://localhost:8002 --region us-east-1 \
#      --attribute-definitions \
#        AttributeName=locale,AttributeType=S \
#        AttributeName=id,AttributeType=S \
#      --key-schema \
#        AttributeName=locale,KeyType=HASH \
#        AttributeName=id,KeyType=RANGE \
#      --provisioned-throughput \
#        ReadCapacityUnits=5,WriteCapacityUnits=5

#test-prepare:
	#make dynamo-recreate

test: #test-prepare
	./mvnw test

testcommit: test
	 git commit -a -m "test passed" && say "passed and committed"

build:
	./mvnw clean
	#make test-prepare
	./mvnw package
	ls -la target/fc-ext-similar-prod.jar

commit:
	git add .
	git commit -a -m "after deploy auto - `date`" || echo already commited

up: build
	pulumi up --cwd=pulumi -s dev -y
	echo "in case of errors, run refresh first"
	make curl

refresh:
	pulumi refresh --cwd=pulumi -s dev

destroy:
	pulumi destroy --cwd=pulumi -s dev # -y
