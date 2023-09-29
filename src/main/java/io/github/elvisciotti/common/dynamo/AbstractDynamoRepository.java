package io.github.elvisciotti.common.dynamo;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

abstract public class AbstractDynamoRepository<E> {

    protected DynamoDbTable<E> table;
    protected DynamoDbEnhancedClient dynamo;

    abstract protected String getPk(E e);

    public Optional<E> findOneByGlobalSecondaryIndex(String indexName, String fieldValue) {
        return table.index(indexName).query(r ->
                        r.queryConditional(QueryConditional.keyEqualTo(k ->
                                k.partitionValue(fieldValue)))).stream().flatMap(r -> r.items()
                        .stream())
                .findFirst();
    }

    public List<E> findAll() {
        return table.scan().items().stream().collect(Collectors.toList());
    }

    public List<E> findAll(Predicate<E> rulePredicate) {
        return table.scan().items().stream().filter(rulePredicate).collect(Collectors.toList());
    }

    public void upsert(E user) {
        table.putItem(user);
    }

    public E patch(E user) {
        return table.updateItem(user);
    }
}
