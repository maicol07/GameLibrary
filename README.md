## Before building/running
### Get Firebase SDK credentials
1. Go to https://console.firebase.google.com/
2. Create a new project
3. Go to the project settings
4. Add an Android app
5. Copy the `google-services.json` file to `app/`

### Get IGDB API Key
1. Go to https://api.igdb.com/signup
2. Create an account
3. Go to https://api.igdb.com/apps
4. Create an app
5. Copy the API key
6. Create a file called `secrets.properties` in `app/`
7. Add the following line to `secrets.properties`:
```properties
IGDBClientId = <your_client_id>
IGDBClientSecret = <your_client_secret>
```

### Get Google keys
1. If you haven't yet specified your app's SHA fingerprint, do so from the [Settings page](https://console.firebase.google.com/project/_/settings/general/) of the Firebase console. Refer to [Authenticating Your Client](https://developers.google.com/android/guides/client-auth) for details on how to get your app's SHA fingerprint.
2. Go to [Firebase console](https://console.firebase.google.com/) and select your project
3. Go to the **Authentication section** > **Sign-in method** > **Add new provider** > **Google**
4. When prompted in the console, download the updated Firebase config file (**google-services.json**), which now contains the OAuth client information required for Google sign-in.
5. Move this updated config file into your Android Studio project, replacing the now-outdated corresponding config file.
6. Add the following lines to `secrets.properties`:
```properties
GoogleServerClientId = <your_client_id>
```

### Generate secrets file
```bash
./gradlew hideSecretFromPropertiesFile -PpropertiesFileName=app/secrets.properties
```

### Generate Compose destinations file
```bash
./gradlew kspDebugKotlin
```