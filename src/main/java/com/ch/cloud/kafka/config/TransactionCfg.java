package com.ch.cloud.kafka.config;

import com.ch.shiro.tx.AbstractTransaction;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;

/**
 * 描述：com.ch.cloud.kafka.config
 *
 * @author 80002023
 *         2017/3/8.
 * @version 1.0
 * @since 1.8
 */
@Configuration
@AutoConfigureAfter(MybatisCfg.class)
public class TransactionCfg extends AbstractTransaction {

    @Override
    @ConditionalOnMissingBean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        return super.defaultAdvisorAutoProxyCreator();
    }
}
