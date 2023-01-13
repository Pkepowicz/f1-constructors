import javax.swing.*;
import org.apache.logging.log4j.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.DefaultValueDataset;


import java.awt.*;

public class GUI {
    private static final Logger log = LogManager.getRootLogger();

    private final static int defaultWidth = 150;
    private final static int defaultHeight = 100;

    private static final String[] constructors = { "Red Bull", "Ferrari", "Mercedes", "Alpine", "Mclaren", "Alfa Romeo",
            "Aston Martin", "Haas", "Alphatauri", "Williams"};

    public static void createAndShowGUI() {
        log.debug("CREATING GUI...");
        Thread.currentThread().setName("Thread: GUI");

        JFrame frame = new JFrame("F1 Constructors");
        final JPanel control = new JPanel(new GridLayout());
        final JComboBox<String> jlist1 = new JComboBox<>(constructors);
        final JLabel iconContainer1 = new JLabel();
        final JComboBox<String> jlist2 = new JComboBox<>(constructors);
        final ImageIcon logo2 = new ImageIcon("rb.jpg");
        final JLabel iconContainer2 = new JLabel();
        final JLabel textArea = new JLabel("Compare constructors", SwingConstants.CENTER);

        DefaultCategoryDataset data = new DefaultCategoryDataset();
        data.setValue(80, "First", "2019");
        data.setValue(56, "First", "2020");
        data.setValue(65, "First", "2021");
        data.setValue(12, "First", "2022");
        data.setValue(1, "Second", "2019");
        data.setValue(11, "Second", "2020");
        data.setValue(54, "Second", "2021");
        data.setValue(100, "Second", "2022");
        final JFreeChart chart = ChartFactory.createLineChart(
                "Amount of points earned in season", "Year", "Points",
                data, PlotOrientation.VERTICAL, false, true, false
        );
        final ChartPanel chartPanel = new ChartPanel(chart);

        iconContainer1.setPreferredSize(new Dimension(defaultWidth,defaultHeight));

        iconContainer1.setIcon(ScaleImage(logo2));
        control.add(iconContainer1);
        control.add(jlist1);
        control.add(jlist2);
        iconContainer2.setIcon(ScaleImage(logo2));
        control.add(iconContainer2);

        textArea.setFont(new Font("Courier", Font.BOLD, 70));

        frame.add(textArea, BorderLayout.NORTH);
        frame.add(control, BorderLayout.CENTER);
        frame.add(chartPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        log.debug("CREATING SUCCESSFUL!");
    }

    private static ImageIcon ScaleImage(ImageIcon iconToScale) {
        float mod = Math.max(iconToScale.getIconHeight()/(float)defaultHeight, iconToScale.getIconWidth()/(float)defaultWidth);
        int newHeight = (int)(0.4999+iconToScale.getIconHeight()/mod);
        int newWidth = (int)(0.4999+iconToScale.getIconWidth()/mod);
        ImageIcon image = new ImageIcon(iconToScale.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH));
        return image;
    }
}
