package com.ch.cloud.kafka.mapper;

import com.ch.cloud.kafka.model.ContentSearch;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

public interface ContentSearchMapper extends Mapper<ContentSearch> {

    @Update({"update bt_content_search set status = '1', update_at = now() where id = #{id} and status = '0'"})
    int start(@Param("id") Long id);

    @Update({"update bt_content_search set status = #{status}, update_at = now() where id = #{id} and status = '1'"})
    int end(@Param("id") Long id, @Param("status") String status);
}