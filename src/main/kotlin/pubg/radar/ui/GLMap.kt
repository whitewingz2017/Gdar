@file:Suppress("NAME_SHADOWING")

package pubg.radar.ui

//import pubg.radar.deserializer.channel.ActorChannel.Companion.actorHasWeapons
//import pubg.radar.deserializer.channel.ActorChannel.Companion.weapons
import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Buttons.LEFT
import com.badlogic.gdx.Input.Buttons.MIDDLE
import com.badlogic.gdx.Input.Buttons.RIGHT
import com.badlogic.gdx.Input.Keys.*
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Color.*
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.DEFAULT_CHARS
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.*
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import pubg.radar.ForceRestart
import com.badlogic.gdx.math.Vector3
import org.apache.commons.math3.ml.neuralnet.Network
import pubg.radar.*
import pubg.radar.deserializer.channel.ActorChannel.Companion.actorHasWeapons
import pubg.radar.deserializer.channel.ActorChannel.Companion.actors
import pubg.radar.deserializer.channel.ActorChannel.Companion.airDropLocation
import pubg.radar.deserializer.channel.ActorChannel.Companion.airDropItems
import pubg.radar.deserializer.channel.ActorChannel.Companion.corpseLocation
import pubg.radar.deserializer.channel.ActorChannel.Companion.droppedItemCompToItem
import pubg.radar.deserializer.channel.ActorChannel.Companion.droppedItemLocation
import pubg.radar.deserializer.channel.ActorChannel.Companion.droppedItemToItem
import pubg.radar.deserializer.channel.ActorChannel.Companion.visualActors
import pubg.radar.deserializer.channel.ActorChannel.Companion.weapons
import pubg.radar.deserializer.channel.ActorChannel.Companion.itemBag
import pubg.radar.struct.Actor
import pubg.radar.struct.Archetype
import pubg.radar.struct.Archetype.*
import pubg.radar.struct.NetworkGUID
import pubg.radar.struct.cmd.*
import pubg.radar.struct.cmd.ActorCMD.actorAims
import pubg.radar.struct.cmd.ActorCMD.actorDowned
import pubg.radar.struct.cmd.ActorCMD.actorBeingRevived
import pubg.radar.struct.cmd.ActorCMD.actorHealth
import pubg.radar.struct.cmd.ActorCMD.actorWithPlayerState
import pubg.radar.struct.cmd.ActorCMD.spectatedCount //SelfSpectated Count
//import pubg.radar.struct.cmd.ActorCMD.playerSpectatedCounts
//import pubg.radar.struct.cmd.PlayerStateCMD.selfSpectated
import pubg.radar.struct.cmd.ActorCMD.playerStateToActor
import pubg.radar.struct.cmd.GameStateCMD.ElapsedWarningDuration
import pubg.radar.struct.cmd.GameStateCMD.NumAlivePlayers
import pubg.radar.struct.cmd.GameStateCMD.bCanKillerSpectate
import pubg.radar.struct.cmd.GameStateCMD.NumAliveTeams
import pubg.radar.struct.cmd.GameStateCMD.PoisonGasWarningPosition
import pubg.radar.struct.cmd.GameStateCMD.PoisonGasWarningRadius
import pubg.radar.struct.cmd.GameStateCMD.RedZonePosition
import pubg.radar.struct.cmd.GameStateCMD.RedZoneRadius
import pubg.radar.struct.cmd.GameStateCMD.SafetyZonePosition
import pubg.radar.struct.cmd.GameStateCMD.SafetyZoneRadius
import pubg.radar.struct.cmd.GameStateCMD.TotalWarningDuration
import pubg.radar.struct.cmd.GameStateCMD.isTeamMatch
import pubg.radar.struct.cmd.PlayerStateCMD.attacks
import pubg.radar.struct.cmd.PlayerStateCMD.playerNames
import pubg.radar.struct.cmd.PlayerStateCMD.playerNumKills
import pubg.radar.struct.cmd.PlayerStateCMD.playerSpec
import pubg.radar.struct.cmd.PlayerStateCMD.selfID
import pubg.radar.struct.cmd.PlayerStateCMD.selfStateID
import pubg.radar.struct.cmd.PlayerStateCMD.teamNumbers

import pubg.radar.util.tuple4
import wumo.pubg.struct.cmd.TeamCMD.team
import wumo.pubg.struct.cmd.TeamCMD.teamNumberss
import java.security.Key
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow
import kotlin.collections.HashMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.math.absoluteValue
import kotlin.math.asin

typealias renderInfo = tuple4<Actor, Float, Float, Float>

//fun Float.d(n: Int) = String.format("%.${n}f", this)
class GLMap : InputAdapter(), ApplicationListener, GameListener {
    companion object {
        operator fun Vector3.component1(): Float = x
        operator fun Vector3.component2(): Float = y
        operator fun Vector3.component3(): Float = z
        operator fun Vector2.component1(): Float = x
        operator fun Vector2.component2(): Float = y

        val spawnErangel = Vector2(795548.3f, 17385.875f)
        val spawnDesert = Vector2(78282f, 731746f)
    }

    init {
        register(this)
    }

    var firstAttach = true

    override fun onGameStart() {
        selfCoords.setZero()
        selfAttachTo = null
        firstAttach = true
		//selfSpectatedCount = 0

        /*
        preSelfCoords.set(if (isErangel) GLMap.spawnErangel else GLMap.spawnDesert)
        selfCoords.set(preSelfCoords)
        preDirection.setZero()
        */
    }

    override fun onGameOver() {
        camera.zoom = 2 / 4f

        aimStartTime.clear()
        attackLineStartTime.clear()
        pinLocation.setZero()
    }

    fun show() {
        val config = Lwjgl3ApplicationConfiguration()
        config.setTitle("RageRadar 2.0 - SHOW MENU[INS]")
        config.useOpenGL3(false, 2, 1)
        config.setWindowedMode(800, 600)
        config.setResizable(true)
        config.setBackBufferConfig(4, 4, 4, 4, 16, 4, 8)
        Lwjgl3Application(this, config)
    }

    private var playersize = 4f
    private var self2Coords = Vector2(0f, 0f)

    private lateinit var spriteBatch: SpriteBatch
    private lateinit var shapeRenderer: ShapeRenderer
    var allPlayers : ArrayList<renderInfo>? = null
    private lateinit var mapErangelTiles: MutableMap<String, MutableMap<String, MutableMap<String, Texture>>>
    private lateinit var mapMiramarTiles: MutableMap<String, MutableMap<String, MutableMap<String, Texture>>>
    private lateinit var mapS: MutableMap<String, MutableMap<String, MutableMap<String, Texture>>>
    private lateinit var mapTiles: MutableMap<String, MutableMap<String, MutableMap<String, Texture>>>
    private lateinit var iconImages: Icons
    private lateinit var corpseboximage: Texture
    private lateinit var airdropimage: Texture
    private lateinit var largeFont: BitmapFont
    private lateinit var littleFont: BitmapFont
    private lateinit var itemNameFont: BitmapFont
    private lateinit var nameFont: BitmapFont

    private lateinit var nameFontRed: BitmapFont
    private lateinit var nameFontGreen: BitmapFont
    private lateinit var nameFontOrange: BitmapFont
    private lateinit var itemFont: BitmapFont

    private lateinit var fontCamera: OrthographicCamera
    private lateinit var itemCamera: OrthographicCamera
    private lateinit var camera: OrthographicCamera
    lateinit var mapCamera : OrthographicCamera
    private lateinit var alarmSound: Sound
    private lateinit var hubpanel: Texture
    private lateinit var hubpanelblank: Texture

	//NumberPlayer
	private lateinit var pnum1: Texture
	private lateinit var pnum2: Texture
	private lateinit var pnum3: Texture
	
    private lateinit var vehicle: Texture
    private lateinit var plane: Texture
    private lateinit var boat: Texture
    private lateinit var bike: Texture
    private lateinit var bike3x: Texture
    private lateinit var buggy: Texture
    private lateinit var van: Texture
    private lateinit var pickup: Texture

    private lateinit var vehicle_b: Texture
    private lateinit var jetski_b: Texture
    private lateinit var boat_b: Texture
    private lateinit var bike_b: Texture
    private lateinit var bike3x_b: Texture
    private lateinit var buggy_b: Texture
    private lateinit var van_b: Texture
    private lateinit var pickup_b: Texture

    private lateinit var arrow: Texture
    private lateinit var arrowsight: Texture
    private lateinit var jetski: Texture
    private lateinit var player: Texture
    private lateinit var teamplayer: Texture
    private lateinit var teamplayersight: Texture
    private lateinit var playersight: Texture

    private lateinit var parachute: Texture
    private lateinit var parachute_team: Texture
    private lateinit var parachute_self: Texture
    private lateinit var grenade: Texture
    private lateinit var hubFont: BitmapFont
    private lateinit var hubFontSmall: BitmapFont
    private lateinit var hubFont1: BitmapFont
    private lateinit var hubFontShadow: BitmapFont
	private lateinit var redFont: BitmapFont
    private lateinit var redFontShadow: BitmapFont
    private lateinit var espFont: BitmapFont
    private lateinit var espFontShadow: BitmapFont
    private lateinit var compaseFont: BitmapFont
    private lateinit var compaseFontShadow: BitmapFont
    private lateinit var littleFontShadow: BitmapFont


    val clipBound = Rectangle()
    val healthBarWidth = 15000f
    val healthBarHeight = 2000f

    val gridWidth = 813000f
    val runSpeed = 7.3 * 100//6.3m/s
    val unit = gridWidth / 8
    val unit2 = unit / 10
    val visionRadius = mapWidth / 8

    val attackLineDuration = 3000
    val pinRadius = 5000f

    private val tileZooms = listOf("256", "512", "1024", "2048", "4096", "8192")
    private val tileRowCounts = listOf(1, 2, 4, 8, 16, 32)
    private val tileSizes = listOf(819200f, 409600f, 204800f, 102400f, 51200f, 25600f)

    private val layout = GlyphLayout()
    private var windowWidth = initialWindowWidth
    private var windowHeight = initialWindowWidth

    private val aimStartTime = HashMap<NetworkGUID, Long>()
    private val attackLineStartTime = LinkedList<Triple<NetworkGUID, NetworkGUID, Long>>()
    private val pinLocation = Vector2()
    private var filterWeapon = -1
    private var filterAttach = 1
    private var filterLvl2 = 1
    private var filterScope = 1
    private var filterHeals = 1
    private var filterAmmo = 1
    private var filterThrow = 1
    private var filterLevel3 = -1
    private var laptopToggle = -1
    private var filterNames = -1
    private var filterUseless = 1
    private var toggleView = 1
	private var screenScale = 1f
    private var toggleAirDropLines = 1

    private var neverShow = arrayListOf("")
    private var allItems = arrayListOf("")
    private var scopesToFilter = arrayListOf("")
    private var weaponsToFilter = arrayListOf("")
    private var attachToFilter = arrayListOf("")
    private var level2Filter = arrayListOf("")
    private var specialItems = arrayListOf("")
    private var healsToFilter = arrayListOf("")
    private var ammoToFilter = arrayListOf("")
    private var lvl3Filter = arrayListOf("")

    private var dragging = false
    private var prevScreenX = -1f
    private var prevScreenY = -1f
    private var screenOffsetX = 0f
    private var screenOffsetY = 0f
    private var toggleVehicles = -1
    private var drawGrid = 1
    private var showZ = 1
    private var toggleVNames = 1

    private fun windowToMap(x: Float, y: Float) =
            Vector2(selfCoords.x + (x - windowWidth / 2.0f) * camera.zoom * windowToMapUnit + screenOffsetX,
                    selfCoords.y + (y - windowHeight / 2.0f) * camera.zoom * windowToMapUnit + screenOffsetY)

    private fun mapToWindow(x: Float, y: Float) =
            Vector2((x - selfCoords.x - screenOffsetX) / (camera.zoom * windowToMapUnit) + windowWidth / 2.0f,
                    (y - selfCoords.y - screenOffsetY) / (camera.zoom * windowToMapUnit) + windowHeight / 2.0f)

    fun Vector2.mapToWindow() = mapToWindow(x, y)
    fun Vector2.windowToMap() = windowToMap(x, y)
    override fun scrolled(amount: Int): Boolean {

        if (camera.zoom > 0.015f && camera.zoom < 1.09f) {
            camera.zoom *= 1.05f.pow(amount)
        } else {
            if (camera.zoom < 0.015f) {
                camera.zoom = 0.016f
                println("Max Zoom")
            }
            if (camera.zoom > 1.09f) {
                camera.zoom = 1.089f
                println("Min Zoom")
            }
        }

        return true
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        when (button) {
            RIGHT -> {
                pinLocation.set(pinLocation.set(screenX.toFloat(), screenY.toFloat()).windowToMap())
                camera.update()
                println(pinLocation)
                return true
            }
            LEFT -> {
                dragging = true
                prevScreenX = screenX.toFloat()
                prevScreenY = screenY.toFloat()
                return true
            }
            MIDDLE -> {
                screenOffsetX = 0f
                screenOffsetY = 0f
            }


        }
        return false
    }

    override fun keyDown(keycode: Int): Boolean {
        if (laptopToggle != -1) { // Laptop Mode ON
            when (keycode) {
                V -> filterUseless = filterUseless * -1 //Filter Trash ON / OFF
                F -> filterLevel3 = filterLevel3 * -1 //Filter Level 3 Itesm  ON / OFF
                A -> filterWeapon = filterWeapon * -1 // Filter Weapons  ON / OFF
                Z -> filterAttach = filterAttach * -1 // Filter Attachments  ON / OFF
                D -> filterLvl2 = filterLvl2 * -1 //Filter Equipment  ON / OFF
                S -> filterScope = filterScope * -1 // Filter Scopes  ON / OFF
                C -> filterHeals = filterHeals * -1 // Filte Heals  ON / OFF
                X -> filterAmmo = filterAmmo * -1 // Filter AMMO  ON / OFF
                Q -> camera.zoom = 1 / 6f //Zoom to SA View
                W -> camera.zoom = 1 / 12f //Zoom to FAR loot view
                E -> camera.zoom = 1 / 32f //Zoom to CLOSE loot view
                ENTER -> {  // Center on Player
                    screenOffsetX = 0f
                    screenOffsetY = 0f
                }

            // Zoom In/Out || Overrides Max/Min Zoom
                LEFT_BRACKET -> camera.zoom = camera.zoom + 0.00525f //Zoom OUT
                RIGHT_BRACKET -> camera.zoom = camera.zoom - 0.00525f //Zoom IN

            // Toggle Transparent Player Icons
                F1 -> laptopToggle = laptopToggle * -1 //Toggle Laptop-Keys Mode
                NUM_4 -> showZ = showZ * -1 // Show if items are above/below/equal with player.
                NUM_7 -> toggleVehicles = toggleVehicles * -1 //Toggle Vehicles (DEFAULT ON)
                NUM_6 -> toggleVNames = toggleVNames * -1 //Toggle Vehicle Names (Default OFF)
                NUM_8 -> filterNames = filterNames * -1 //Show Player Names (Default ON)
                NUM_5 -> drawGrid = drawGrid * -1 //Show Grid (Default ON)
                NUM_9 -> toggleAirDropLines = toggleAirDropLines * -1 //Show Lines to Airdrops
                NUM_0 -> toggleView = toggleView * -1 //Show View Lines (Default ON)
            }
        } else { // Laptop Mode OFF
            when (keycode) {
            // Icon Filters
                PERIOD -> filterUseless = filterUseless * -1 //Filter Trash ON / OFF
                NUMPAD_0 -> filterLevel3 = filterLevel3 * -1 //Filter Level 3 Items ON / OFF
                NUMPAD_4 -> filterWeapon = filterWeapon * -1 //Filter Weapons ON / OFF
                NUMPAD_1 -> filterAttach = filterAttach * -1 //Filter Attachments ON / OFF
                NUMPAD_6 -> filterLvl2 = filterLvl2 * -1 //Filter Equip ON / OFF
                NUMPAD_5 -> filterScope = filterScope * -1 //Filter Scopes ON / OFF
                NUMPAD_3 -> filterHeals = filterHeals * -1 //Filter Heals ON / OFF
                NUMPAD_2 -> filterAmmo = filterAmmo * -1 //Turn AMMO Filter ON/OFF
                NUMPAD_7 -> camera.zoom = 1 / 6f //Zoom to SA view
                NUMPAD_8 -> camera.zoom = 1 / 12f //Zoom to FAR loot view
                NUMPAD_9 -> camera.zoom = 1 / 32f //Zoom to Close loot view
                ENTER -> { //Center on Player
                    screenOffsetX = 0f
                    screenOffsetY = 0f
                }

            // Functions
                F1 -> laptopToggle = laptopToggle * -1 //Switch to Laptop Keys
                F4 -> showZ = showZ * -1 // Show if items are above/below/equal with player.
                F7 -> toggleVehicles = toggleVehicles * -1 //Show Vehicles (Default ON)
                F6 -> toggleVNames = toggleVNames * -1 //Show Vehicle Names (Default OFF)
                F8 -> filterNames = filterNames * -1 //Show Names (Default ON)
                F5 -> drawGrid = drawGrid * -1 //Toggle Grid (Default ON)
                F9 -> toggleAirDropLines = toggleAirDropLines * -1 //Show Lines to Airdrops
                F11 -> toggleView = toggleView * -1 //Toggle View Lines (Default ON)
                // F12 -> ForceRestart() //NOT YET IMPLEMENTED

            // Zoom In/Out || Overrides Max/Min Zoom
                MINUS -> camera.zoom = camera.zoom + 0.00525f //Zoom OUT
                PLUS -> camera.zoom = camera.zoom - 0.00525f //Zoom IN

            }
        }
        return false
    }


    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        if (!dragging) return false
        with(camera) {
            screenOffsetX += (prevScreenX - screenX.toFloat()) * camera.zoom * 500
            screenOffsetY += (prevScreenY - screenY.toFloat()) * camera.zoom * 500
            prevScreenX = screenX.toFloat()
            prevScreenY = screenY.toFloat()
        }
        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (button == LEFT) {
            dragging = false
            return true
        }
        return false
    }

    override fun create() {
        spriteBatch = SpriteBatch()
        shapeRenderer = ShapeRenderer()
        Gdx.input.inputProcessor = this
        camera = OrthographicCamera(windowWidth, windowHeight)
        with(camera) {
            setToOrtho(true, windowWidth * windowToMapUnit, windowHeight * windowToMapUnit)
            zoom = 1 / 4f
            update()
            position.set(mapWidth / 2, mapWidth / 2, 0f)
            update()
        }

        itemCamera = OrthographicCamera(initialWindowWidth, initialWindowWidth)
        fontCamera = OrthographicCamera(initialWindowWidth, initialWindowWidth)
        alarmSound = Gdx.audio.newSound(Gdx.files.internal("sounds/Alarm.wav"))
        hubpanel = Texture(Gdx.files.internal("images/hub_panel.png"))
        hubpanelblank = Texture(Gdx.files.internal("images/hub_panel_blank_long.png"))
        corpseboximage = Texture(Gdx.files.internal("icons/box.png"))
        airdropimage = Texture(Gdx.files.internal("icons/airdrop.png"))
        pnum1 = Texture(Gdx.files.internal("images/n2.png"))
        pnum2 = Texture(Gdx.files.internal("images/n3.png"))
        pnum3 = Texture(Gdx.files.internal("images/n4.png"))
		vehicle = Texture(Gdx.files.internal("images/vehicle.png"))
        arrow = Texture(Gdx.files.internal("images/arrow.png"))
        plane = Texture(Gdx.files.internal("images/plane.png"))
        player = Texture(Gdx.files.internal("images/player.png"))
        teamplayer = Texture(Gdx.files.internal("images/team.png"))
        playersight = Texture(Gdx.files.internal("images/green_view_line.png"))
        teamplayersight = Texture(Gdx.files.internal("images/teamsight.png"))
        arrowsight = Texture(Gdx.files.internal("images/red_view_line.png"))
        parachute = Texture(Gdx.files.internal("images/parachute.png"))
        parachute_team = Texture(Gdx.files.internal("images/parachuteteam.png"))
        parachute_self = Texture(Gdx.files.internal("images/parachuteplayer.png"))

        boat = Texture(Gdx.files.internal("images/boat.png"))
        bike = Texture(Gdx.files.internal("images/bike.png"))
        jetski = Texture(Gdx.files.internal("images/jetski.png"))
        bike3x = Texture(Gdx.files.internal("images/bike3x.png"))
        pickup = Texture(Gdx.files.internal("images/pickup.png"))
        van = Texture(Gdx.files.internal("images/van.png"))
        buggy = Texture(Gdx.files.internal("images/buggy.png"))

        boat_b = Texture(Gdx.files.internal("images/boat_b.png"))
        vehicle_b = Texture(Gdx.files.internal("images/vehicle_b.png"))
        bike_b = Texture(Gdx.files.internal("images/bike_b.png"))
        jetski_b = Texture(Gdx.files.internal("images/jetski_b.png"))
        bike3x_b = Texture(Gdx.files.internal("images/bike3x_b.png"))
        pickup_b = Texture(Gdx.files.internal("images/pickup_b.png"))
        van_b = Texture(Gdx.files.internal("images/van_b.png"))
        buggy_b = Texture(Gdx.files.internal("images/buggy_b.png"))

        grenade = Texture(Gdx.files.internal("images/grenade.png"))
        iconImages = Icons(Texture(Gdx.files.internal("images/item-sprites.png")), 64)
        mapErangelTiles = mutableMapOf()
        mapMiramarTiles = mutableMapOf()
        var cur = 0
        tileZooms.forEach {
            mapErangelTiles[it] = mutableMapOf()
            mapMiramarTiles[it] = mutableMapOf()
            for (i in 1..tileRowCounts[cur]) {
                val y = if (i < 10) "0$i" else "$i"
                mapErangelTiles[it]?.set(y, mutableMapOf())
                mapMiramarTiles[it]?.set(y, mutableMapOf())
                for (j in 1..tileRowCounts[cur]) {
                    val x = if (j < 10) "0$j" else "$j"
                    mapErangelTiles[it]!![y]?.set(x, Texture(Gdx.files.internal("tiles/Erangel/$it/${it}_${y}_$x.png")))
                    mapMiramarTiles[it]!![y]?.set(x, Texture(Gdx.files.internal("tiles/Miramar/$it/${it}_${y}_$x.png")))
                }
            }
            cur++
        }
        mapTiles = mapErangelTiles


        val generatorHub = FreeTypeFontGenerator(Gdx.files.internal("font/AGENCYFB.TTF"))
        val paramHub = FreeTypeFontParameter()
        val hubhub = FreeTypeFontParameter()
		val redHub = FreeTypeFontParameter()


        hubhub.characters = DEFAULT_CHARS
        hubhub.size = 24
        hubhub.color = WHITE
        hubFont1 = generatorHub.generateFont(hubhub)
		
		redHub.characters = DEFAULT_CHARS
		redHub.size = 30
		redHub.color = RED
		redFont = generatorHub.generateFont(redHub)
		redHub.color = Color(1f, 0f, 0f, 0.4f)
		redFontShadow = generatorHub.generateFont(redHub)



        paramHub.characters = DEFAULT_CHARS
        paramHub.size = 30
        paramHub.color = WHITE
        hubFont = generatorHub.generateFont(paramHub)
        paramHub.color = Color(1f, 1f, 1f, 0.4f)
        hubFontShadow = generatorHub.generateFont(paramHub)
        paramHub.size = 16
        paramHub.color = WHITE
        espFont = generatorHub.generateFont(paramHub)
        paramHub.color = Color(1f, 1f, 1f, 0.2f)
        espFontShadow = generatorHub.generateFont(paramHub)
        paramHub.borderColor = Color.BLACK;
        paramHub.borderWidth = 3f;
        paramHub.characters = DEFAULT_CHARS
        paramHub.size = 12
        paramHub.color = WHITE
        hubFontSmall = generatorHub.generateFont(paramHub)
        val generatorNumber = FreeTypeFontGenerator(Gdx.files.internal("font/NUMBER.TTF"))
        val paramNumber = FreeTypeFontParameter()
        paramNumber.characters = DEFAULT_CHARS
        paramNumber.size = 24
        paramNumber.color = WHITE
        largeFont = generatorNumber.generateFont(paramNumber)
        val generator = FreeTypeFontGenerator(Gdx.files.internal("font/GOTHICB.TTF"))
        val param = FreeTypeFontParameter()
        param.characters = DEFAULT_CHARS
        param.size = 38
        param.color = WHITE
        largeFont = generator.generateFont(param)
        param.size = 15
        param.color = WHITE
        littleFont = generator.generateFont(param)

        param.borderColor = Color.BLACK;
        param.borderWidth = 1.5f;
        param.color = WHITE
        param.size = 12
        itemNameFont = generator.generateFont(param)
        param.borderColor = Color.BLACK;
        param.borderWidth = 1f;
        param.color = WHITE
        param.size = 10
        nameFont = generator.generateFont(param)
        param.color = RED
        param.size = 10
        nameFontRed = generator.generateFont(param)
        val healthColor = Color(0f, 0.392f, 0f, 1f)
        param.color = healthColor
        param.size = 10
        nameFontGreen = generator.generateFont(param)
        param.color = ORANGE
        param.size = 10
        nameFontOrange = generator.generateFont(param)
        param.color = WHITE
        param.size = 6
        itemFont = generator.generateFont(param)
        val compaseColor = Color(0f, 0.95f, 1f, 1f)  //Turquoise1
        param.color = compaseColor
        param.size = 10
        compaseFont = generator.generateFont(param)
        param.size = 20
        param.color = Color(0f, 0f, 0f, 0.5f)
        compaseFontShadow = generator.generateFont(param)
        param.characters = DEFAULT_CHARS
        param.size = 20
        param.color = WHITE
        littleFont = generator.generateFont(param)
        param.color = Color(0f, 0f, 0f, 0.5f)
        littleFontShadow = generator.generateFont(param)

        generatorHub.dispose()
        generatorNumber.dispose()
        generator.dispose()

    }

    override fun render() {
        Gdx.gl.glClearColor(0.417f, 0.417f, 0.417f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        if (gameStarted)
            mapTiles = if (isErangel) mapErangelTiles else mapMiramarTiles
        else return
        val currentTime = System.currentTimeMillis()

        // CharMovComp


        selfAttachTo?.apply {
            if (Type == Plane || Type == Parachute) {
                firstAttach = false
                selfCoords.set(location.x, location.y, location.z)
                selfDirection = rotation.y
            } else {
                firstAttach = false
                selfCoords.set(location.x, location.y, location.z)
                selfDirection = rotation.y
            }
        }


        val (selfX, selfY, selfZ) = selfCoords
        self2Coords = Vector2(selfX, selfY)

        //move camera
        camera.position.set(selfX + screenOffsetX, selfY + screenOffsetY, 0f)
        camera.update()

        val cameraTileScale = Math.max(windowWidth, windowHeight) / camera.zoom
        val useScale: Int
        useScale = when {
            cameraTileScale > 8192 -> 5
            cameraTileScale > 4096 -> 4
            cameraTileScale > 2048 -> 3
            cameraTileScale > 1024 -> 2
            cameraTileScale > 512 -> 1
            cameraTileScale > 256 -> 0
            else -> 0
        }
        val (tlX, tlY) = Vector2(0f, 0f).windowToMap()
        val (brX, brY) = Vector2(windowWidth, windowHeight).windowToMap()
        val tileZoom = tileZooms[useScale]
        val tileRowCount = tileRowCounts[useScale]
        val tileSize = tileSizes[useScale]

        val xMin = (tlX.toInt() / tileSize.toInt()).coerceIn(1, tileRowCount)
        val xMax = ((brX.toInt() + tileSize.toInt()) / tileSize.toInt()).coerceIn(1, tileRowCount)
        val yMin = (tlY.toInt() / tileSize.toInt()).coerceIn(1, tileRowCount)
        val yMax = ((brY.toInt() + tileSize.toInt()) / tileSize.toInt()).coerceIn(1, tileRowCount)


        paint(camera.combined) {
            for (i in yMin..yMax) {
                val y = if (i < 10) "0$i" else "$i"
                for (j in xMin..xMax) {
                    val x = if (j < 10) "0$j" else "$j"
                    val tileStartX = (j - 1) * tileSize
                    val tileStartY = (i - 1) * tileSize
                    draw(mapTiles[tileZoom]!![y]!![x], tileStartX, tileStartY, tileSize, tileSize,
                            0, 0, 256, 256,
                            false, true)
                }
            }
        }

        shapeRenderer.projectionMatrix = camera.combined
        Gdx.gl.glEnable(GL20.GL_BLEND)

        drawCircles()

        val typeLocation = EnumMap<Archetype, MutableList<renderInfo>>(Archetype::class.java)
        for ((_, actor) in visualActors) {
            typeLocation.compute(actor.Type) { _, v ->
                val list = v ?: ArrayList()
                val (centerX, centerY) = actor.location
                val direction = actor.rotation.y
                allPlayers?.add(tuple4(actor,centerX,centerY,direction))
                list.add(tuple4(actor, centerX, centerY, direction))
                list
            }

        }


        val playerStateGUID = actorWithPlayerState[selfID] ?: return
        val numKills = playerNumKills[playerStateGUID] ?: 0
        val zero = numKills.toString()
		

        paint(fontCamera.combined) {
		

            // NUMBER PANEL
            val numText = "$NumAlivePlayers"
            layout.setText(hubFont, numText)
            spriteBatch.draw(hubpanel, windowWidth - 130f, windowHeight - 60f)
            hubFontShadow.draw(spriteBatch, "ALIVE", windowWidth - 85f, windowHeight - 29f)
            hubFont.draw(spriteBatch, "$NumAlivePlayers", windowWidth - 110f - layout.width / 2, windowHeight - 29f)
			
            val teamText = "${GameStateCMD.NumAliveTeams}"
			//val numSpectator = "$selfSpectatedCount"

            if (teamText != numText && teamText > "0") {
                layout.setText(hubFont, teamText)
                spriteBatch.draw(hubpanel, windowWidth - 260f, windowHeight - 60f)
                hubFontShadow.draw(spriteBatch, "TEAM", windowWidth - 215f, windowHeight - 29f)
                hubFont.draw(spriteBatch, "${GameStateCMD.NumAliveTeams}", windowWidth - 240f - layout.width / 2, windowHeight - 29f)
			//Spectate
			//val numText1 = "$numSpecs"
            //layout.setText(hubFont, numText1)
            //hubFont.draw(spriteBatch, "EYES: $numSpectator", windowWidth - 240f - layout.width / 2, windowHeight - 59f)

			
			
			}
			val specCount = spectatedCount
			val eyes = specCount.toString()
			val numText1 = "$eyes"
			layout.setText(hubFont, numText1)
			spriteBatch.draw(hubpanel, windowWidth - 400f, windowHeight - 60f)
			redFontShadow.draw(spriteBatch, "EYES", windowWidth - 355f, windowHeight - 29f)
			redFont.draw(spriteBatch, "$eyes", windowWidth - 610f + 228f - layout.width / 2, windowHeight - 29f)
			
			/*if (selfSpectatedCount > 0){
               layout.setText(redFont, numSpectator)
                spriteBatch.draw(hubpanel, windowWidth - 380f, windowHeight - 60f)
                redFontShadow.draw(spriteBatch, "EYES", windowWidth - 335f, windowHeight - 29f)
                redFont.draw(spriteBatch, "${numSpectator}", windowWidth - 360f - layout.width / 2, windowHeight - 29f)
            }*/


			
            // ITEM ESP FILTER PANEL
            spriteBatch.draw(hubpanelblank, 30f, windowHeight - 60f)
			// This is what you were trying to do
            if (filterWeapon != 1)
                espFont.draw(spriteBatch, "WEAPON[A]", 40f, windowHeight - 25f)
            else
                espFontShadow.draw(spriteBatch, "WEAPON[A]", 40f, windowHeight - 25f)

            if (filterAttach != 1)
                espFont.draw(spriteBatch, "ATTACH[Z]", 40f, windowHeight - 42f)
            else
                espFontShadow.draw(spriteBatch, "ATTACH[Z]", 40f, windowHeight - 42f)

            if (filterScope != 1)
                espFont.draw(spriteBatch, "SCOPE[S]", 100f, windowHeight - 25f)
            else
                espFontShadow.draw(spriteBatch, "SCOPE[S]", 100f, windowHeight - 25f)

            if (filterAmmo != 1)
                espFont.draw(spriteBatch, "AMMO[X]", 100f, windowHeight - 42f)
            else
                espFontShadow.draw(spriteBatch, "AMMO[X]", 100f, windowHeight - 42f)

            if (filterLvl2 != 1)
                espFont.draw(spriteBatch, "EQUIP[D]", 150f, windowHeight - 25f)
            else
                espFontShadow.draw(spriteBatch, "EQUIP[D]", 150f, windowHeight - 25f)

            if (filterHeals != 1)
                espFont.draw(spriteBatch, "HEALS[C]", 150f, windowHeight - 42f)
            else
                espFontShadow.draw(spriteBatch, "HEALS[C]", 150f, windowHeight - 42f)

            if (filterLevel3 != 1)
                espFont.draw(spriteBatch, "LEVEL3[F]", 200f, windowHeight - 25f)
            else
                espFontShadow.draw(spriteBatch, "LEVEL3[F]", 200f, windowHeight - 25f)

            if (filterUseless != 1)
                espFont.draw(spriteBatch, "TRASH[V]", 200f, windowHeight - 42f)
            else
                espFontShadow.draw(spriteBatch, "TRASH[V]", 200f, windowHeight - 42f)

            if (laptopToggle == 1) {
                espFont.draw(spriteBatch, "[F1] Laptop Mode ON", 270f, windowHeight - 25f)
            } else {
                espFont.draw(spriteBatch, "[F1] Laptop Mode OFF", 270f, windowHeight - 25f)
            }

            val pinDistance = (pinLocation.cpy().sub(selfX, selfY).len() / 100).toInt()
            val (x, y) = pinLocation.mapToWindow()

            safeZoneHint()
            drawPlayerNames(typeLocation[Player], selfX, selfY)
            littleFont.draw(spriteBatch, "$pinDistance", x, windowHeight - y)
        }

        // This makes the array empty if the filter is off for performance with an inverted function since arrays are expensive

        scopesToFilter = if (filterScope != 1) {
            arrayListOf("")
        } else {
            arrayListOf("DotSight", "Aimpoint", "Holosight")
        }
        neverShow = if (filterUseless != 1) {
            arrayListOf("")
        } else {
            arrayListOf("Armor1", "Bag1", "Helmet1", "U.Ext", "AR.Ext", "S.Ext", "U.ExtQ", "Choke", "FH", "U.Supp", "UMP", "Vector", "UZI", "Pan", "Bandage", "9mm", "45mm", "FlashBang", "SmokeBomb", "Molotov", "SawnOff","Crowbar","Sickle","Machete","Pan","Crossbow","R45", "R1895","P92","P1911","P18C")
        }

        attachToFilter = if (filterAttach != 1) {
            arrayListOf("")
        } else {
            arrayListOf("AR.Stock", "S.Loops", "CheekPad", "A.Grip", "V.Grip", "AR.ExtQ", "S.ExtQ", "AR.Comp", "AR.Supp", "S.Supp", "S.Comp")
        }

        weaponsToFilter = if (filterWeapon != 1) {
            arrayListOf("")
        } else {
            arrayListOf("M16A4", "SCAR-L", "AK47", "SKS", "Mini14", "DP28", "FlareGun")
        }

        healsToFilter = if (filterHeals != 1) {
            arrayListOf("")
        } else {
            arrayListOf("FirstAid", "MedKit", "Drink", "Pain", "Syringe")
        }

        ammoToFilter = if (filterAmmo != 1) {
            arrayListOf("")
        } else {
            arrayListOf("556mm", "762mm", "300mm")
        }

        lvl3Filter = if (filterLevel3 != 1) {
            arrayListOf("")
        } else {
            arrayListOf("Helmet3", "Armor3", "Bag3", "CQBSS", "ACOG", "HK416", "Kar98k")
        }

        level2Filter = if (filterLvl2 != 1) {
            arrayListOf("")
        } else {
            arrayListOf("Bag2", "Armor2", "Helmet2", "Grenade")
        }


        specialItems = arrayListOf("AWM", "M24", "M249", "Mk14", "Groza", "G1B", "AUG", "Helmet3", "Armor3", "Bag3", "Syringe", "MedKit", "AR.Supp", "S.Supp", "CQBSS", "ACOG", "GhillieBrown", "GhillieGreen", "FlareGun")


        val iconScale = 1f / camera.zoom
        paint(itemCamera.combined) {

            droppedItemLocation.values
                    .forEach {
                        val (x, y, z) = it._1
                        val items = it._2
                        val (sx, sy) = Vector2(x, y).mapToWindow()
                        val syFix = windowHeight - sy
                        items.forEach {
                            if ((items !in neverShow && items !in weaponsToFilter && items !in scopesToFilter && items !in attachToFilter && items !in level2Filter
                                    && items !in ammoToFilter && items !in healsToFilter && items !in lvl3Filter)
                                    && iconScale > 11 && sx > 0 && sx < windowWidth && syFix > 0 && syFix < windowHeight) {
                                iconImages.setIcon(items)

                                draw(iconImages.icon,
                                        sx - 10, syFix - 10,
                                        30f, 30f)

                                if (showZ == 1) {
                                    if (z > (selfCoords.z + 200)) {
                                        itemNameFont.draw(spriteBatch, "^", sx-5,windowHeight - sy+5)
                                    } else if (z < (selfCoords.z - 100)) {
                                        itemNameFont.draw(spriteBatch, "v", sx-5 ,windowHeight - sy+5)
                                    } else {
                                        itemNameFont.draw(spriteBatch, "o", sx-5,windowHeight - sy+5)
                                    }
                                }
                            }
                        }
                    }

            //Draw Corpse Icon
            corpseLocation.values.forEach {
                val (x, y) = it
                val (sx, sy) = Vector2(x + 16, y - 16).mapToWindow()
                val syFix = windowHeight - sy
                val iconScale = 2f / camera.zoom
                spriteBatch.draw(corpseboximage, sx - iconScale / 2, syFix + iconScale / 2, iconScale, -iconScale,
                        0, 0, 64, 64,
                        false, true)
            }
            //Draw Airdrop Icon
            /*airDropLocation.keys.forEach {
                val grid = airDropLocation[it] ?: return@forEach
                val items = itemBag[it]
                val (x, y) = grid
                val (sx, sy) = Vector2(x, y).mapToWindow()
                val syFix = windowHeight - sy
                val iconScale = if (camera.zoom < 1 / 14f) {
                    2f / camera.zoom
                } else {
                    2f / (1 / 15f)
                }
                spriteBatch.draw(airdropimage, sx - iconScale / 2, syFix + iconScale / 2, iconScale, -iconScale,
                        0, 0, 64, 64,
                        false, true)

                if(items?.size != 0) {
                    val fontText = "ITEMS: ${items?.size}"
                    var wep_xoff = getPositionOffset(nameFont,fontText)
                    nameFontGreen.draw(spriteBatch,
                            "${fontText}", sx - wep_xoff.toFloat() - 5, windowHeight - sy +20)
                } else {
                    val fontText = "EMPTY"
                    var wep_xoff = getPositionOffset(nameFont,fontText)
                    nameFontRed.draw(spriteBatch,
                            "${fontText}", sx - wep_xoff.toFloat() - 5, windowHeight - sy +20)
                }
                val loot = airDropItems[it]
                loot?.forEachIndexed { i, item ->
                    val offset = (i+1) * 13
                    val item = item
					when(item){
					"AWM" 	-> { redFont.draw(spriteBatch, "| $item", sx +15, windowHeight - sy + 45 - offset) 	}
					"Groza" -> { redFont.draw(spriteBatch, "| $item", sx +15, windowHeight - sy + 45 - offset) 	}
					"M24"	-> { redFont.draw(spriteBatch, "| $item", sx +15, windowHeight - sy + 45 - offset) 	}
					"M249"	-> { redFont.draw(spriteBatch, "| $item", sx +15, windowHeight - sy + 45 - offset) 	}
					"AUG"	-> { redFont.draw(spriteBatch, "| $item", sx +15, windowHeight - sy + 45 - offset) 	}
					"Mk14"	-> { redFont.draw(spriteBatch, "| $item", sx +15, windowHeight - sy + 45 - offset) 	}
					"ACOG"	-> { nameFont.draw(spriteBatch, "| 8X", sx +15, windowHeight - sy + 45 - offset) 	}
					else 	-> { nameFont.draw(spriteBatch, "| $item", sx +15, windowHeight - sy + 45 - offset) }
					}
                    /*nameFont.draw(spriteBatch, "| $item",
                        sx +15, windowHeight - sy + 45 - offset)*/
                }
            }*/

			            //Draw Airdrop Icon
				airDropLocation.keys.forEach {
                val grid = airDropLocation[it] ?: return@forEach
                val (x, y) = grid
                val items = airDropItems
				val (sx, sy) = Vector2(x, y).mapToWindow()
				val syFix = windowHeight - sy
				val iconScale = if (camera.zoom < 1 / 14f) 
				{
					(2f / camera.zoom)* screenScale
				}else {
					(2f / (1 / 15f))* screenScale
				}
				spriteBatch.draw(airdropimage, sx - iconScale / 2, syFix + iconScale /2, iconScale, -iconScale, 0, 0, 64, 64, false, true)
				
				try {
				airDropItems[it]?.forEachIndexed { i, item ->
				val (netGUID, item) = item
				if (toggleAirDropLines == 1){
				val fontText = "${item}"
				val pad = 1 * 3
				val dist = ((i * getTextHeight(nameFont, fontText))) + pad
				nameFont.draw(spriteBatch, "| ${item}", sx + (20 *screenScale), (windowHeight - sy + 30) - dist)
				}
				}
				} catch (e: Exception) {
					println("${e}")
				}
            }




            //OLD
            //drawMyself(tuple4(null, selfX, selfY, selfDir.angle()))
            drawPawns(typeLocation)
            drawMyself(tuple4(null, selfX, selfY, selfDirection))
        }


        val zoom = camera.zoom
        Gdx.gl.glEnable(GL20.GL_BLEND)
        draw(Filled) {
            color = redZoneColor
            circle(RedZonePosition, RedZoneRadius, 500)
            color = visionColor
            circle(selfX, selfY, visionRadius, 500)
            color = pinColor
            circle(pinLocation, pinRadius * zoom, 10)

            drawPlayersH(typeLocation[Player])
        }
        drawGrid()
        drawAirdropDir()
        drawAttackLine(currentTime)
        //OLD
        //preSelfCoords.set(selfX,selfY)
        //preDirection = selfDir

        Gdx.gl.glDisable(GL20.GL_BLEND)
    }


    private fun ShapeRenderer.drawPlayersH(players: MutableList<renderInfo>?)
    {
        players?.forEach {
            drawAllPlayerHealth(Color(0x32cd32ff) , it)
        }
    }

    private fun drawMyself(actorInfo: renderInfo) {
        val (_, x, y, dir) = actorInfo
        val (sx, sy) = Vector2(x, y).mapToWindow()
        var parents = actors[selfID]?.attachParent
        if (parents == null) {
            if (toggleView != -1) {
                spriteBatch.draw(
                        playersight,
                        sx + 1, windowHeight - sy - 2,
                        2.toFloat() / 2,
                        2.toFloat() / 2,
                        12.toFloat(), 2.toFloat(),
                        20f, 10f,
                        dir * -1, 0, 0, 800, 64, true, false)
            }
            spriteBatch.draw(
                    player,
                    sx, windowHeight - sy - 2, 4.toFloat() / 2,
                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 5f, 4f,
                    dir * -1, 0, 0, 64, 64, true, false)
        }
    }


    private fun drawAttackLine(currentTime: Long) {
        while (attacks.isNotEmpty()) {
            val (A, B) = attacks.poll()
            attackLineStartTime.add(Triple(A, B, currentTime))
        }
        if (attackLineStartTime.isEmpty()) return
        draw(Line) {
            val iter = attackLineStartTime.iterator()
            while (iter.hasNext()) {
                val (A, B, st) = iter.next()
                if (A == selfStateID || B == selfStateID) {
                    if (A != B) {
                        val otherGUID = playerStateToActor[if (A == selfStateID) B else A]
                        if (otherGUID == null) {
                            iter.remove()
                            continue
                        }
                        val other = actors[otherGUID]
                        if (other == null || currentTime - st > attackLineDuration) {
                            iter.remove()
                            continue
                        }
                        color = attackLineColor
                        val (xA, yA, zA) = other.location
                        val (xB, yB, zB) = selfCoords
                        line(xA, yA, xB, yB)
                    }
                } else {
                    val actorAID = playerStateToActor[A]
                    val actorBID = playerStateToActor[B]
                    if (actorAID == null || actorBID == null) {
                        iter.remove()
                        continue
                    }
                    val actorA = actors[actorAID]
                    val actorB = actors[actorBID]
                    if (actorA == null || actorB == null || currentTime - st > attackLineDuration) {
                        iter.remove()
                        continue
                    }
                    color = attackLineColor
                    val (xA, yA, zA) = actorA.location
                    val (xB, yB, zB) = actorB.location
                    line(xA, yA, xB, yB)
                }
            }
        }
    }

    private fun drawCircles() {
        Gdx.gl.glLineWidth(2f)
        draw(Line) {
            //vision circle

            color = safeZoneColor
            circle(PoisonGasWarningPosition, PoisonGasWarningRadius, 100)

            color = BLUE
            circle(SafetyZonePosition, SafetyZoneRadius, 100)

            if (PoisonGasWarningPosition.len() > 0) {
                color = safeDirectionColor
                line(self2Coords, PoisonGasWarningPosition)
            }

        }

        Gdx.gl.glLineWidth(1f)
    }

    private fun drawAirdropDir() {
        Gdx.gl.glLineWidth(1f)
        if (toggleAirDropLines == 1) {
            draw(Line) {
                airDropLocation.keys.forEach {
                    val grid = airDropLocation[it] ?: return@forEach
                    val items = itemBag[it]
                    val (x, y,z) = grid
                    val airdropcoords = (Vector2(x, y))
                    color = YELLOW
                    if(items?.size != 0) {
                        line(self2Coords, airdropcoords)
                    }
                }
                Gdx.gl.glDisable(GL20.GL_BLEND)
            }
        }
    }


    private fun drawGrid() {
        if (drawGrid != 1) {
            draw(Filled) {
                color = BLACK
                //thin grid
                for (i in 0..7)
                    for (j in 0..9) {
                        rectLine(0f, i * unit + j * unit2, gridWidth, i * unit + j * unit2, 100f)
                        rectLine(i * unit + j * unit2, 0f, i * unit + j * unit2, gridWidth, 100f)
                    }
                color = BLACK
                //thick grid
                for (i in 0..7) {
                    rectLine(0f, i * unit, gridWidth, i * unit, 250f)
                    rectLine(i * unit, 0f, i * unit, gridWidth, 250f)
                }
            }
        }
    }

    private fun drawPawns(typeLocation: EnumMap<Archetype, MutableList<renderInfo>>) {
        val iconScale = 3f / camera.zoom
        for ((type, actorInfos) in typeLocation) {
            when (type) {
                TwoSeatBoat -> actorInfos?.forEach {
                    if (toggleVehicles != 1) {
                        val (actor, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()
                        if (toggleVNames != 1) compaseFont.draw(spriteBatch, "JSKI", sx + 15, windowHeight - sy - 2)
                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        var iconSize = iconScale/2
                        when {
                            playersize*4 > iconScale/2 -> iconSize = (playersize*4).toFloat()
                            else -> iconSize = (iconScale/2).toFloat()
                        }
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 100) { //occupied
                            val numPlayers = actor.attachChildren?.size
                            spriteBatch.draw(
                                    jetski,
                                    sx + 2, windowHeight - sy - 2, 2.toFloat() / 2,
                                    2.toFloat() / 2, 2.toFloat(), 2.toFloat(), iconSize, iconSize,
                                    dir * -1, 0, 0, 128, 128, true, false)
                            if(numPlayers > 0) nameFontRed.draw(spriteBatch, "$numPlayers", sx + 20, windowHeight - sy + 20)

                        } else {
                            spriteBatch.draw(
                                    jetski_b,
                                    sx + 2, windowHeight - sy - 2, 2.toFloat() / 2,
                                    2.toFloat() / 2, 2.toFloat(), 2.toFloat(), iconScale / 2, iconScale / 2,
                                    dir * -1, 0, 0, 128, 128, true, false
                            )
                        }
                    }
                }
                SixSeatBoat -> actorInfos?.forEach {
                    if (toggleVehicles != 1) {
                        val (actor, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()
                        if (toggleVNames != 1) compaseFont.draw(spriteBatch, "BOAT", sx + 15, windowHeight - sy - 2)
                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        var iconSize = iconScale/2
                        when {
                            playersize*4 > iconScale/2 -> iconSize = (playersize*4).toFloat()
                            else -> iconSize = (iconScale/2).toFloat()
                        }
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 80) { //occupied
                            val numPlayers = actor.attachChildren?.size
                            spriteBatch.draw(
                                    boat,
                                    sx + 2, windowHeight - sy - 2, 2.toFloat() / 2,
                                    2.toFloat() / 2, 2.toFloat(), 2.toFloat(), iconSize, iconSize,
                                    dir * -1, 0, 0, 128, 128, true, false
                            )
                            if(numPlayers > 0) nameFontRed.draw(spriteBatch, "$numPlayers", sx + 20, windowHeight - sy + 20)
                        } else {
                            spriteBatch.draw(
                                    boat_b,
                                    sx + 2, windowHeight - sy - 2, 2.toFloat() / 2,
                                    2.toFloat() / 2, 2.toFloat(), 2.toFloat(), iconScale, iconScale,
                                    dir * -1, 0, 0, 128, 128, true, false
                            )
                        }
                    }
                }
                TwoSeatBike -> actorInfos?.forEach {
                    if (toggleVehicles != 1) {
                        val (actor, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()
                        if (toggleVNames != 1) compaseFont.draw(spriteBatch, "BIKE", sx + 15, windowHeight - sy - 2)
                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        var iconSize = iconScale/2
                        when {
                            playersize*4 > iconScale/2 -> iconSize = (playersize*4).toFloat()
                            else -> iconSize = (iconScale/2).toFloat()
                        }
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 80) {
                            val numPlayers = actor.attachChildren?.size
                            spriteBatch.draw(
                                    bike,
                                    sx + 2, windowHeight - sy - 2, 2.toFloat() / 2,
                                    2.toFloat() / 2, 2.toFloat(), 2.toFloat(), iconSize, iconSize,
                                    dir * -1, 0, 0, 128, 128, true, false
                            )
                            if(numPlayers > 0) nameFontRed.draw(spriteBatch, "$numPlayers", sx + 20, windowHeight - sy + 20)
                        } else {
                            spriteBatch.draw(
                                    bike_b,
                                    sx + 2, windowHeight - sy - 2, 2.toFloat() / 2,
                                    2.toFloat() / 2, 2.toFloat(), 2.toFloat(), iconScale / 3, iconScale / 3,
                                    dir * -1, 0, 0, 128, 128, true, false
                            )
                        }
                    }
                }
                TwoSeatCar -> actorInfos?.forEach {
                    if (toggleVehicles != 1) {
                        val (actor, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()
                        if (toggleVNames != 1) compaseFont.draw(spriteBatch, "BUGGY", sx + 15, windowHeight - sy - 2)
                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        var iconSize = iconScale/2
                        when {
                            playersize*4 > iconScale/2 -> iconSize = (playersize*4).toFloat()
                            else -> iconSize = (iconScale/2).toFloat()
                        }
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 80) {
                            val numPlayers = actor.attachChildren?.size
                            spriteBatch.draw(
                                    buggy,
                                    sx + 2, windowHeight - sy - 2,
                                    2.toFloat() / 2, 2.toFloat() / 2,
                                    2.toFloat(), 2.toFloat(),
                                    iconSize, iconSize,
                                    dir * -1, 0, 0, 128, 128, false, false
                            )
                            if(numPlayers > 0) nameFontRed.draw(spriteBatch, "$numPlayers", sx + 20, windowHeight - sy + 20)
                        } else {
                            spriteBatch.draw(
                                    buggy_b,
                                    sx + 2, windowHeight - sy - 2,
                                    2.toFloat() / 2, 2.toFloat() / 2,
                                    2.toFloat(), 2.toFloat(),
                                    iconScale / 2, iconScale / 2,
                                    dir * -1, 0, 0, 128, 128, false, false
                            )
                        }
                    }
                }
                ThreeSeatCar -> actorInfos?.forEach {
                    if (toggleVehicles != 1) {
                        val (actor, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()
                        if (toggleVNames != 1) compaseFont.draw(spriteBatch, "BIKE", sx + 15, windowHeight - sy - 2)
                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        var iconSize = iconScale/2
                        when {
                            playersize*4 > iconScale/2 -> iconSize = (playersize*4).toFloat()
                            else -> iconSize = (iconScale/2).toFloat()
                        }
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 80) {
                            val numPlayers = actor.attachChildren?.size
                            spriteBatch.draw(
                                    bike3x,
                                    sx + 2, windowHeight - sy - 2, 2.toFloat() / 2, 2.toFloat() / 2,
                                    2.toFloat(), 2.toFloat(), iconSize, iconSize,
                                    dir * -1, 0, 0, 128, 128, true, false
                            )
                            if(numPlayers > 0) nameFontRed.draw(spriteBatch, "$numPlayers", sx + 20, windowHeight - sy + 20)
                        } else {
                            spriteBatch.draw(
                                    bike3x_b,
                                    sx + 2, windowHeight - sy - 2, 2.toFloat() / 2, 2.toFloat() / 2,
                                    2.toFloat(), 2.toFloat(), iconScale / 2, iconScale / 2,
                                    dir * -1, 0, 0, 128, 128, true, false
                            )
                        }
                    }

                }
                FourSeatDU -> actorInfos?.forEach {
                    if (toggleVehicles != 1) {
                        val (actor, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()
                        if (toggleVNames != 1) compaseFont.draw(spriteBatch, "CAR", sx + 15, windowHeight - sy - 2)
                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        var iconSize = iconScale/2
                        when {
                            playersize*4 > iconScale/2 -> iconSize = (playersize*4).toFloat()
                            else -> iconSize = (iconScale/2).toFloat()
                        }
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 80) {
                            val numPlayers = actor.attachChildren?.size
                            spriteBatch.draw(
                                    vehicle,
                                    sx + 2, windowHeight - sy - 2,
                                    2.toFloat() / 2, 2.toFloat() / 2,
                                    2.toFloat(), 2.toFloat(),
                                    iconSize, iconSize,
                                    dir * -1, 0, 0, 128, 128, false, false
                            )
                            if(numPlayers > 0) nameFontRed.draw(spriteBatch, "$numPlayers", sx + 20, windowHeight - sy + 20)
                        } else {
                            spriteBatch.draw(
                                    vehicle_b,
                                    sx + 2, windowHeight - sy - 2,
                                    2.toFloat() / 2, 2.toFloat() / 2,
                                    2.toFloat(), 2.toFloat(),
                                    iconScale / 2, iconScale / 2,
                                    dir * -1, 0, 0, 128, 128, false, false
                            )
                        }
                    }

                }
                FourSeatP -> actorInfos?.forEach {
                    if (toggleVehicles != 1) {
                        val (actor, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()
                        if (toggleVNames != 1) compaseFont.draw(spriteBatch, "PICKUP", sx + 15, windowHeight - sy - 2)
                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        var iconSize = iconScale/2
                        when {
                            playersize*4 > iconScale/2 -> iconSize = (playersize*4).toFloat()
                            else -> iconSize = (iconScale/2).toFloat()
                        }
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 80) {
                            val numPlayers = actor.attachChildren?.size
                            spriteBatch.draw(
                                    pickup,
                                    sx + 2, windowHeight - sy - 2,
                                    2.toFloat() / 2, 2.toFloat() / 2,
                                    2.toFloat(), 2.toFloat(),
                                    iconSize, iconSize,
                                    dir * -1, 0, 0, 128, 128, true, false
                            )
                            if(numPlayers > 0) nameFontRed.draw(spriteBatch, "$numPlayers", sx + 20, windowHeight - sy + 20)
                        } else {
                            spriteBatch.draw(
                                    pickup_b,
                                    sx + 2, windowHeight - sy - 2,
                                    2.toFloat() / 2, 2.toFloat() / 2,
                                    2.toFloat(), 2.toFloat(),
                                    iconScale / 2, iconScale / 2,
                                    dir * -1, 0, 0, 128, 128, true, false
                            )
                        }
                    }
                }
                SixSeatCar -> actorInfos?.forEach {
                    if (toggleVehicles != 1) {
                        val (actor, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()
                        if (toggleVNames != 1) compaseFont.draw(spriteBatch, "VAN", sx + 15, windowHeight - sy - 2)
                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        var iconSize = iconScale/2
                        when {
                            playersize*4 > iconScale/2 -> iconSize = (playersize*4).toFloat()
                            else -> iconSize = (iconScale/2).toFloat()
                        }
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 80) {
                            val numPlayers = actor.attachChildren?.size
                            spriteBatch.draw(
                                    van,
                                    sx + 2, windowHeight - sy - 2,
                                    2.toFloat() / 2, 2.toFloat() / 2,
                                    2.toFloat(), 2.toFloat(),
                                    iconSize, iconSize,
                                    dir * -1, 0, 0, 128, 128, true, false
                            )
                            if(numPlayers > 0) nameFontRed.draw(spriteBatch, "$numPlayers", sx + 20, windowHeight - sy + 20)
                        } else {
                            spriteBatch.draw(
                                    van_b,
                                    sx + 2, windowHeight - sy - 2,
                                    2.toFloat() / 2, 2.toFloat() / 2,
                                    2.toFloat(), 2.toFloat(),
                                    iconScale / 2, iconScale / 2,
                                    dir * -1, 0, 0, 128, 128, true, false
                            )
                        }
                    }
                }
                Player -> actorInfos?.forEach {
                    for ((_, _) in typeLocation) {
                        val (actor, x, y, dir) = it
						actor!!
                        val (sx, sy) = Vector2(x, y).mapToWindow()
						val selfStateGUID = actorWithPlayerState[selfID] ?: return@forEach
						val playerStateGUID = actorWithPlayerState[actor.netGUID] ?: return@forEach
						var playerStateGUIDx = NetworkGUID(playerStateGUID.toString().drop(18).dropLast(1).toInt() + 2)
							val name = playerNames[playerStateGUID] ?: return@forEach
							
						
						if (teamNumbers[playerStateGUID] == teamNumbers[selfStateGUID])
						{
						when (teamNumberss[playerStateGUIDx]){
							0 -> espFont.draw(spriteBatch, "$name [YELLOW]\n", 145f, windowHeight - 635f) 
							1 -> espFont.draw(spriteBatch, "$name [ORANGE]\n", 145f, windowHeight - 655f) 
							2 -> espFont.draw(spriteBatch, "$name [BLUE]\n", 145f, windowHeight - 675f) 							
							3 -> espFont.draw(spriteBatch, "$name [GREEN]\n", 145f, windowHeight - 695f) 
							4 -> espFont.draw(spriteBatch, "$name [5]\n", 145f, windowHeight - 725f) 
							5 -> espFont.draw(spriteBatch, "$name [6]\n", 145f, windowHeight - 745f) 
							6 -> espFont.draw(spriteBatch, "$name [7]\n", 145f, windowHeight - 765f) 
							}
						}
                        if (isTeammate(actor)) {
                            if (toggleView != -1) {
                                spriteBatch.draw(
                                        teamplayersight,
                                        sx + 1, windowHeight - sy - 2,
                                        2.toFloat() / 2,
                                        2.toFloat() / 2,
                                        12.toFloat(), 2.toFloat(),
                                        20f, 6f,
                                        dir * -1, 0, 0, 800, 64, true, false)
                            }

							val playerStateGUID = actorWithPlayerState[actor.netGUID] ?: return@forEach
							val name = playerNames[playerStateGUID] ?: return@forEach
							var playerStateGUIDx = NetworkGUID(playerStateGUID.toString().drop(18).dropLast(1).toInt() + 2)
							/*when (teamNumberss[playerStateGUIDx]){
							0 -> espFont.draw(spriteBatch, "$name [YELLOW]\n", 145f, windowHeight - 635f) 
							1 -> espFont.draw(spriteBatch, "$name [ORANGE]\n", 145f, windowHeight - 655f) 
							2 -> espFont.draw(spriteBatch, "$name [BLUE]\n", 145f, windowHeight - 675f) 							
							3 -> espFont.draw(spriteBatch, "$name [GREEN]\n", 145f, windowHeight - 695f) 
							4 -> espFont.draw(spriteBatch, "$name [5]\n", 145f, windowHeight - 725f) 
							5 -> espFont.draw(spriteBatch, "$name [6]\n", 145f, windowHeight - 745f) 
							6 -> espFont.draw(spriteBatch, "$name [7]\n", 145f, windowHeight - 765f) 
							}*/
							
							if (teamNumbers[playerStateGUIDx] != null)
			{
			}
                            spriteBatch.draw(
                                    teamplayer,
                                    sx, windowHeight - sy - 2, 4.toFloat() / 2,
                                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 4f, 3f,
                                    dir * -1, 0, 0, 64, 64, true, false)
                        } else {
                            if (toggleView != -1) {
                                spriteBatch.draw(
                                        arrowsight,
                                        sx + 1, windowHeight - sy - 2,
                                        2.toFloat() / 2,
                                        2.toFloat() / 2,
                                        12.toFloat(), 2.toFloat(),
                                        10f, 6f,
                                        dir * -1, 0, 0, 800, 64, true, false)
                            }
                            spriteBatch.draw(
                                    arrow,
                                    sx, windowHeight - sy - 2, 4.toFloat() / 2,
                                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 4f, 3f,
                                    dir * -1, 0, 0, 64, 64, true, false)
                        }
                    }
                }
                Parachute -> actorInfos?.forEach {
                    for ((_, _) in typeLocation) {
                        val (actor, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()
                        val children = actor!!.attachChildren
                        if (children.isNotEmpty() && (actorWithPlayerState[ArrayList(children.keys)[0]]) != null) {
                            val list = ArrayList(children.keys)
                            val playerStateGUID = actorWithPlayerState[list[0]] ?: continue
                            val name = playerNames[playerStateGUID]
                            var myPlayerStateGUID = actorWithPlayerState[selfID]
                            var myName = playerNames[myPlayerStateGUID]
                            if (name == myName) {
                                spriteBatch.draw(
                                        parachute_self,
                                        sx - 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                        4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 2 * playersize, 2 * playersize,
                                        dir * -1, 0, 0, 128, 128, true, false)
                            } else if (name in team) {
                                spriteBatch.draw(
                                        parachute_team,
                                        sx - 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                        4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 2 * playersize, 2 * playersize,
                                        dir * -1, 0, 0, 128, 128, true, false)
										nameFont.draw(spriteBatch, "$name", sx + 10, windowHeight - sy + 10)
                            }
                        } else {
                            spriteBatch.draw(
                                    parachute,
                                    sx - 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 2 * playersize, 2 * playersize,
                                    dir * -1, 0, 0, 128, 128, true, false)
                        }
                    }
                }
                Plane -> actorInfos?.forEach {
                    for ((_, _) in typeLocation) {
                        val (_, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()
                        spriteBatch.draw(
                                plane,
                                sx - 3, windowHeight - sy - 3, 4.toFloat() / 2,
                                4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 300f, 15f,
                                dir * -1, 0, 0, 1829, 64, true, false)
                    }
                }
                Grenade -> actorInfos?.forEach {
                    val (_, x, y, dir) = it
                    val (sx, sy) = Vector2(x, y).mapToWindow()
                    spriteBatch.draw(
                            grenade,
                            sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                            4.toFloat() / 2, 4.toFloat(), 4.toFloat(), playersize, playersize,
                            dir * -1, 0, 0, 16, 16, true, false)
                }

                else -> {
                    //nothing
                }
            }
        }
    }

    fun drawPlayerNames(players: MutableList<renderInfo>?, selfX: Float, selfY: Float) {
        players?.forEach {
            val (actor, x, y, _) = it
            actor!!
            val dir = Vector2(x - selfX, y - selfY)
            val distance = (dir.len() / 100).toInt()
            val angle = ((dir.angle() + 90) % 360).toInt()
            val (sx, sy) = mapToWindow(x, y)
            val playerStateGUID = actorWithPlayerState[actor.netGUID] ?: return@forEach
            val name = playerNames[playerStateGUID] ?: return@forEach
            //val name = playerNames[playerStateGUID] ?: return@forEach
			//   val teamNumber = teamNumbers[playerStateGUID] ?: 0
            val numKills = playerNumKills[playerStateGUID] ?: 0
            val health = actorHealth[actor.netGUID] ?: 100f
			//val SpectatedCounts = playerSpectatedCounts[actor.netGUID] ?: 0
            val equippedWeapons = actorHasWeapons[actor.netGUID]
            val df = DecimalFormat("###.#")		
			var playerStateGUIDx = NetworkGUID(playerStateGUID.toString().drop(18).dropLast(1).toInt() + 2)
			//val number = teamMemberNumber[playerStateGUIDx]
			var myPlayerStateGUID = actorWithPlayerState[selfID]
            val myName = playerNames[actor.netGUID]
			var weapon: MutableList<String> = mutableListOf<String>()
            val offsetDist = 3
            if (equippedWeapons != null) {
                for (w in equippedWeapons) {
                    val a = weapons[w] ?: continue
                    val result = a.archetype.pathName.split("_")
                    weapon.add("${result[2].substring(4)}")
                }
            }
			
            if (filterNames != 1) {
			if (teamNumbers[playerStateGUIDx] != null)
			{
			 val number = teamNumbers[playerStateGUIDx]
			    val fontText = "${distance}m | $angle° | [$number]"
                var wep_xoff = getPositionOffset(nameFont,fontText)
                nameFont.draw(spriteBatch,
                        "$fontText", sx - wep_xoff.toFloat(), windowHeight - sy +30)
             when {
			 number == null -> {
			 espFont.draw(spriteBatch, "$myName [YELLOW[NULL]]\n", 45f, windowHeight - 435f) 
			 spriteBatch.draw(pnum3, 30f, windowHeight - 650, 15f, 15f)
			 }
			 number == 0 -> {
			 espFont.draw(spriteBatch, "$myName [YELLOW]\n", 45f, windowHeight - 535f) 
			 spriteBatch.draw(pnum3, 30f, windowHeight - 650, 15f, 15f)
			 }
			 number == 1 -> {
			 espFont.draw(spriteBatch, "$name [ORANGE]\n", 45f, windowHeight - 600f) 
			 spriteBatch.draw(pnum1, 30f, windowHeight - 615f, 15f, 15f)
			 }
			 number == 2 -> {
			 espFont.draw(spriteBatch, "$name [BLUE]\n", 45f, windowHeight - 718f) 
			 spriteBatch.draw(pnum2, 30f, windowHeight - 633f, 15f, 15f)
			 }
			 number == 3 -> {
			 espFont.draw(spriteBatch, "$name [GREEN]\n", 45f, windowHeight - 835f) 
			 spriteBatch.draw(pnum3, 30f, windowHeight - 650, 15f, 15f)
			 }number == 4 -> {
			 espFont.draw(spriteBatch, "$name [4]\n", 45f, windowHeight - 635f) 
			 spriteBatch.draw(pnum3, 30f, windowHeight - 650, 15f, 15f)
			 }
			 else ->{
			 espFont.draw(spriteBatch, "$myName [8]\n", 45f, windowHeight - 635f) 
			 spriteBatch.draw(pnum3, 30f, windowHeight - 660, 15f, 15f)
			 }
			}
			 
			}else{
			val fontText = "${distance}m | $angle°"
			//First Let's Draw DISTANCE
                //val fontText = "${distance}m | $angle° | [$number]"
                var wep_xoff = getPositionOffset(nameFont,fontText)
                nameFont.draw(spriteBatch,
                        "$fontText", sx - wep_xoff.toFloat(), windowHeight - sy +30)
                
			}
			//Name
                val fontText1 = "${name}"
                var  name_xoff = getPositionOffset(nameFont,fontText1)
                nameFont.draw(spriteBatch,
                        "$fontText1", sx -  name_xoff.toFloat(), windowHeight - sy +20)
                //HEALTH / STATUS
                when {
                    actorDowned[actor.netGUID] == true -> {
                        val fontText = "[DOWNED]"
                        val health_xoff = getPositionOffset(nameFontRed,fontText)
                        //var health_xoff = ("[DOWNED]".length) * offsetDist
                        nameFontRed.draw(spriteBatch, fontText, sx - health_xoff.toFloat(), windowHeight - sy + -15)
                    }
                    actorBeingRevived[actor.netGUID] == true -> {
                        val fontText = "[REVIVE]"
                        val health_xoff = getPositionOffset(nameFontRed,fontText)
                        //var health_xoff = ("[DOWNED]".length) * offsetDist
                        nameFontRed.draw(spriteBatch, fontText, sx - health_xoff.toFloat(), windowHeight - sy + -15)
                    }
					/*actorAims[actor.netGUID] == true -> {
					val fontText = "[AIM]"
                        val health_xoff = getPositionOffset(nameFontRed,fontText)
                        nameFontRed.draw(spriteBatch, fontText, sx - health_xoff.toFloat(), windowHeight - sy + -25) 
				
					}*/
                    /*
                    (health > 75) -> {
                        val fontText = "[${df.format(health)}]"
                        val health_xoff = getPositionOffset(nameFontGreen,fontText)
                        //var health_xoff = ("[DOWNED]".length) * offsetDist
                        nameFontGreen.draw(spriteBatch, fontText, sx - health_xoff.toFloat(), windowHeight - sy -10)
                    }
                    (health > 15) -> {
                        val fontText = "[${df.format(health)}]"
                        val health_xoff = getPositionOffset(nameFontOrange,fontText)
                        //var health_xoff = ("[DOWNED]".length) * offsetDist
                        nameFontOrange.draw(spriteBatch, fontText, sx - health_xoff.toFloat(), windowHeight - sy -10)
                    }*/
                    else -> {
                        /*
                        val fontText = "[${df.format(health)}]"
                        val health_xoff = getPositionOffset(nameFontRed,fontText)
                        //var health_xoff = ("[DOWNED]".length) * offsetDist
                        nameFontRed.draw(spriteBatch, fontText, sx - health_xoff.toFloat(), windowHeight - sy -10)
                        */
                    }
                }				
				
                // WEAPONS
                weapon.forEachIndexed { index, element ->
                    when(element){
                        "AWM" ->{
                            val dist = (index + 2) * 12
                            val fontText = "${element}"
                            var wep_xoff = getPositionOffset(nameFont,fontText)
                            nameFontRed.draw(spriteBatch,
                                    "${element}", sx - wep_xoff.toFloat(), windowHeight - sy - dist)
                        }
                        "M24" ->{
                            val dist = (index + 2) * 12
                            val fontText = "${element}"
                            var wep_xoff = getPositionOffset(nameFont,fontText)
                            nameFontRed.draw(spriteBatch,
                                    "${element}", sx - wep_xoff.toFloat(), windowHeight - sy - dist)
                        }
                        "Mk14" ->{
                            val dist = (index + 2) * 12
                            val fontText = "${element}"
                            var wep_xoff = getPositionOffset(nameFont,fontText)
                            nameFontRed.draw(spriteBatch,
                                    "${element}", sx - wep_xoff.toFloat(), windowHeight - sy - dist)
                        }
						"Groza" ->{
                            val dist = (index + 2) * 12
                            val fontText = "${element}"
                            var wep_xoff = getPositionOffset(nameFont,fontText)
                            nameFontRed.draw(spriteBatch,
                                    "${element}", sx - wep_xoff.toFloat(), windowHeight - sy - dist)
                        }
                        "M249" ->{
                            val dist = (index + 2) * 12
                            val fontText = "${element}"
                            var wep_xoff = getPositionOffset(nameFont,fontText)
                            nameFontRed.draw(spriteBatch,
                                    "${element}", sx - wep_xoff.toFloat(), windowHeight - sy - dist)
                        }
                        "Kar98k" ->{
                            val dist = (index + 2) * 12
                            val fontText = "${element}"
                            var wep_xoff = getPositionOffset(nameFont,fontText)
                            nameFontOrange.draw(spriteBatch,
                                    "${element}", sx - wep_xoff.toFloat(), windowHeight - sy - dist)
                        }
                        "SKS" ->{
                            val dist = (index + 2) * 12
                            val fontText = "${element}"
                            var wep_xoff = getPositionOffset(nameFont,fontText)
                            nameFontOrange.draw(spriteBatch,
                                    "${element}", sx - wep_xoff.toFloat(), windowHeight - sy - dist)
                        }
						"FlareGun" ->{
                            val dist = (index + 2) * 12
                            val fontText = "${element}"
                            var wep_xoff = getPositionOffset(nameFont,fontText)
                            nameFontOrange.draw(spriteBatch,
                                    "${element}", sx - wep_xoff.toFloat(), windowHeight - sy - dist)
                        }
                        else ->{
                            val dist = (index + 2) * 12
                            val fontText = "${element}"
                            var wep_xoff = getPositionOffset(nameFont,fontText)
                            nameFont.draw(spriteBatch,
                                    "${element}", sx - wep_xoff.toFloat(), windowHeight - sy - dist)
                        }
                    }
                }
            }
        }
    }

    fun ShapeRenderer.drawAllPlayerHealth(pColor : Color? , actorInfo : renderInfo)
    {
        val (actor , x , y , dir) = actorInfo
        val health = actorHealth[actor?.netGUID] ?: 100f
        val width = (camera.zoom * 50) * 600
        val height = (camera.zoom * 50) * 100
        val he = y + (camera.zoom * 50) * 250
        val healthWidth = (health / 100.0 * width).toFloat()

        if(filterNames != 1) {
            color = Color(0f, 0f, 0f, 0.5f)
            rectLine((x - width / 2), he, (x - width / 2) + width, he, height)

            color = when
            {
                health > 80f -> Color(0f, 1f, 0f, 0.5f)
                health > 33f -> Color(1f, 0.647f, 0f, 0.5f)
                else         -> Color(1f, 0f, 0f, 0.5f)
            }
            rectLine(x - width / 2, he, x - width / 2 + healthWidth, he, height)
        }
    }

    private fun getPositionOffset(bitmapFont: BitmapFont, value: String): Float {
        val glyphLayout = GlyphLayout()
        glyphLayout.setText(bitmapFont, value)
        return glyphLayout.width / 2
    }
	
	private fun getTextHeight(bitmapFont: BitmapFont, value: String): Float {
		val glyphLayout = GlyphLayout()
		glyphLayout.setText(bitmapFont, value)
		return glyphLayout.height
	}

    private var lastPlayTime = System.currentTimeMillis()
    private fun safeZoneHint() {
        if (PoisonGasWarningPosition.len() > 0) {
            val dir = PoisonGasWarningPosition.cpy().sub(self2Coords)
            val road = dir.len() - PoisonGasWarningRadius
            if (road > 0) {
                val runningTime = (road / runSpeed).toInt()
                val (x, y) = dir.nor().scl(road).add(self2Coords).mapToWindow()
                littleFont.draw(spriteBatch, "$runningTime", x, windowHeight - y)
                val remainingTime = (TotalWarningDuration - ElapsedWarningDuration).toInt()
                if (remainingTime == 60 && runningTime > remainingTime) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastPlayTime > 10000) {
                        lastPlayTime = currentTime
                        //alarmSound.play()
                    }
                }
            }
        }
    }

    private inline fun draw(type: ShapeType, draw: ShapeRenderer.() -> Unit) {
        shapeRenderer.apply {
            begin(type)
            draw()
            end()
        }
    }

    private inline fun paint(matrix: Matrix4, paint: SpriteBatch.() -> Unit) {
        spriteBatch.apply {
            projectionMatrix = matrix
            begin()
            paint()
            end()
        }
    }

    private fun ShapeRenderer.circle(loc: Vector2, radius: Float, segments: Int) {
        circle(loc.x, loc.y, radius, segments)
    }


    private fun isTeammate(actor: Actor?): Boolean {
        if (actor != null) {
            val playerStateGUID = actorWithPlayerState[actor.netGUID]
            if (playerStateGUID != null) {
                val name = playerNames[playerStateGUID] ?: return false
                if (name in team)
                    return true
            }
        }
        return false
    }

    override fun resize(width: Int, height: Int) {
        windowWidth = width.toFloat()
        windowHeight = height.toFloat()
        camera.setToOrtho(true, windowWidth * windowToMapUnit, windowHeight * windowToMapUnit)
        itemCamera.setToOrtho(false, windowWidth, windowHeight)
        fontCamera.setToOrtho(false, windowWidth, windowHeight)
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun dispose() {
        deregister(this)
        alarmSound.dispose()
        nameFont.dispose()
        largeFont.dispose()
        littleFont.dispose()
        corpseboximage.dispose()
        airdropimage.dispose()
        vehicle.dispose()
        iconImages.iconSheet.dispose()
        compaseFont.dispose()
        compaseFontShadow.dispose()

        var cur = 0
        tileZooms.forEach {
            for (i in 1..tileRowCounts[cur]) {
                val y = if (i < 10) "0$i" else "$i"
                for (j in 1..tileRowCounts[cur]) {
                    val x = if (j < 10) "0$j" else "$j"
                    mapErangelTiles[it]!![y]!![x]!!.dispose()
                    mapMiramarTiles[it]!![y]!![x]!!.dispose()
                    mapTiles[it]!![y]!![x]!!.dispose()
                }
            }
            cur++
        }
        spriteBatch.dispose()
        shapeRenderer.dispose()
    }
}
