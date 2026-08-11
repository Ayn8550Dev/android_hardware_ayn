/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

#include <android-base/file.h>
#include <android-base/logging.h>
#include <string>
#include <vector>

using android::base::ReadFileToString;
using android::base::WriteStringToFile;

int main() {
    const std::vector<std::string> search_paths = {
        "/mnt/vendor/persist/odin/joys",
        "/mnt/vendor/persist/retrostation/joys"
    };

    const std::vector<std::string> triggers = {
        "left_xy_convert=1.0"
    };

    for (const auto& path : search_paths) {
        std::string content;
        if (ReadFileToString(path, &content)) {
            for (const auto& trigger : triggers) {
                if (content.find(trigger) != std::string::npos) {
                    if (WriteStringToFile("1", "/sys/class/moorechip-joystick/joystick/left_stick_axis_swap")) {
                        LOG(INFO) << "Applied left joystick axis swap";
                    }
                    return 0;
                }
            }
        }
    }

    return 0;
}
