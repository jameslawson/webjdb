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


You may need to set your `JAVA_HOME` environment variable:

    export JAVA_HOME=$(/usr/libexec/java_home)


### Getting started
1. clone the repo then cd to the root of the project.
2. Install gradle `brew install gradle`.
3. Compile the target `javac -g Target.java` (-g flag needed, it adds debug info)
4. Run the target application, telling it to listen on port 5050: `runtarget.sh`
5. Build and run the webserver+debugger `./gradlew bootRun`
6. Open your browser at localhost:8080
7. Click connect

After the above five steps, the debugger should have connected to the target, Target.java 
(the program we are debugging), 
looked at the stack frame of the main method, seen that the variable called `test` has 
value 10 and have sent `test = 10` via websockets to the browser. Finally, the client browser 
should have printed `test = 10` on the webpage for the user to see.

### Todo

Right now I'm working on:
- Add a step button
- Inspecting all variables in current stack and sending this to the browser

### References

Uses code from: 
- https://spring.io/guides/gs/messaging-stomp-websocket/
- http://wayne-adams.blogspot.co.uk/2011/10/generating-minable-event-stream-with.html
- http://smithlabsoftware.googlecode.com/svn/trunk/fiji/src-plugins/Script_Editor/fiji/scripting/StartDebugging.java
