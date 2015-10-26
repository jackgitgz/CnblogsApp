CREATE TABLE `app_article` (
  `linkmd5id` char(32) NOT NULL COMMENT 'url md5编码id',
  `title` text COMMENT '标题',
  `desc` text COMMENT '描述',
  `link` text COMMENT 'url链接',
  `listUrl` text COMMENT '分页url链接',
  `view` int(11) DEFAULT NULL COMMENT '查看次数',
  `comment` int(11) DEFAULT NULL COMMENT '评论次数',
  `diggnum` int(11) DEFAULT NULL COMMENT '赞次数',
  `postdate` varchar(256) DEFAULT NULL COMMENT '发布时间',
  `updated` datetime DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY (`linkmd5id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8