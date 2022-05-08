package com.ch.cloud.nacos.mapper2;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * desc:
 *
 * @author zhimin
 * @date 2022/5/9 00:29
 */
@Mapper
public interface NamespaceProjectsMapper {

    @Select("select project_id from rt_project_namespace where namespace_id = #{namespaceId}")
    List<Long> findProjectId(Long namespaceId);
}
