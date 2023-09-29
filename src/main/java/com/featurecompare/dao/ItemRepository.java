package com.featurecompare.dao;

import com.featurecompare.config.Constants;
import io.github.elvisciotti.common.dynamo.AbstractPkSkDynamoRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;


// https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-dynamodb-enhanced.html
@Singleton
@Slf4j
public class ItemRepository extends AbstractPkSkDynamoRepository<Item> {

    @Inject
    public ItemRepository(DynamoDbEnhancedClient dynamo) {
        this.dynamo = dynamo;
        table = this.dynamo.table(Constants.FC_EXT_SIMILAR_TABLE, TableSchema.fromBean(Item.class));
    }

    @Override
    protected String getPk(Item e) {
        return e.getLocale();
    }

    @Override
    protected String getSk(Item e) {
        return e.getId();
    }
}
