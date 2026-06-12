import java.sql.*;
public class SeedRoles {
  public static void main(String[] args) throws Exception {
    String url = "jdbc:mysql://124.221.104.56:3306/vrd_auth?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai";
    try (Connection c = DriverManager.getConnection(url, "root", "Wl1298236196!");
         Statement s = c.createStatement()) {
      try (ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM sys_role")) {
        rs.next();
        if (rs.getInt(1) > 0) { System.out.println("roles exist: " + rs.getInt(1)); return; }
      }
      s.executeUpdate("INSERT INTO sys_role (role_code, role_name, description, status, deleted, create_time, update_time) VALUES ('ADMIN','Admin','Full access',1,0,NOW(),NOW()),('OPERATOR','Operator','Operate',1,0,NOW(),NOW()),('VIEWER','Viewer','Read only',1,0,NOW(),NOW())");
      s.executeUpdate("INSERT INTO sys_user_role (user_id, role_id, create_time) SELECT 1, id, NOW() FROM sys_role WHERE role_code='ADMIN' LIMIT 1");
      System.out.println("seed ok");
    }
  }
}