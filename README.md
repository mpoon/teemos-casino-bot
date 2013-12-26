# Teemo's Casino Bot

The bot is developed and built using IntelliJ IDEA 13 and Groovy. Make sure the Groovy Plugin is enabled in IntelliJ by going to `Settings → Plugins`.

Install JDK 1.7 x86 to default location.

Install Groovy 2.1.8 to default location.

Open up IntelliJ IDEA and `Check out from Version Control` which is either in the Quick Start menu or under `VCS`. Choose Github, enter Github credentials and clone the `teemos-casino-bot.git` repo.

Click `Yes` for creating a new IDEA project. Choose `Create project from existing sources` and keep the default name for the project. Uncheck the `/leaguelib/src` folder so that it is not added to the project roots. Keep going and if IntelliJ asks about overwriting the module file, click `Reuse`. Select or add JDK 1.7 x86 to the project SDK. Import wizard should finish now.

## Install some dependencies

Go to `File -> Project Structure -> Libraries`. Add and download `org.codehaus.groovy.modules.http-builder:http-builder:0.6` from Maven, then click OK.

Right click on `leaguelib/src/build.xml` and `Add as Ant Build File`. In `Run -> Edit Configurations -> Application -> Bot -> Before launch`,  do `Add -> Run Ant target -> leaguelib/clean-build` and move it before the `Make`.

