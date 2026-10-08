#!/usr/bin/env bash
# ==============================================================================
# Script kiểm tra trạng thái tường lửa UFW và chuẩn đoán socket mạng
# Học phần: IT209 - Session 07 - BT2
# Sinh viên: Phạm Thanh Phong - MSSV: N24DTCN120
# ==============================================================================

echo "=============================================================================="
echo " KIỂM TRA TRẠNG THÁI TƯỜNG LỬA UFW (IT209 - SESSION 07 - BT2)"
echo "=============================================================================="

# 1. Kiểm tra trạng thái chi tiết (Status Verbose)
echo ""
echo "[1] ĐẦU RA LỆNH: sudo ufw status verbose"
echo "------------------------------------------------------------------------------"
sudo ufw status verbose
echo "------------------------------------------------------------------------------"

# 2. Kiểm tra danh sách luật kèm chỉ số (Status Numbered)
echo ""
echo "[2] ĐẦU RA LỆNH: sudo ufw status numbered"
echo "------------------------------------------------------------------------------"
sudo ufw status numbered
echo "------------------------------------------------------------------------------"

# 3. Kiểm tra cổng MySQL 3306 có bị chặn từ ngoài không
echo ""
echo "[3] KIỂM ĐỊNH BẢO VỆ CỔNG MYSQL (3306/tcp):"
if sudo ufw status | grep -E "3306.*ALLOW"; then
    echo "[CRITICAL WARNING] Cổng 3306 đang MỞ ra ngoài! Cần xóa bỏ rule ngay."
else
    echo "[PASS] Cổng 3306 (MySQL) KHÔNG nằm trong danh sách Allow -> Được bảo vệ an toàn bởi Default Deny Incoming."
fi

# 4. Kiểm tra các cổng đang lắng nghe trên hệ thống (ss socket statistics)
echo ""
echo "[4] KIỂM TRA CÁC TIẾN TRÌNH ĐANG LẮNG NGHE (ss -tlnp):"
sudo ss -tlnp | grep -E ":22|:80|:8082|:3306" || echo "Không có tiến trình mẫu nào đang chạy."

echo ""
echo "=============================================================================="
echo " HOÀN TẤT KIỂM ĐỊNH UFW MULTI-PORT!"
echo "=============================================================================="
