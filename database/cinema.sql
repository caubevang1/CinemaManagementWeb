-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: cinemaweb
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */
;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */
;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */
;
/*!50503 SET NAMES utf8 */
;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */
;
/*!40103 SET TIME_ZONE='+00:00' */
;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */
;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */
;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */
;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */
;

--
-- Table structure for table `booking`
--

DROP TABLE IF EXISTS `booking`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `booking` (
    `booking_id` int NOT NULL AUTO_INCREMENT,
    `schedule_id` int NOT NULL,
    `price` decimal(12, 2) NOT NULL,
    `booking_day` datetime(6) DEFAULT NULL,
    `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    PRIMARY KEY (`booking_id`),
    KEY `fk_booking_user_idx` (`user_id`),
    KEY `fk_booking_schedule_idx` (`schedule_id`),
    CONSTRAINT `fk_booking_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `schedule` (`schedule_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `FKkgseyy7t56x7lkjgu3wah5s3t` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 3 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `booking`
--

LOCK TABLES `booking` WRITE;
/*!40000 ALTER TABLE `booking` DISABLE KEYS */
;
/*!40000 ALTER TABLE `booking` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `booking_seat`
--

DROP TABLE IF EXISTS `booking_seat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `booking_seat` (
    `id` int NOT NULL AUTO_INCREMENT,
    `booking_id` int DEFAULT NULL,
    `seat_schedule_id` int DEFAULT NULL,
    `price` decimal(12, 2) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_booking_seat_seat_schedule` (`seat_schedule_id`),
    KEY `booking_id` (`booking_id`),
    CONSTRAINT `booking_seat_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `booking` (`booking_id`),
    CONSTRAINT `booking_seat_ibfk_2` FOREIGN KEY (`seat_schedule_id`) REFERENCES `seat_schedule` (`seat_schedule_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `booking_seat`
--

LOCK TABLES `booking_seat` WRITE;
/*!40000 ALTER TABLE `booking_seat` DISABLE KEYS */
;
/*!40000 ALTER TABLE `booking_seat` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `bookingfoodanddrink`
--

DROP TABLE IF EXISTS `bookingfoodanddrink`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `bookingfoodanddrink` (
    `booking_fd_id` int NOT NULL AUTO_INCREMENT,
    `booking_id` int DEFAULT NULL,
    `fd_id` int DEFAULT NULL,
    `quantity` int DEFAULT NULL,
    `price` decimal(12, 2) DEFAULT NULL,
    PRIMARY KEY (`booking_fd_id`),
    KEY `booking_id` (`booking_id`),
    KEY `fd_id` (`fd_id`),
    CONSTRAINT `bookingfoodanddrink_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `booking` (`booking_id`),
    CONSTRAINT `bookingfoodanddrink_ibfk_2` FOREIGN KEY (`fd_id`) REFERENCES `foodanddrink` (`fd_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 2 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `bookingfoodanddrink`
--

LOCK TABLES `bookingfoodanddrink` WRITE;
/*!40000 ALTER TABLE `bookingfoodanddrink` DISABLE KEYS */
;
/*!40000 ALTER TABLE `bookingfoodanddrink` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `cinema`
--

DROP TABLE IF EXISTS `cinema`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `cinema` (
    `cinema_id` int NOT NULL AUTO_INCREMENT,
    `cinema_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `cinema_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    PRIMARY KEY (`cinema_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 4 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `cinema`
--

LOCK TABLES `cinema` WRITE;
/*!40000 ALTER TABLE `cinema` DISABLE KEYS */
;
/*!40000 ALTER TABLE `cinema` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `foodanddrink`
--

DROP TABLE IF EXISTS `foodanddrink`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `foodanddrink` (
    `fd_id` int NOT NULL AUTO_INCREMENT,
    `fd_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `cinema_id` int NOT NULL,
    `fd_price` decimal(12, 2) NOT NULL,
    `image_food_and_drink` longtext COLLATE utf8mb4_unicode_ci,
    PRIMARY KEY (`fd_id`),
    KEY `fk_foodanddrink_room_idx` (`cinema_id`),
    CONSTRAINT `FK2bfct4r9wwpgl4ee44p4kydrn` FOREIGN KEY (`cinema_id`) REFERENCES `cinema` (`cinema_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 8 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `foodanddrink`
--

LOCK TABLES `foodanddrink` WRITE;
/*!40000 ALTER TABLE `foodanddrink` DISABLE KEYS */
;
/*!40000 ALTER TABLE `foodanddrink` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `movie`
--

DROP TABLE IF EXISTS `movie`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;

CREATE TABLE `movie` (
    `movie_id` int NOT NULL AUTO_INCREMENT,
    `movie_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `movie_poster` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `movie_genre` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `movie_length` int NOT NULL,
    `movie_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `movie_review` double DEFAULT NULL,
    `tmdb_id` int DEFAULT NULL,
    `trailer_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `release_date` date DEFAULT NULL, -- Kiểu DATE trong MySQL tương ứng với LocalDate của Java
    -- Trạng thái phim (enum MovieStatus): NOW_SHOWING | COMING_SOON | ENDED.
    -- Public chỉ hiển thị phim != ENDED; admin thấy tất cả.
    `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NOW_SHOWING',
    PRIMARY KEY (`movie_id`),
    -- Chống thêm phim trùng: không cho 2 phim cùng tên + cùng ngày phát hành.
    -- Lưu ý: MySQL coi NULL là phân biệt nên release_date phải có giá trị thì ràng buộc mới hiệu lực.
    UNIQUE KEY `uq_movie_name_release_date` (`movie_name`, `release_date`),
    -- Chống import trùng phim từ TMDB (nhiều phim nhập tay có tmdb_id = NULL vẫn hợp lệ).
    UNIQUE KEY `uq_movie_tmdb_id` (`tmdb_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 12 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────────────────
-- Dành cho DATABASE ĐANG CHẠY (đã tạo bảng movie từ trước): chạy thủ công 1 lần.
-- Nếu đang tồn tại 2 dòng cùng movie_name + cùng release_date (không NULL), phải
-- dedupe trước, nếu không lệnh ALTER sẽ báo lỗi "Duplicate entry".
-- ─────────────────────────────────────────────────────────────────────────────
-- ALTER TABLE `movie`
--   ADD UNIQUE KEY `uq_movie_name_release_date` (`movie_name`, `release_date`),
--   ADD UNIQUE KEY `uq_movie_tmdb_id` (`tmdb_id`);

/*!40101 SET character_set_client = @saved_cs_client */
;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `movie`
--

LOCK TABLES `movie` WRITE;
/*!40000 ALTER TABLE `movie` DISABLE KEYS */
;
/*!40000 ALTER TABLE `movie` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `password_otp`
--

DROP TABLE IF EXISTS `password_otp`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `password_otp` (
    `otp` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `expiry_time` datetime(6) NOT NULL,
    `valid` tinyint(1) NOT NULL,
    `user_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    PRIMARY KEY (`otp`),
    KEY `fk_password_otp_user1_idx` (`user_id`),
    CONSTRAINT `fk_password_otp_user1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `password_otp`
--

LOCK TABLES `password_otp` WRITE;
/*!40000 ALTER TABLE `password_otp` DISABLE KEYS */
;
/*!40000 ALTER TABLE `password_otp` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `permission`
--

DROP TABLE IF EXISTS `permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `permission` (
    `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    PRIMARY KEY (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `permission`
--

LOCK TABLES `permission` WRITE;
/*!40000 ALTER TABLE `permission` DISABLE KEYS */
;
/*!40000 ALTER TABLE `permission` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `role` (
    `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    PRIMARY KEY (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */
;
/*!40000 ALTER TABLE `role` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `role_permissions`
--

DROP TABLE IF EXISTS `role_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `role_permissions` (
    `role_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `permission_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    PRIMARY KEY (
        `role_name`,
        `permission_name`
    ),
    KEY `fk_role_has_permission_permission1_idx` (`permission_name`),
    KEY `fk_role_has_permission_role1_idx` (`role_name`),
    CONSTRAINT `fk_role_has_permission_permission1` FOREIGN KEY (`permission_name`) REFERENCES `permission` (`name`),
    CONSTRAINT `fk_role_has_permission_role1` FOREIGN KEY (`role_name`) REFERENCES `role` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `role_permissions`
--

LOCK TABLES `role_permissions` WRITE;
/*!40000 ALTER TABLE `role_permissions` DISABLE KEYS */
;
/*!40000 ALTER TABLE `role_permissions` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `room`
--

DROP TABLE IF EXISTS `room`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `room` (
    `room_id` int NOT NULL AUTO_INCREMENT,
    `cinema_id` int NOT NULL,
    `room_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `num_col` int NOT NULL,
    `num_row` int NOT NULL,
    PRIMARY KEY (`room_id`),
    KEY `fk_room_cinema_idx` (`cinema_id`),
    CONSTRAINT `fk_room_cinema` FOREIGN KEY (`cinema_id`) REFERENCES `cinema` (`cinema_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 10 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `room`
--

LOCK TABLES `room` WRITE;
/*!40000 ALTER TABLE `room` DISABLE KEYS */
;
/*!40000 ALTER TABLE `room` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `schedule`
--

DROP TABLE IF EXISTS `schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `schedule` (
    `schedule_id` int NOT NULL AUTO_INCREMENT,
    `movie_id` int NOT NULL,
    `room_id` int NOT NULL,
    -- Gộp ngày + giờ vào DATETIME (xử lý đúng suất qua nửa đêm). Rạp suy ra qua room.cinema.
    `schedule_start` datetime NOT NULL,
    `schedule_end` datetime NOT NULL,
    `format` varchar(20) NOT NULL DEFAULT '2D',
    `audio_type` varchar(20) NOT NULL DEFAULT 'SUBTITLE',
    PRIMARY KEY (`schedule_id`),
    KEY `fk_schedule_room_idx` (`room_id`),
    KEY `fk_schedule_film_idx` (`movie_id`),
    CONSTRAINT `fk_schedule_film` FOREIGN KEY (`movie_id`) REFERENCES `movie` (`movie_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_schedule_room` FOREIGN KEY (`room_id`) REFERENCES `room` (`room_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 5 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `schedule`
--

LOCK TABLES `schedule` WRITE;
/*!40000 ALTER TABLE `schedule` DISABLE KEYS */
;
/*!40000 ALTER TABLE `schedule` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `seat`
--

DROP TABLE IF EXISTS `seat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `seat` (
    `seat_id` int NOT NULL AUTO_INCREMENT,
    `seat_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `room_id` int NOT NULL,
    `seat_row` char(1) COLLATE utf8mb4_unicode_ci NOT NULL,
    `seat_number` int NOT NULL,
    `seat_price` decimal(12, 2) NOT NULL,
    PRIMARY KEY (`seat_id`),
    UNIQUE KEY `uq_seat_room_pos` (
        `room_id`,
        `seat_row`,
        `seat_number`
    ),
    KEY `fk_seat_room_idx` (`room_id`),
    CONSTRAINT `fk_seat_room` FOREIGN KEY (`room_id`) REFERENCES `room` (`room_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 181 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `seat`
--

LOCK TABLES `seat` WRITE;
/*!40000 ALTER TABLE `seat` DISABLE KEYS */
;
/*!40000 ALTER TABLE `seat` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `seat_schedule`
--

DROP TABLE IF EXISTS `seat_schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `seat_schedule` (
    `seat_schedule_id` int NOT NULL AUTO_INCREMENT,
    `schedule_id` int NOT NULL,
    `seat_id` int NOT NULL,
    -- 3 trạng thái thay cho boolean: AVAILABLE / HELD / BOOKED.
    `seat_state` varchar(20) NOT NULL DEFAULT 'AVAILABLE',
    `held_until` datetime DEFAULT NULL,
    `held_by_user_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `version` bigint NOT NULL DEFAULT 0,
    `price` decimal(12, 2) DEFAULT NULL,
    PRIMARY KEY (`seat_schedule_id`),
    UNIQUE KEY `uq_seat_schedule_schedule_seat` (`schedule_id`, `seat_id`),
    KEY `seat_schedule_ibfk_2` (`seat_id`),
    KEY `fk_seat_schedule_held_by_idx` (`held_by_user_id`),
    CONSTRAINT `seat_schedule_ibfk_1` FOREIGN KEY (`schedule_id`) REFERENCES `schedule` (`schedule_id`),
    CONSTRAINT `seat_schedule_ibfk_2` FOREIGN KEY (`seat_id`) REFERENCES `seat` (`seat_id`),
    CONSTRAINT `fk_seat_schedule_held_by` FOREIGN KEY (`held_by_user_id`) REFERENCES `user` (`user_id`) ON DELETE SET NULL
) ENGINE = InnoDB AUTO_INCREMENT = 3 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `seat_schedule`
--

LOCK TABLES `seat_schedule` WRITE;
/*!40000 ALTER TABLE `seat_schedule` DISABLE KEYS */
;
/*!40000 ALTER TABLE `seat_schedule` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `user` (
    `user_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `first_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `last_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `date_of_birth` date NOT NULL,
    `gender` int DEFAULT NULL,
    `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `phone_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `point` double DEFAULT '0',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `user_id_UNIQUE` (`user_id`),
    UNIQUE KEY `username_UNIQUE` (`username`),
    UNIQUE KEY `email_UNIQUE` (`email`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */
;
/*!40000 ALTER TABLE `user` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `user_roles`
--

DROP TABLE IF EXISTS `user_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `user_roles` (
    `user_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `role_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    PRIMARY KEY (`user_id`, `role_name`),
    KEY `fk_user_has_role_role1_idx` (`role_name`),
    KEY `fk_user_has_role_user1_idx` (`user_id`),
    CONSTRAINT `fk_user_has_role_role1` FOREIGN KEY (`role_name`) REFERENCES `role` (`name`),
    CONSTRAINT `fk_user_has_role_user1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `user_roles`
--

LOCK TABLES `user_roles` WRITE;
/*!40000 ALTER TABLE `user_roles` DISABLE KEYS */
;
/*!40000 ALTER TABLE `user_roles` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `comment`
--

DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `comment` (
    `comment_id` int NOT NULL AUTO_INCREMENT,
    `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `movie_id` int NOT NULL,
    `parent_id` int DEFAULT NULL,
    `content` text NOT NULL,
    `created_at` datetime NOT NULL,
    `updated_at` datetime DEFAULT NULL,
    PRIMARY KEY (`comment_id`),
    KEY `idx_comment_movie` (`movie_id`),
    KEY `idx_comment_parent` (`parent_id`),
    KEY `fk_comment_user` (`user_id`),
    CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_comment_movie` FOREIGN KEY (`movie_id`) REFERENCES `movie` (`movie_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `comment` (`comment_id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `comment`
--

LOCK TABLES `comment` WRITE;
/*!40000 ALTER TABLE `comment` DISABLE KEYS */
;
/*!40000 ALTER TABLE `comment` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `friendship`
--

DROP TABLE IF EXISTS `friendship`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `friendship` (
    `friendship_id` int NOT NULL AUTO_INCREMENT,
    `requester_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `addressee_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `created_at` datetime NOT NULL,
    `responded_at` datetime DEFAULT NULL,
    PRIMARY KEY (`friendship_id`),
    UNIQUE KEY `uq_friendship_pair` (
        `requester_id`,
        `addressee_id`
    ),
    KEY `idx_friendship_addressee` (`addressee_id`),
    CONSTRAINT `fk_friend_requester` FOREIGN KEY (`requester_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_friend_addressee` FOREIGN KEY (`addressee_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `friendship`
--

LOCK TABLES `friendship` WRITE;
/*!40000 ALTER TABLE `friendship` DISABLE KEYS */
;
/*!40000 ALTER TABLE `friendship` ENABLE KEYS */
;
UNLOCK TABLES;

--
-- Table structure for table `chat_message`
--

DROP TABLE IF EXISTS `chat_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */
;
/*!50503 SET character_set_client = utf8mb4 */
;
CREATE TABLE `chat_message` (
    `message_id` int NOT NULL AUTO_INCREMENT,
    `sender_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `recipient_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `sent_at` datetime NOT NULL,
    `read_at` datetime DEFAULT NULL,
    PRIMARY KEY (`message_id`),
    KEY `idx_chat_pair` (`sender_id`, `recipient_id`, `sent_at`),
    KEY `idx_chat_unread` (`recipient_id`, `read_at`),
    CONSTRAINT `fk_chat_sender` FOREIGN KEY (`sender_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_chat_recipient` FOREIGN KEY (`recipient_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */
;

--
-- Dumping data for table `chat_message`
--

LOCK TABLES `chat_message` WRITE;
/*!40000 ALTER TABLE `chat_message` DISABLE KEYS */
;
/*!40000 ALTER TABLE `chat_message` ENABLE KEYS */
;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */
;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */
;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */
;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */
;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */
;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */
;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */
;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */
;

-- Dump completed on 2025-05-20 23:33:17
