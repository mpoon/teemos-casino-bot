/**
 * Created with IntelliJ IDEA.
 * User: mpoon
 * Date: 11/23/13
 * Time: 2:51 AM
 * To change this template use File | Settings | File Templates.
 */


import groovy.util.logging.Log
import groovyx.net.http.*

import static groovyx.net.http.Method.*

@Log
class Bot {
    SpectateHandler spectateHandler
    LoLHandler lolHandler

    def Bot() {
        spectateHandler = new SpectateHandler()
        lolHandler = new LoLHandler()
    }

    def gameStart_toServer(http, int gameId) {
        def delays = [0, 100, 500, 1000, 5000, 30000, 60000]
        Thread.start {
            def success = false
            while(!success) {
                println "game_start: " + delays[0]
                Thread.sleep(delays[0])
                try {
                    http.request( POST ) {
                        uri.path = lolHandler.botConfig.saltyspoonApiGameStartEndpoint
                        uri.query = [game_id: gameId]
                        headers.'Authorization' = lolHandler.botConfig.api_key

                        response.success = { resp, json ->
                            log.info("Bot: POST game_start: ${resp.statusLine}")
                            log.info("Bot: ${json}")
                            success = true
                        }

                        response.failure = { resp ->
                            log.severe("Bot: game_start API error: ${resp.statusLine}")
                            if(delays.size() > 1) {
                                delays.remove(0)
                            }
                        }
                    }
                }
                catch(Exception e) {
                    log.info("Bot: game_start API error: ${e.message}")
                    if(delays.size() > 1) {
                        delays.remove(0)
                    }
                }
            }
        }
    }

    def gameEnd_toServer(http, int gameId, String winner) {
        def delays = [0, 100, 500, 1000, 5000, 30000, 60000]
        Thread.start {
            def success = false
            while(!success) {
                println "game_end: " + delays[0]
                Thread.sleep(delays[0])
                try {
                    http.request( POST ) {
                        uri.path = lolHandler.botConfig.saltyspoonApiGameEndEndpoint
                        uri.query = [game_id: gameId, winner: winner]
                        headers.'Authorization' = lolHandler.botConfig.api_key

                        response.success = { resp, json ->
                            log.info("Bot: POST game_end: ${resp.statusLine}")
                            log.info("Bot: ${json}")
                            success = true
                        }

                        response.failure = { resp ->
                            log.severe("Bot: game_end API error: ${resp.statusLine}")
                            if(delays.size() > 1) {
                                delays.remove[0]
                            }
                        }
                    }
                }
                catch(Exception e) {
                    log.info("Bot: game_end API error: ${e.message}")
                    if(delays.size() > 1) {
                        delays.remove[0]
                    }
                }
            }
        }
    }

    def run() {
        def http = (lolHandler.botConfig.dev.toBoolean()) ?
                new HTTPBuilder(lolHandler.botConfig.saltyspoonApiBaseDev) :
                new HTTPBuilder(lolHandler.botConfig.saltyspoonApiBase)

        while(true) {

            gameStart_toServer(http, lolHandler.game.getId())

            if(!lolHandler.botConfig.dev.toBoolean()) {
                spectateHandler.startSpectate(lolHandler.getFeaturedSummoner())
            }

            while(!lolHandler.getGameDone()) {
                Thread.sleep(lolHandler.botConfig.gameCheckInterval);
                lolHandler.refresh()
            }

            Thread.sleep(lolHandler.botConfig.spectateDestroyDelay)

            gameEnd_toServer(http, lolHandler.game.getId(), lolHandler.winner.toString().toLowerCase())

            if(!lolHandler.botConfig.dev.toBoolean()) {
                spectateHandler.endSpectate()
            }

            lolHandler.reset()
            lolHandler.init()
        }
    }

    public static void main(String[] args) {
        Bot bot = new Bot()
        bot.run()
    }
}