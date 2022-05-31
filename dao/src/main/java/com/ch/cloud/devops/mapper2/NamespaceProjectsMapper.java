package com.ch.cloud.devops.mapper2;

import com.ch.cloud.devops.dto.NamespaceDto;
import com.ch.cloud.devops.dto.ProjectNamespaceDTO;
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

    @Select("SELECT n.id FROM rt_project_namespace pn " +
            "INNER JOIN bt_namespace n ON n.ID = pn.NAMESPACE_ID " +
            "WHERE pn.project_id = #{projectId} AND n.cluster_id = #{clusterId} AND n.TYPE = #{namespaceType}")
    List<Long> findNamespaceIdsByProjectIdAndClusterIdAndNamespaceType(@Param("projectId") Long projectId, @Param("clusterId") Long clusterId, @Param("namespaceType") String namespaceType);


    @Insert("insert into rt_project_namespace(namespace_id,project_id) values(#{namespaceId},#{projectId})")
    int insert(@Param("namespaceId") Long namespaceId, @Param("projectId") Long projectId);

    @Delete("delete from rt_project_namespace where namespace_id = #{namespaceId} and project_id = #{projectId}")
    int delete(@Param("namespaceId") Long namespaceId, @Param("projectId") Long projectId);

    @Select("SELECT distinct n.cluster_id FROM rt_project_namespace pn " +
            "INNER JOIN bt_namespace n ON n.ID = pn.NAMESPACE_ID " +
            "INNER JOIN bt_nacos_cluster nc ON n.cluster_id = nc.id " +
            "WHERE pn.project_id = #{projectId} AND n.TYPE = #{namespaceType} order by nc.sort asc, nc.id asc")
    List<Long> findClusterIdsByProjectIdAndNamespaceType(@Param("projectId") Long projectId, @Param("namespaceType") String namespaceType);

    @Results({
            @Result(property = "projectId", column = "project_id"),
            @Result(property = "namespaceId", column = "NAMESPACE_ID"),
            @Result(property = "namespaceName", column = "NAME"),
            @Result(property = "groupId", column = "GROUP_ID"),
    })
    @Select("SELECT pn.*, n.name FROM rt_project_namespace pn " +
            "INNER JOIN bt_namespace n ON n.ID = pn.NAMESPACE_ID " +
            "WHERE pn.project_id = #{projectId} AND n.cluster_id = #{clusterId} AND n.TYPE = #{namespaceType}")
    List<ProjectNamespaceDTO> findByProjectIdAndClusterIdAndNamespaceType(@Param("projectId") Long projectId, @Param("clusterId") Long clusterId, @Param("namespaceType") String namespaceType);

    @Select("SELECT n.* FROM rt_project_namespace pn " +
            "INNER JOIN bt_namespace n ON n.ID = pn.NAMESPACE_ID " +
            "WHERE pn.project_id = #{projectId} AND n.cluster_id = #{clusterId} AND n.TYPE = #{namespaceType}")
    List<NamespaceDto> findNamespacesByProjectIdAndClusterIdAndNamespaceType(@Param("projectId") Long projectId, @Param("clusterId") Long clusterId, @Param("namespaceType") String namespaceType);

    @Insert("insert into rt_project_namespace(namespace_id,project_id,group_id) values(#{namespaceId},#{projectId},#{groupId})")
    int insert2(@Param("namespaceId") Long namespaceId, @Param("projectId") Long projectId, @Param("groupId") String groupId);

    @Results({
            @Result(property = "projectId", column = "project_id"),
            @Result(property = "namespaceId", column = "NAMESPACE_ID"),
            @Result(property = "groupId", column = "GROUP_ID"),
    })
    @Select("select * from rt_project_namespace where namespace_id = #{namespaceId} and project_id = #{projectId}")
    ProjectNamespaceDTO findByProjectIdAndNamespaceId(Long projectId, Long namespaceId);
}
