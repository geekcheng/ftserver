package FTServer;

import FTServer.FTS.Engine;
import iBoxDB.LocalServer.*;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        //Path
        String path = System.getProperty("user.home") + File.separatorChar + "ftsdata100" + File.separatorChar;
        new File(path).mkdirs();

        if (!new File(path).exists()) {

            String tmpPath = sce.getServletContext().getRealPath("/")
                    + "WEB-INF" + File.separatorChar + "DB" + File.separatorChar;

            path = tmpPath;
            (new File(path)).mkdirs();
        }

        Logger.getLogger(App.class.getName()).log(Level.INFO,
                System.getProperty("java.version"));
        Logger.getLogger(App.class.getName()).log(Level.INFO,
                String.format("DB Path=%s ", path));

        DB.root(path);

        //Config
        DB db = new DB(1);
        DatabaseConfig cfg = db.getConfig().DBConfig;
        long tm = java.lang.Runtime.getRuntime().maxMemory();
        cfg.CacheLength = tm / 3;
        cfg.FileIncSize = (int) cfg.mb(4);
        Logger.getLogger(App.class.getName()).log(Level.INFO, "DB Cache=" + cfg.CacheLength / 1024 / 1024 + "MB"
                + " AppMEM=" + tm / 1024 / 1024 + "MB");

        new Engine().Config(cfg);

        cfg.EnsureTable(Page.class, "Page", "id");
        cfg.EnsureIndex(Page.class, "Page", true, "url(" + Page.MAX_URL_LENGTH + ")");
        cfg.EnsureTable(PageLock.class, "PageLock", "url(" + Page.MAX_URL_LENGTH + ")");
        App.Auto = db.open();

        Logger.getLogger(App.class.getName()).log(Level.INFO, "DB Started...");

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        IndexPage.closeBGTask();
        if (App.Auto != null) {
            App.Auto.getDatabase().close();
        }
        App.Auto = null;
        Logger.getLogger(App.class.getName()).log(Level.INFO, "DB Closed");
    }
}
