import java.sql.*;
public class DbTest {
  static void test(String pass) {
    String url = "jdbc:mysql://124.221.104.56:3306/vrd_auth?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai";
    try (Connection c = DriverManager.getConnection(url, "root", pass)) {
      System.out.println("CONNECT_OK pass=" + pass);
      try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM sys_user")) {
        if (rs.next()) System.out.println("USER_COUNT=" + rs.getInt(1));
      }
    } catch (Exception e) {
      System.out.println("FAIL pass=" + pass + " => " + e.getMessage());
    }
  }
  public static void main(String[] args) { test("Wl1298236196!"); test("root123"); }
}
