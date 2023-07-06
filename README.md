## Before building/running
### Get Firebase SDK
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

### Generate secrets file
```bash
./gradlew hideSecretFromPropertiesFile -PpropertiesFileName=app/secrets.properties
```

### Generate Compose destinations file
```bash
./gradlew kspDebugKotlin
```