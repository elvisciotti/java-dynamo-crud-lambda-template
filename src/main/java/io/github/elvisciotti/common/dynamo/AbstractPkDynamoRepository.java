package io.github.elvisciotti.common.dynamo;

import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.util.Optional;

abstract public class AbstractPkDynamoRepository<E> extends AbstractDynamoRepository<E> {

    private Key keyFromPk(String pk) {
        return Key.builder()
                .partitionValue(pk)
                .build();
    }


    public Optional<E> findOneByPk(String pk) {
        return Optional.ofNullable(table.getItem(keyFromPk(pk)));
    }

    // DELETE
    public E deleteByPk(String pk) {
        return table.deleteItem(keyFromPk(pk));
    }
}
