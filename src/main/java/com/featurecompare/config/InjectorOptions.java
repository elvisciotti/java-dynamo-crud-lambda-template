package com.featurecompare.config;

import lombok.Builder;

public record InjectorOptions(boolean useProdEnv, int dynamoPort) {
    @Builder
    public InjectorOptions {
    }

}
