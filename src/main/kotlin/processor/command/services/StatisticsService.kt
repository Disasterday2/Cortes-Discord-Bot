package processor.command.services

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.JFreeChart
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator
import org.jfree.chart.labels.StandardPieSectionLabelGenerator
import org.jfree.chart.plot.PiePlot
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.renderer.category.LineAndShapeRenderer
import org.jfree.chart.ui.RectangleInsets
import org.jfree.chart.util.SortOrder
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.general.DefaultPieDataset
import org.litote.kmongo.coroutine.aggregate
import org.litote.kmongo.descending
import processor.models.Team
import processor.models.enums.DBCollection
import processor.models.statistics.DamageStatistic
import processor.models.statistics.TeamDamageStatistic
import processor.utilities.AccessManager
import processor.utilities.GCSeasonCalculator
import processor.utilities.MongoManager
import java.awt.BasicStroke
import java.awt.Color
import java.awt.geom.Ellipse2D
import java.io.File
import java.io.IOException
import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.Instant

class StatisticsService {

    private companion object {
        val discordColor: Color = Color(54, 57, 63, 255)
        val calculator = GCSeasonCalculator()
    }

    suspend fun getTeamDamageStatisticsWithLimit(team: Team, limit: Int): List<TeamDamageStatistic> {

        val orString = mutableListOf<String>()

        orString.add("{name: \"${team.name.toUpperCase()}\"}")

        if (team.oldNames != null) {
            for (name in team.oldNames!!) {
                orString.add("{name: \"${name.toUpperCase()}\"}")
            }
        }

        return MongoManager.getDatabase()
            .getCollection<TeamDamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName)
            .find(
                "{\$and: [ {\$or: [${orString.joinToString(",")}]}, {statType: \"${
                    "TeamDamageStatistic"
                }\"}]}"
            )
            .sort(descending(TeamDamageStatistic::createdAt))
            .limit(limit).toList().sortedByDescending { it.createdAt }


    }

    suspend fun getDamageStatisticWithLimit(user: String, limit: Int): List<DamageStatistic> {
        return MongoManager.getDatabase()
            .getCollection<DamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName)
            .find("{name: \"${user.toUpperCase()}\", statType: \"${"DamageStatistic"}\"}")
            .sort(descending(DamageStatistic::createdAt))
            .limit(limit).toList().sortedByDescending { it.createdAt }
    }

    /**
     * @return A String containing the status Message
     */
    suspend fun createTeamStatistic(team: Team, range: Pair<Instant, Instant>, isLegacy: Boolean): String {

        var orString = ""
        for (i in 0 until team.members.size) {
            orString += if (i + 1 == team.members.size) {
                "{name: \"${team.members[i]?.toUpperCase()}\"}"
            } else {
                "{name: \"${team.members[i]?.toUpperCase()}\"},"
            }

        }

        val weekBegin = range.first
        val weekEnd = range.second

        val andString =
            "{createdAt: {\$gte: ISODate(\"$weekBegin\")}}, {createdAt: {\$lte: ISODate(\"$weekEnd\")}}"

        val teamStatistic = MongoManager.getDatabase()
            .getCollection<DamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName)
            .aggregate<TeamDamageStatistic>(
                "[" +
                        "{\$match: " +
                        "{\$and: [{\$or: " +
                        "[$orString]" +
                        "}," + //or-close
                        "$andString]}" + //and-close
                        "}," + //match-close
                        "{\$group: " +
                        "{_id: \"${team.name}\", damage: {\$sum: \"\$damage\"}, members: {\$addToSet: \"\$name\"}}" +
                        "}" +
                        "{\$project: " +
                        "{_id: \"\$_id\", name: \"${team.name}\", damage: \"\$damage\", members: \"\$members\", statType: \"${"TeamDamageStatistic"}\", createdAt: ${if (isLegacy) "ISODate(\"$weekBegin\")" else "new Date()"}}" +
                        "}" +
                        "]"
            ).first()

        if (teamStatistic != null) {
            if (GCSeasonCalculator().teamEntryExists(teamStatistic.name, weekBegin, weekEnd)) {
                return "There already exists an entry of this team for this week!"
            } else {
                coroutineScope {
                    launch {
                        MongoManager.getDatabase()
                            .getCollection<TeamDamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName)
                            .insertOne(teamStatistic)
                    }
                }
            }
        } else {
            return "No damage statistics existent! Please insert damage statistics of individual users first!"
        }

        return "Successfully created team statistic!"
    }

    suspend fun sendGraphInTextChannel(damageStatistics: List<DamageStatistic>, channel: TextChannel): Unit {
        val file = this.createLineGraphFromDamageStatistics(damageStatistics)
        channel.sendFile(file).queue { file.delete() }
    }

    private suspend fun createLineGraphFromDamageStatistics(damageStatistics: List<DamageStatistic>): File {
        val dataSet = DefaultCategoryDataset()

        for (i in 1..damageStatistics.size) {
            dataSet.addValue(
                damageStatistics[damageStatistics.size - i].damage / 1_000_000_000_000_000.00,
                damageStatistics[damageStatistics.size - i].name,
                "${calculator.getSeasonFromTime(damageStatistics[damageStatistics.size - i].createdAt).number}-${
                    calculator.calculateWeeksFromTime(
                        damageStatistics[damageStatistics.size - i].createdAt
                    )
                }"
            )
        }

        val chart: JFreeChart = ChartFactory.createLineChart(
            damageStatistics[0].name,
            "Season-Week",
            "Damage in Q",
            dataSet,
            PlotOrientation.VERTICAL,
            true,
            false,
            false
        )

        this.setLineChartProperties(chart)

        val file = File("test.jpg")
        val width = if (damageStatistics.size <= 10) {
            800
        } else {
            800 + 500 * (damageStatistics.size / 10) - 200
        }
        val height = 600

        try {

            runBlocking {
                ChartUtils.saveChartAsPNG(file, chart, width, height)
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }

    private fun setLineChartProperties(chart: JFreeChart): Unit {
        chart.backgroundPaint = discordColor
        chart.title.paint = Color.WHITE
        chart.legend.itemPaint = Color.WHITE
        chart.legend.backgroundPaint = discordColor

        val plot = chart.categoryPlot

        plot.backgroundPaint = discordColor
        plot.outlinePaint = discordColor
        plot.rangeGridlinePaint = Color(108, 122, 137, 150)

        val domainAxis = plot.domainAxis
        val rangeAxis = plot.rangeAxis

        domainAxis.labelPaint = Color.WHITE
        domainAxis.axisLinePaint = Color.WHITE
        domainAxis.tickLabelPaint = Color.WHITE

        rangeAxis.labelPaint = Color.WHITE
        rangeAxis.axisLinePaint = Color.WHITE
        rangeAxis.tickLabelPaint = Color.WHITE
        rangeAxis.upperMargin = 0.15


        val renderer = LineAndShapeRenderer()

        renderer.setSeriesShape(0, Ellipse2D.Double(-5.0, -5.0, 10.0, 10.0));

        renderer.setSeriesPaint(0, Color.decode("#ED7D31"))
        renderer.setSeriesStroke(0, BasicStroke(1.5f))
        renderer.defaultItemLabelPaint = Color.WHITE
        renderer.setDefaultItemLabelsVisible(true, false)

        val itemLabelGenerator = StandardCategoryItemLabelGenerator("{2}", DecimalFormat("0.0"))
        renderer.defaultItemLabelGenerator = itemLabelGenerator
        renderer.itemLabelAnchorOffset = 7.5

        val font = renderer.defaultItemLabelFont
        val newFont = font.deriveFont(13.0f)
        renderer.defaultItemLabelFont = newFont


        plot.renderer = renderer

    }

    suspend fun sendPieChartInTextChannel(
        damageStatistics: List<DamageStatistic>,
        channel: TextChannel
    ): Unit {
        val file = this.createPieChartFromDamageStatistics(damageStatistics)
        channel.sendFile(file).queue { file.delete() }
    }

    private suspend fun createPieChartFromDamageStatistics(damageStatistics: List<DamageStatistic>): File {
        val dataSet = DefaultPieDataset<String>()

        for (i in 1..damageStatistics.size) {
            dataSet.setValue(
                damageStatistics[damageStatistics.size - i].name,
                damageStatistics[damageStatistics.size - i].damage / 1_000_000_000_000_000.00
            )
        }

        dataSet.sortByValues(SortOrder.DESCENDING)

        val chart: JFreeChart = ChartFactory.createPieChart(
            "Season ${calculator.getSeasonFromTime(damageStatistics[0].createdAt).number} Week ${
                calculator.calculateWeeksFromTime(
                    damageStatistics[0].createdAt
                )
            }",
            dataSet
        )

        this.setPieChartProperties(chart)

        val file = File("test.jpg")
        try {
            runBlocking {
                ChartUtils.saveChartAsPNG(file, chart, 800, 600)
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }

    private suspend fun setPieChartProperties(chart: JFreeChart): Unit {
        chart.backgroundPaint = discordColor
        chart.title.paint = Color.WHITE
        chart.legend.itemPaint = Color.WHITE
        chart.legend.backgroundPaint = discordColor

        val plot: PiePlot<String> = chart.plot as PiePlot<String> // Only way to get the correct Plot type
        plot.simpleLabels = true // Makes the labels go on top of the parts

        plot.backgroundPaint = discordColor
        plot.outlinePaint = discordColor

        // Invisible Colors
        plot.labelOutlinePaint = Color(0, 0, 0, 0)
        plot.shadowPaint = Color(0, 0, 0, 0)
        plot.labelShadowPaint = Color(0, 0, 0, 0)

        plot.labelBackgroundPaint = discordColor
        plot.labelBackgroundPaint = discordColor
        plot.labelPaint = Color.WHITE

        plot.labelPadding = RectangleInsets(2.0, 4.0, 2.0, 4.0)

        val font = plot.labelFont
        val newFont = font.deriveFont(15.0f)
        plot.labelFont = newFont

        plot.labelLinksVisible = false
        // plot.labelLinkStroke = BasicStroke(0.75f)
        // plot.labelLinkStyle = PieLabelLinkStyle.STANDARD

        val labelGenerator =
            StandardPieSectionLabelGenerator("{2}", NumberFormat.getNumberInstance(), DecimalFormat("0.0%"))
        plot.labelGenerator = labelGenerator
        plot.defaultSectionOutlinePaint = Color.WHITE
        plot.autoPopulateSectionOutlinePaint = false
        plot.defaultSectionOutlineStroke = BasicStroke(1.5f)
        plot.legendItemShape = Ellipse2D.Double(-5.0, -5.0, 15.0, 15.0)

        //plot.setExplodePercent("FUCKING_STARS", 0.30)
        //plot.setExplodePercent("MERIDA", 0.30)

        //Set Colors for teams
        plot.setSectionPaint("HUSKIES", Color.decode("#999999"))
        plot.setSectionPaint("KILLSHOT", Color.decode("#FFC7C7"))
        plot.setSectionPaint("MERIDA", Color.decode("#62461B"))
        plot.setSectionPaint("CHASEINGSHEEPS", Color.decode("#ED7D31"))
        plot.setSectionPaint("GIGACHAD_GANYUS", Color.decode("#F44336"))
        plot.setSectionPaint("ACE", Color.decode("#B2FDFF"))
        plot.setSectionPaint("PEPES_AND_NUGGETS", Color.decode("#6AA84F"))
        plot.setSectionPaint("PUSSYCAT_BALLS", Color.decode("#9043E3"))
        plot.setSectionPaint("WEEBTEAM", Color.decode("#FBE407"))
        plot.setSectionPaint("BIGBRAIN", Color.decode("#FF7676"))
        plot.setSectionPaint("RANDOM_ONE", Color.decode("#C7A1F1"))
        plot.setSectionPaint("AKATSUKI", Color.decode("#539ED6"))
        plot.setSectionPaint("ODD_OTTERS", Color.decode("#5B5B5B"))
        plot.setSectionPaint("FANCY_FERRETS", Color.decode("#A79603"))
        plot.setSectionPaint("PERFECT_CHAOS", Color.decode("#44D638"))
        plot.setSectionPaint("FUCKING_STARS", Color.decode("#5555FF"))
        plot.setSectionPaint("FLAREON", Color.decode("#F44336"))
        plot.setSectionPaint("SYLVEON", Color.decode("#6AA84F"))
        plot.setSectionPaint("ESPEON", Color.decode("#9043E3"))
        plot.setSectionPaint("JOLTEON", Color.decode("#FF7676"))
        plot.setSectionPaint("VAPOREON", Color.decode("#C7A1F1"))
        plot.setSectionPaint("GLACION", Color.decode("#5B5B5B"))


        plot.simpleLabelOffset = RectangleInsets(-30.0, -30.0, -30.0, -30.0)
        plot.startAngle = 180.00
        

    }

    @Throws(NumberFormatException::class)
    public fun getLimit(command: List<String>, guild: Guild, sender: User): Int {
        var limit = 10
        if (AccessManager(guild, sender).isManager() && command[command.size - 1].matches(Regex("\\d*"))) {
            try {
                limit = Integer.parseInt(command[command.size - 1])
            } catch (e: NumberFormatException) {
                //channelWriter.writeChannel("Limit was not a number!")
                throw NumberFormatException("Limit was not a number")
            }
        }
        return limit
    }
}