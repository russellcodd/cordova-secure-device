This has been forked from the OutSystems experts version, and hardcoded in the ios and android src files to NOT check the pincode/pattern security. This lets us have a device that still checks for rooting or jailbreaking, but we're not bothered if the end user has a PIN set.

# cordova-secure-device

When the plugin initializes it validates if the device is compromised by validating if it is rooted or jailbroken, and if a pin, pattern, or password is set. If the device is compromised the webview is removed and an alert message is shown. Upon closing the dialog the app exits.

The plugin does not expose any javascript interface, the validation is done when the plugin initializes.

## OutSystems Experts Changes

Forked version that removes i18n and adds the ability to customize the messages shown depending on wether the device is rooted/jailbroken or no lock screen is set with pattern/pin.

### Customizing warning messages

The following preferences can be used to customize each message:

- `SecurePluginRootedDeviceString`: Sets the custom message that appears when the device is rooted/jailbroken. Defaults to: `This application does not run on a device that is rooted.`
- `SecurePluginNoLockSafetyString`: Sets the custom message that appears when the device doesn't have either a PIN or Pattern to unlock it. Defaults to: `This application does not run on a device that does not have a passcode set.`
- `SecurePluginDialogCloseLabel`: Sets the label of the button on the dialog. Defaults to "Close".

On your application config.xml file set one, or all, of the following preferences:

```xml
<preference name="SecurePluginRootedDeviceString" value="Rooted device..."/>
<preference name="SecurePluginNoLockSafetyString" value="No Unlock PIN/Pattern is set."/>
<preference name="SecurePluginDialogCloseLabel" value="CLOSE NOW"/>
```


### Customizing behavior
The plugin default behavior checks if the phone has a password,pin or pattern defined. To deactivate this, change the preference `CheckPattern`.
On your application config.xml file set one, or all, of the following preferences:

```xml
<preference name="CheckPattern" value="false"/>
```

## License

```
Copyright 2016 André Vieira

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
