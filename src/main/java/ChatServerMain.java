import javax.servlet.ServletException;
import java.io.File;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

public class ChatServerMain {
    private static final String STATIC_DIR = "web/WEB-INF/";

    public static void main(String[] args) throws ServletException, LifecycleException {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);

        File staticDir = new File(STATIC_DIR);
        tomcat.addWebapp("/", staticDir.getAbsolutePath());

        tomcat.start();
        tomcat.getServer().await();
    }
}
