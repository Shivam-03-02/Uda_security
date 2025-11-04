package com.udacity.catpoint.service;

import com.udacity.catpoint.application.StatusListener;
import com.udacity.catpoint.data.AlarmStatus;
import com.udacity.catpoint.data.ArmingStatus;
import com.udacity.catpoint.data.SecurityRepository;
import com.udacity.catpoint.data.Sensor;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

/**
 * Service that receives information about changes to the security system. Responsible for
 * forwarding updates to the repository and making any decisions about changing the system state.
 */
public class SecurityService {

    private com.udacity.catpoint.service.ImageService imageService;
    private SecurityRepository securityRepository;
    private Set<StatusListener> statusListeners = new HashSet<>();
    private boolean catCurrentlyDetected = false;

    public SecurityService(SecurityRepository securityRepository, com.udacity.catpoint.service.ImageService imageService) {
        this.securityRepository = securityRepository;
        this.imageService = imageService;
    }

    public void setArmingStatus(ArmingStatus armingStatus) {
        if (armingStatus == ArmingStatus.DISARMED) {
            setAlarmStatus(AlarmStatus.NO_ALARM);
            // clear any previously detected cat when disarmed
            catCurrentlyDetected = false;
        } else {
            // when arming the system, reset all sensors to inactive
            new java.util.ArrayList<>(securityRepository.getSensors()).forEach(s -> {
                s.setActive(Boolean.FALSE);
                securityRepository.updateSensor(s);
            });
            // if arming to HOME and a cat was recently detected, set ALARM
            if (armingStatus == ArmingStatus.ARMED_HOME && catCurrentlyDetected) {
                setAlarmStatus(AlarmStatus.ALARM);
            }
        }
        securityRepository.setArmingStatus(armingStatus);
    }

    private void catDetected(Boolean cat) {
        catCurrentlyDetected = cat;
        if (cat && getArmingStatus() == ArmingStatus.ARMED_HOME) {
            setAlarmStatus(AlarmStatus.ALARM);
        } else if (!cat) {
            // only set NO_ALARM if no sensors are active
            boolean anyActive = securityRepository.getSensors().stream().anyMatch(Sensor::getActive);
            if (!anyActive) {
                setAlarmStatus(AlarmStatus.NO_ALARM);
            }
        }

        statusListeners.forEach(sl -> sl.catDetected(cat));
    }

    public void addStatusListener(StatusListener statusListener) {
        statusListeners.add(statusListener);
    }

    public void removeStatusListener(StatusListener statusListener) {
        statusListeners.remove(statusListener);
    }

    public void setAlarmStatus(AlarmStatus status) {
        securityRepository.setAlarmStatus(status);
        statusListeners.forEach(sl -> sl.notify(status));
    }

    private void handleSensorActivated() {
        if (securityRepository.getArmingStatus() == ArmingStatus.DISARMED) {
            return; // no problem if the system is disarmed
        }
        AlarmStatus current = securityRepository.getAlarmStatus();
        if (current == AlarmStatus.NO_ALARM) {
            setAlarmStatus(AlarmStatus.PENDING_ALARM);
        } else if (current == AlarmStatus.PENDING_ALARM) {
            setAlarmStatus(AlarmStatus.ALARM);
        }
    }

    private void handleSensorDeactivated() {
        AlarmStatus current = securityRepository.getAlarmStatus();
        if (current == AlarmStatus.PENDING_ALARM) {
            // only clear to NO_ALARM if no sensors are active
            boolean anyActive = securityRepository.getSensors().stream().anyMatch(Sensor::getActive);
            if (!anyActive) {
                setAlarmStatus(AlarmStatus.NO_ALARM);
            }
        } else if (current == AlarmStatus.ALARM) {
            setAlarmStatus(AlarmStatus.PENDING_ALARM);
        }
    }

    public void changeSensorActivationStatus(Sensor sensor, Boolean active) {
        // update the sensor first so handlers see the new state
        boolean previousActive = sensor.getActive();
        if (previousActive == active) {
            // if activation event occurs while already active and system is pending, escalate to alarm
            if (active && securityRepository.getAlarmStatus() == AlarmStatus.PENDING_ALARM) {
                setAlarmStatus(AlarmStatus.ALARM);
            }
            return; // no state change
        }

        sensor.setActive(active);
        securityRepository.updateSensor(sensor);

        if (!previousActive && active) {
            handleSensorActivated();
        } else if (previousActive && !active) {
            handleSensorDeactivated();
        }
    }

    public void processImage(BufferedImage currentCameraImage) {
        catDetected(imageService.imageContainsCat(currentCameraImage, 50.0f));
    }

    public AlarmStatus getAlarmStatus() {
        return securityRepository.getAlarmStatus();
    }

    public Set<Sensor> getSensors() {
        return securityRepository.getSensors();
    }

    public void addSensor(Sensor sensor) {
        securityRepository.addSensor(sensor);
    }

    public void removeSensor(Sensor sensor) {
        securityRepository.removeSensor(sensor);
    }

    public ArmingStatus getArmingStatus() {
        return securityRepository.getArmingStatus();
    }
}
