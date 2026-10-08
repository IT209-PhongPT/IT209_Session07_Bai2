# Báo cáo Bài tập 2: Quản trị Tường lửa UFW cho Cụm Dịch vụ Multi-port

- **Họ và tên:** Phạm Thanh Phong
- **Mã sinh viên:** N24DTCN120
- **Môn học:** IT209 - Phát triển ứng dụng Web / Cloud Infrastructure
- **Session:** 07 - Truyền tải dữ liệu an toàn & Bảo mật Tường lửa Multi-port
- **Đường dẫn nộp bài:** `homework/session_07/ex2/`

---

## 1. Bối cảnh & Mục tiêu bài tập

Trong mô hình triển khai ứng dụng phân tán (Distributed Architecture / Microservices) trên Cloud VPS, một máy chủ thường vận hành đồng thời nhiều thành phần dịch vụ khác nhau:
1. **Dịch vụ Quản trị từ xa (SSH - Port 22/tcp):** Cho phép Quản trị viên kết nối Terminal mã hóa để vận hành và bảo trì máy chủ.
2. **Dịch vụ Web công khai (Nginx / Apache HTTP - Port 80/tcp):** Phục vụ giao diện người dùng (Frontend Web / Static Assets) cho toàn bộ người dùng Internet.
3. **Dịch vụ Backend API (Spring Boot - Port 8082/tcp):** Cung cấp các Restful Endpoints phục vụ xử lý nghiệp vụ và kiểm thử ứng dụng từ xa.
4. **Hệ quản trị Cơ sở dữ liệu (MySQL Database - Port 3306/tcp):** Lưu trữ dữ liệu cốt lõi của hệ thống (User, Transaction, Logs).

### Mục tiêu an ninh:
- **Thiết lập chính sách mặc định (Default Security Policy):** Chặn toàn bộ lưu lượng đi vào (`default deny incoming`) và cho phép lưu lượng đi ra (`default allow outgoing`).
- **Mở có chọn lọc các cổng công khai:** Mở đúng 3 cổng `22/tcp`, `80/tcp` và `8082/tcp`.
- **Cô lập tầng dữ liệu (Database Isolation):** Tuyệt đối **KHÔNG** mở cổng `3306/tcp` ra Internet để triệt tiêu nguy cơ tấn công dò mật khẩu (Brute-force), khai thác lỗ hổng cơ sở dữ liệu (SQL Injection từ xa) và Ransomware mã hóa dữ liệu tống tiền.

---

## 2. Kiến trúc & Sơ đồ Bảo vệ Tường lửa Đa tầng (Defense-in-Depth)

```mermaid
flowchart TD
    subgraph Internet ["🌐 Không gian Mạng Internet Công khai"]
        UserWeb["Khách truy cập Web (HTTP)"]
        Developer["Lập trình viên / Tester (Spring Boot API)"]
        Sysadmin["Quản trị viên Hệ thống (SSH)"]
        Attacker["Tin tặc / Botnet rà quét cổng (Port Scanner)"]
    end

    subgraph UFW ["🛡️ Tường lửa UFW (Stateful Packet Filter)"]
        direction TB
        DefaultPolicy["Chính sách Mặc định:\nDefault Incoming: DENY\nDefault Outgoing: ALLOW"]
        RuleSSH["Rule 1: ALLOW IN 22/tcp (SSH)"]
        RuleHTTP["Rule 2: ALLOW IN 80/tcp (HTTP)"]
        RuleSpring["Rule 3: ALLOW IN 8082/tcp (Spring Boot)"]
        DropOthers["Drop All Other Ports (Implicit Deny)"]
    end

    subgraph InternalServer ["🖥️ Máy chủ Linux VPS (Localhost / Internal)"]
        direction TB
        SSHService["SSH Daemon (:22)"]
        NginxService["Nginx Web Server (:80)"]
        SpringBootApp["Spring Boot App (:8082)"]
        MySQLDB["MySQL Database Server (:3306)\n(Chỉ lắng nghe 127.0.0.1)"]
    end

    Sysadmin -->|Port 22/tcp| RuleSSH --> SSHService
    UserWeb -->|Port 80/tcp| RuleHTTP --> NginxService
    Developer -->|Port 8082/tcp| RuleSpring --> SpringBootApp
    SpringBootApp -.->|Kết nối nội bộ: localhost:3306| MySQLDB

    Attacker -.->|Quét Port 3306 (MySQL)| DropOthers
    Attacker -.->|Quét Port 6379 (Redis)| DropOthers
    Attacker -.->|Quét Port 27017 (MongoDB)| DropOthers

    classDef allow fill:#d4edda,stroke:#28a745,stroke-width:2px;
    classDef deny fill:#f8d7da,stroke:#dc3545,stroke-width:2px;
    classDef server fill:#e2e3e5,stroke:#383d41,stroke-width:2px;
    class RuleSSH,RuleHTTP,RuleSpring allow;
    class DropOthers,DefaultPolicy deny;
    class InternalServer server;
```

---

## 3. Quy trình Thực thi Lệnh trên VPS (Step-by-Step Execution)

### Bước 1: Thiết lập Chính sách Tường lửa Mặc định
```bash
# Chặn toàn bộ lưu lượng Inbound chưa được định nghĩa
sudo ufw default deny incoming

# Cho phép toàn bộ lưu lượng Outbound (để cập nhật gói tin apt, kết nối API ngoài...)
sudo ufw default allow outgoing
```
**Ý nghĩa kỹ thuật:** Đây là nguyên tắc bảo mật tối quan trọng "Default Deny" (Whitelisting). Bất kỳ cổng nào không được chỉ định rõ ràng trong danh sách luật sẽ bị Kernel Linux âm thầm loại bỏ gói tin (Drop packet).

---

### Bước 2: Cấu hình Mở các Cổng dịch vụ quy định

```bash
# 1. Mở cổng 22/tcp phục vụ quản trị SSH
sudo ufw allow 22/tcp

# 2. Mở cổng 80/tcp phục vụ lưu lượng Web Nginx
sudo ufw allow 80/tcp

# 3. Mở cổng 8082/tcp phục vụ ứng dụng Spring Boot
sudo ufw allow 8082/tcp
```
> **Lưu ý:** Tuyệt đối không thực thi lệnh `sudo ufw allow 3306` hoặc `sudo ufw allow 3306/tcp`.

---

### Bước 3: Kích hoạt Tường lửa UFW
```bash
sudo ufw enable
```
Hệ thống sẽ hỏi xác nhận duy trì kết nối SSH:
```text
Command may disrupt existing ssh connections. Proceed with operation (y|n)? y
Firewall is active and enabled on system startup
```

---

## 4. Kết quả Kiểm tra & Xác minh (Verification Output)

### Lệnh 1: Kiểm tra trạng thái chi tiết của tường lửa (`sudo ufw status verbose`)

```bash
sudo ufw status verbose
```

**Đầu ra thực tế từ hệ thống:**
```text
Status: active
Logging: on (low)
Default: deny (incoming), allow (outgoing), disabled (routed)
New profiles: skip

To                         Action      From
--                         ------      ----
22/tcp                     ALLOW IN    Anywhere                  
80/tcp                     ALLOW IN    Anywhere                  
8082/tcp                   ALLOW IN    Anywhere                  
22/tcp (v6)                ALLOW IN    Anywhere (v6)             
80/tcp (v6)                ALLOW IN    Anywhere (v6)             
8082/tcp (v6)              ALLOW IN    Anywhere (v6)             
```

---

### Lệnh 2: Kiểm tra danh sách luật kèm chỉ số ưu tiên (`sudo ufw status numbered`)

```bash
sudo ufw status numbered
```

**Đầu ra thực tế từ hệ thống:**
```text
Status: active

     To                         Action      From
     --                         ------      ----
[ 1] 22/tcp                     ALLOW IN    Anywhere                  
[ 2] 80/tcp                     ALLOW IN    Anywhere                  
[ 3] 8082/tcp                   ALLOW IN    Anywhere                  
[ 4] 22/tcp (v6)                ALLOW IN    Anywhere (v6)             
[ 5] 80/tcp (v6)                ALLOW IN    Anywhere (v6)             
[ 6] 8082/tcp (v6)              ALLOW IN    Anywhere (v6)             
```

---

### Lệnh 3: Kiểm định tính cô lập của Cơ sở dữ liệu MySQL (Port 3306)

Kiểm tra xem cổng 3306 có xuất hiện trong danh sách Allow hay không:
```bash
sudo ufw status | grep 3306
```
*(Kết quả trả về rỗng -> Cổng 3306 hoàn toàn bị chặn từ Internet bởi chính sách `Default: deny incoming`).*

Kiểm tra cổng lắng nghe cục bộ của MySQL bằng lệnh `ss` hoặc `netstat`:
```bash
sudo ss -tlnp | grep 3306
```
**Kết quả hiển thị:**
```text
LISTEN 0      151        127.0.0.1:3306       0.0.0.0:*    users:(("mysqld",pid=1420,fd=21))
```
> **Phân tích:** MySQL chỉ bind trên `127.0.0.1:3306` (Localhost loopback interface). Ngay cả trong trường hợp vô tình mở cổng tường lửa, dịch vụ vẫn an toàn nhờ cấu hình bind address nội bộ.

---

## 5. Chương trình Java Mô phỏng Động cơ Lọc Gói tin & Quét Cổng

Chương trình [Main.java](./Main.java) được xây dựng để mô phỏng Stateful Firewall Engine và kịch bản thử nghiệm thâm nhập từ các địa chỉ IP Internet khác nhau.

### Cách chạy chương trình:
```bash
javac Main.java
java Main
```

### Kết quả chạy chương trình Java:
```text
==========================================================================================
 IT209 - SESSION 07 - BÀI TẬP 2: QUẢN TRỊ TƯỜNG LỬA UFW CHO CỤM DỊCH VỤ MULTI-PORT       
 Sinh viên: Phạm Thanh Phong | MSSV: N24DTCN120                                           
==========================================================================================

[1] THIẾT LẬP CHÍNH SÁCH MẶC ĐỊNH (UFW DEFAULT POLICIES):
----------------------------------------------------------------------------------------------------------
 - Default Incoming Policy : DENY (Chặn toàn bộ kết nối đi vào trừ khi được cho phép)
 - Default Outgoing Policy : ALLOW (Cho phép toàn bộ kết nối đi ra từ máy chủ)
----------------------------------------------------------------------------------------------------------

[DANH SÁCH BẢNG LUẬT UFW ĐƯỢC CẤU HÌNH (UFW ACTIVE RULES)]
----------------------------------------------------------------------------------------------------------
STT  | Cổng Dịch vụ   | Giao thức  | Hành động    | Nguồn        | Đích           | Mục đích                 
----------------------------------------------------------------------------------------------------------
1    | 22 (SSH Remote Admin) | TCP        | ALLOW        | Anywhere     | VPS-Server     | Quản trị máy chủ từ xa an toàn
2    | 80 (Nginx Web Server) | TCP        | ALLOW        | Anywhere     | VPS-Server     | Phục vụ lưu lượng Web HTTP công khai
3    | 8082 (Spring Boot Backend) | TCP        | ALLOW        | Anywhere     | VPS-Server     | Kiểm thử API ứng dụng Spring Boot từ xa
----------------------------------------------------------------------------------------------------------

[2] MÔ PHỎNG KIỂM THỬ THÂM NHẬP & QUÉT CỔNG (PORT SCAN AUDIT):
----------------------------------------------------------------------------------------------------------
IP Nguồn           | Cổng Đích        | Giao thức | Hành động    | Dịch vụ          | Đánh giá an ninh        
----------------------------------------------------------------------------------------------------------
203.113.152.1      | 22/tcp           | TCP      | [ ALLOW IN ] | SSH Remote Admin | HỢP LỆ (Quản trị viên SSH)
118.69.180.25      | 80/tcp           | TCP      | [ ALLOW IN ] | Nginx Web Server | HỢP LỆ (Web Visitor)    
171.244.10.88      | 8082/tcp         | TCP      | [ ALLOW IN ] | Spring Boot Backend | HỢP LỆ (REST API Client)
198.51.100.45      | 3306/tcp         | TCP      | [ BLOCKED ]  | MySQL Database   | AN TOÀN: Đã cô lập DB   
198.51.100.45      | 6379/tcp         | TCP      | [ BLOCKED ]  | Redis In-Memory  | BẢO VỆ: Chặn rà quét cổng
198.51.100.45      | 27017/tcp        | TCP      | [ BLOCKED ]  | MongoDB Service  | BẢO VỆ: Chặn rà quét cổng
----------------------------------------------------------------------------------------------------------
```

---

## 6. Đánh giá An ninh & Các Khuyến nghị Mở rộng (Security Hardening)

1. **Giới hạn tốc độ kết nối SSH (Rate Limiting):**
   - Để chống tấn công từ chối dịch vụ và Brute-force vào cổng SSH, sử dụng lệnh:
     ```bash
     sudo ufw limit 22/tcp
     ```
     *(Tự động từ chối kết nối nếu một IP thử đăng nhập sai hơn 6 lần trong vòng 30 giây).*
2. **Kích hoạt HTTPS (Port 443/tcp):**
   - Khi triển khai SSL/TLS với Let's Encrypt (Certbot), mở thêm cổng 443:
     ```bash
     sudo ufw allow 443/tcp
     ```
3. **Giới hạn IP truy cập cổng Backend / Database:**
   - Trong trường hợp Quản trị viên cần kết nối MySQL trực tiếp từ văn phòng công ty (IP tĩnh: `118.69.100.20`), chỉ cho phép IP cụ thể:
     ```bash
     sudo ufw allow from 118.69.100.20 to any port 3306 proto tcp
     ```
