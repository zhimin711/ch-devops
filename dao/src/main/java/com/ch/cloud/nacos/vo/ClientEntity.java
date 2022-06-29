package com.ch.cloud.nacos.vo;

import com.ch.cloud.nacos.domain.NacosCluster;

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

    private String username;

    private String password;

    private T data;

    public ClientEntity(NacosCluster cluster, T data) {
        this.url = cluster.getUrl();
        this.username = cluster.getUsername();
        this.password = cluster.getPassword();
        this.data = data;
    }

    public static <T> ClientEntity<T> build(NacosCluster cluster) {
        ClientEntity<T> clientEntity = new ClientEntity<>();
        clientEntity.url = cluster.getUrl();
        clientEntity.username = cluster.getUsername();
        clientEntity.password = cluster.getPassword();
        return clientEntity;
    }

    public static <T> ClientEntity<T> build(NacosCluster cluster, T data) {
        ClientEntity<T> clientEntity = new ClientEntity<>();
        clientEntity.url = cluster.getUrl();
        clientEntity.username = cluster.getUsername();
        clientEntity.password = cluster.getPassword();
        clientEntity.data = data;
        return clientEntity;
    }
}
