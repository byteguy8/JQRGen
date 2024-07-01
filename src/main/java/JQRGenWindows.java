import com.google.zxing.WriterException;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.SortedMap;

public class JQRGenWindows extends JFrame {
    private int width = 256;
    private int height = 256;
    private static final String DEFAULT_EMPTY_QR_TEXT = "Write some text...";

    private int defaultCharsetIndex = -1;

    private final JMenu jmFile = new JMenu("File");
    private final JMenuItem jmiSave = new JMenuItem("Save To");

    private final JLabel jlImage = new JLabel(DEFAULT_EMPTY_QR_TEXT, JLabel.CENTER);

    private final JLabel jlCharsets = new JLabel("Charset:");
    private final JComboBox<String> jcbxCharsets = new JComboBox<>();

    private final JLabel jlText = new JLabel("Text:");
    private final JTextField jtfText = new JTextField();

    private void clearImageText() {
        SwingUtilities.invokeLater(() -> jlImage.setText(""));
    }

    private void clearText() {
        SwingUtilities.invokeLater(() -> jtfText.setText(""));
    }

    private void resetImage() {
        SwingUtilities.invokeLater(() -> {
            jlImage.setIcon(null);
            jlImage.setText(DEFAULT_EMPTY_QR_TEXT);
        });
    }

    private void setImage(BufferedImage image) {
        SwingUtilities.invokeLater(() -> jlImage.setIcon(new ImageIcon(image)));
    }

    private void updateImageLabel() {
        String imageText = jlImage.getText();
        int charsetIndex = jcbxCharsets.getSelectedIndex();
        String text = jtfText.getText();

        if (charsetIndex == -1) {
            JOptionPane.showMessageDialog(
                    null,
                    "You must select a charset",
                    "Image Generation Error",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        if (!imageText.isEmpty() && !text.isEmpty()) clearImageText();

        if (text.isEmpty()) {
            resetImage();
            return;
        }

        String charset = jcbxCharsets.getItemAt(charsetIndex);

        Utils.executorService.execute(() -> {
            try {
                BufferedImage image = Utils.generateQR(text, charset, width, height);
                setImage(image);
            } catch (WriterException ex) {
                JOptionPane.showMessageDialog(
                        null,
                        "Failed to generate QR image",
                        "Image Generation Error",
                        JOptionPane.ERROR_MESSAGE
                );

                resetImage();
                clearText();
            } catch (UnsupportedEncodingException | UnsupportedOperationException ex) {
                JOptionPane.showMessageDialog(
                        null,
                        String.format("Failed to generate QR image. Unsupported charset: '%s'", charset),
                        "Image Generation Error",
                        JOptionPane.ERROR_MESSAGE
                );

                resetImage();
                clearText();
            }
        });
    }

    private void initListeners() {
        jmiSave.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String value = jtfText.getText();
                int charset_index = jcbxCharsets.getSelectedIndex();

                if (charset_index == -1) {
                    JOptionPane.showMessageDialog(
                            null,
                            "You need to select a charset",
                            "Image Save Error",
                            JOptionPane.ERROR_MESSAGE
                    );

                    return;
                }

                if (value.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            null,
                            "You need to generate a QR first",
                            "Image Save Error",
                            JOptionPane.ERROR_MESSAGE
                    );

                    return;
                }

                new SaveDialog(value, jcbxCharsets.getItemAt(charset_index)).initAndShow();
            }
        });

        jcbxCharsets.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) updateImageLabel();
        });

        jtfText.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateImageLabel();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateImageLabel();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });
    }

    private void init() {
        jmFile.add(jmiSave);

        JMenuBar jMenuBar = new JMenuBar();

        jMenuBar.add(jmFile);

        setJMenuBar(jMenuBar);

        int i = 0;

        SortedMap<String, Charset> charsets = Charset.availableCharsets();

        for (String key : Charset.availableCharsets().keySet()) {
            Charset charset = charsets.get(key);
            String name = charset.name();

            if (defaultCharsetIndex == -1 && name.equalsIgnoreCase("utf-8")) defaultCharsetIndex = i;
            if (defaultCharsetIndex == -1 && name.equalsIgnoreCase("utf8")) defaultCharsetIndex = i;

            jcbxCharsets.addItem(name);

            i += 1;
        }

        if (defaultCharsetIndex >= 0) jcbxCharsets.setSelectedIndex(defaultCharsetIndex);
    }

    private void createLayout() {
        GroupLayout gl = new GroupLayout(getContentPane());

        getContentPane().setLayout(gl);

        gl.setAutoCreateGaps(true);
        gl.setAutoCreateContainerGaps(true);

        gl.setHorizontalGroup(gl.createParallelGroup()
                .addGroup(gl.createSequentialGroup()
                        .addComponent(jlImage, width, width, width))
                .addGroup(gl.createSequentialGroup()
                        .addComponent(jlCharsets)
                        .addComponent(jcbxCharsets))
                .addGroup(gl.createSequentialGroup()
                        .addComponent(jlText)
                        .addComponent(jtfText)));

        gl.setVerticalGroup(gl.createSequentialGroup()
                .addGroup(gl.createParallelGroup()
                        .addComponent(jlImage, height, height, height))
                .addGroup(gl.createParallelGroup()
                        .addComponent(jlCharsets)
                        .addComponent(jcbxCharsets, 24, 24, 24))
                .addGroup(gl.createParallelGroup()
                        .addComponent(jlText)
                        .addComponent(jtfText, 24, 24, 24)));
    }

    public void initAndShow() {
        init();
        initListeners();

        createLayout();
        pack();
        setResizable(false);

        setTitle("JQRGen");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setVisible(true);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new JQRGenWindows().initAndShow());
    }
}
