
CREATE TABLE IF NOT EXISTS `upload_file` (
  `id` varchar(32) NOT NULL COMMENT 'id',
  `user_id` varchar(32) NOT NULL COMMENT '用户id',
  `file_name` varchar(100) NOT NULL COMMENT '文件名',
  `suffix` varchar(50) NOT NULL COMMENT '文件后缀',
  `client_file_path` varchar(400) NOT NULL COMMENT '文件在客户端的地址',
  `server_relative_path` varchar(200) NOT NULL COMMENT '服务器相对地址',
  `file_size` bigint(20) NOT NULL DEFAULT '0' COMMENT '文件大小',
  `chunk_size` int(11) NOT NULL DEFAULT '0' COMMENT '块大小',
  `finish_chunk` int(11) NOT NULL DEFAULT '0' COMMENT '完成的块数',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态',
  `transcode_status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '转码状态',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='上传文件';

