package utils;

import java.io.InputStream;
import java.util.Properties;

public final class ViewConfig {

    private static final String GUI_BASE_PATH;

    static {
        try (InputStream input =
                     ViewConfig.class.getClassLoader()
                             .getResourceAsStream("view.properties")) {

            Properties props = new Properties();
            if (input == null) {
                throw new IllegalStateException("view.properties not found");
            }

            props.load(input);
            GUI_BASE_PATH = props.getProperty("gui.base.path");

        } catch (Exception e) {
            throw new IllegalStateException("Unable to load GUI base path", e);
        }
    }

    private ViewConfig() {
    }

    public static String getGuiBasePath() {
        return GUI_BASE_PATH;
    }
}