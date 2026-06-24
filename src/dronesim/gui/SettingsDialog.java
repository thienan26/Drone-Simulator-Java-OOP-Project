package dronesim.gui;

import dronesim.controller.DashboardController;

import javax.swing.*;
import java.awt.*;

/**
 * Modal dialog for entering the API server URL and authentication token.
 * On save, settings are persisted to config.properties and a dashboard refresh is triggered.
 */
public class SettingsDialog extends JDialog {

    private final JTextField urlField;
    private final JPasswordField tokenField;
    private final DashboardController controller;

    public SettingsDialog(Frame parent, DashboardController controller) {
        super(parent, "API Settings", true);
        this.controller = controller;

        urlField   = new JTextField(controller.getConfig().getBaseUrl(), 40);
        tokenField = new JPasswordField(controller.getConfig().getToken(), 40);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Server URL:"), gbc);
        gbc.gridx = 1;
        form.add(urlField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Auth Token:"), gbc);
        gbc.gridx = 1;
        form.add(tokenField, gbc);

        JButton saveBtn   = new JButton("Save & Connect");
        JButton cancelBtn = new JButton("Cancel");

        saveBtn.addActionListener(e -> onSave());
        cancelBtn.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancelBtn);
        buttons.add(saveBtn);

        setLayout(new BorderLayout(8, 8));
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    private void onSave() {
        String url   = urlField.getText().trim();
        String token = new String(tokenField.getPassword()).trim();

        if (url.isBlank()) {
            JOptionPane.showMessageDialog(this, "Server URL cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (token.isBlank()) {
            JOptionPane.showMessageDialog(this, "Token cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        controller.applySettings(url, token);
        dispose();
    }
}
