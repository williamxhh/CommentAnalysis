DROP TABLE IF EXISTS `judge`;
CREATE TABLE `judge` (
  `comment_path` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `validation_state` int(11) DEFAULT '0',
  `is_redundant` int(11) NOT NULL DEFAULT '0',
  `validation_content` varchar(255) DEFAULT NULL,
  `completeness_score` int(11) DEFAULT NULL,
  `consistency_score` double DEFAULT NULL,
  `information_score` int(11) DEFAULT NULL,
  `readability_score` int(11) DEFAULT NULL,
  `objectivity_score` int(11) DEFAULT NULL,
  `verifiability_score` int(11) DEFAULT NULL,
  `relativity_score` int(11) DEFAULT NULL,
  `image_count` int(11) NOT NULL DEFAULT '0',
  `url_count` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`comment_path`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



DROP TABLE IF EXISTS `new_template`;
CREATE TABLE `new_template` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `path_file` varchar(255) NOT NULL,
  `lxr_type` varchar(255) DEFAULT NULL,
  `new_template` varchar(2047) DEFAULT NULL,
  `same_editor_template` varchar(2047) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12194 DEFAULT CHARSET=utf8;
