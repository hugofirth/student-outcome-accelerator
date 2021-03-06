/** soar
  *
  * Copyright (c) 2017 Hugo Firth
  * Email: <me@hugofirth.com/>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at:
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package uk.ac.ncl.la.soar.glance.web.client.component

import cats._
import cats.implicits._
import diode._
import diode.react._
import diode.data._
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.vdom.VdomElement
import uk.ac.ncl.la.soar.ModuleCode
import uk.ac.ncl.la.soar.data.StudentRecords

import scala.scalajs.js
import scala.collection.immutable.SortedMap

/**
  * Component for listing students in a data table
  */
object StudentsDataTable {

  case class Props(
    records: Seq[StudentRecords[SortedMap, ModuleCode, Double]],
    headings: Seq[(String, Option[String])],
    renderCell: (StudentRecords[SortedMap, ModuleCode, Double], String) => String,
    selectStudent: StudentRecords[SortedMap, ModuleCode, Double] => Callback
  )

  class Backend(bs: BackendScope[Props, Unit]) {

    private lazy val jQ = js.Dynamic.global.$
    private val tableSelector = "#training-table"

    def mounted = Callback { initDataTable() }
    def willUnmount = Callback { destroyDataTable() }


    def render(props: Props): VdomElement = {

      <.table(
        ^.className := "table table-striped table-bordered table-hover",
        ^.id := "training-table",
        <.thead(
          <.tr(
            props.headings.toList match {
              case hd :: tl =>
                (<.td(hd._1) :: tl.map { h =>
                  <.td(
                    ^.className := "long-heading",
                    <.span(
                      h._2.whenDefined(t => ^.title := t),
                      h._1
                    )
                  )
                }).toTagMod
              case Nil =>
                EmptyVdom
            }
          )
        ),
        <.tbody(
          TagMod.fromTraversableOnce(props.records.map(tableRow(props, _)))
        )
      )
    }

    private def tableRow(props: Props,
                         studentRecord: StudentRecords[SortedMap, ModuleCode, Double]) = {

      //Get the row columns for the given record
      val columns = props.headings.map { case (title, tip) => props.renderCell(studentRecord, title) }

      //Return the row, with a td for each column and also using the onclick listener
      <.tr(
        ^.onClick --> props.selectStudent(studentRecord),
        TagMod.fromTraversableOnce(columns.map(<.td(_)))
      )
    }

    private def initDataTable(re: Boolean = false) = {
      val table = jQ(tableSelector)
      println(s"${if(re) "Re-" else ""}Initialising datatable")
      table.DataTable(js.Dictionary("ordering" -> false))
      ()
    }

    private def destroyDataTable() = {
      val table = jQ(tableSelector)
      println("Destroying datatable")
      table.DataTable().destroy()
      ()
    }

  }

  def component = ScalaComponent.builder[Props]("StudentList")
    .renderBackend[Backend]
    .componentDidMount(scope => scope.backend.mounted)
    .componentWillUnmount(scope => scope.backend.willUnmount)
    .shouldComponentUpdateConst(false)
    .build

}
