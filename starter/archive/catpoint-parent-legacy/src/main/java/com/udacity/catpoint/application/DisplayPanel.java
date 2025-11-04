package com.udacity.catpoint.application;

import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.udacity.catpoint.data.AlarmStatus;
import com.udacity.catpoint.service.SecurityService;
import com.udacity.catpoint.service.StyleService;

/**
 * Displays the current status of the system. Implements the StatusListener
 * interface so that it can be notified whenever the status changes.
 */
public class DisplayPanel extends JPanel implements StatusListener {

    private JLabel currentStatusLabel;

    public DisplayPanel(SecurityService securityService) {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        securityService.addStatusListener(this);

        JLabel panelLabel = new JLabel("Very Secure Home Security");
        JLabel systemStatusLabel = new JLabel("System Status:");
        currentStatusLabel = new JLabel();

        panelLabel.setFont(StyleService.HEADING_FONT);

        notify(securityService.getAlarmStatus());

        add(panelLabel);
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.add(systemStatusLabel);
        row.add(currentStatusLabel);
        add(row);

    }

    @Override
    public void notify(AlarmStatus status) {
        currentStatusLabel.setText(status.getDescription());
        currentStatusLabel.setBackground(status.getColor());
        currentStatusLabel.setOpaque(true);
    }

    @Override
    public void catDetected(boolean catDetected) {
        // no behavior necessary
    }

    @Override
    public void sensorStatusChanged() {
        // no behavior necessary
    }
}
