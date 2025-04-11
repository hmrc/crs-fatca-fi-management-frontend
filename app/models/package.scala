/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import play.api.libs.json._

package object models {

  implicit class RichJsObject(jsObject: JsObject) {

    def setObject(path: JsPath, value: JsValue): JsResult[JsObject] =
      jsObject.set(path, value).flatMap(_.validate[JsObject])

    def removeObject(path: JsPath): JsResult[JsObject] =
      jsObject.remove(path).flatMap(_.validate[JsObject])

  }

  implicit class RichJsValue(jsValue: JsValue) {

    def set(path: JsPath, value: JsValue): JsResult[JsValue] =
      (path.path, jsValue) match {

        case (Nil, _) =>
          JsError("path cannot be empty")

        case ((_: RecursiveSearch) :: _, _) =>
          JsError("recursive search not supported")

        case ((n: IdxPathNode) :: Nil, _) =>
          setIndexNode(n, jsValue, value)

        case ((n: KeyPathNode) :: Nil, _) =>
          setKeyNode(n, jsValue, value)

        case (first :: second :: rest, oldValue) =>
          Reads
            .optionNoError(Reads.at[JsValue](JsPath(first :: Nil)))
            .reads(oldValue)
            .flatMap {
              opt =>
                opt
                  .map(JsSuccess(_))
                  .getOrElse {
                    second match {
                      case _: KeyPathNode =>
                        JsSuccess(Json.obj())
                      case _: IdxPathNode =>
                        JsSuccess(Json.arr())
                      case _: RecursiveSearch =>
                        JsError("recursive search is not supported")
                    }
                  }
                  .flatMap {
                    _.set(JsPath(second :: rest), value).flatMap {
                      newValue =>
                        oldValue.set(JsPath(first :: Nil), newValue)
                    }
                  }
            }
      }

    private def setIndexNode(node: IdxPathNode, oldValue: JsValue, newValue: JsValue): JsResult[JsValue] = {

      val index: Int = node.idx

      oldValue match {
        case oldValue: JsArray if index >= 0 && index <= oldValue.value.length =>
          if (index == oldValue.value.length) {
            JsSuccess(oldValue.append(newValue))
          } else {
            JsSuccess(JsArray(oldValue.value.updated(index, newValue)))
          }
        case oldValue: JsArray =>
          JsError(s"array index out of bounds: $index, $oldValue")
        case _ =>
          JsError(s"cannot set an index on $oldValue")
      }
    }

    private def removeIndexNode(node: IdxPathNode, valueToRemoveFrom: JsArray): JsResult[JsValue] = {
      val index: Int = node.idx

      valueToRemoveFrom match {
        case valueToRemoveFrom: JsArray if index >= 0 && index < valueToRemoveFrom.value.length =>
          val updatedJsArray = valueToRemoveFrom.value.slice(0, index) ++ valueToRemoveFrom.value
            .slice(index + 1, valueToRemoveFrom.value.size)
          JsSuccess(JsArray(updatedJsArray))
        case valueToRemoveFrom: JsArray => JsError(s"array index out of bounds: $index, $valueToRemoveFrom")
        case _                          => JsError(s"cannot set an index on $valueToRemoveFrom")
      }
    }

    private def setKeyNode(node: KeyPathNode, oldValue: JsValue, newValue: JsValue): JsResult[JsValue] = {

      val key = node.key

      oldValue match {
        case oldValue: JsObject =>
          JsSuccess(oldValue + (key -> newValue))
        case _ =>
          JsError(s"cannot set a key on $oldValue")
      }
    }

    def remove(path: JsPath): JsResult[JsValue] =
      if (path.path.exists(_.isInstanceOf[RecursiveSearch])) {
        JsError("recursive search is not supported")
      } else {
        path.path match {
          case Nil =>
            JsError("path cannot be empty")

          case List(key: KeyPathNode) =>
            jsValue match {
              case obj: JsObject =>
                if (obj.keys.contains(key.key)) JsSuccess(obj - key.key)
                else JsError("cannot find value at path")
              case _ =>
                JsError(s"cannot remove a key on $jsValue")
            }

          case List(idx: IdxPathNode) =>
            jsValue match {
              case arr: JsArray =>
                removeIndexNode(idx, arr)
              case _ =>
                JsError(s"cannot remove an index on $jsValue")
            }

          case first :: second :: rest =>
            handleRecursiveRemove(jsValue, first, second, rest)

          case _ =>
            JsError(s"unsupported path structure: ${path.path}")
        }
      }

    private def handleRecursiveRemove(
      oldValue: JsValue,
      first: PathNode,
      second: PathNode,
      rest: List[PathNode]
    ): JsResult[JsValue] = {
      val subPath        = JsPath(first :: Nil)
      val subValueResult = Reads.optionNoError(Reads.at[JsValue](subPath)).reads(oldValue)

      subValueResult.flatMap {
        case Some(subValue) =>
          subValue.remove(JsPath(second :: rest)).flatMap {
            newValue =>
              oldValue.set(subPath, newValue)
          }

        case None =>
          val maybeDefault = second match {
            case _: KeyPathNode => Some(Json.obj())
            case _: IdxPathNode => Some(Json.arr())
            case _              => None
          }

          maybeDefault match {
            case Some(defaultValue) =>
              defaultValue.remove(JsPath(second :: rest)).flatMap {
                newValue =>
                  oldValue.set(subPath, newValue)
              }

            case None =>
              JsError("unsupported path node in recursive removal")
          }
      }
    }

  }

}
