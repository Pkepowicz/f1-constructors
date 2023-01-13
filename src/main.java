import javax.swing.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.jsoup.*;

public class main {

    static Logger log = LogManager.getRootLogger();
    public static void main(String[] args) {
        Configurator.initialize(new DefaultConfiguration());
        Configurator.setRootLevel(Level.DEBUG);

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() { GUI.createAndShowGUI(); }
        });
    }
}
