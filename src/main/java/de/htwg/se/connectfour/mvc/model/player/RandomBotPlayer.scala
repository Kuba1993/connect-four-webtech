package de.htwg.se.connectfour.mvc.model.player

import de.htwg.se.connectfour.mvc.controller.logic.Validator
import de.htwg.se.connectfour.mvc.controller.Controller
import de.htwg.se.connectfour.mvc.model.Grid

import scala.util.Random

case class RandomBotPlayer(controller: Controller) extends Player {
  override val name: String = "Dumb bot"
  val r = Random
  val grid: Grid = controller.grid
  val validator = Validator(grid)

  override def playTurn(): Int = {
    var robotsColumn: Int = -1
    if (grid.isFull) return -1
    do {
      robotsColumn = r.nextInt(grid.columns)
    } while (!validator.isColumnValidAndNotFull(robotsColumn))
    robotsColumn
  }

  override def isReal: Boolean = false
}
