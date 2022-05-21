package com.ch.cloud.devops.mapper2;

import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/5/9 00:29
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

    @Select("SELECT distinct n.cluster_id FROM rt_project_namespace pn " +
            "INNER JOIN bt_namespace n ON n.ID = pn.NAMESPACE_ID " +
            "WHERE pn.project_id = #{projectId} AND n.TYPE = #{namespaceType}")
    List<Long> findClusterIdsByProjectIdAndNamespaceType(@Param("projectId") Long projectId, @Param("namespaceType") String namespaceType);

}
