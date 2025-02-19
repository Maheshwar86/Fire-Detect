import cv2
import numpy as np
import os
import pickle

def extract_color_histogram(image):
    hsv_image = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)
    hist = cv2.calcHist([hsv_image], [0, 1, 2], None, [50, 60, 70], [0, 180, 0, 256, 0, 256])
    cv2.normalize(hist, hist)
    return hist.flatten()

def create_histograms(directory):
    histograms = {}
    for filename in os.listdir(directory):
        if filename.endswith(('.png', '.jpg', '.jpeg')):
            image_path = os.path.join(directory, filename)
            image = cv2.imread(image_path)
            if image is not None:
                hist = extract_color_histogram(image)
                histograms[filename] = hist
                print(f"Processed {filename}")
            else:
                print(f"Failed to load {filename}")
    return histograms

def save_histograms(histograms, output_file):
    with open(output_file, 'wb') as file:
        pickle.dump(histograms, file)
    print(f"Histograms saved to {output_file}")

def main():
    directory = 'F://Fire Detection program//FireDetect//Fire Images'  # Update this path to your directory with fire photos
    output_file = 'fire_histograms.pkl'  # Output file to save the histograms

    histograms = create_histograms(directory)
    save_histograms(histograms, output_file)

if __name__ == "__main__":
    main()
