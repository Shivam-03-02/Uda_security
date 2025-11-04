package com.udacity.catpoint.service;

import java.awt.image.BufferedImage;

/**
 * Backwards-compatible ImageService interface for legacy module.
 */
public interface ImageService {
    boolean imageContainsCat(BufferedImage image, float confidenceThreshold);
}
