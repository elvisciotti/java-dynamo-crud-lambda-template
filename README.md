# Java 17 Serverless CRUD with DynamoDB Template

Draft of a Java AWS lambda (exposed via API gateway) performing DynamoDB operations exposed via REST.
I used this template to create a lamda service for
my [Browser extension to compare E-commerce products](https://chrome.google.com/webstore/detail/compare-amazon-side-to-si/bbgnjgojcifngncffebelnaljklbiilf)
to suggest similar items to compare with based on what other users compared.

Deployed with Pullumi (typescript) on AWS.

* Java 17
* AWS lamda
* Google Juice to load beans
* Junit 5 with generic TestContainer for DynamoDB
* CRUD operations for an `Item` table with `locale` PK and `id` secondary key
* Jackson to return JSON data
* Makefile to launch tasks

## Example

Post

    curl -H "content-type:application/json" -XPOST -d '{"locale": "en", "id":"asin1", "title": "title2"}'  "https://XXXX.execute-api.us-east-1.amazonaws.com/prod/items" | jq

Get one y PK and Secondary key

    curl "https://XXXX.execute-api.us-east-1.amazonaws.com/prod/items/en/ASIN1" | jq

Get all by PK

    curl "https://XXXX.execute-api.us-east-1.amazonaws.com/prod/items/en" | jq

## TODO

* Logging
* 