import groovy.util.logging.Log
import net.sourceforge.tess4j.Tesseract
import net.sourceforge.tess4j.TesseractException

import java.awt.*
import java.beans.PropertyChangeListener
import java.util.List

@Log
class VisualHandler {
    Robot robot
    Rectangle KDABoxBlue = new Rectangle(784, 1037, 81, 154)
    Rectangle KDABoxPurple = new Rectangle(1057, 1037, 81, 154)
    Tesseract tesseract
    int OCR_POLLING = 1000
    ObservableMap stats
    List TEAMS = ['blue', 'purple']
    List PLAYERS = ['0', '1', '2', '3', '4']
    List STATS = ['kills', 'deaths', 'assists']
    boolean game_end

    VisualHandler() {
        robot = new Robot()
        tesseract = Tesseract.getInstance()
        tesseract.setLanguage("kda")
        init()
    }

    def init() {
        game_end = false
        stats = [:] as ObservableMap
        TEAMS.each { team ->
            PLAYERS.each { player ->
                STATS.each { stat ->
                    stats["${team}_${player}_${stat}"] = 0
                }
            }
        }
        stats.addPropertyChangeListener({ evt ->
            log.info("${evt.propertyName}: ${evt.oldValue} -> ${evt.newValue}")
        } as PropertyChangeListener)
    }

    def run() {
        Thread.start {
            while (!game_end) {
                def teams = [blue: robot.createScreenCapture(KDABoxBlue),
                        purple: robot.createScreenCapture(KDABoxPurple)]
                try {
                    teams = teams.collectEntries { key, value -> [key, tesseract.doOCR(value).split('\n')] }
                    if (teams.every { it.value.length == 5 }) {
                        teams = teams.collectEntries { key, value -> [key, value*.split('/')] }
                        if (teams.every { it.value.every { it.size() == 3 } } &&
                                teams.every { it.value.flatten().every { it.isInteger() } }) {
                            teams.each() { key, value ->
                                value.eachWithIndex() { kda, i ->
                                    STATS.eachWithIndex() { stat, j ->
                                        if (stats["${key}_${i}_${stat}"] != kda[j].toInteger()) {
                                            stats["${key}_${i}_${stat}"] = kda[j].toInteger()
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (TesseractException e) {
                    System.err.println(e.getMessage());
                }
                Thread.sleep(OCR_POLLING)
            }
        }
    }

    def stop() {
        game_end = true
    }

    def reset() {
        init()
    }
}