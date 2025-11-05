package com.udacity.catpoint.application;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.udacity.catpoint.data.PretendDatabaseSecurityRepositoryImpl;
import com.udacity.catpoint.data.SecurityRepository;
import com.udacity.catpoint.image.service.FakeImageService;
import com.udacity.catpoint.image.service.ImageService;
import com.udacity.catpoint.service.SecurityService;

/**
 * This is the primary JFrame for the application that contains all the top-level JPanels.
 */
public class CatpointGui extends JFrame {
    private SecurityRepository securityRepository = new PretendDatabaseSecurityRepositoryImpl();
    private ImageService imageService = new FakeImageService();
    private SecurityService securityService = new SecurityService(securityRepository, imageService);
    private DisplayPanel displayPanel = new DisplayPanel(securityService);
    private ControlPanel controlPanel = new ControlPanel(securityService);
    private SensorPanel sensorPanel = new SensorPanel(securityService);
    private ImagePanel imagePanel = new ImagePanel(securityService);

    public CatpointGui() {
        setLocation(100, 100);
        setSize(600, 850);
        setTitle("Very Secure App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(displayPanel);
        mainPanel.add(imagePanel);
        mainPanel.add(controlPanel);
        mainPanel.add(sensorPanel);

        getContentPane().add(mainPanel);

    }
}
