**Inapphelp** is a library to provide in-app support for your app's users. Your app’s users can easily raise requests or report issues from within your app.

With inapphelp, you can:

- Let users report issues within your app, along with device & app information
- Provide self-support by showing FAQ articles
- Customise your Inapphelp screen to blend with your app theme


Library based on [helpstack.io](http://www.helpstack.io), with some improvments (like push notifications and user interaction logs)

## Installation

Installing the HelpStack library is fairly straight-forward. 

1. Add jcenter as a repository to your app's build.gradle

        repositories {
          jcenter()
        }
        
2. Add *ru.apps-m:inapphelp:0.0.2* as a dependency in the same build.gradle
        
        dependencies {
          compile 'ru.apps-m:inapphelp:0.0.2'
        }

## External Dependencies

Inapphelp depends on the following libraries:

    com.android.support:appcompat-v7:20.0.0
    com.google.code.gson:gson:2.2.4
    org.apache.httpcomponents:httpmime:4.2.6
    com.mcxiaoke.volley:library-aar:1.+
  

## License

MIT license. See the LICENSE file for more info.
