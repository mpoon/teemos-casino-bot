import com.achimala.leaguelib.connection.LeagueAccount
import com.achimala.leaguelib.connection.LeagueConnection
import com.achimala.leaguelib.connection.LeagueServer
import com.achimala.leaguelib.models.*
import groovyx.net.http.HTTPBuilder

import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import groovy.util.logging.*

/**
 * Created with IntelliJ IDEA.
 * User: mpoon
 * Date: 11/21/13
 * Time: 1:48 AM
 * To change this template use File | Settings | File Templates.
 */

@Log
class LoLHandler {
    def botConfig
    LeagueConnection conn
    LeagueSummoner featuredSummoner
    LeagueGame game
    TeamType team
    TeamType winner
    boolean initialized = false
    boolean gameDone = false

    LoLHandler(){
        log.entering('LoLHandler', 'LoLHandler')
        log.info('LoLHandler:  slurp bot config')
        try {
            botConfig = new ConfigSlurper().parse(new File('botConfig').toURL())
        }
        catch(Exception e) {
            log.severe('LoLHandler: ' + e.message)
            throw e
        }

        conn = connect()
        if(conn == null) {
            log.severe('LoLHandler: Error connecting to League of Legends servers')
            throw new Exception("LoLHandler: Error connecting to League of Legends servers")
        }
        init()
        log.exiting('LoLHandler', 'LoLHandler')
    }

    def LeagueSummoner getFeaturedSummoner() {
        log.entering('LoLHandler', 'getFeaturedSummoner')
        log.exiting('LoLHandler', 'getFeaturedSummoner', this.featuredSummoner)
        return this.featuredSummoner
    }

    def connect() {
        log.entering('LoLHandler', 'connect')
        LeagueServer server = LeagueServer.findServerByCode(botConfig.server)
        LeagueConnection c = new LeagueConnection(server)
        if(botConfig.dev) {
            c.getAccountQueue().addAccount(new LeagueAccount(server, botConfig.version, botConfig.dev_user, botConfig.dev_pass))
        }
        else {
            c.getAccountQueue().addAccount(new LeagueAccount(server, botConfig.version, botConfig.user, botConfig.pass))
        }
        Map exceptions = c.getAccountQueue().connectAll()
        if(exceptions != null) {
            for(account in exceptions.keySet())
                log.severe('LoLHandler: ' + account + " error: " + exceptions.get(account))
            return null
        }
        log.exiting('LoLHandler', 'connect', c)
        return c
    }

    def init() {
        log.entering('LoLHandler', 'init')
        featuredSummoner = conn.getSummonerService().getSummonerByName(getFeaturedSummonerName())
        conn.getGameService().fillActiveGameData(featuredSummoner)
        game = featuredSummoner.getActiveGame()
        team = game.getPlayerTeamType()
        initialized = true
        refresh()
        log.exiting('LoLHandler', 'init')
    }

    def refresh() {
        log.entering('LoLHandler', 'refresh')
        if(!initialized) {
            log.warning('LoLHandler: refreshing when not initialized')
            return
        }
        conn.getPlayerStatsService().fillMatchHistory(featuredSummoner)
        List<MatchHistoryEntry> matches = featuredSummoner.getMatchHistory()
        MatchHistoryEntry mostRecentMatch = matches.get(matches.size()-1)
        if(mostRecentMatch.getGameId() == game.getId()) {
            winner = team
            if(mostRecentMatch.getStat(MatchHistoryStatType.LOSE)) {
                winner = (team == TeamType.BLUE) ? TeamType.PURPLE : TeamType.BLUE
            }
            log.info("LoLHandler: Game: ${game.getId()} Winner: ${winner.toString()}")
            gameDone = true;
        }
        log.exiting('LoLHandler', 'refresh')
    }

    def reset() {
        log.entering('LoLHandler', 'reset')
        featuredSummoner = null
        game = null
        team = null
        winner = null
        initialized = false
        gameDone = false
        log.exiting('LoLHandler', 'reset')
    }

    def private getFeaturedSummonerName() {
        log.entering('LoLHandler', 'getFeaturedSummonerName')
        def http = new HTTPBuilder(botConfig.spectatorApiBase)
        def featuredGames = http.request(GET, JSON) {
            uri.path = botConfig.spectatorApiFeaturedEndpoint

            return response.success = { resp, json ->
                return json
            }
        }
        try {
            return featuredGames["gameList"][0]["participants"][0]["summonerName"]
        }
        catch(e) {
            log.severe('LoLHandler: Unexpected featured games API response')
            throw new Exception("LoLHandler: Unexpected featured games API response")
        }
        log.exiting('LoLHandler', 'getFeaturedSummonerName')
    }
}
