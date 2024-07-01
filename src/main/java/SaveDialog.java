import com.google.zxing.WriterException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SaveDialog extends JDialog {
    private final String value;
    private final String charset;

    private final JLabel jlWidth = new JLabel("Width");
    private final JTextField jtfWidth = new JTextField();
    private final JLabel jlHeight = new JLabel("Height:");
    private final JTextField jtfHeight = new JTextField();

    private final JLabel jlPath = new JLabel("Path:");
    private final JTextField jtfPath = new JTextField();
    private final JButton jbSelectPath = new JButton("...");

    private final JButton jbSave = new JButton("Save");

    private final Executor executorService = Executors.newCachedThreadPool();

    public SaveDialog(String value, String charset) {
        this.value = value;
        this.charset = charset;
    }

    private void enableFields(boolean flag) {
        SwingUtilities.invokeLater(() -> {
            jtfWidth.setEditable(flag);
            jtfHeight.setEditable(flag);
            jtfPath.setEditable(flag);

            jbSelectPath.setEnabled(flag);
            jbSave.setEnabled(flag);
        });
    }

    private int with() {
        Integer w = Utils.strToIntegerOrNull(jtfWidth.getText());

        if (w == null) return 0;

        return w;
    }

    private int height() {
        Integer h = Utils.strToIntegerOrNull(jtfHeight.getText());

        if (h == null) return 0;

        return h;
    }

    private File validateFields() {
        int width = with();
        int height = height();
        String path = jtfPath.getText();

        if (width <= 0) {
            JOptionPane.showMessageDialog(
                    null,
                    "Width value must be greater than 0",
                    "Illegal Value",
                    JOptionPane.ERROR_MESSAGE
            );

            return null;
        }

        if (height <= 0) {
            JOptionPane.showMessageDialog(
                    null,
                    "Height value must be greater than 0",
                    "Illegal Value",
                    JOptionPane.ERROR_MESSAGE
            );

            return null;
        }

        if (path.isEmpty()) {
            JOptionPane.showMessageDialog(
                    null,
                    "Path is empty",
                    "Illegal Value",
                    JOptionPane.ERROR_MESSAGE
            );

            return null;
        }

        File file = new File(path);

        if (file.isDirectory()) {
            JOptionPane.showMessageDialog(
                    null,
                    "The selected path is a directory.\n" +
                            "If you selected the path with the file chooser, write the name\n" +
                            "at the end of the path.",
                    "Illegal Value",
                    JOptionPane.ERROR_MESSAGE
            );

            return null;
        }

        return file;
    }

    private void init() {
        jbSelectPath.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();

                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.showOpenDialog(null);

                File directory = chooser.getSelectedFile();

                if (directory == null) return;

                jtfPath.setText(directory.getAbsolutePath());
            }
        });

        jbSave.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final File file = validateFields();

                if (file == null) return;

                if (file.exists()) {
                    int answer = JOptionPane.showConfirmDialog(
                            null,
                            String.format("Already exists the file: \n'%s'\nDo you want to override it?", file.getAbsolutePath()),
                            "Confirm Override",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );

                    if (answer == JOptionPane.NO_OPTION) return;
                }

                final int width = with();
                final int height = height();

                Utils.executorService.execute(() -> {
                    boolean saved = false;

                    enableFields(false);

                    try {
                        BufferedImage image = Utils.generateQR(value, charset, width, height);
                        String absolutePath = file.getAbsolutePath();

                        Utils.writeImgToFile(absolutePath, image);

                        saved = true;
                    } catch (WriterException | IOException ex) {
                        JOptionPane.showMessageDialog(
                                null,
                                ex.getMessage(),
                                "Failed Save Image",
                                JOptionPane.ERROR_MESSAGE
                        );
                    } finally {
                        enableFields(true);
                    }

                    if (!saved) {
                        JOptionPane.showMessageDialog(
                                null,
                                String.format(
                                        "Be sure the path:\n" + "'%s'\n" + "is correct or parent of path exists",
                                        file.getAbsolutePath()),
                                "Failed Save Image",
                                JOptionPane.ERROR_MESSAGE
                        );

                        return;
                    }

                    JOptionPane.showMessageDialog(
                            null,
                            String.format(
                                    "Image saved correctly at:\n" + "%s",
                                    file.getAbsolutePath()),
                            "Operation Success",
                            JOptionPane.PLAIN_MESSAGE
                    );

                    SwingUtilities.invokeLater(() -> dispose());
                });
            }
        });
    }

    private void createLayout() {
        GroupLayout gl = new GroupLayout(getContentPane());

        setLayout(gl);

        gl.setAutoCreateGaps(true);
        gl.setAutoCreateContainerGaps(true);

        gl.setHorizontalGroup(gl.createParallelGroup()
                .addGroup(gl.createSequentialGroup()
                        .addComponent(jlWidth)
                        .addComponent(jtfWidth, 128, 128, 128)
                        .addComponent(jlHeight)
                        .addComponent(jtfHeight, 128, 128, 128))
                .addGroup(gl.createSequentialGroup()
                        .addComponent(jlPath)
                        .addComponent(jtfPath, 512, 512, 512)
                        .addComponent(jbSelectPath))
                .addGroup(gl.createSequentialGroup()
                        .addComponent(jbSave)));

        gl.setVerticalGroup(gl.createSequentialGroup()
                .addGroup(gl.createParallelGroup()
                        .addComponent(jlWidth)
                        .addComponent(jtfWidth, 24, 24, 24)
                        .addComponent(jlHeight)
                        .addComponent(jtfHeight, 24, 24, 24))
                .addGroup(gl.createParallelGroup()
                        .addComponent(jlPath)
                        .addComponent(jtfPath, 24, 24, 24)
                        .addComponent(jbSelectPath))
                .addGroup(gl.createParallelGroup()
                        .addComponent(jbSave)));
    }

    public void initAndShow() {
        init();

        createLayout();
        pack();
        setResizable(false);

        setTitle("Save QR Image");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setModalityType(ModalityType.APPLICATION_MODAL);

        setVisible(true);
    }
}
