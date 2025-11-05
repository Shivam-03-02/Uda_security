package com.udacity.catpoint.service;

import com.udacity.catpoint.application.StatusListener;
import com.udacity.catpoint.data.*;
import com.udacity.catpoint.image.service.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class SecurityServiceTest {

    private SecurityService securityService;
    private TestSecurityRepository repo;
    private TestImageService imageService;

    private static class TestSecurityRepository implements SecurityRepository {
        private Set<Sensor> sensors = new HashSet<>();
        private AlarmStatus alarmStatus = AlarmStatus.NO_ALARM;
        private ArmingStatus armingStatus = ArmingStatus.DISARMED;

        @Override
        public void addSensor(Sensor sensor) {
            sensors.add(sensor);
        }

        @Override
        public void removeSensor(Sensor sensor) {
            sensors.remove(sensor);
        }

        @Override
        public void updateSensor(Sensor sensor) {
            sensors.remove(sensor);
            sensors.add(sensor);
        }

        @Override
        public void setAlarmStatus(AlarmStatus alarmStatus) {
            this.alarmStatus = alarmStatus;
        }

        @Override
        public void setArmingStatus(ArmingStatus armingStatus) {
            this.armingStatus = armingStatus;
        }

        @Override
        public Set<Sensor> getSensors() {
            return sensors;
        }

        @Override
        public AlarmStatus getAlarmStatus() {
            return alarmStatus;
        }

        @Override
        public ArmingStatus getArmingStatus() {
            return armingStatus;
        }
    }

    private static class TestImageService implements ImageService {
        boolean containsCat = false;
        public void setContainsCat(boolean v) { containsCat = v; }
        @Override
        public boolean imageContainsCat(BufferedImage image, float confidenceThreshold) {
            return containsCat;
        }
    }

    private static class TestStatusListener implements StatusListener {
        boolean notified = false;
        AlarmStatus lastStatus = null;
        Boolean catDetected = null;

        @Override
        public void notify(AlarmStatus status) {
            notified = true;
            lastStatus = status;
        }

        @Override
        public void catDetected(boolean catDetected) {
            this.catDetected = catDetected;
        }

        @Override
        public void sensorStatusChanged() {
            // not needed for tests
        }
    }

    private Sensor createSensor(String name, SensorType type, boolean active) {
        Sensor s = new Sensor(name, type);
        s.setActive(active);
        return s;
    }

    @BeforeEach
    public void setup() {
        repo = new TestSecurityRepository();
        imageService = new TestImageService();
        securityService = new SecurityService(repo, imageService);
    }

    @Test
    public void armedAndSensorActivated_putsPending() {
        repo.setArmingStatus(ArmingStatus.ARMED_AWAY);
        Sensor s = createSensor("Front", SensorType.DOOR, false);
        repo.addSensor(s);

        securityService.changeSensorActivationStatus(s, true);

        assertEquals(AlarmStatus.PENDING_ALARM, repo.getAlarmStatus());
    }

    @Test
    public void armedAndSensorActivated_whenPending_setsAlarm() {
        repo.setArmingStatus(ArmingStatus.ARMED_AWAY);
        repo.setAlarmStatus(AlarmStatus.PENDING_ALARM);
        Sensor s = createSensor("Front", SensorType.DOOR, false);
        repo.addSensor(s);

        securityService.changeSensorActivationStatus(s, true);

        assertEquals(AlarmStatus.ALARM, repo.getAlarmStatus());
    }

    @Test
    public void pendingAlarm_and_allSensorsInactive_returnToNoAlarm() {
        repo.setArmingStatus(ArmingStatus.ARMED_AWAY);
        Sensor s = createSensor("Front", SensorType.DOOR, true);
        repo.addSensor(s);
        repo.setAlarmStatus(AlarmStatus.PENDING_ALARM);

        securityService.changeSensorActivationStatus(s, false);

        assertEquals(AlarmStatus.NO_ALARM, repo.getAlarmStatus());
    }

    @Test
    public void alarmActive_changeInSensorState_noEffect() {
        repo.setArmingStatus(ArmingStatus.ARMED_AWAY);
        Sensor s = createSensor("Front", SensorType.DOOR, false);
        repo.addSensor(s);
        repo.setAlarmStatus(AlarmStatus.ALARM);

        securityService.changeSensorActivationStatus(s, true);

        assertEquals(AlarmStatus.ALARM, repo.getAlarmStatus());
    }

    @Test
    public void sensorActivated_whileAlreadyActive_andPending_setsAlarm() {
        repo.setArmingStatus(ArmingStatus.ARMED_AWAY);
        Sensor s = createSensor("Front", SensorType.DOOR, true);
        repo.addSensor(s);
        repo.setAlarmStatus(AlarmStatus.PENDING_ALARM);

        // duplicate activation
        securityService.changeSensorActivationStatus(s, true);

        assertEquals(AlarmStatus.ALARM, repo.getAlarmStatus());
    }

    @ParameterizedTest
    @EnumSource(AlarmStatus.class)
    public void sensorDeactivated_whileAlreadyInactive_noChange(AlarmStatus initialStatus) {
        repo.setArmingStatus(ArmingStatus.ARMED_AWAY);
        Sensor s = createSensor("Front", SensorType.DOOR, false);
        repo.addSensor(s);
        repo.setAlarmStatus(initialStatus);

        securityService.changeSensorActivationStatus(s, false);

        assertEquals(initialStatus, repo.getAlarmStatus());
    }

    @Test
    public void imageServiceDetectsCat_whileArmedHome_putsAlarm() {
        repo.setArmingStatus(ArmingStatus.ARMED_HOME);
        imageService.setContainsCat(true);

        securityService.processImage(null);

        assertEquals(AlarmStatus.ALARM, repo.getAlarmStatus());
    }

    @Test
    public void imageServiceNoCat_setsNoAlarm_ifNoSensorsActive() {
        repo.setArmingStatus(ArmingStatus.ARMED_HOME);
        imageService.setContainsCat(false);

        securityService.processImage(null);

        assertEquals(AlarmStatus.NO_ALARM, repo.getAlarmStatus());
    }

    @Test
    public void disarmed_setsNoAlarm() {
        repo.setAlarmStatus(AlarmStatus.ALARM);
        securityService.setArmingStatus(ArmingStatus.DISARMED);

        assertEquals(AlarmStatus.NO_ALARM, repo.getAlarmStatus());
    }

    @Test
    public void armed_resetsAllSensorsToInactive() {
        Sensor s1 = createSensor("Door1", SensorType.DOOR, true);
        Sensor s2 = createSensor("Window1", SensorType.WINDOW, true);
        repo.addSensor(s1);
        repo.addSensor(s2);

        securityService.setArmingStatus(ArmingStatus.ARMED_AWAY);

        assertFalse(repo.getSensors().stream().anyMatch(Sensor::getActive));
    }

    @Test
    public void armedHomeWhileCameraShowsCat_setsAlarm() {
        imageService.setContainsCat(true);
        securityService.processImage(null); // sets catCurrentlyDetected true

        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        assertEquals(AlarmStatus.ALARM, repo.getAlarmStatus());
    }

    @Test
    public void sensorActivationWhileDisarmed_noChange() {
        repo.setArmingStatus(ArmingStatus.DISARMED);
        Sensor s = createSensor("Door", SensorType.DOOR, false);
        repo.addSensor(s);

        securityService.changeSensorActivationStatus(s, true);

        assertEquals(AlarmStatus.NO_ALARM, repo.getAlarmStatus());
    }

    @Test
    public void sensorDeactivated_fromAlarm_setsPending() {
        repo.setArmingStatus(ArmingStatus.ARMED_AWAY);
        Sensor s = createSensor("Front", SensorType.DOOR, true);
        repo.addSensor(s);
        repo.setAlarmStatus(AlarmStatus.ALARM);

        securityService.changeSensorActivationStatus(s, false);

        assertEquals(AlarmStatus.PENDING_ALARM, repo.getAlarmStatus());
    }

    @Test
    public void imageNoCat_withActiveSensor_doesNotSetNoAlarm() {
        repo.setArmingStatus(ArmingStatus.ARMED_HOME);
        Sensor s = createSensor("Front", SensorType.DOOR, true);
        repo.addSensor(s);
        repo.setAlarmStatus(AlarmStatus.PENDING_ALARM);
        imageService.setContainsCat(false);

        securityService.processImage(null);

        assertEquals(AlarmStatus.PENDING_ALARM, repo.getAlarmStatus());
    }

    @Test
    public void setAlarmStatus_notifiesListeners_and_catDetected_notifies() {
        TestStatusListener tsl = new TestStatusListener();
        securityService.addStatusListener(tsl);

        // test alarm status notify
        securityService.setAlarmStatus(AlarmStatus.ALARM);
        assertTrue(tsl.notified);
        assertEquals(AlarmStatus.ALARM, tsl.lastStatus);

        // test catDetected notification via processImage
        tsl.notified = false;
        imageService.setContainsCat(true);
        repo.setArmingStatus(ArmingStatus.ARMED_HOME);
        securityService.processImage(null);
        assertEquals(Boolean.TRUE, tsl.catDetected);

        // remove listener and ensure no further notifications
        securityService.removeStatusListener(tsl);
        tsl.notified = false;
        securityService.setAlarmStatus(AlarmStatus.NO_ALARM);
        assertFalse(tsl.notified);
    }
}
