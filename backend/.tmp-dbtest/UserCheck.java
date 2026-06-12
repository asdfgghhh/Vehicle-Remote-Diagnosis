import java.sql.*;

public class UserCheck {
  public static void main(String[] args) throws Exception {
    String url = "jdbc:mysql://124.221.104.56:3306/vrd_auth?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai";
    try (Connection c = DriverManager.getConnection(url, "root", "Wl1298236196!")) {
      try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT id, username, password, status, deleted FROM sys_user")) {
        while (rs.next()) {
          System.out.println("id=" + rs.getLong(1) + " user=" + rs.getString(2) + " status=" + rs.getInt(4) + " deleted=" + rs.getInt(5));
          System.out.println("hash=" + rs.getString(3));
        }
      }
    }
  }
}
