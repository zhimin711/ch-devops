package com.ch.cloud.nacos.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/5/8 17:01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientEntity<T> {

    private String url;

    private T data;
}
