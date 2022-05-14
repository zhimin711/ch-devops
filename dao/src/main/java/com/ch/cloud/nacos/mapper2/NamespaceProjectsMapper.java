package com.ch.cloud.nacos.mapper2;

import org.apache.ibatis.annotations.*;

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
    List<Long> findProjectIdByNamespaceId(Long namespaceId);

    @Select("select namespace_id from rt_project_namespace where project_id = #{projectId}")
    List<Long> findNamespaceIdByProjectId(Long projectId);

    @Insert("insert into rt_project_namespace(namespace_id,project_id) values(#{namespaceId},#{projectId})")
    int insert(@Param("namespaceId") Long namespaceId, @Param("projectId") Long projectId);

    @Delete("delete from rt_project_namespace where namespace_id = #{namespaceId} and project_id = #{projectId}")
    int delete(@Param("namespaceId") Long namespaceId, @Param("projectId") Long projectId);
}
