DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    room_id VARCHAR(255) NOT NULL,
    id_number VARCHAR(255) NOT NULL,
    count INT,
    energy DOUBLE,
    cost DOUBLE
);

DROP TABLE IF EXISTS `report_item`;

CREATE TABLE report_item (
    id bigint AUTO_INCREMENT NOT NULL,
    room_id VARCHAR(255) NOT NULL,
    state VARCHAR(255) NOT NULL,
    fan_speed VARCHAR(255) NOT NULL,
    duration VARCHAR(255),
    start_time VARCHAR(255),
    end_time VARCHAR(255),
    start_temp DOUBLE,
    end_temp DOUBLE,
    energy DOUBLE,
    cost DOUBLE,
    PRIMARY KEY (id)
)