import java.util.*;

/**
 * Học phần: IT209 - Phát triển ứng dụng Web / Cloud Infrastructure
 * Session 07 - Bài tập 2: Quản trị Tường lửa UFW cho Cụm Dịch vụ Multi-port
 * 
 * Sinh viên: Phạm Thanh Phong
 * MSSV: N24DTCN120
 * 
 * Chương trình Java mô phỏng:
 * 1. Động cơ phân tích quy tắc lọc gói tin UFW (Stateful Packet Filtering Engine).
 * 2. Mô hình hóa chính sách kiểm soát truy cập cho Cụm Multi-port (SSH: 22, HTTP: 80, Spring Boot: 8082, MySQL: 3306).
 * 3. Mô phỏng kiểm thử thâm nhập & quét cổng từ Internet (Port Scan & Penetration Simulation).
 */
public class Main {

    public enum Action {
        ALLOW, DENY, REJECT
    }

    public enum Direction {
        INCOMING, OUTGOING
    }

    public record FirewallRule(
            int priority,
            String serviceName,
            int port,
            String protocol,
            Direction direction,
            Action action,
            String sourceIp,
            String destinationIp,
            String description
    ) {}

    public record Packet(
            String sourceIp,
            int sourcePort,
            String destinationIp,
            int destinationPort,
            String protocol,
            String servicePayload
    ) {}

    public static void main(String[] args) {
        printHeader();

        // 1. Khởi tạo chính sách mặc định (Default Policies)
        Action defaultIncomingAction = Action.DENY;
        Action defaultOutgoingAction = Action.ALLOW;

        System.out.println("\n[1] THIẾT LẬP CHÍNH SÁCH MẶC ĐỊNH (UFW DEFAULT POLICIES):");
        System.out.println("----------------------------------------------------------------------------------------------------------");
        System.out.println(" - Default Incoming Policy : " + defaultIncomingAction + " (Chặn toàn bộ kết nối đi vào trừ khi được cho phép)");
        System.out.println(" - Default Outgoing Policy : " + defaultOutgoingAction + " (Cho phép toàn bộ kết nối đi ra từ máy chủ)");
        System.out.println("----------------------------------------------------------------------------------------------------------");

        // 2. Danh sách các quy tắc mở cổng dịch vụ cụ thể
        List<FirewallRule> rules = new ArrayList<>();
        rules.add(new FirewallRule(1, "SSH Remote Admin", 22, "tcp", Direction.INCOMING, Action.ALLOW, "Anywhere", "VPS-Server", "Quản trị máy chủ từ xa an toàn"));
        rules.add(new FirewallRule(2, "Nginx Web Server", 80, "tcp", Direction.INCOMING, Action.ALLOW, "Anywhere", "VPS-Server", "Phục vụ lưu lượng Web HTTP công khai"));
        rules.add(new FirewallRule(3, "Spring Boot Backend", 8082, "tcp", Direction.INCOMING, Action.ALLOW, "Anywhere", "VPS-Server", "Kiểm thử API ứng dụng Spring Boot từ xa"));

        displayFirewallRulesTable(rules);

        // 3. Mô phỏng động cơ lọc gói tin (Packet Filtering Simulation)
        System.out.println("\n[2] MÔ PHỎNG KIỂM THỬ THÂM NHẬP & QUÉT CỔNG (PORT SCAN AUDIT):");
        System.out.println("----------------------------------------------------------------------------------------------------------");

        List<Packet> testPackets = List.of(
                new Packet("203.113.152.1", 52140, "103.179.188.10", 22, "tcp", "SSH-2.0-OpenSSH_8.9"),
                new Packet("118.69.180.25", 48120, "103.179.188.10", 80, "tcp", "GET / HTTP/1.1"),
                new Packet("171.244.10.88", 61002, "103.179.188.10", 8082, "tcp", "GET /api/v1/health HTTP/1.1"),
                new Packet("198.51.100.45", 34560, "103.179.188.10", 3306, "tcp", "MySQL Handshake Init Attempt"),
                new Packet("198.51.100.45", 34561, "103.179.188.10", 6379, "tcp", "Redis PING Probe Attempt"),
                new Packet("198.51.100.45", 34562, "103.179.188.10", 27017, "tcp", "MongoDB Connection Probe")
        );

        System.out.printf("%-18s | %-16s | %-8s | %-12s | %-16s | %-24s%n",
                "IP Nguồn", "Cổng Đích", "Giao thức", "Hành động", "Dịch vụ", "Đánh giá an ninh");
        System.out.println("----------------------------------------------------------------------------------------------------------");

        for (Packet packet : testPackets) {
            evaluatePacket(packet, rules, defaultIncomingAction);
        }
        System.out.println("----------------------------------------------------------------------------------------------------------");

        // 4. Phân tích mô hình kiến trúc đa tầng (Multi-tier Isolation Architecture)
        printArchitectureAnalysis();
    }

    private static void printHeader() {
        System.out.println("==========================================================================================");
        System.out.println(" IT209 - SESSION 07 - BÀI TẬP 2: QUẢN TRỊ TƯỜNG LỬA UFW CHO CỤM DỊCH VỤ MULTI-PORT       ");
        System.out.println(" Sinh viên: Phạm Thanh Phong | MSSV: N24DTCN120                                           ");
        System.out.println("==========================================================================================");
    }

    private static void displayFirewallRulesTable(List<FirewallRule> rules) {
        System.out.println("\n[DANH SÁCH BẢNG LUẬT UFW ĐƯỢC CẤU HÌNH (UFW ACTIVE RULES)]");
        System.out.println("----------------------------------------------------------------------------------------------------------");
        System.out.printf("%-4s | %-14s | %-10s | %-8s | %-12s | %-14s | %-25s%n",
                "STT", "Cổng Dịch vụ", "Giao thức", "Hành động", "Nguồn", "Đích", "Mục đích");
        System.out.println("----------------------------------------------------------------------------------------------------------");
        for (FirewallRule rule : rules) {
            System.out.printf("%-4d | %-14s | %-10s | %-8s | %-12s | %-14s | %-25s%n",
                    rule.priority(),
                    rule.port() + " (" + rule.serviceName() + ")",
                    rule.protocol().toUpperCase(),
                    rule.action(),
                    rule.sourceIp(),
                    rule.destinationIp(),
                    rule.description());
        }
        System.out.println("----------------------------------------------------------------------------------------------------------");
    }

    private static void evaluatePacket(Packet packet, List<FirewallRule> rules, Action defaultAction) {
        FirewallRule matchedRule = null;
        for (FirewallRule rule : rules) {
            if (rule.port() == packet.destinationPort() && rule.protocol().equalsIgnoreCase(packet.protocol())) {
                matchedRule = rule;
                break;
            }
        }

        if (matchedRule != null) {
            String evaluation = (matchedRule.port() == 22) ? "HỢP LỆ (Quản trị viên SSH)" :
                                (matchedRule.port() == 80) ? "HỢP LỆ (Web Visitor)" :
                                (matchedRule.port() == 8082) ? "HỢP LỆ (REST API Client)" : "HỢP LỆ";
            System.out.printf("%-18s | %-16s | %-8s | %-12s | %-16s | %-24s%n",
                    packet.sourceIp(),
                    packet.destinationPort() + "/" + packet.protocol(),
                    packet.protocol().toUpperCase(),
                    "[ ALLOW IN ]",
                    matchedRule.serviceName(),
                    evaluation);
        } else {
            String service = (packet.destinationPort() == 3306) ? "MySQL Database" :
                             (packet.destinationPort() == 6379) ? "Redis In-Memory" :
                             (packet.destinationPort() == 27017) ? "MongoDB Service" : "Unknown Port";
            String evaluation = (packet.destinationPort() == 3306) ? "AN TOÀN: Đã cô lập DB" : "BẢO VỆ: Chặn rà quét cổng";

            System.out.printf("%-18s | %-16s | %-8s | %-12s | %-16s | %-24s%n",
                    packet.sourceIp(),
                    packet.destinationPort() + "/" + packet.protocol(),
                    packet.protocol().toUpperCase(),
                    "[ BLOCKED ]",
                    service,
                    evaluation);
        }
    }

    private static void printArchitectureAnalysis() {
        System.out.println("\n[3] PHÂN TÍCH KIẾN TRÚC BẢO VỆ ĐA TẦNG (DEFENSE-IN-DEPTH ARCHITECTURE):");
        System.out.println("----------------------------------------------------------------------------------------------------------");
        System.out.println(" 1. Tầng Web / Công khai (Port 80/tcp, Port 8082/tcp):");
        System.out.println("    - Mở công khai cho mọi IP Internet truy cập website và gọi Restful Web APIs.");
        System.out.println(" 2. Tầng Quản trị Hệ thống (Port 22/tcp):");
        System.out.println("    - Mở cho Sysadmin cấu hình từ xa qua SSH (Khuyến nghị: Đổi cổng mặc định hoặc dùng VPN/IP Whitelisting).");
        System.out.println(" 3. Tầng Cơ sở Dữ liệu (Port 3306/tcp - MySQL):");
        System.out.println("    - TUYỆT ĐỐI KHÔNG mở ra Internet. Ứng dụng Spring Boot kết nối tới MySQL thông qua localhost");
        System.out.println("      (127.0.0.1:3306) hoặc dải mạng nội bộ riêng (VPC Internal Network).");
        System.out.println("    - Ngăn chặn triệt để các cuộc tấn công SQL Injection từ xa, brute-force root MySQL, và ransomware tống tiền.");
        System.out.println("==========================================================================================================");
    }
}
