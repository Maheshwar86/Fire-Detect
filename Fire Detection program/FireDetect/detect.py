import cv2
import numpy as np
import pickle
import sys
import os

def load_histograms(file_name):
    try:
        with open(file_name, 'rb') as file:
            histograms = pickle.load(file)
        print(f"Loaded {len(histograms)} histograms from {file_name}")
        return histograms
    except FileNotFoundError as e:
        print(f"Error loading histograms: {e}")
        return None

def extract_color_histogram(image):
    hsv_image = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)
    hist = cv2.calcHist([hsv_image], [0, 1, 2], None, [50, 60, 70], [0, 180, 0, 256, 0, 256])
    cv2.normalize(hist, hist)
    return hist.flatten()

def is_pure_color(image, lower_color, upper_color):
    hsv_image = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)
    mask = cv2.inRange(hsv_image, lower_color, upper_color)
    color_pixels = cv2.countNonZero(mask)
    total_pixels = mask.size
    color_ratio = color_pixels / total_pixels
    return color_ratio > 0.5  # Threshold to check if the image is predominantly this color

def exclude_pure_colors(image):
    # Define HSV ranges for pure red, pure yellow, and pure orange
    pure_red_lower = np.array([0, 100, 100])
    pure_red_upper = np.array([10, 255, 255])
    pure_yellow_lower = np.array([20, 100, 100])
    pure_yellow_upper = np.array([30, 255, 255])
    pure_orange_lower = np.array([11, 100, 100])
    pure_orange_upper = np.array([19, 255, 255])

    if is_pure_color(image, pure_red_lower, pure_red_upper):
        return True
    if is_pure_color(image, pure_yellow_lower, pure_yellow_upper):
        return True
    if is_pure_color(image, pure_orange_lower, pure_orange_upper):
        return True

    return False

def match_histogram(scanned_image, histograms):
    scanned_hist = extract_color_histogram(scanned_image)
    print(f"Extracted histogram from scanned image: {scanned_hist[:5]}...")  # Print first 5 values for brevity
    for filename, hist in histograms.items():
        similarity = cv2.compareHist(scanned_hist, hist, cv2.HISTCMP_CORREL)
        print(f"Comparing with {filename}, similarity: {similarity}")
        if similarity > 0.8:  # Adjusted threshold value for better detection
            return True, filename
    return False, None

def detect_fire_pattern(image):
    # Convert image to HSV color space
    hsv_image = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)

    # Define range for fire-like colors in HSV
    lower_fire = np.array([0, 50, 50])
    upper_fire = np.array([35, 255, 255])

    # Threshold the HSV image to get only fire-like colors
    mask = cv2.inRange(hsv_image, lower_fire, upper_fire)

    # Check if a significant portion of the image is fire-like
    fire_pixels = cv2.countNonZero(mask)
    total_pixels = mask.size
    fire_ratio = fire_pixels / total_pixels

    print(f"Fire pixel ratio: {fire_ratio}")

    return fire_ratio > 0.2  # Increased threshold value for fire detection

def main(scanned_image_path):
    histograms = load_histograms('F://Fire Detection program//FireDetect//fire_histograms.pkl')  # Update the full path
    if histograms is None:
        print("No histograms loaded or error occurred.")
        return False

    scanned_image = cv2.imread(scanned_image_path)
    if scanned_image is None:
        print(f"Failed to load image from {scanned_image_path}")
        return False

    if exclude_pure_colors(scanned_image):
        print("Image contains pure red, yellow, or orange. Not a fire.")
        return False

    print(f"Loaded scanned image from {scanned_image_path}")
    fire_detected, matched_image = match_histogram(scanned_image, histograms)

    # Additional fire pattern detection
    if detect_fire_pattern(scanned_image):
        fire_detected = True

    if fire_detected:
        print("Fire detected! Matched with:", matched_image)
        print("Fire detected!")  # Ensure this exact output for Java to capture
        return True
    else:
        print("No fire detected.")
        return False

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python detect.py <scanned_image_path>")
        sys.exit(1)

    scanned_image_path = sys.argv[1]  # Path to the scanned image
    fire_detected = main(scanned_image_path)
    if fire_detected:
        print("Fire detected!")  # Ensure this exact output for Java to capture
    else:
        print("No fire detected.")
