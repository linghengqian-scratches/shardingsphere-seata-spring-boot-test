package com.lingh.shardingsphereseataspringboottest.commons.algorithm;

import lombok.Getter;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithmMetaData;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;

@Getter
public final class TestQueryAssistedShardingEncryptAlgorithm implements EncryptAlgorithm {

    private final EncryptAlgorithmMetaData metaData = new EncryptAlgorithmMetaData(false, true, false);

    @Override
    public String encrypt(final Object plainValue, final AlgorithmSQLContext algorithmSQLContext) {
        return "assistedEncryptValue";
    }

    @Override
    public Object decrypt(final Object cipherValue, final AlgorithmSQLContext algorithmSQLContext) {
        throw new UnsupportedOperationException(String.format("Algorithm `%s` is unsupported to decrypt", getType()));
    }

    @Override
    public String getType() {
        return "assistedTest";
    }
}
