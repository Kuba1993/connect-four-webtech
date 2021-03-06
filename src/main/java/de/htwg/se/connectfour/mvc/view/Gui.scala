package de.htwg.se.connectfour.mvc.view

import java.awt.Color

import de.htwg.se.connectfour.mvc.controller.{Controller, Draw, FilledColumn, GridChanged, InvalidMove, PlayerGridChanged, PlayerWon}
import de.htwg.se.connectfour.mvc.model.player.{RandomBotPlayer, RealPlayer}
import de.htwg.se.connectfour.mvc.model.types.CellType

import scala.swing.event.{ButtonClicked, Key}
import scala.swing.{Action, BorderPanel, Button, CheckBox, Dialog, Dimension, Frame, GridPanel, Label, MainFrame, Menu, MenuBar, MenuItem, Swing, TextField}

case class Gui(controller: Controller, gamingPlayers: GamingPlayers) extends Frame {

  val columns: Int = controller.columns
  val rows: Int = controller.rows
  val statusLine = new TextField(controller.statusText, 20)
  val isOpponentRealBox = new CheckBox("Real player")

  val blocks: Array[Array[Label]] = {
    val blocks: Array[Array[Label]] = Array.ofDim[Label](columns, rows)
    for (i <- 0 until rows; j <- 0 until columns) {
      blocks(j)(i) = new Label {
        opaque = true
        horizontalAlignment
        border = Swing.LineBorder(Color.BLACK, 1)
      }
    }
    blocks
  }

  setupMainFrame()
  setupReactions
  listenTo(controller)
  listenTo(isOpponentRealBox)

  def setupMainFrame(): Unit = {
    val WIDTH = 700
    val HEIGHT = 500

    val mainFrame = new MainFrame()
    mainFrame.title = "Connect four game"
    mainFrame.preferredSize = new Dimension(WIDTH, HEIGHT)
    mainFrame.contents = new BorderPanel {
      add(createPanel(), BorderPanel.Position.Center)
      add(statusLine, BorderPanel.Position.South)
    }

    mainFrame.menuBar = createMenuBar()
    mainFrame.centerOnScreen()
    mainFrame.visible = true
  }

  def createPanel(): GridPanel = {
    new GridPanel(columns, rows + 1) {
      val buttons: Array[Button] = new Array[Button](rows)
      for (i <- 0 until rows) contents += Button(String.valueOf(i + 1))(buttonAction(i))
      for (j <- 0 until columns - 1; i <- 0 until rows) {
        contents += blocks(i)(j)
      }
    }
  }

  def createMenuBar(): MenuBar = {
    new MenuBar {
      contents += new Menu("Game") {
        mnemonic = Key.G
        contents += new MenuItem(Action("New") {
          startNewGame()
        })
        contents += new MenuItem(Action("Quit") {
          mnemonic = Key.Q
          quit()
        })
      }
      contents += new MenuItem(Action("Undo") {
        controller.undo()
      })
      contents += new MenuItem(Action("Redo") {
        controller.redo()
      })
      contents += isOpponentRealBox
    }
  }

  def setupReactions: reactions.type = {
    reactions += {
      case _: PlayerGridChanged => redraw()
      case _: GridChanged => redraw()
      case _: PlayerWon => showWon()
      case _: Draw => showDraw()
      case _: FilledColumn => Dialog.showMessage(message = "Please choose another one.", title = "Column is filled")
      case _: InvalidMove => redraw()
      case ButtonClicked(`isOpponentRealBox`) => startNewGame()
    }
  }

  def buttonAction(chosenColumn: Int): Unit = {
    gamingPlayers.applyTurn(chosenColumn)
    playBotIfGoing()
  }

  def playBotIfGoing(): Unit = {
    if (!gamingPlayers.currentPlayer.isReal) {
      val robotsColumn = gamingPlayers.currentPlayer.playTurn()
      gamingPlayers.applyTurn(robotsColumn)
    }
  }


  def redraw(): Unit = {
    for (i <- 0 until columns; j <- 0 until rows) redrawCell(i, j)
    statusLine.text = controller.statusText
  }

  def redrawCell(column: Int, row: Int): Unit = {
    controller.cell(column, row).cellType match {
      case CellType.FIRST =>
        blocks(column)(row).background = Color.red
      case CellType.SECOND =>
        blocks(column)(row).background = Color.blue
      case CellType.EMPTY =>
        blocks(column)(row).background = Color.white
    }
  }

  def showWon(): Unit = {
    val winner = "Player " + gamingPlayers.previousPlayer.name + " has won"
    val option = Dialog.showConfirmation(message = "Play a new game?", optionType = Dialog.Options.YesNo, title = winner)
    startNewOrQuit(option == Dialog.Result.Ok)
  }

  def showDraw(): Unit = {
    val option = Dialog.showConfirmation(message = "Nobody won.\nPlay a new game?", optionType = Dialog.Options.YesNo, title = "Draw")
    startNewOrQuit(option == Dialog.Result.Ok)
  }

  def startNewOrQuit(startNew: Boolean): Unit = if (startNew) startNewGame() else quit()

  def startNewGame(): Unit = {
    controller.createEmptyGrid(controller.columns, controller.rows)
    val secondPlayer = if (isOpponentRealBox.selected) RealPlayer("David") else RandomBotPlayer(controller)
    gamingPlayers.setSecondPlayer(secondPlayer)
  }

  def quit(): Unit = sys.exit(0)


}
