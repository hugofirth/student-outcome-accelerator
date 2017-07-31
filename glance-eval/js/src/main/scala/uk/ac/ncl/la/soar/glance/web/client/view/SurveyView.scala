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
package uk.ac.ncl.la.soar.glance.web.client.view

import cats._
import cats.implicits._
import diode.data._
import diode.react.ReactPot._
import diode.react._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import uk.ac.ncl.la.soar.ModuleCode
import uk.ac.ncl.la.soar.data.StudentRecords
import uk.ac.ncl.la.soar.glance.Survey
import uk.ac.ncl.la.soar.glance.web.client.SurveyModel
import uk.ac.ncl.la.soar.glance.web.client.component.{Select, StudentCharts, StudentsTable}
import uk.ac.ncl.la.soar.glance.web.client.data.CohortAttainmentSummary
import uk.ac.ncl.la.soar.glance.web.client.style.Icon

import scala.collection.immutable.SortedMap
import scala.scalajs.js

/**
  * React Component for the SurveyView
  */
object SurveyView {

  type Props = ModelProxy[Pot[SurveyModel]]

  case class State(selected: Option[StudentRecords[SortedMap, ModuleCode, Double]])

  class Backend(bs: BackendScope[Props, State]) {

    def mounted(props: Props) = Callback {}

    def handleStudentClick(bs: BackendScope[Props, State])
                          (student: StudentRecords[SortedMap, ModuleCode, Double]) = {
      bs.modState(s => s.copy(selected = Some(student)))
    }

    val indexCol = "Student Number"

    /** Construct the presentation of the modules as a sorted list to fill some table headings */
    private def modules(survey: Survey) = survey.modules.toList.sorted

    /** Construct the full presentation of table headings, including modules */
    private def headings(survey: Survey) = indexCol :: modules(survey)

    /** Construct the presentation of the students to fill table rows */
    private def students(survey: Survey) = survey.entries

    /** Construct the function which provides the presentation of a table cell, given a StudentRecord and string key */
    private def renderCell(default: String)(student: StudentRecords[SortedMap, ModuleCode, Double], key: String) =
      key match {
        case k if k == indexCol => student.number
        case k => student.record.get(k).fold(default)(_.toString)
      }

    def render(p: Props, s: State): VdomElement = {
      //Get the necessary data from the model
      //This is a bit of a nested Mess - TODO: Make sure we're understanding the model construction properly
      val model = p()

      //TODO: Investigate Diode Pot conditional rendering. Doesn't render message on fail
      <.div(
        ^.id := "training",
        <.span(
          ^.className := "sub-title",
          Icon.list(Icon.Medium),
          <.h2("Training Data")),
        <.div(
          ^.className := "table-responsive",
          model.renderFailed(ex => "Error loading survey"),
          model.renderPending(_ > 50, _ => <.p("Loading ...")),
          model.render { sm =>
            StudentsTable.component(
              StudentsTable.Props(
                students(sm.survey),
                headings(sm.survey),
                renderCell(" "),
                handleStudentClick(bs)
              )
            )
          }
        ),
        <.span(
          ^.className := "sub-title",
          Icon.search(Icon.Medium),
          <.h2("Detailed View")
        ),
        model.render { sm =>
          StudentCharts.component(
            StudentCharts.Props(s.selected, sm.summary)
          )
        }
      )
    }

  }

  val component = ScalaComponent.builder[ModelProxy[Pot[SurveyModel]]]("SurveyView")
    .initialStateFromProps(p => State(None))
    .renderBackend[Backend]
    .componentDidMount(scope => scope.backend.mounted(scope.props))
    .build

}