/*
 * Copyright (c) 2021 Dmitry Barashev, BarD Software s.r.o.
 *
 * This file is part of GanttProject, an open-source project management tool.
 *
 * GanttProject is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * GanttProject is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GanttProject.  If not, see <http://www.gnu.org/licenses/>.
 */

package biz.ganttproject.print

import biz.ganttproject.app.*
import biz.ganttproject.lib.DateRangePicker
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.stage.FileChooser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import net.sourceforge.ganttproject.GPLogger
import net.sourceforge.ganttproject.IGanttProject
import net.sourceforge.ganttproject.action.GPAction
import net.sourceforge.ganttproject.chart.Chart
import net.sourceforge.ganttproject.gui.UIFacade
import net.sourceforge.ganttproject.util.FileUtil
import org.osgi.service.prefs.Preferences
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors
import javax.print.attribute.standard.MediaSize
import kotlin.reflect.KClass
import kotlin.reflect.full.staticProperties
import javafx.print.Paper as FxPaper


/**
 * @author dbarashev@bardsoftware.com
 */
fun showPrintDialog(activeChart: Chart, preferences: Preferences) {
  val i18n = RootLocalizer
  Previews.initChart(activeChart, preferences)
  Previews.setMediaSize(Previews.mediaSizeKey)
  dialog { dlg ->
    dlg.addStyleClass("dlg", "dlg-print-preview")
    dlg.addStyleSheet(
      "/biz/ganttproject/app/Dialog.css",
      "/biz/ganttproject/print/Print.css"
    )
    dlg.setHeader(
      HBox().also { hbox ->
        hbox.alignment = Pos.CENTER_LEFT
        hbox.styleClass.add("header")
        hbox.children.addAll(
          // -- Page format
          Label(i18n.formatText("choosePaperFormat")).also {
            HBox.setMargin(it, Insets(0.0, 5.0, 0.0, 15.0))
          },
          ComboBox(FXCollections.observableList(
            //Previews.papers.keys.toList()
            Previews.mediaSizes.keys.toList()
          )).also { comboBox ->
            comboBox.setOnAction {
              Previews.setMediaSize(comboBox.selectionModel.selectedItem)
            }
            comboBox.selectionModel.select(Previews.mediaSizeKey)
          },

          // -- Page orientation
          Label(i18n.formatText("option.export.itext.landscape.label")).also {
            HBox.setMargin(it, Insets(0.0, 5.0, 0.0, 15.0))
          },
          ComboBox(FXCollections.observableList(
            Orientation.values().map { i18n.formatText(it.name.lowercase()) }.toList()
          )).also { comboBox ->
            comboBox.setOnAction {
              Previews.orientation = Orientation.values()[comboBox.selectionModel.selectedIndex]
            }
            comboBox.selectionModel.select(i18n.formatText(Previews.orientation.name.lowercase()))
          },

          // -- Date range
          Label(i18n.formatText("print.preview.dateRange")).also {
            HBox.setMargin(it, Insets(0.0, 5.0, 0.0, 15.0))
          },
          DateRangePicker(activeChart).let {
            it.selectedRange.addListener { _, _, newValue ->
              Previews.onDateRangeChange(newValue.startDate, newValue.endDate)
            }
            it.button.styleClass.addAll("btn-regular")
            it.component
          }
        )
      }
    )

    val contentPane = BorderPane().also {
      it.styleClass.add("content-pane")
      it.center = ScrollPane(Pane(Previews.gridPane).also {it.styleClass.add("all-pages")})
    }
    dlg.setContent(contentPane)
    dlg.setButtonPaneNode(
      HBox().also { hbox ->
        hbox.alignment = Pos.CENTER_LEFT
        hbox.children.addAll(
          // -- Preview zoom

          Label(i18n.formatText("print.preview.scale")).also {
            HBox.setMargin(it, Insets(0.0, 5.0, 0.0, 15.0))
          },
          //FontAwesomeIconView(FontAwesomeIcon.BAR_CHART),
          Slider(0.0, 10.0, 0.0).also { slider ->
            //slider.isShowTickMarks = true
            slider.majorTickUnit = 1.0
            slider.blockIncrement = 1.0
            slider.isSnapToTicks = true
            slider.valueProperty().addListener { _, _, newValue ->
              Previews.zooming = newValue.toInt()
            }
            slider.value = 4.0
          },
//          FontAwesomeIconView(FontAwesomeIcon.BAR_CHART).also {
//            it.scaleX =2.0
//            it.scaleY =2.0
//          },
        )
      }
    )
    dlg.setupButton(ButtonType.YES) {
      it.text = i18n.formatText("print.export.button.exportAsZip").removeMnemonicsPlaceholder()
      it.styleClass.addAll("btn-attention", "secondary")
      it.onAction = EventHandler {
        exportPages(Previews.pages, activeChart.project, dlg)
      }
    }
    val btnApply = dlg.setupButton(ButtonType.APPLY) {
      it.text = i18n.formatText("project.print").removeMnemonicsPlaceholder()
      it.styleClass.addAll("btn-attention")
      it.onAction = EventHandler {
        //printPages(Previews.pages, Previews.paper)
        printPages(Previews.pages, Previews.mediaSize, Previews.orientation)
      }
    }
    dlg.onShown = {
      btnApply?.requestFocus()
    }
  }
}

private object Previews {
  private val zoomFactors = listOf(1.0, 1.25, 1.5, 2.0, 2.5, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0)
  private val readImageScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())

  lateinit var chart: Chart
  lateinit var dateRange: ClosedRange<Date>
  private lateinit var preferences: Preferences

  val mediaSizes: Map<String, MediaSize> = mutableMapOf<String, MediaSize>().let {
    it.putAll(mediaSizes(MediaSize.ISO::class))
    it.putAll(mediaSizes(MediaSize.JIS::class))
    it.putAll(mediaSizes(MediaSize.NA::class))
    it.toMap()
  }


  fun initChart(chart: Chart, preferences: Preferences) {
    this.chart = chart
    this.dateRange = chart.startDate.rangeTo(chart.endDate)
    this.preferences = preferences.node("/configuration/print")
    mediaSizes[this.preferences.get("page-size", MediaSize.ISO.A4.name)]?.let {
      mediaSize = it
    }
    orientation = Orientation.valueOf(this.preferences.get("page-orientation", Orientation.LANDSCAPE.name).uppercase())
  }

  val papers: Map<String, FxPaper> = mutableMapOf<String, FxPaper>().let {
    it.putAll(papers())
    it.toMap()
  }

  val gridPane = GridPane().also {
    it.vgap = 10.0
    it.hgap = 10.0
    it.padding = Insets(10.0, 10.0, 10.0, 10.0)
  }

  var mediaSize: MediaSize = MediaSize.ISO.A4
  set(value) {
    field = value
    updateTiles()
  }

  val mediaSizeKey: String get() =
    this.mediaSizes.filter { it.value == this.mediaSize }.firstNotNullOfOrNull { it.key } ?: MediaSize.ISO.A4.name

  var paper: FxPaper = FxPaper.A4
  set(value) {
    field = value
    mediaSize = MediaSize((field.width/72.0).toFloat(), (field.height/72.0).toFloat(), MediaSize.INCH)
  }
  fun setMediaSize(name: String) {
    mediaSizes[name]?.let { mediaSize = it }
    preferences.put("page-size", name)
    //papers[name]?.let { paper = it }
  }

  var zoomFactor = 1.0
  set(value) {
    field = value
    updatePreviews()
  }

  var orientation: Orientation = Orientation.LANDSCAPE
  set(value) {
    field = value
    preferences.put("page-orientation", value.name.lowercase())
    updateTiles()
  }

  var pages: List<PrintPage> = listOf()
  set(value) {
    field = value
    updatePreviews()
  }

  var zooming: Int = 4
  set(value) {
    field = value
    zoomFactor = zoomFactors[value]
  }

  fun onDateRangeChange(start: Date, end: Date) {
    dateRange = start.rangeTo(end)
    updateTiles()
  }

  private fun updateTiles() {
    val channel = Channel<PrintPage>()
    readImages(channel)
    createImages(chart, mediaSize, 144, orientation, dateRange, channel)
  }

  private fun updatePreviews() {
    Platform.runLater {
      gridPane.children.clear()
      pages.forEach { page ->
        val previewWidth = mediaSize.previewWidth() * zoomFactor * page.widthFraction
        val previewHeight = mediaSize.previewHeight() * zoomFactor * page.heightFraction
        Pane(ImageView(
          Image(
            page.imageFile.inputStream(),
            previewWidth, previewHeight,
            true,
            true
          )
        )).also {
          it.prefWidth = zoomFactor * (if (orientation == Orientation.LANDSCAPE) mediaSize.previewWidth() else mediaSize.previewHeight())
          it.prefHeight = zoomFactor * (if (orientation == Orientation.LANDSCAPE) mediaSize.previewHeight() else mediaSize.previewWidth())
          gridPane.add(StackPane(it).also {
              border -> border.styleClass.add("page")
          }, page.column, page.row)
        }
      }
    }
  }

  fun readImages(channel: Channel<PrintPage>) {
    readImageScope.launch {
      pages = channel.receiveAsFlow().toList()
    }
  }

  private fun MediaSize.previewWidth() = BASE_PREVIEW_WIDTH * this.getX(MediaSize.MM) / MediaSize.ISO.A4.getX(MediaSize.MM)
  private fun MediaSize.previewHeight() = BASE_PREVIEW_HEIGHT * this.getY(MediaSize.MM) / MediaSize.ISO.A4.getY(MediaSize.MM)
}

private fun exportPages(pages: List<PrintPage>, project: IGanttProject, dlg: DialogController) {
  val fileChooser = FileChooser()
  fileChooser.title = RootLocalizer.formatText("storageService.local.save.fileChooser.title")
  fileChooser.extensionFilters.add(
    FileChooser.ExtensionFilter(RootLocalizer.formatText("filterzip"), "zip")
  )
  fileChooser.initialFileName = FileUtil.replaceExtension(project.document.fileName, "zip")
  val file = fileChooser.showSaveDialog(null)
  if (file != null) {
    try {
      val zipBytes = FileUtil.zip(pages.mapIndexed { index, page ->
        "${project.document.fileName}_page$index.png" to { page.imageFile.inputStream() }
      }.toList())
      file.writeBytes(zipBytes)
    } catch (ex: IOException) {
      ourLogger.error("Failed to write an archive with the exported pages to {}", file.absolutePath, ex)
      dlg.showAlert(RootLocalizer.create("print.export.alert.title"), createAlertBody(ex.message ?: ""))
    }
  }
}

fun createPrintAction(uiFacade: UIFacade, preferences: Preferences): GPAction {
  return GPAction.create("project.print") {
    showPrintDialog(uiFacade.activeChart, preferences)
  }
}

private fun mediaSizes(clazz: KClass<*>): Map<String, MediaSize> =
  clazz.staticProperties.filter {
    it.get() is MediaSize
  }.associate {
    it.name to it.get() as MediaSize
  }

private fun papers(): Map<String, FxPaper> =
    FxPaper::class.staticProperties
      .filter { it.get() is FxPaper }
      .associate { it.name to it.get() as FxPaper }

private const val BASE_PREVIEW_WIDTH = 270.0
private const val BASE_PREVIEW_HEIGHT = 210.0
private val ourLogger = GPLogger.create("Print")
