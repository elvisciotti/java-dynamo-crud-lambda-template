import * as pulumi from "@pulumi/pulumi";
import * as aws from "@pulumi/aws";

// constants
const JAR_OUT = __dirname + '/../target/fc-ext-similar-prod.jar'
const PREFIX = 'fc-ext-similar'
const DYNAMO_DB_TABLE = 'fc-ext-similar-items';
const HANDLER = 'com.featurecompare.controller.MainHttpHandler';
const ROUTES = [
    'POST /items',
    'GET /items/{locale}',
    'GET /items/{locale}/{id}',
    'PATCH /items/{locale}/{id}',
    'DELETE /items/{locale}/{id}',
    'GET /debug',
];
const DYNAMO_CONFIG = {
    name: DYNAMO_DB_TABLE,
    // keep in sync with Makefile creation and handler.js CRUD
    attributes: [
        {
            name: "locale",
            type: "S",
        },
        {
            name: "id",
            type: "S",
        },
    ],
    hashKey: "locale",
    rangeKey: "id",
    billingMode: "PAY_PER_REQUEST",
    tags: {},
};


// aws id
const awsAccountId = pulumi.output(aws.getCallerIdentity({async: true})).accountId;


// s3 (underscore not accepted)
const s3Bucket = new aws.s3.Bucket(`${PREFIX}-s3-bucket`, {
    bucket: `${PREFIX}-lambda`,
    forceDestroy: true,
});


// let fileArchive = new pulumi.asset.FileArchive("./file.zip");
// let remoteArchive = new pulumi.asset.RemoteArchive("http://contoso.com/file.zip");
// let assetArchive = new pulumi.asset.AssetArchive({
//     "pulumi-added-at.txt": new pulumi.asset.StringAsset(Date.now().toLocaleString()),
//     // TODO
//     "TODO java dir": new pulumi.asset.FileArchive(OUT_DIR),
// });


const s3Object = new aws.s3.BucketObject(`${PREFIX}-s3-object`, {
    bucket: s3Bucket.id,
    key: `${PREFIX}-lambda-files.zip`,
    // source: new pulumi.asset.FileAsset(ZIP_FILE),
    source: new pulumi.asset.FileArchive(JAR_OUT)
});

// iam = policy
const iamForLambda = new aws.iam.Role(`${PREFIX}-iam-role-for-lambda`, {
    assumeRolePolicy: JSON.stringify({
        Version: "2012-10-17",
        Statement: [{
            Sid: "",
            Action: "sts:AssumeRole",
            Effect: "Allow",
            Principal: {
                Service: "lambda.amazonaws.com",
            },
        }],
    }),
});
new aws.iam.RolePolicyAttachment(`${PREFIX}-lambda-iam-role-policy`, {
    role: iamForLambda,
    policyArn: aws.iam.ManagedPolicies.AWSLambdaExecute,
});

// API Gateway
const apiGateway = new aws.apigatewayv2.Api(`${PREFIX}-api-gw`, {
    protocolType: "HTTP",

});
const apiGwLogGroup = new aws.cloudwatch.LogGroup(`${PREFIX}-gw-log-group`, {
    name: pulumi.interpolate`/aws/api_gw/${apiGateway.name}`,
    retentionInDays: 1,
});
const apiStage = new aws.apigatewayv2.Stage(`${PREFIX}-stage`, {
    apiId: apiGateway.id,
    name: "prod",
    autoDeploy: true,
    accessLogSettings: {
        destinationArn: pulumi.output(apiGwLogGroup).apply((logGroup) => logGroup.arn),
        format: JSON.stringify({
            requestId: "$context.requestId",
            sourceIp: "$context.identity.sourceIp",
            requestTime: "$context.requestTime",
            protocol: "$context.protocol",
            httpMethod: "$context.httpMethod",
            resourcePath: "$context.resourcePath",
            routeKey: "$context.routeKey",
            status: "$context.status",
            responseLength: "$context.responseLength",
            integrationErrorMessage: "$context.integrationErrorMessage",
        }),
    },
});


const db = new aws.dynamodb.Table(DYNAMO_DB_TABLE, DYNAMO_CONFIG);

const dynamoDBPolicy = new aws.iam.PolicyAttachment("dynamoDBPolicy", {
    policyArn: "arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess", // Replace with a more restrictive policy if needed
    roles: [iamForLambda.name],
});


const lambdaFunction = new aws.lambda.Function(`${PREFIX}-lambda-main`, {
    s3Bucket: s3Bucket.id,
    s3Key: s3Object.key,
    runtime: "java17",
    handler: HANDLER,
    sourceCodeHash: pulumi.output(s3Object).apply((b) => b.etag),
    role: iamForLambda.arn,
    timeout: 10
});

const cloudwatchLogGroup = new aws.cloudwatch.LogGroup(`${PREFIX}-lambda-log-group`, {
    name: pulumi.interpolate`/aws/lambda/${lambdaFunction.name}`,
    retentionInDays: 1,
});

const lambdaPermission = new aws.lambda.Permission(`${PREFIX}-lambda-permission-for-invoking`, {
    action: "lambda:InvokeFunction",
    function: lambdaFunction.name,
    principal: 'apigateway.amazonaws.com',
    sourceArn: pulumi.interpolate`${apiGateway.executionArn}/*/*`, // pulumi.interpolate
});

const gwIntegration = new aws.apigatewayv2.Integration(`${PREFIX}-gw-integration`, {
    apiId: apiGateway.id,
    integrationUri: lambdaFunction.invokeArn,
    integrationType: 'AWS_PROXY',
    integrationMethod: 'POST',
});


ROUTES.forEach((routeKey, index) => {
    new aws.apigatewayv2.Route(`${PREFIX}-gw-route-${routeKey.replace(/[^a-zA-Z]/g, '').toLowerCase()}`, {
        apiId: apiGateway.id,
        routeKey: routeKey,
        target: pulumi.interpolate`integrations/${gwIntegration.id}`,
    });
})


const lambdaDynamoDBPermission = new aws.lambda.Permission(`${PREFIX}-lambda-permission-for-dynamo`, {
    action: "lambda:InvokeFunction",
    function: lambdaFunction.name,
    principal: "dynamodb.amazonaws.com",
    sourceArn: pulumi.interpolate`arn:aws:dynamodb:${aws.config.region}:${awsAccountId}:table/${db.name}`,
});


// Output
exports.invoke_url = pulumi.interpolate`${apiStage.invokeUrl}`;

