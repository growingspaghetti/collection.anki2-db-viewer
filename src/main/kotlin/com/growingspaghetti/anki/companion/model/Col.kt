package com.growingspaghetti.anki.companion.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.growingspaghetti.anki.companion.KtObjectMapper
import java.math.BigDecimal
import java.util.*

/**
 * Col is about collection
 */
data class Col(
        val id: Long,
        val crt: Long,
        val mod: Long,
        val scm: Long,
        val ver: Int,
        val dty: Int, // dirty. not used
        val usn: Int,
        val ls: Long,
        val conf: String,
        val models: String,
        val decks: String,
        val dconf: String,
        val tags: String
)

fun Col.crtCreationDate() = Date(this.crt * 1000)
fun Col.modModifiedDate() = Date(this.mod)
fun Col.scmSchemaModifiedDate() = Date(this.scm)
fun Col.lsLastSyncedDate() = Date(this.ls)

fun Col.html() = """
<table><tbody>
    <tr><td>Created on</td><td>${this.crtCreationDate()}</td></tr>
    <tr><td>Last modified on</td><td>${this.modModifiedDate()}</td></tr>
    <tr><td>Last schema modified on</td><td>${this.scmSchemaModifiedDate()}</td></tr>
    <tr><td>Last synced on</td><td>${this.lsLastSyncedDate()}</td></tr>
    
    <tr><td>Version</td><td>${this.ver}</td></tr>
    <tr><td>Update sequence number</td><td>${this.usn}</td></tr>
</tbody></table>
""".trimIndent()

data class Deck(
        val id: Long,
        val mod: Long,
        val mid: Long,
        val conf: Long,
        val name: String,
        val desc: String,
        val extendNew: Int,
        val extendRev: Int,
        val usn: Int,
        val collapsed: Boolean,
        val browserCollapsed: Boolean,
        @JsonFormat(shape = JsonFormat.Shape.ARRAY)
        val newToday: Pair<Int, Int>,
        @JsonFormat(shape = JsonFormat.Shape.ARRAY)
        val revToday: Pair<Int, Int>,
        @JsonFormat(shape = JsonFormat.Shape.ARRAY)
        val lrnToday: Pair<Int, Int>,
        @JsonFormat(shape = JsonFormat.Shape.ARRAY)
        val timeToday: Pair<Int, Int>,
        val dyn: Int
) {
    companion object {
        fun columnIdentifiers() = arrayOf("name", "id", "mod", "mid", "conf",
                "desc", "extendNew", "extendRev", "usn", "collapsed", "browserCollapsed",
                "newToday", "revToday", "lrnToday", "timeToday", "dyn")
    }
}

fun Col.deckList(): List<Deck> {
    val decks = KtObjectMapper.mapper.readTree(this.decks)
    return decks
            .fields()
            .asSequence()
            .map { KtObjectMapper.mapper.convertValue(it.value, Deck::class.java) }
            .toList()
}

fun Deck.idCreationDate() = Date(this.id)
fun Deck.modModifiedDate() = Date(this.mod * 1000)
fun Deck.row() = arrayOf(this.name, this.id, this.mod, this.mid, this.conf,
        this.desc, this.extendNew, this.extendRev, this.usn, this.collapsed, this.browserCollapsed,
        this.newToday, this.revToday, this.lrnToday, this.timeToday, this.dyn)

fun Deck.html() = """
<table><tbody>
    <tr><td>Name</td><td>${this.name}</td></tr>
    <tr><td>ID</td><td>${this.id}</td></tr>
    <tr><td>Created on</td><td>${this.idCreationDate()}</td></tr>
    <tr><td>Last modified on</td><td>${this.modModifiedDate()}</td></tr>
    <tr><td>mid</td><td>${this.mid}</td></tr>
    <tr><td>conf</td><td>${this.conf}</td></tr>
    <tr><td>dynamic</td><td>${if (this.dyn > 0) "Yes" else "No"}</td></tr>
    <tr><td>Update sequence number</td><td>${this.usn}</td></tr>
</tbody></table>
""".trimIndent()


data class Model(
        val id: Long,
        val name: String,
        val flds: List<Fld>,
        val tmpls: List<Templ>,
        val tags: List<String>,
        @JsonFormat(shape = JsonFormat.Shape.ARRAY)
        val req: List<Triple<Int, String, List<Int>>>?,
        val vers: List<kotlin.Any>?, // not used
        val sortf: Int,
        val mod: Long,
        val did: Int,
        val type: Int,
        val latexPre: String,
        val latexPost: String,
        val css: String,
        val usn: Int,
        val latexsvg: Boolean?
) {
    companion object {
        fun columnIdentifiers() = arrayOf("name", "id", "flds", "tmpls", "tags",
                "req", "vers", "sortf", "mod", "did", "type",
                "latexPre", "latexPost", "css", "usn", "latexsvg")
    }
}

fun Model.idCreationDate() = Date(this.id)
fun Model.modModifiedDate() = Date(this.mod * 1000)
fun Model.row() = arrayOf(this.name, this.id, this.flds, this.tmpls, this.tags,
        this.req, this.vers, this.sortf, this.mod, this.did, this.type,
        this.latexPre, this.latexPost, this.css, this.usn, this.latexsvg)

data class Fld(
        val ord: Int,
        val sticky: Boolean,
        val rtl: Boolean,
        val media: List<kotlin.Any>?, // not used
        val font: String,
        val name: String,
        val size: Int
);

data class Templ(
        val afmt: String,
        val bafmt: String,
        val bqfmt: String,
        val did: kotlin.Any?, // null by default
        val name: String,
        val ord: Int,
        val qfmt: String
)

fun Col.modelList(): List<Model> {
    val models = KtObjectMapper.mapper.readTree(this.models)
    return models
            .fields()
            .asSequence()
            .map { KtObjectMapper.mapper.convertValue(it.value, Model::class.java) }
            .toList()
}

fun Model.html() = """
<table><tbody>
    <tr><td>Name</td><td>${this.name}</td></tr>
    <tr><td>ID</td><td>${this.id}</td></tr>
    <tr><td>Created on</td><td>${this.idCreationDate()}</td></tr>
    <tr><td>Last modified on</td><td>${this.modModifiedDate()}</td></tr>
    <tr><td>Number of fields</td><td>${this.flds.size}</td></tr>
    <tr><td>Names of fields</td><td>${this.flds.map { it.name }}</td></tr>
    <tr><td>Number of templates</td><td>${this.tmpls.size}</td></tr>
    <tr><td>Names of templates</td><td>${this.tmpls.map { it.name }}</td></tr>
    <tr><td>Update sequence number</td><td>${this.usn}</td></tr>
</tbody></table>
""".trimIndent()

data class Conf(
        val activeCols: List<String>,
        val lastUnburied: Int,
        val nextPos: Int,
        val sortType: String,
        val newSpread: Int,
        val savedFilters: kotlin.Any, // empty object {}
        val curModel: String, // "Long"
        val curDeck: Long,
        val collapseTime: Int,
        val dayLearnFirst: Boolean,
        val timeLim: Int,
        val dueCounts: Boolean,
        val activeDecks: List<Long>,
        val sortBackwards: Int, // 0 | 1 ?
        val estTimes: Boolean,
        val newBury: Boolean,
        val addToCur: Boolean,
        val nightMode: Boolean
)

fun Col.confObject(): Conf {
    val conf = KtObjectMapper.mapper.readTree(this.conf)
    return KtObjectMapper.mapper.convertValue(conf, Conf::class.java)
}

fun Conf.html(decks: List<Deck>): String {
    val curDeck = decks
            .filter { it.id == this.curDeck }.firstOrNull()?.name
    return """
    <table><tbody>
        <tr><td>Active columns</td><td>${this.activeCols}</td></tr>
        <tr><td>Sort type</td><td>${this.sortType}</td></tr>
        <tr><td>Current deck</td><td>$curDeck</td></tr>
    </tbody></table>
    """.trimIndent()
}

data class DConf(
        val id: Long,
        val dyn: Boolean,
        val mod: Long,
        val rev: Rev,
        val lapse: Lapse,
        val usn: Int,
        val name: String,
        val timer: Int,
        val replayq: Boolean,
        val new: New,
        val autoplay: Boolean,
        val maxTaken: Int
)

fun DConf.modModifiedDate() = Date(this.mod * 1000)

data class Rev(
        val bury: Boolean,
        val minSpace: Int,
        val ease4: BigDecimal,
        val ivlFct: Int,
        val maxIvl: Int,
        val perDay: Int,
        val fuzz: BigDecimal
)

data class Lapse(
        val minInt: Int,
        val leechFails: Int,
        val mult: Int,
        val leechAction: Int,
        val delays: List<Int>
)

data class New(
        val bury: Boolean,
        val order: Int,
        val ints: List<Int>,
        val separate: Boolean,
        val perDay: Int,
        val delays: List<Int>,
        val initialFactor: Int
)

fun Col.dconfList(): List<DConf> {
    val dconf = KtObjectMapper.mapper.readTree(this.dconf)
    return dconf
            .fields()
            .asSequence()
            .map { KtObjectMapper.mapper.convertValue(it.value, DConf::class.java) }
            .toList()
}

fun DConf.html(): String {
    return """
    <table><tbody>
        <tr><td>Name</td><td>${this.name}</td></tr>
        <tr><td>ID</td><td>${this.id}</td></tr>
        <tr><td>Last modified on</td><td>${this.modModifiedDate()}</td></tr>
        <tr><td>Revision</td><td>${this.rev}</td></tr>
        <tr><td>New</td><td>${this.new}</td></tr>
    </tbody></table>
    """.trimIndent()
}

data class Tags(
        val dialogue: Int,
        val leech: Int,
        val marked: Int
)

fun Col.tagsObject(): Tags {
    val tags = KtObjectMapper.mapper.readTree(this.tags)
    return KtObjectMapper.mapper.convertValue(tags, Tags::class.java)
}
