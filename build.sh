#!/bin/sh

wget https://dl.google.com/android/android-sdk_r24.4.1-linux.tgz
tar zxvf android-sdk*.tgz

# Define android Home and add PATHs (After that you can run "android")
export ANDROID_HOME="/opt/atlassian/pipelines/agent/build/android-sdk-linux"
export PATH="$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools:$PATH"

# Update android sdk
sdkmanager "extras;google;google_play_services" "extras;google;m2repository"
android update sdk --no-ui --filter "build-tools-25.0.2,android-25,extra-android-m2repository" -a
android-sdk-update@1.0.4
touch ~/.android/repositories.cfg

# Accept all licences http://stackoverflow.com/questions/38096225/automatically-accept-all-sdk-licences
mkdir -p "$ANDROID_HOME/licenses"
echo -e "\n8933bad161af4178b1185d1a37fbf41ea5269c55\nd56f5187479451eabf01fb78af6dfcb131a6481e" > "${ANDROID_HOME}/licenses/android-sdk-license"
echo -e "84831b9409646a918e30573bab4c9c91346d8abd" > "$ANDROID_HOME/licenses/android-sdk-preview-license"
yes | sudo sdkmanager --licenses

# Build apk
chmod a+x gradlew
./gradlew assembleDebug