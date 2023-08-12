package com.fastscala.templates.bootstrap5.examples.data

import io.circe.generic.semiauto

import scala.io.Source

case class CountryName(
                        common: String
                        , official: String
                      )

case class Country(
                    name: CountryName
                    , independent: Option[Boolean]
                    , tld: Array[String]
                    , cca2: String
                    , ccn3: String
                    , cca3: String
                    , cioc: String
                    , status: String
                    , unMember: Option[Boolean]
                    , capital: Array[String]
                    , altSpellings: Array[String]
                    , region: String
                    , subregion: String
                    , latlng: Option[Array[Double]]
                    , landlocked: Option[Boolean]
                    , borders: Array[String]
                    , area: Double
                    , callingCodes: Array[String]
                    , flag: String
                  )

object CountriesData {

  lazy val data = {
    import io.circe._, io.circe.parser.parse
    implicit val CountryNameDecoder: Decoder[CountryName] = semiauto.deriveDecoder[CountryName]
    implicit val CountryDecoder: Decoder[Country] = semiauto.deriveDecoder[Country]
    parse(Source.fromInputStream(getClass.getResourceAsStream("/countries.json")).getLines().mkString("\n")).right.get.as[Array[Country]].right.get
  }
}
