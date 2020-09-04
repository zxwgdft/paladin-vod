package com.paladin.vod.mapper;

import com.paladin.framework.mybatis.CustomMapper;
import com.paladin.vod.model.UploadFile;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author TontoZhou
 * @since 2020/8/4
 */
public interface UploadFileMapper extends CustomMapper<UploadFile> {

    @Update("UPDATE upload_file SET finish_chunk = finish_chunk + 1, update_time = now() WHERE id = #{id} AND `status` = 1")
    int updateFinishChunk(@Param("id") String id);

    @Update("UPDATE upload_file SET `status` = #{status}, update_time = now() WHERE id = #{id}")
    int updateStatus(@Param("id") String id, @Param("status") Integer status);

    @Update("UPDATE upload_file SET `status` = 3 WHERE `status` = 1 AND update_time < date_sub(now(), INTERVAL 5 DAY)")
    int updateExpiredUploadFile();

    @Select("SELECT id, server_relative_path AS serverRelativePath, suffix FROM upload_file WHERE `status` = 3")
    List<UploadFile> findErrorUploadFile();

    @Update("UPDATE upload_file SET `transcode_status` = #{status} WHERE id = #{id}")
    int updateTranscodeStatus(@Param("id") String id, @Param("status") int status);

    @Select("SELECT transcode_status FROM upload_file WHERE id = #{id}")
    int getTranscodeStatus(@Param("id") String id);
}
