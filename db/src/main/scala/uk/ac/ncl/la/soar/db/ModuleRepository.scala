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
package uk.ac.ncl.la.soar.db

import cats._
import cats.implicits._
import doobie.imports._
import monix.eval.Task
import monix.cats._
import uk.ac.ncl.la.soar.{ModuleCode, StudentNumber}
import uk.ac.ncl.la.soar.data.{Module, ModuleRecords}
import Implicits._

class ModuleDb private[glance] (xa: Transactor[Task]) extends Repository[Module] {

  import ModuleDb._

  override type PK = ModuleCode

  override val init: Task[Unit] = initQ.transact(xa)

  override val list: Task[List[Module]] = listQ.transact(xa)

  override def find(id: ModuleCode): Task[Option[Module]] = findQ(id).transact(xa)

  def findRecord(id: ModuleCode): Task[Option[ModuleRecords[Map, StudentNumber, Double]]]

  override def save(entry: Module): Task[Unit] = saveQ(entry).transact(xa)

  override def delete(id: ModuleCode): Task[Boolean] = deleteQ(id).transact(xa)

}

private[db] object ModuleDb extends RepositoryCompanion[Module, ModuleDb] {

  override val initQ: ConnectionIO[Unit] = ().pure[ConnectionIO]

  override val listQ: ConnectionIO[List[Module]] = sql"SELECT * FROM modules;".query[Module].list

  override def findQ(id: ModuleCode): ConnectionIO[Option[Module]] =
    sql"SELECT * FROM modules m WHERE m.code = $id;".query[Module].option

  override def saveQ(entry: Module): ConnectionIO[Unit] =
    sql"INSERT INTO modules (code) VALUES (${entry.code});".update.run.map(_ => ())

  override def deleteQ(id: ModuleCode): ConnectionIO[Boolean] =
    sql"DELETE FROM modules m WHERE m.code = $id;".update.run.map(_ > 0)

  def findRecordQ(id: ModuleCode): ConnectionIO[Option[ModuleRecords[Map, StudentNumber, Double]]] =
    sql"""
      SELECT * FROM module_score m WHERE m.code = $id;
    """
}


