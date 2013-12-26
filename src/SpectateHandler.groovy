import com.achimala.leaguelib.models.LeagueSummoner
import com.gvaneyck.spectate.SpectateAnyone
import groovy.util.logging.*

/**
 * Created with IntelliJ IDEA.
 * User: mpoon
 * Date: 11/21/13
 * Time: 1:13 AM
 * To change this template use File | Settings | File Templates.
 */

@Log
class SpectateHandler {
    boolean initialized = false
    Process league_of_legends = null

    SpectateHandler() {
        log.entering('SpectateHandler', 'SpectateHandler')
        init()
        log.exiting('SpectateHandler', 'SpectateHandler')
    }

    def init() {
        log.entering('SpectateHandler', 'init')
        SpectateAnyone.initRegionMap()
        try {
            SpectateAnyone.setupClient("spectateConfig")
            initialized = true
        }
        catch(Exception e) {
            log.severe('SpectateHandler: ' + e.message)
        }
        log.exiting('SpectateHandler', 'init')
    }

    def startSpectate(LeagueSummoner summoner) {
        log.entering('SpectateHandler', 'startSpectate')
        if(!initialized) {
            log.severe('SpectateHandler: not initialized')
            return
        }

        SpectateAnyone.txtName.setText(summoner.getName())
        league_of_legends = SpectateAnyone.handleSpectate()

        if(league_of_legends == null) {
            log.severe('SpectateHandler: Error starting League of Legends spectate mode')
        }

        log.exiting('SpectateHandler', 'startSpectate')
        return
    }

    def endSpectate() {
        log.entering('SpectateHandler', 'endSpectate')
        if(league_of_legends != null) {
            league_of_legends.destroy()
        }
        log.exiting('SpectateHandler', 'endSpectate')
        return
    }
}
