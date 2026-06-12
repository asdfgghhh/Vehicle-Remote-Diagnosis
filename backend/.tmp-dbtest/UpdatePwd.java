import java.sql.*;
public class UpdatePwd {
  public static void main(String[] args) throws Exception {
    String url = "jdbc:mysql://124.221.104.56:3306/vrd_auth?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai";
    try (Connection c = DriverManager.getConnection(url, "root", "Wl1298236196!")) {
      try (PreparedStatement ps = c.prepareStatement("UPDATE sys_user SET password=? WHERE username='admin'")) {
        ps.setString(1, "$2a$10$9sCn.a.mx9Xm/bvr6Dytm.RTNkwAggG334aleN7n7e9WiJKOKhBxa");
        System.out.println("updated=" + ps.executeUpdate());
      }
      try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT username, password FROM sys_user WHERE username='admin'")) {
        while (rs.next()) System.out.println(rs.getString(1) + " => " + rs.getString(2));
      }
    }
  }
}