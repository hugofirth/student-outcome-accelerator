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
package uk.ac.ncl.la.soar.glance.web.client

import com.thoughtworks.binding.Binding.{BindingSeq, Var, Vars}
import org.scalajs.dom.raw.Node
import com.thoughtworks.binding.{Binding, dom}
import cats._
import cats.implicits._
import io.circe._
import uk.ac.ncl.la.soar.glance.Survey

import scala.scalajs.js.annotation.JSExport

/**
  * Entry point for client program
  */
@JSExport
object Main {


  /**
    * Defines Routes for current program
    * TODO: again move to own file at some point
    */

  //Lets get the survey data
  val surveysJson = ApiClient.loadSurveys

  //Holder for Surveys
  val survey = Var(None: Option[Survey])

  surveysJson.fold(
    { case e @ DecodingFailure(_, _) =>
      //Create error message
      println(e.show)
    },
    { case s => survey.value = s.headOption }
  )

  /**
    * Defines the header of our single page app
    * TODO: Move to its own file
    */
  @dom
  def header: Binding[Node] = {
    <nav class="navbar navbar-inverse navbar-fixed-top">
      <div class="container-fluid">
        <div class="navbar-header">
          <a class="navbar-brand" href="#">Glance Survey - Base</a>
        </div>
      </div>
    </nav>
  }



  @dom
  def glanceApp: Binding[BindingSeq[Node]] = {
    //Bind header
    { header.bind }
    //Bind main and do routing
    { BaseSurvey.main(survey).bind }
    //Bind footer
  }

  @JSExport
  def main(container: Node) = dom.render(container, glanceApp)

}
