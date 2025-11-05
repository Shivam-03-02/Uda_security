package com.udacity.catpoint.image.service;

import java.awt.image.BufferedImage;

/**
 * Interface describing the Image Service behavior used by SecurityService.
 */
public interface ImageService {
    boolean imageContainsCat(BufferedImage image, float confidenceThreshold);
}
