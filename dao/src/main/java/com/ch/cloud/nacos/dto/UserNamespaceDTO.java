package com.ch.cloud.nacos.dto;

import com.ch.pojo.VueRecord;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述：
 *
 * @author Zhimin.Ma
 * @since 2022/5/16
 */
@Data
public class UserNamespaceDTO {

    private List<VueRecord> clusters = new ArrayList<>();

    private List<VueRecord> namespaces = new ArrayList<>();;
}
