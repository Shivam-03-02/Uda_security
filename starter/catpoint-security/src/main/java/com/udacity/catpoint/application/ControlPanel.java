package com.udacity.catpoint.application;

import java.awt.FlowLayout;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.udacity.catpoint.data.ArmingStatus;
import com.udacity.catpoint.service.SecurityService;
import com.udacity.catpoint.service.StyleService;

/**
 * JPanel containing the buttons to manipulate arming status of the system.
 */
public class ControlPanel extends JPanel {

    private SecurityService securityService;
    private Map<ArmingStatus, JButton> buttonMap;


    public ControlPanel(SecurityService securityService) {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.securityService = securityService;

        JLabel panelLabel = new JLabel("System Control");
        panelLabel.setFont(StyleService.HEADING_FONT);

        add(panelLabel);

        //create a map of each status type to a corresponding JButton
        buttonMap = Arrays.stream(ArmingStatus.values())
                .collect(Collectors.toMap(status -> status, status -> new JButton(status.getDescription())));

        //add an action listener to each button that applies its arming status and recolors all the buttons
        buttonMap.forEach((k, v) -> {
            v.addActionListener(e -> {
                securityService.setArmingStatus(k);
                buttonMap.forEach((status, button) -> button.setBackground(status == k ? status.getColor() : null));
            });
        });

        //map order above is arbitrary, so loop again in order to add buttons in enum-order
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Arrays.stream(ArmingStatus.values()).forEach(status -> buttonPanel.add(buttonMap.get(status)));
        add(buttonPanel);

        ArmingStatus currentStatus = securityService.getArmingStatus();
        buttonMap.get(currentStatus).setBackground(currentStatus.getColor());


    }
}
