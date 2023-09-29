package io.github.elvisciotti.common.dynamo;

import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

abstract public class AbstractPkSkDynamoRepository<E> extends AbstractDynamoRepository<E> {

    private static Key keyFromPkAndSk(String pk, String sk) {
        return Key.builder()
                .partitionValue(pk)
                .sortValue(sk)
                .build();
    }

    abstract protected String getSk(E e);

    public Optional<E> findOneByPkAndSk(String pk, String sk) {
        Key key = keyFromPkAndSk(pk, sk);

        return Optional.ofNullable(table.getItem(key));
    }

    public List<E> findByPkOrderSkDesc(String pk) {
        return findByPkOrderedSkDesc(pk)
                .items().stream()
                .collect(Collectors.toList());
    }

    public PageIterable<E> findByPkOrderedSkDesc(String pk) {
        QueryConditional queryByPk = QueryConditional.keyEqualTo(
                k -> k.partitionValue(pk)
        );
        QueryEnhancedRequest queryByPkWithSkDesc = QueryEnhancedRequest.builder()
                .scanIndexForward(true)
                .queryConditional(queryByPk).build();

        return table.query(queryByPkWithSkDesc);
    }


    public void deleteByPkAndSk(String pk, String sk) {
        table.deleteItem(keyFromPkAndSk(pk, sk));
    }

    public String nextAvailableSk(String pk) {
        int maxUsedId = findByPkOrderedSkDesc(pk)
                .items().stream()
                .mapToInt(r -> Integer.valueOf(getSk(r)))
                .max().orElse(0);

        return String.valueOf(maxUsedId + 1);
    }
}
