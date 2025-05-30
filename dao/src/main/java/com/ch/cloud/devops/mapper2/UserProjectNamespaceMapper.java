package com.ch.cloud.devops.mapper2;

import com.ch.cloud.devops.dto.NamespaceDto;
import com.ch.cloud.devops.dto.UserProjectNamespaceDto;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * decs:
 *
 * @author zhimin.ma
 * @since 221/11/6
 */
@Mapper
public interface UserProjectNamespaceMapper {
    
    @Insert("INSERT INTO rt_user_namespace (PROJECT_ID,USER_ID,NAMESPACE_ID,permission) VALUES (#{projectId},#{userId},#{namespaceId},#{permission})")
    int insert(@Param("projectId") Long projectId, @Param("userId") String userId,
            @Param("namespaceId") Long namespaceId, @Param("permission") String permission);
    
    @Delete("DELETE FROM rt_user_namespace where PROJECT_ID=#{projectId} and USER_ID=#{userId} and NAMESPACE_ID=#{namespaceId}")
    int delete(Long projectId, String userId, Long namespaceId);
    
    @Select("Select permission FROM rt_user_namespace where PROJECT_ID=#{projectId} and USER_ID=#{userId} and NAMESPACE_ID=#{namespaceId}")
    String selectPermission(Long projectId, String userId, Long namespaceId);
    
    @Delete("update rt_user_namespace set permission=#{permission} where PROJECT_ID=#{projectId} and USER_ID=#{userId} and NAMESPACE_ID=#{namespaceId}")
    int updatePermission(Long projectId, String userId, Long namespaceId, String permission);
    
    @Select("SELECT count(1) from rt_user_namespace where PROJECT_ID=#{projectId} and USER_ID=#{userId} and NAMESPACE_ID=#{namespaceId}")
    int exists(@Param("projectId") Long projectId, @Param("userId") String userId,
            @Param("namespaceId") Long namespaceId);
    
    @Select("select count(1) from rt_user_namespace where USER_ID=#{userId} and NAMESPACE_ID=#{namespaceId} and PROJECT_ID=#{projectId}")
    int countByUserIdAndNamespaceIdAndProjectId(@Param("userId") String userId,
            @Param("namespaceId") Long namespaceId, @Param("projectId") Long projectId);
    
    @Select("select count(1) from rt_user_namespace where USER_ID=#{userId} and NAMESPACE_ID=#{namespaceId} and PROJECT_ID=#{projectId} "
            + "and permission like concat('%',#{permission},'%')")
    int countByUserIdAndNamespaceIdAndProjectIdLikePermission(@Param("userId") String userId,
            @Param("namespaceId") Long namespaceId, @Param("projectId") Long projectId,  @Param("permission") String permission);
    
    @Select("<script>SELECT t1.id as namespaceId,t1.name as namespaceName,t1.cluster_id as clusterId,t1.uid as nacosNamespaceId"
            + ",t2.permission,t2.project_id as projectId,t2.user_id as userId from bt_namespace t1"
            + " INNER JOIN rt_user_namespace t2 ON t1.id  = t2.NAMESPACE_ID"
            + " WHERE EXISTS(SELECT * FROM rt_project_namespace WHERE PROJECT_ID = t2.PROJECT_ID and NAMESPACE_ID = t2.NAMESPACE_ID)"
            + " and t2.project_id =#{projectId} and t2.USER_ID=#{userId}"
            + "<if test='clusterIds!=null and clusterIds.size()>0'>  and t1.cluster_id in\n"
            + "            <foreach item=\"item\" index=\"index\" collection=\"clusterIds\"\n"
            + "                open=\"(\" separator=\",\" close=\")\">\n" + "                #{item}\n"
            + "            </foreach></if>"
            + " and t1.type = #{namespaceType} order by t1.cluster_id,t1.sort,t1.id asc</script>")
    List<UserProjectNamespaceDto> listUserNamespacesByType(String namespaceType, String userId, Long projectId, List<Long> clusterIds);
    
    @Select("SELECT t1.id,t1.name,t1.cluster_id as clusterId,t1.uid from bt_namespace t1"
            + " INNER JOIN rt_user_namespace t2 ON t1.id  = t2.NAMESPACE_ID"
            + " WHERE EXISTS(SELECT * FROM rt_project_namespace WHERE PROJECT_ID = t2.PROJECT_ID and NAMESPACE_ID = t2.NAMESPACE_ID)"
            + " and t2.project_id =#{projectId} and t2.USER_ID=#{userId} and t1.type = #{namespaceType} order by t1.sort,t1.id asc")
    List<NamespaceDto> findNamespacesByUsernameAndProjectIdAndNamespaceType(String userId, Long projectId,
            String namespaceType);
}
