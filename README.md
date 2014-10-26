#Archivalry

##What is Archivalry?
Archivalry is an (web) application to serve files and repositories thereof.
Archivalry is developed to be simple and small and as such does not have some of the more fancy features (or styling).
Various repository types are supported, although they may not be *completely* to spec, most of them should just work (famous last words) (feel free to create an issue if something doesn't work).
I wrote Archivalry as a quicky project, as such the code isn't very clean or pretty and neither is the UI. But it works.

##What kind of repositories are supported?
* Direct file access (via simple api, or via link available from dashboard)
* Maven (Maven 2 only. Basic support only. Only tested with sbt. No maven-metadata.xml.)

##API
See ````doc/API.md````

##Installation
###Debian
On your local machine:
* Get source
* Run ````sbt debian:packageBin```` (make sure to have the dev packages for dpkg installed, as well as sbt)
* Your deb will be under ./target/
* Upload this file to your server and install it.
* Make sure to also install a jre (I recommend openjdk-7-jre-headless on ubuntu)
* Run this command on your server: ````sudo archivalry -DapplyEvolutions.default=true```` This will run archivalry once. When you see ````[info] play - Application started (Prod)```` you can kill it with CTRL-C.
* Now run this on your server: ````sudo chown -R archivalry:archivalry /usr/share/archivalry ; sudo chown -R archivalry:archivalry /usr/share/archivalry/*```` (to fix some perms that were messed up by the previous command)
* Now run this on your server: ````sudo service archivalry restart```` and away you go.
* I recommend getting apache2 (or nginx, although I have no experience with the latter) to function as a (reverse) proxy. See your webserver docs for how to do this.

###RPM based
It should basically be the same as the Debian instructions.
Some notes though:
* The sbt command will have to be ````sbt rpm:packageBin````
* I haven't tested rpm. It might not work.
* You need to use the distro specific method for restarting a service to restart the archivalry service where the debian guide uses the ````sudo service archivalry restart```` command.

###Manually (CURRENTLY BROKEN)

At the moment this process doesn't work. Please one the packaged processes, these are officially supported.

So far I haven't gotten a proper install procedure together when doing manual installs, as such, this may not work.
The basic gist of the process is this:
* Get source
* Run ````sbt assembly```` (make sure to have sbt installed)
* Copy the assembled jar to your server (in target/scala*).
* Run the jar with ````java -jar <filename>.jar```` (it will set stuff up in the current workingdirectory)
* Set your web server up to serve 127.0.0.1:9000 on some port (it *must* be as / because of a bug in the routing code).
* Log into /admin/ with the username admin and the password password.
* *Change* your admin password (if you delete the admin account it will be recreated with the default settings).
* Be on your merry way!

##After installation
* Be sure to log into <server ip>:9000/admin/ (the admin panel) with the username admin and the password password. You should change the admin password in the admin panel as a first thing.

#Contributing
Make sure to first set the basepath in app/controllers/Files.scala properly.
