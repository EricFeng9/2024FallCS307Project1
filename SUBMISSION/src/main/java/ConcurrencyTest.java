import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ConcurrencyTest {
    private static String host = "localhost";
    private static String dbname = "project_db";
    private static String user = "postgres";
    private static String pwd = "Lekge66811616";
    private static String port = "5432";

    public static void main(String[] args) {
        int nThreads = 500;
        ExecutorService executor = Executors.newFixedThreadPool(nThreads);  // 创建100个线程
        List<Future<Boolean>> results = new ArrayList<>();  // 用于存储每个任务的结果

        for (int i = 0; i < 1000; i++) {  // 创建大量并发请求
            Future<Boolean> result = executor.submit(new Callable<Boolean>() {
                public Boolean call() {
                    return performQuery();  // 假设performQuery()返回Boolean值，true表示成功，false表示失败
                }
            });
            results.add(result);
        }
        executor.shutdown(); // 关闭ExecutorService，不再接受新任务

        int successCount = 0;
        int failureCount = 0;

        // 遍历Future列表以获取每个任务的执行结果
        for (Future<Boolean> result : results) {
            try {
                if (result.get()) {  // result.get() 阻塞直到任务完成，返回任务的结果
                    successCount++;
                } else {
                    failureCount++;
                }
            } catch (Exception e) {
                failureCount++;  // 处理异常情况，如任务执行中断等
            }
        }
        System.out.println("当前访问的线程数:"+nThreads);
        System.out.println("成功的访问次数: " + successCount);
        System.out.println("失败的访问次数: " + failureCount);
    }

    private static boolean performQuery() {
        String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbname;
        try (Connection conn = DriverManager.getConnection(url, user, pwd);
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM articles WHERE id = ?")) {
            stmt.setInt(1, 1);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // 可以在这里处理查询结果
                }
            }
            //System.out.println(true);
            return true; // 如果没有异常发生，返回true表示成功
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(false);
            return false; // 如果发生异常，返回false表示失败
        }
    }
}