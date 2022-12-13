package io.wollinger.rataudc

import org.json.JSONObject
import java.io.File

fun main() {
    File("config.json").also { cfg ->
        if(!cfg.exists()) {
            cfg.createNewFile()
            cfg.writeText("{}")
        }

        fun cfg(json: JSONObject, key: String, desc: String): String {
            if(!json.has(key)) {
                print(desc)
                json.put(key, readln())
            }
            return json[key] as String
        }

        JSONObject(cfg.readText()).also { json ->
            Ratau.token = cfg(json, "token", "Enter token: ")
            Ratau.ownerID = cfg(json, "ownerID", "Enter owner id: ")
            Ratau.ownerUsername = cfg(json, "ownerUsername", "Enter owner username: ")
        }.also { cfg.writeText(it.toString(4)) }
    }
    Ratau.init()
}