# IT209 - Session 07 - Bài tập 2: Quản trị Tường lửa UFW cho Cụm Dịch vụ Multi-port

- **Sinh viên:** Phạm Thanh Phong
- **MSSV:** N24DTCN120
- **Đường dẫn nộp bài:** `homework/session_07/ex2/`

---

## Danh mục tệp tin trong dự án

| Tên tệp tin | Mô tả chức năng |
| :--- | :--- |
| [IT209_Session07_BT2_PhamThanhPhong_N24DTCN120.md](./IT209_Session07_BT2_PhamThanhPhong_N24DTCN120.md) | Báo cáo chi tiết cấu hình UFW Multi-port, phân tích bảo vệ đa tầng và kết quả xác minh |
| [Main.java](./Main.java) | Ứng dụng Java mô phỏng động cơ lọc gói tin Stateful Packet Filter và kịch bản quét cổng |
| [setup_ufw_multiport.sh](./setup_ufw_multiport.sh) | Script tự động hóa cấu hình UFW (deny incoming, allow 22, 80, 8082, block 3306) |
| [verify.sh](./verify.sh) | Script kiểm tra và trích xuất kết quả `ufw status verbose`, `ufw status numbered` và `ss -tlnp` |

---

## Tóm tắt quy trình thực hiện & Đầu ra yêu cầu

### 1. Cấu hình tự động trên Linux VPS:
```bash
sudo chmod +x setup_ufw_multiport.sh verify.sh
./setup_ufw_multiport.sh
./verify.sh
```

### 2. Đầu ra lệnh kiểm tra `sudo ufw status verbose`:
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

### 3. Thực thi kiểm định với Java:
```bash
javac Main.java
java Main
```
