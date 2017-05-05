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
package uk.ac.ncl.la.soar.glance

import monix.execution.Scheduler.Implicits.global
import monix.eval.Task
import doobie.imports._

import java.util.UUID

/**
  * Repository trait for retrieving objects from the [[Survey]] ADT from a database
  */
sealed trait Repository[+A] {
  val init: Task[Unit]
  val list: Task[List[A]]
  def find(id: UUID): Task[Option[A]]
  def save[B >: A](entry: B): Task[A]
  def delete(id: UUID): Task[Boolean]
  def sync[B >: A](entries: List[B]): Task[List[A]]
}

object Repository {

  lazy val Survey: Task[SurveyDb] = createSchema

  /** Init method to set up the database */
  private val createSchema: Task[SurveyDb] = {
    val xa = DriverManagerTransactor[Task]("org.postgresql.Driver", "jdbc:postgresql:surveys", "postgres",
      "mysecretpassword")

    val sDb = new SurveyDb(xa)
    for {
      _ <- sDb.init
    } yield sDb
  }
}

class SurveyDb(xa: Transactor[Task]) extends Repository[EmptySurvey] {

  implicit val uuidMeta: Meta[UUID] = Meta[String].nxmap(UUID.fromString, _.toString)

  //Survey: pk varchar id, string module, string common
  //SurveyEntry: pk varchar id, fk varchar SurveyId, fk varchar StudentNumber
  //SurveyResponse: pk varchar id, varchar respondent, ? date, int type
  //Query: pk varchar id, fk varchar SurveyId, fk varchar StudentNumber, varchar ModuleCode
  //QueryResponse: pk varchar id, fk varchar SurveyResponseId, fk varchar QueryId, int Score not null
  //Student: pk varchar number
  //ModuleScore: pk varchar id, fk varchar StudentNumber, int score not null, varchar ModuleCode
  private lazy val createTableQuery: ConnectionIO[Int] =
    sql"""
      CREATE TABLE surveys (
        id VARCHAR PRIMARY KEY,
        module VARCHAR NOT NULL,
        common VARCHAR
      );

      CREATE TABLE students (
        number VARCHAR PRIMARY KEY
      );

      CREATE TABLE surveys_students (
        id VARCHAR PRIMARY KEY,
        survey_id VARCHAR REFERENCES surveys(id),
        student_number VARCHAR REFERENCES students(number)
      );

      CREATE TABLE survey_queries (
        id VARCHAR PRIMARY KEY,
        survey_id VARCHAR REFERENCES surveys(id),
        student_number VARCHAR REFERENCES students(number),
        module VARCHAR NOT NULL
      );

      CREATE TABLE module_score (
        id VARCHAR PRIMARY KEY,
        student_number VARCHAR REFERENCES students(number),
        score DECIMAL(5,2) NOT NULL,
        CHECK (score > 0.00),
        CHECK (score < 100.00),
        module VARCHAR NOT NULL
      );
    """.update.run

  override val init: Task[Unit] = createTableQuery.map(_ => ()).transact(xa)

  override val list: Task[List[EmptySurvey]] = _

  override def find(id: UUID): Task[Option[EmptySurvey]] = ???

  override def save[B >: EmptySurvey](entry: B): Task[EmptySurvey] = ???

  override def delete(id: UUID): Task[Boolean] = ???

  override def sync[B >: EmptySurvey](entries: List[B]): Task[List[EmptySurvey]] = ???
}
