# Adding SIM Swap Detection to your Android Firebase Phone Auth projects

## Requirements

- A [tru.ID Account](https://tru.id)
- A [Firebase Account](https://console.firebase.google.com)
- A mobile phone with a SIM card and mobile data connection

## Getting Started

Clone the starter-files branch via:

```bash
git clone -b starter-files --single-branch https://github.com/tru-ID/firebase-phone-auth-sim-swap-detection-android.git
```

If you're only interested in the finished code in main then run:

```bash
git clone -b main https://github.com/tru-ID/firebase-phone-auth-sim-swap-detection-android.git
```

Create a [tru.ID Account](https://tru.id)

Install the tru.ID CLI via:

```bash
npm i -g @tru_id/cli

```

Input your **tru.ID** credentials which can be found within the tru.ID [console](https://developer.tru.id/console)

Install the **tru.ID** CLI [development server plugin](https://github.com/tru-ID/cli-plugin-dev-server)

Create a new **tru.ID** project within the root directory via:

```
tru projects:create firebase-android --project-dir .
```

Run the development server, pointing it to the directly containing the newly created project configuration. This will also open up a ngrok to your development server making it publicly accessible to the Internet so that your mobile phone can access it when only connected to mobile data.

```
tru server -t --project-dir .
```

## Setting up Firebase for Android

This project uses Firebase Android. To set it up, head over to the official [documentation](https://firebase.google.com/docs/auth/android/phone-auth)

You will get a Ngrok URL in the form `https://{subdomain}.{region}.ngrok.io`.

Open the project up in your Android capable IDE, navigate to `app/src/main/java/com/example/firebaseandroid/api/retrofit/RetrofitService.kt` [here](https://github.com/tru-ID/firebase-phone-auth-sim-swap-detection-android/blob/main/app/src/main/java/com/example/firebaseandroid/API/retrofit/RetrofitService.kt#L17) and replace the value of `base_url` with the Ngrok URL.

Finally, connect your phone to your computer so it's used for running the Android project and run the application from your IDE.

## References

- [**tru.ID** docs](https://developer.tru.id/docs)
- [Firebase Phone Auth Android docs](https://firebase.google.com/docs/auth/android/phone-auth)

## Meta

Distributed under the MIT License. See [LICENSE](https://github.com/tru-ID/firebase-phone-auth-sim-swap-detection-android/blob/main/LICENSE.md)

[**tru.ID**](https://tru.id)
