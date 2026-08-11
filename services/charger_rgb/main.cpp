/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

#include <android-base/file.h>
#include <unistd.h>
#include <string>
#include <vector>

int main() {
    const std::string RED = "255 0 0";
    const std::string ORANGE = "255 25 0";
    const std::string YELLOW = "255 255 0";
    const std::string GREEN = "0 255 0";
    const std::string BRIGHTNESS = "127";
    const std::string CAPACITY = "/sys/class/power_supply/battery/capacity";

    const std::vector<std::string> sides = {"left", "right"};

    const std::vector<std::string> leds = {"stick",   "stick:0", "stick:1",
                                           "stick:2", "stick:3", "strip"};

    std::string capacity;
    std::string color;
    std::string last_color = "";
    int battery_level = 50;

    while (true) {
        if (android::base::ReadFileToString(CAPACITY, &capacity)) {
            battery_level = std::stoi(capacity);
        }

        if (battery_level >= 90) {
            color = GREEN;
        } else if (battery_level >= 50) {
            color = YELLOW;
        } else if (battery_level >= 25) {
            color = ORANGE;
        } else {
            color = RED;
        }

        if (color != last_color) {
            last_color = color;

            for (const std::string& side : sides) {
                for (const std::string& led : leds) {
                    std::string path = "/sys/class/leds/" + side + ":" + led;

                    if (!access(path.c_str(), F_OK)) {
                        android::base::WriteStringToFile(BRIGHTNESS, path + "/brightness");
                        android::base::WriteStringToFile(color, path + "/multi_intensity");
                    }
                }
            }
        }

        sleep(60);
    }

    return 0;
}
