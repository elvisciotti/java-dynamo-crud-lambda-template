package com.featurecompare.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.LocalDateTime;

@DynamoDbBean
@Data
@Builder(builderClassName = "RuleBuilder")
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    private String locale;
    private String id;
    private String extMode;
    private String url;
    private String title;
    private String price;
    private Float stars;
    private Integer starsCount;
    private LocalDateTime addedAt;
    private LocalDateTime updatedAt;

    @DynamoDbPartitionKey
    public String getLocale() {
        return locale;
    }

    @DynamoDbSortKey
    public String getId() {
        return id;
    }
}
