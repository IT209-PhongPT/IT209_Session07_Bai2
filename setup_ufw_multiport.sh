#!/usr/bin/env bash
# ==============================================================================
# Học phần: IT209 - Phát triển ứng dụng Web / Cloud Infrastructure
# Session 07 - Bài tập 2: Quản trị Tường lửa UFW cho Cụm Dịch vụ Multi-port
# Sinh viên: Phạm Thanh Phong - MSSV: N24DTCN120
#
# Mục đích: Thiết lập chính sách bảo vệ đa cổng cho VPS (SSH: 22, Web: 80, Spring Boot: 8082, chặn MySQL: 3306)
# ==============================================================================

set -euo pipefail

echo "=============================================================================="
echo " CẤU HÌNH TƯỜNG LỬA UFW CHO CỤM DỊCH VỤ MULTI-PORT (IT209 - SESSION 07 - BT2)"
echo " Sinh viên: Phạm Thanh Phong | MSSV: N24DTCN120"
echo "=============================================================================="

# 1. Đảm bảo gói ufw đã được cài đặt
if ! command -v ufw &>/dev/null; then
    echo "[+] Đang cài đặt UFW (Uncomplicated Firewall)..."
    sudo apt-get update -y && sudo apt-get install -y ufw
fi

# 2. Reset UFW về trạng thái ban đầu để tránh xung đột luật cũ (tùy chọn)
echo "[+] Khởi tạo lại các bảng luật (Reset UFW rules)..."
sudo ufw --force reset

# 3. Khai báo các chính sách mặc định (Default Security Policies)
echo "[+] Thiết lập Default Incoming: DENY (Chặn mọi kết nối đi vào)"
sudo ufw default deny incoming

echo "[+] Thiết lập Default Outgoing: ALLOW (Cho phép mọi kết nối đi ra)"
sudo ufw default allow outgoing

# 4. Mở các cổng dịch vụ theo yêu cầu
echo "[+] Cho phép cổng 22/tcp (Quản trị từ xa SSH):"
sudo ufw allow 22/tcp comment 'Allow SSH Admin'

echo "[+] Cho phép cổng 80/tcp (Nginx Web Server HTTP):"
sudo ufw allow 80/tcp comment 'Allow Web HTTP'

echo "[+] Cho phép cổng 8082/tcp (Ứng dụng Spring Boot):"
sudo ufw allow 8082/tcp comment 'Allow Spring Boot Backend'

# Lưu ý bảo mật: Cổng 3306 (MySQL) KHÔNG ĐƯỢC thêm luật allow, mặc định sẽ bị deny incoming.
echo "[!] Lưu ý: Cổng MySQL (3306/tcp) KHÔNG được mở để đảm bảo cô lập CSDL tuyệt đối."

# 5. Kích hoạt tường lửa UFW
echo "[+] Đang kích hoạt tường lửa UFW..."
sudo ufw --force enable

echo ""
echo "=============================================================================="
echo " CẤU HÌNH TƯỜNG LỬA HOÀN TẤT THÀNH CÔNG!"
echo "=============================================================================="
