# webjdb
*Java debugging in your browser.*    
Uses the [Java Debugger Interface](http://docs.oracle.com/javase/7/docs/jdk/api/jpda/jdi/) and websockets so that you can debug a java program from within a web browser. 

**Potential Advantages**
- Customizable - for those who want to write javascript to customize a debugger's UI 
eg. change themes, layout, keyboard shortcuts, custom plugins 
- Highly visual - browsers can give strong visual feedback - CSS3 transisitons, d3.js data visualization. 
Much easier to write these graphical features for web browsers than on native.
- Portable - works on all OS's, on any modern browser
- Lightweight - minimalist UI, quick to open/close. 
- Keyboard oriented - access all aspects of debugger without touching the mouse
- It's not jdb - jdb sucks for actual debugging

**Potential Users**: vim/emacs users, those who perhaps don't want to use a java IDE,
those interested in web technologies. Ideal for small java projects - e.g. 
practicing for interviews, competitive programming, quickly testing a java language feature. 

----

**Currently a proof of concept. Very early in development.**    
Right now webjdb can only inspect the stack frame of a program 
and send the value of a variable to be displayed in the browser.

### Dependencies
- Spring 4
- Gradle
- Websockets (sock.js)
- JDK >= 1.8.0_05 (needs JDI which belongs to JDK's tools.jar)




### Getting started

You need to download the [Java Development Kit 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html). 
You may also need to set your `JAVA_HOME` environment variable. This 
can be done by adding the following to your .bashrc/.zshrc:

    export JAVA_HOME=$(/usr/libexec/java_home)


1. clone the repo then cd to the root of the project.
2. Install gradle `brew install gradle`.
3. Compile the target `javac -g Target.java` (-g flag needed, it adds debug info)
4. In one terminal session, run the target application, telling it to listen on port 5050: `runtarget.sh`
5. In another terminal session, build and run the webserver+debugger: `./gradlew bootRun`
6. Open your browser at localhost:8080
7. Click the *connect* button. This tells the debugger to connect to the target that's listening on port 5050.

At this point, you can click the *step* and *print frame* buttons to step through the program and 
print the contents of the current stack frame. 

### References

Uses code from: 
- https://spring.io/guides/gs/messaging-stomp-websocket/
- http://wayne-adams.blogspot.co.uk/2011/10/generating-minable-event-stream-with.html
- http://smithlabsoftware.googlecode.com/svn/trunk/fiji/src-plugins/Script_Editor/fiji/scripting/StartDebugging.java
